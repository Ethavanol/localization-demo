package navigation_changing;

import MAP.Direction;
import MAP.LocalizationMap;
import MAP.MapMarker;
import MAP.MapType;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import epistemic_jason.asSemantics.AgentEpistemic;
import epistemic_jason.asSemantics.modelListener.ModelResponse;
import epistemic_jason.asSemantics.modelListener.World;
import epistemic_jason.formula.PropFormula;
import jason.JasonException;
import jason.asSemantics.Event;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Pred;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import jason.environment.grid.Location;
import jason.infra.local.LocalAgArch;
import jason.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class NavChangingAgent extends AgentEpistemic {

    private HashMap<Pair<String, String>, List<Direction>> shortestDirectionsFromLocationToOtherLocation = new HashMap<>();
    private boolean[][] wallGrid;
    private static final boolean OPENWORLD = false;

    NavChangingEnv navEnv;

    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        var arch = this.getTS().getAgArch();

        while (arch != null && arch.getNextAgArch() != null && !(arch instanceof LocalAgArch))
            arch = arch.getNextAgArch();

        var myArch = (LocalAgArch) arch;
        navEnv = (NavChangingEnv) myArch.getEnvInfraTier().getUserEnvironment();
    }

    //This function is called when the model has been created
    @Override
    public void modelCreated(){
        System.out.println("Model created");
        this.readMap(MapType.LOCALIZATION_5x5);
        ModelResponse model = this.getWorldsResponseModel();
        for(World world : model.getWorlds()){
            Pair<String, String> locationsValues = getLocandLocGoalValues(world);
            shortestDirectionsFromLocationToOtherLocation.put(locationsValues, getAllShortestDirections(stringToLocation(locationsValues.getFirst()), stringToLocation(locationsValues.getSecond())));
        }
        try {
            replaceSemanticModel(model);
        } catch (RuntimeException e) {
            throw e;
        }
        // for the rule dir(direction) :- direction to be applied, so we can test if we know right, left, up or down,
        // the directions need to be in the BB. But as we don't want them all to be added to the reasonner worlds, we add them manually.
        try {
            this.addDirectionsToBB();
        } catch (JasonException e) {
            throw new RuntimeException(e);
        }
    }


    //This function is called each time an event has been applied to the model
    @Override
    public void eventModelApplied(Event event){
        // we update the directions of the worlds in the model only if the agent moved and his possibles locations changed
        if(event.getTrigger().getOperator().equals(Trigger.TEOperator.add) && event.getTrigger().getLiteral().toString().startsWith("on(")){
            if(event.getTrigger().getLiteral().toString().startsWith("on(moved(")) {
                try {
                    ModelResponse model = this.getWorldsResponseModel();
                    replaceSemanticModel(model);
                } catch (RuntimeException e) {
                    throw e;
                }
            }
//             else {
//                try {
//                    ModelResponse model = this.getWorldsResponseModel();
//                    updateViewFromSemanticModel(model);
//                } catch (RuntimeException e) {
//                    throw e;
//                }
//            }
        }
    }


    public void replaceSemanticModel(ModelResponse model) throws RuntimeException {
        try {
            List<Pair<World, List<PropFormula>>> newProps = updateDirectionsInWorlds(model.getWorlds());
            this.getEpistemicExtension().replacePropsInModel(newProps);
            //If you don't want the view to show the possible worlds, comment the following line
            //navEnv.getModel().updatePossible(newProps);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse response model", e);
        } catch (RuntimeException e) {
            throw e;
        }
    }


    public void updateViewFromSemanticModel(ModelResponse model) {
        try {
            List<Pair<World, List<PropFormula>>> newProps = updateDirectionsInWorlds(model.getWorlds());
            //If you don't want the view to show the possible worlds, comment the following line
            navEnv.getModel().updatePossible(newProps);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private void addDirectionsToBB() throws JasonException {
        for (Direction dir : Direction.values()) {
            this.getBB().add(ASSyntax.createLiteral(dir.toString()));
        }
    }


    public List<Pair<World, List<PropFormula>>> updateDirectionsInWorlds(List<World> worlds) throws ParseException {
        List<Pair<World, List<PropFormula>>> newProps = new ArrayList<>();
        for (World w : worlds) {
            // we get the directions generated before
            Map<String, Boolean> propositions = w.getPropositions();

            String locValue = null;
            String locGoalValue = null;

            List<PropFormula> listNewProps = new ArrayList<>();
            for (String prop : propositions.keySet()) {
                if (prop.startsWith("loc(")) {
                    locValue = prop;
                } else if (prop.startsWith("locGoal(")) {
                    locGoalValue = prop;
                } else if(!isDirection(prop) && propositions.get(prop)) {
                    listNewProps.add(new PropFormula(new Pred(prop)));
                }
            }

            Pair<String, String> locationsValues = new Pair<String,String>(locValue, locGoalValue);
            List<Direction> directions = shortestDirectionsFromLocationToOtherLocation.get(locationsValues);
            if (directions == null) directions = List.of();

            listNewProps.add(new PropFormula(new Pred(locationsValues.getFirst())));
            listNewProps.add(new PropFormula(new Pred(locationsValues.getSecond())));

            // we add the new directions to the list of new propositions
            directions.stream().forEach(direction -> {
                String propToAdd = direction != null ? direction.toString() : null;
                listNewProps.add(new PropFormula(new Pred(ASSyntax.createLiteral(propToAdd))));
            });

            //here we wanna keep the at(goal) proposition if she exists
            // otherwise the agent won't never stop
            if (Boolean.TRUE.equals(w.getPropositions().get("at(goal)"))) {
                listNewProps.add(new PropFormula(new Pred(ASSyntax.parseLiteral("at(goal)"))));
            }
            newProps.add(new Pair<>(w, listNewProps));
        }
        return newProps;
    }


    private boolean isDirection(String prop) {
        for (Direction dir : Direction.values()) {
            if (prop.equals(dir.toString())) {
                return true;
            }
        }
        return false;
    }


    public Pair<String, String> getLocandLocGoalValues(World world){
        String locValue = null;
        String locGoalValue = null;
        for (String prop : world.getPropositions().keySet()) {
            if (prop.startsWith("loc(")) {
                locValue = prop;
            } else if (prop.startsWith("locGoal(")) {
                locGoalValue = prop;
            }
        }
        return new Pair<>(locValue, locGoalValue);
    }

    public void readMap(MapType mapType){
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(mapType.getFileName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load map!", e);
        }
        LocalizationMap map = gson.fromJson(reader, LocalizationMap.class);

        wallGrid = new boolean[map.getWidth()][map.getHeight()];
        MapMarker prev = null;
        for (var marker : map.getMarkers()) {
            if (marker.getType() == 4)
                wallGrid[marker.getLocation().x][marker.getLocation().y] = true;
        }

    }

    public Boolean isWall(int x, int y){
        return wallGrid[x][y];
    }

    public Location stringToLocation(String location){
        String[] coordinates = location.substring(location.indexOf("(")+1, location.indexOf(")")).split(",");
        return new Location(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
    }


    public List<Direction> getAllShortestDirections(Location start, Location goal) {
        if (start.equals(goal)) {
            return Collections.emptyList();
        }

        int width = wallGrid.length;
        int height = wallGrid[0].length;

        Queue<Location> queue = new LinkedList<>();
        Map<Location, List<Location>> cameFrom = new HashMap<>();
        Map<Location, Integer> distance = new HashMap<>();
        Set<Location> visited = new HashSet<>();

        queue.add(start);
        distance.put(start, 0);
        visited.add(start);

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            int currentDist = distance.get(current);

            for (Direction dir : Direction.values()) {
                Location neighbor = move(current, dir, width, height);
                if (neighbor != null && !isWall(neighbor.x, neighbor.y)) {
                    int newDist = currentDist + 1;

                    if (!distance.containsKey(neighbor)) {
                        queue.add(neighbor);
                        distance.put(neighbor, newDist);
                        cameFrom.put(neighbor, new ArrayList<>(List.of(current)));
                    } else if (distance.get(neighbor) == newDist) {
                        cameFrom.get(neighbor).add(current);
                    }
                }
            }
        }

        if (!cameFrom.containsKey(goal)) {
            return List.of();
        }

        Set<Direction> result = new HashSet<>();
        collectShortestDirections(start, goal, cameFrom, result);

        return new ArrayList<>(result);
    }

    private void collectShortestDirections(Location start, Location current, Map<Location, List<Location>> cameFrom, Set<Direction> result) {
        for (Location parent : cameFrom.getOrDefault(current, List.of())) {
            if (parent.equals(start)) {
                Direction dir = directionFromTo(start, current);
                if (dir != null) {
                    result.add(dir);
                }
            } else {
                collectShortestDirections(start, parent, cameFrom, result);
            }
        }
    }

    private Location move(Location loc, Direction dir, int width, int height) {
        int x = loc.x;
        int y = loc.y;

        switch (dir) {
            case UP:
                y = y - 1;
                break;
            case DOWN:
                y = y + 1;
                break;
            case LEFT:
                x = x - 1;
                break;
            case RIGHT:
                x = x + 1;
                break;
        }

        if (OPENWORLD) {
            // Wrap around the grid (torus topology)
            x = (x + width) % width;
            y = (y + height) % height;
            return new Location(x, y);
        } else {
            // Stay within bounds
            if (x >= 0 && x < width && y >= 0 && y < height) {
                return new Location(x, y);
            } else {
                return null;
            }
        }
    }


    private Direction directionFromTo(Location from, Location to) {
        if (to.x == from.x + 1 && to.y == from.y) return Direction.RIGHT;
        if (to.x == from.x - 1 && to.y == from.y) return Direction.LEFT;
        if (to.y == from.y + 1 && to.x == from.x) return Direction.DOWN;
        if (to.y == from.y - 1 && to.x == from.x) return Direction.UP;
        return null;
    }

}
package simple_navigation;

import MAP.LocalizationMap;
import MAP.MapMarker;
import MAP.MapType;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import epistemic_jason.asSemantics.modelListener.World;
import epistemic_jason.formula.PropFormula;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import jason.util.Pair;
import mapc.MapcModel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Logger;

public class NavModel extends GridWorldModel {

    public static final int GOAL = (int) Math.pow(2, 3);

    public static final int POSSIBLE_LOC = (int) Math.pow(2, 4);
    public static final int POSSIBLE_LOC_GOAL = (int) Math.pow(2, 5);

    public Map<Location, List<Integer>> gridPossibleObjects = new HashMap<>();

    private static final String NORTH = "up";
    private static final String SOUTH = "down";
    private static final String EAST = "right";
    private static final String WEST = "left";
    private static final boolean OPEN_WORLD = false;
    private boolean inputEnabled = true;

    private final Logger logger = Logger.getLogger(getClass().getName());

    private List<Location> lastPositions;
    private List<MapEventListener> mapEventListeners;

    private List<Location> goalLocations;

    private int width;
    private int height;
    private int nbAgts;

    private boolean showPossibleWorlds = true;

    //The 2 next following lines are writtne to print the possible worlds in the view.
    // They are not necessary and can be removed. They are here to help the user understand how it is working
    HashMap<Location, Set<Direction>> possibleLocsWithDirections = new HashMap<>();
    Set<Location> possibleGoals = new HashSet<>();

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    public NavModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        this.width = w;
        this.height = h;
        this.nbAgts = nbAgs;
        this.goalLocations = new ArrayList<>();
        this.mapEventListeners = new ArrayList<>();
        this.lastPositions = new ArrayList<>(Arrays.asList(null, null, null));
    }

    public NavModel(LocalizationMap map, MapType mapType) {
        this(map.getWidth(), map.getHeight(), map.getNbAgts());

        for (var marker : map.getMarkers()) {
            if (marker.getType() == GOAL)
                goalLocations.add(marker.getLocation());

            this.add(marker.getType(), marker.getLocation().x, marker.getLocation().y);
        }

        this.setAgPos(0, map.getAgentStart());
//        randomAgentSpawn(this.width,this.height,this.nbAgts);
    }

    public static NavModel loadFromFile(MapType mapType) {
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(mapType.getFileName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load map!", e);
        }
        LocalizationMap map = gson.fromJson(reader, LocalizationMap.class);

        return new NavModel(map, mapType);
    }

    public int getNbAgts() {
        return nbAgts;
    }

    public List<Location> getGoalLocations() {
        return goalLocations;
    }

    public void randomAgentSpawn(int w, int h, int nbAgs) {
        Random rand = new Random();
        boolean notpositionned = true;
        for(int i = 0; i < nbAgs; i++){
            notpositionned = true;
            while(notpositionned){
                int x = rand.nextInt(w);
                int y = rand.nextInt(h);
                if(isFree(x,y)){
                    this.setAgPos(i,0,0);
                    notpositionned = false;
                }
            }
        }
    }

    @Override
    public void setAgPos(int ag, Location l) {
        // Set last position if agent 0
        this.lastPositions.set(ag, getAgPos(ag));
        super.setAgPos(ag, l);
    }


    synchronized public boolean move(Direction dir, int ag) throws Exception {
        if(OPEN_WORLD) {
            return moveOpenWorld(dir,ag);
        } else {
            return moveCloseWorld(dir,ag);
        }
    }

    // closed-world version
    synchronized public boolean moveCloseWorld(Direction dir, int ag) throws Exception {
        if (ag < 0) {
            logger.warning("** Trying to move unknown agent!");
            return false;
        }
        Location l = getAgPos(ag);
        if (l == null) {
            logger.warning("** We lost the location of agent " + (ag + 1) + "!"+this);
            return false;
        }

        Location n = null;
        switch (dir) {
            case UP:
                n =  new Location(l.x, l.y - 1);
                break;
            case DOWN:
                n =  new Location(l.x, l.y + 1);
                break;
            case RIGHT:
                n =  new Location(l.x + 1, l.y);
                break;
            case LEFT:
                n =  new Location(l.x - 1, l.y);
                break;
        }

        if (n != null) {
            if(!inGrid(n)){
                notifyListeners(ag, TypeEvent.FAILED, dir);
            } else if (isFreeOfObstacle(n)){
                this.setAgPos(ag, n);
                notifyListeners(ag, TypeEvent.MOVED, dir);
            }
        }
        return true;
    }


    // Open-World version
    synchronized public boolean moveOpenWorld(Direction dir, int ag) throws Exception {
        if (ag < 0) {
            logger.warning("** Trying to move unknown agent!");
            return false;
        }

        Location l = getAgPos(ag);
        if (l == null) {
            logger.warning("** We lost the location of agent " + (ag + 1) + "!"+this);
            return false;
        }

        int newX = l.x;
        int newY = l.y;

        switch (dir) {
            case UP:
                newY = (l.y - 1 + height) % height;
                break;
            case DOWN:
                newY = (l.y + 1) % height;
                break;
            case RIGHT:
                newX = (l.x + 1) % width;
                break;
            case LEFT:
                newX = (l.x - 1 + width) % width;
                break;
        }

        Location n = new Location(newX, newY);

        if (isFreeOfObstacle(n)) {
            this.setAgPos(ag, n);
            notifyListeners(ag, TypeEvent.MOVED, dir);
            return true;
        }

        return false;
    }


    public void addMapListener(MapEventListener listener) {
        this.mapEventListeners.add(listener);
        this.notifyListeners(0, TypeEvent.MOVED, null);
    }


    private synchronized void notifyListeners(Integer agentId, TypeEvent typeEvent, Direction moveDirection) {
        Location agentLoc = getAgPos(agentId);

        if (moveDirection == null) {
            moveDirection = getLastLocation(agentId);
        }

        for (var listener : mapEventListeners)
            listener.agentMoved(new MapEvent(this, agentLoc, moveDirection, typeEvent, agentId), agentId);
    }

    public Direction getLastLocation(Integer agentId) {
        if (lastPositions.get(agentId) == null)
            return null;

        return getDirectionAtom(lastPositions.get(agentId), getAgPos(agentId));
    }

    private Direction getDirectionAtom(Location src, Location dst) {

        var delta = delta(src, dst);

        if (delta.x == 1)
            return Direction.RIGHT;
        if (delta.x == -1)
            return Direction.LEFT;
        if (delta.y == -1)
            return Direction.UP;
        if (delta.y == 1)
            return Direction.DOWN;

        throw new NullPointerException("Huh?");
    }


    public Location delta(Location src, Location dst) {
        if(OPEN_WORLD){
            return deltaOpenWorld(src,dst);
        } else {
            return deltaCloseWorld(src,dst);
        }
    }


    // Closed-World version
    public Location deltaCloseWorld(Location src, Location dst) {
        Location delta = new Location(dst.x - src.x, dst.y - src.y);
        if ((Math.abs(delta.x) != 1 && Math.abs(delta.y) != 1) || (delta.x == delta.y)) {
            System.out.println("Invalid Direction? " + delta);
            throw new NullPointerException();
        }

        return delta;
    }

    // Open-world version
    public Location deltaOpenWorld(Location src, Location dst) {
        int dx = dst.x - src.x;
        int dy = dst.y - src.y;

        // gestion du wrap horizontal
        if (dx > 1) dx = dx - width;
        if (dx < -1) dx = dx + width;

        // gestion du wrap vertical
        if (dy > 1) dy = dy - height;
        if (dy < -1) dy = dy + height;

        Location delta = new Location(dx, dy);

        if ((Math.abs(delta.x) != 1 || delta.y != 0) &&
                (Math.abs(delta.y) != 1 || delta.x != 0)) {
            System.out.println("Invalid Direction? " + delta);
            throw new NullPointerException();
        }

        return delta;
    }

    public List<Literal> getObsPercepts(Integer agent) {
        // Get directional percepts
        Location agentLoc = getAgPos(agent);
        var arrList = new ArrayList<Literal>();

        arrList.add(getObsPercept(agentLoc, 0, -1));
        arrList.add(getObsPercept(agentLoc, 0, 1));
        arrList.add(getObsPercept(agentLoc, -1, 0));
        arrList.add(getObsPercept(agentLoc, 1, 0));
        return arrList;
    }


    private Literal getObsPercept(Location cur, int x, int y) {

        int pX = cur.x + x;
        int pY = cur.y + y;
        if(OPEN_WORLD){
            pX = (pX + width) % width;
            pY = (pY + height) % height;
        }

        Location delta = new Location(pX, pY);
        Literal locAtom;
        if (x == 0 && y == -1)
            locAtom = ASSyntax.createAtom(NORTH);
        else if (x == 0 && y == 1)
            locAtom = ASSyntax.createAtom(SOUTH);
        else if (x == 1 && y == 0)
            locAtom = ASSyntax.createAtom(EAST);
        else // if (x == -1 && y == 0)
            locAtom = ASSyntax.createAtom(WEST);
        if(inGrid(delta) && !isFreeOfObstacle(delta)){
            return ASSyntax.createLiteral("obs", locAtom);
        }
        return ASSyntax.createLiteral(Literal.LNeg, "obs", locAtom);
    }


    public synchronized void signalInput(boolean val) {
        this.inputEnabled = val;
    }


    /** The following section is not necessary.
     * We just want to show the possibles worlds on the view to help the user understand what's going on. **/

    public void updatePossible(List<Pair<World, List<PropFormula>>> newProps){
        this.clearPossible();
        possibleLocsWithDirections = new HashMap<>();
        possibleGoals = new HashSet<>();
        for(Pair<World, List<PropFormula>> pair : newProps){
            Location loc = null;
            for(PropFormula prop : pair.getSecond()){
                String str = prop.getPropLit().toString();
                if (str.startsWith("locGoal")){
                    String[] args = str.substring(str.indexOf('(') + 1, str.indexOf(')')).split(",");
                    possibleGoals.add(new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
                } else if(str.startsWith("loc")){
                    String[] args = str.substring(str.indexOf('(') + 1, str.indexOf(')')).split(",");
                    loc = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    possibleLocsWithDirections.computeIfAbsent(
                            loc,
                            k -> new HashSet<>()
                    );
                } else {
                    boolean isDirection = Arrays.stream(Direction.values())
                            .anyMatch(d -> d.toString().equals(str));
                    if(isDirection){
                        possibleLocsWithDirections.get(loc).add(Direction.valueOf(str.toUpperCase()));
                    }
                }
            }
        }
        this.addPossible();
//        this.view.update();
    }

    private synchronized void addPossible() {
        for (var location : possibleLocsWithDirections.keySet()) {
            // For relative agent positions
            addObject(POSSIBLE_LOC, location);
            this.add(POSSIBLE_LOC, location);
        }

        for (var location : possibleGoals) {
            // For relative agent positions
            addObject(POSSIBLE_LOC_GOAL, location);
            this.add(POSSIBLE_LOC_GOAL, location);
        }

        this.view.getCanvas().invalidate();
    }

    private synchronized void clearPossible() {
        for (var location : possibleLocsWithDirections.keySet()) {
            removeObject(POSSIBLE_LOC, location);
            this.remove(POSSIBLE_LOC, location);
        }

        for (var location : possibleGoals) {
            removeObject(POSSIBLE_LOC_GOAL, location);
            this.remove(POSSIBLE_LOC_GOAL, location);
        }
    }

    public void addObject(int object, Location loc) {
        gridPossibleObjects.computeIfAbsent(loc, k -> new ArrayList<>()).add(object);
    }

    public void removeObject(int object, Location loc) {
        List<Integer> objects = gridPossibleObjects.get(loc);
        if (objects != null) {
            objects.remove(Integer.valueOf(object));
            if (objects.isEmpty()) {
                gridPossibleObjects.remove(loc);
            }
        }
    }

    public List<Integer> getObjects(Location loc) {
        return gridPossibleObjects.getOrDefault(loc, new ArrayList<>());
    }


}

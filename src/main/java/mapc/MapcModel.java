package mapc;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import epistemic.DebugConfig;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static mapc.Terrain.NONE;


public class MapcModel extends GridWorldModel {

    public static final int GOAL = (int) Math.pow(2, 3);
    private static final String NORTH = "up";
    private static final String SOUTH = "down";
    private static final String EAST = "right";
    private static final String WEST = "left";
    public static final int RED_DISP = (int) Math.pow(2, 5);
    private boolean inputEnabled = true;
    private final Logger logger = Logger.getLogger(getClass().getName());

    private List<Location> lastPositions;
    private List<MapEventListener> mapEventListeners;
    private boolean usedNone = false;

    private List<Location> goalLocations;

    private final DebugConfig config;

    private static final boolean SHOULD_USE_REL_PERCEPTS = false;
    private static final boolean GENERATE_DIRS = true; // not needed when we generate larger maps

    private int width;
    private int height;
    private int nbAgts;

    private int countUniqueObjects = 1;

    Map<List<Integer>, Integer> closestObjCache = new HashMap<>();

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    public MapcModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        this.width = w;
        this.height = h;
        this.nbAgts = nbAgs;
        this.config = DebugConfig.getInstance();
        this.goalLocations = new ArrayList<>();
        this.mapEventListeners = new ArrayList<>();
        this.lastPositions = new ArrayList<>(Arrays.asList(null, null, null));
    }

    public MapcModel(LocalizationMap map, MapType mapType) {
        this(map.getWidth(), map.getHeight(), map.getNbAgts());

        MapMarker prev = null;
        for (var marker : map.getMarkers()) {
            if (marker.getType() == GOAL)
                goalLocations.add(marker.getLocation());

            this.add(marker.getType(), marker.getLocation().x, marker.getLocation().y);
            prev = marker;
        }

        randomAgentSpawn(this.width,this.height,this.nbAgts);

        File newFile = new File("./generated_map_data_" + mapType.name() + ".asl");
        try {

            // Delete existing file
            newFile.delete();
            newFile.createNewFile();

            FileWriter bos = new FileWriter(newFile);
            bos.write(dumpMapBeliefs());
            bos.close();
            logger.info("Wrote auto-generated map beliefs to " + newFile.getCanonicalPath());

        } catch (IOException e) {
            e.printStackTrace();
            logger.warning("Failed to output generated map belief file.. continuing anyways.");
        }
    }

    public static MapcModel loadFromFile(MapType mapType) {
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(mapType.getFileName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load map!", e);
        }
        LocalizationMap map = gson.fromJson(reader, LocalizationMap.class);

        return new MapcModel(map, mapType);
    }

    public int getNbAgts() {
        return nbAgts;
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
                    this.setAgPos(i,11,8);
                    notpositionned = false;
                }
            }
        }
    }

    public String dumpMapBeliefs() {
        Map<Location, List<Literal>> locationPercepts = new LinkedHashMap<>();
        Map<Location, List<Literal>> dirGoalBeliefs = new LinkedHashMap<>();
        Map<Location, List<Literal>> dirDispBeliefs = new LinkedHashMap<>();

        StringBuilder fileString = new StringBuilder();

        fileString.append("/** These are the beliefs generated for the map that are added automatically to the BB **/\n")
                .append("/** This file is not loaded by the agent. It is just the output for debugging purposes and will be overwritten. **/\n");


        // Generate range and single rules
        var rangeSingleRule = createRange(getWidth(), getHeight());
        for (Rule r : rangeSingleRule)
            fileString.append(r).append(".\n");

        fileString.append("\n");

        for (Location location : getAllLocations()) {
            if (!inGrid(location))
                continue;

            if (!isFreeOfObstacle(location)) {
                fileString.append(getLocationLiteral(location).setNegated(Literal.LNeg));
                fileString.append(".\r\n");
                continue;
            }

            locationPercepts.put(location, getLocationPercepts(location));

            if (GENERATE_DIRS) {
                var dirListTerm = getDirectionsToObj(location, GOAL);
                if (!dirListTerm.isEmpty())
                    dirGoalBeliefs.put(location, dirListTerm);

                dirListTerm = getDirectionsToObj(location, RED_DISP);
                if (!dirListTerm.isEmpty())
                    dirDispBeliefs.put(location, dirListTerm);
            }
        }

        fileString.append("\r\n");
        fileString.append("// Obstacle mappings\r\n");
        for (var entry : locationPercepts.entrySet())
            if (!entry.getValue().isEmpty())
                fileString.append(getBeliefASLString("", entry.getValue())); // Don't print comment before beliefs

        try {
            fileString.append(getBeliefASLString("", List.of(ASSyntax.parseRule("~obs(D) :- not(obs(D))."))));
            fileString.append("\n");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        if (!dirGoalBeliefs.isEmpty())
            fileString.append("// Direction mappings\r\n");
        for (var entry : dirGoalBeliefs.entrySet())
            fileString.append(getBeliefASLString("", entry.getValue()));
        for (var entry : dirDispBeliefs.entrySet())
            fileString.append(getBeliefASLString("", entry.getValue()));


        return fileString.toString();
    }

    public List<Literal> dumpMapBeliefsToBB() {
        List<Literal> bels = new ArrayList<>();

        Map<Location, List<Literal>> locationPercepts = new LinkedHashMap<>();
           Map<Location, List<Literal>> dirGoalBeliefs = new LinkedHashMap<>();


        // Generate range and single rules
        var rangeSingleRule = createRange(getWidth(), getHeight());
        bels.addAll(rangeSingleRule);

        for (Location location : getAllLocations()) {
            if (!inGrid(location))
                continue;

            if (!isFreeOfObstacle(location)) {
                bels.add(getLocationLiteral(location).setNegated(Literal.LNeg));
                continue;
            }

            locationPercepts.put(location, getLocationPercepts(location));

            var dirListTerm = getDirectionsToObj(location, GOAL);
            if (!dirListTerm.isEmpty())
                dirGoalBeliefs.put(location, dirListTerm);
        }

        for (var entry : locationPercepts.entrySet())
            if (!entry.getValue().isEmpty()) {
                bels.addAll(entry.getValue());
            }

        for (var entry : dirGoalBeliefs.entrySet())
            bels.addAll(entry.getValue());
        return bels;
    }


    private List<Rule> createRange(int width, int height) {
        var unif = new Unifier();
        unif.bind(new VarTerm("Width"), new NumberTermImpl(width - 1));
        unif.bind(new VarTerm("Height"), new NumberTermImpl(height - 1));

        try {
            var blankRange = ASSyntax.parseRule("range(loc(X, Y)) :- .range(X, 0, Width) & .range(Y, 0, Height).");
            // var blankSingle = ASSyntax.parseRule("single(loc(X, Y)) :- .range(X, XList) & .member(Y, YList).");
            return List.of(new Rule(blankRange, unif) // ,
                    // new Rule(blankSingle, unif)
            );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Location> getAllLocations() {
        List<Location> allLocations = new ArrayList<>();
        for (int x = 0; x < this.getWidth(); x++)
            for (int y = 0; y < this.getHeight(); y++)
                allLocations.add(new Location(x, y));
        return allLocations;
    }

    public List<Location> getGoalLocations() {
        return goalLocations;
    }

    private String getBeliefASLString(String heading, Collection<Literal> mapping) {
        StringBuilder builder = new StringBuilder();

        if (!heading.isEmpty())
            builder.append("// ").append(heading).append("\n");

        // Print beliefs one X coordinate at a time
        for (var belief : mapping)
            builder.append(belief.toString()).append(".\n");

        if (!heading.isEmpty())
            builder.append("\n");

        return builder.toString();
    }

    private List<Literal> getLocationPercepts(Location location) {
        Literal locationLit = getLocationLiteral(location);
        List<Literal> locationPercepts = new ArrayList<>();

        // Get percepts for this location
        var percepts = getPercepts(location);


        for (Literal percept : percepts)
            if (SHOULD_USE_REL_PERCEPTS) {
                if (percept.getTerm(2) == Terrain.OBSTACLE.getTerrainAtom()) {
                    locationPercepts.add(ASSyntax.createRule(ASSyntax.createLiteral("obs", percept.getTerm(0), percept.getTerm(1)), getLocationLiteral(location)));
                }
            } else {
                if (percept.getTerm(1) == Terrain.OBSTACLE.getTerrainAtom()) {
                    locationPercepts.add(ASSyntax.createRule(ASSyntax.createLiteral("obs", percept.getTerm(0)), getLocationLiteral(location)));
                }
            }

        return locationPercepts;
    }

    private Literal getLocationLiteral(Location location) {
        return ASSyntax.createLiteral("loc", ASSyntax.createNumber(location.x), ASSyntax.createNumber(location.y));
    }

    public List<Literal> getPercepts(Location agentPos) {
        Location left = new Location(-1, 0);
        Location right = new Location(1, 0);
        Location down = new Location(0, 1);
        Location up = new Location(0, -1);

        // Get directional percepts
        var arrList = new ArrayList<Literal>();

        if (config.useMaxPercepts()) {

            int sz = 3;
            int count = 0;
            for (int i = -sz; i <= sz; i++)
                for (int j = -sz; j <= sz; j++) {
                    if (i == 0 && j == 0)
                        continue;
                    count++;
                    arrList.add(getDirPercept(agentPos, i, j));
                }

        } else {
            arrList.add(getDirPercept(agentPos, 0, -1));
            arrList.add(getDirPercept(agentPos, 0, 1));
            arrList.add(getDirPercept(agentPos, -1, 0));
            arrList.add(getDirPercept(agentPos, 1, 0));
        }

        return arrList;
    }

    private Literal getDirPercept(Location cur, int x, int y) {
        Location delta = new Location(cur.x + x, cur.y + y);
        Terrain t = getPerceptTerrain(delta.x, delta.y);
        Atom a = t.getTerrainAtom();
        if (config.useUniquePercepts()) {
            countUniqueObjects++;
            return ASSyntax.createLiteral("percept", getLocationLiteral(new Location(x, y)), ASSyntax.createAtom(String.valueOf(Math.random())));
        }

        if (config.useMaxPercepts()) {
            if (!usedNone)
                return ASSyntax.createLiteral("percept", getLocationLiteral(new Location(x, y)), NONE.getTerrainAtom());
            else
                return ASSyntax.createLiteral("percept", getLocationLiteral(new Location(x, y)), Terrain.OBSTACLE.getTerrainAtom());
        }

        Literal locAtom;

        // Should we be using loc perceptions: percept(loc(0,-1)...) instead of percept(north,...)
        if (SHOULD_USE_REL_PERCEPTS) {
            return ASSyntax.createLiteral("percept", new NumberTermImpl(x), new NumberTermImpl(y), a);
        } else {
            if (x == 0 && y == -1)
                locAtom = ASSyntax.createAtom(NORTH);
            else if (x == 0 && y == 1)
                locAtom = ASSyntax.createAtom(SOUTH);
            else if (x == 1 && y == 0)
                locAtom = ASSyntax.createAtom(EAST);
            else // if (x == -1 && y == 0)
                locAtom = ASSyntax.createAtom(WEST);
            return ASSyntax.createLiteral("percept", locAtom, a);
        }
    }

    private Terrain getPerceptTerrain(int x, int y) {
        Location loc = new Location(x, y);
        if (!inGrid(loc) || isFreeOfObstacle(loc))
            return NONE;

        return Terrain.OBSTACLE;
    }

    @NotNull
    private List<Literal> getDirectionsToObj(Location location, int obj) {
//        var dirListTerm = new ListTermImpl();
        String objStr = obj == GOAL ? "goal" : "disp";

        List<Literal> result = new ArrayList<>();
        for (Term dirTerm : getNearestObjectDirections(location, obj))
            if (dirTerm != NONE.getTerrainAtom())
                result.add(ASSyntax.createRule(ASSyntax.createLiteral("dir", dirTerm, new Atom(objStr)), getLocationLiteral(location)));

        return result;
    }

    private List<Literal> getNearestObjectDirections(Location curLocation, int obj) {
        List<Literal> goalDirections = new ArrayList<>();

        if (hasObject(obj, curLocation)) {
            goalDirections.add(NONE.getTerrainAtom());
            return goalDirections;
        }

        Location west = new Location(curLocation.x - 1, curLocation.y);
        Location east = new Location(curLocation.x + 1, curLocation.y);
        Location north = new Location(curLocation.x, curLocation.y - 1);
        Location south = new Location(curLocation.x, curLocation.y + 1);

        int westPath = findPathToClosestObject(west, obj);
        int eastPath = findPathToClosestObject(east, obj);
        int northPath = findPathToClosestObject(north, obj);
        int southPath = findPathToClosestObject(south, obj);

        int minPath = Math.min(Math.min(Math.min(westPath, northPath), eastPath), southPath);

        if (minPath == Integer.MAX_VALUE)
            logger.info("Could not find object (" + obj + "). Returning all directions!");

        if (westPath == minPath)
            goalDirections.add(ASSyntax.createAtom("left"));
        if (eastPath == minPath)
            goalDirections.add(ASSyntax.createAtom("right"));
        if (northPath == minPath)
            goalDirections.add(ASSyntax.createAtom("up"));
        if (southPath == minPath)
            goalDirections.add(ASSyntax.createAtom("down"));

        return goalDirections;
    }

    private int findPathToClosestObject(Location curLocation, int obj) {
        Queue<Location> bfsQ = new LinkedList<>();
        Set<Location> visited = new HashSet<>();

        List<Integer> key = List.of(curLocation.x, curLocation.y, obj);

        if (closestObjCache.containsKey(key))
            return closestObjCache.get(key);

        bfsQ.add(curLocation);
        int pathLength = 0;

        while (!bfsQ.isEmpty()) {
            int curNodes = bfsQ.size();
            for (int i = 0; i < curNodes; i++) {

                Location next = bfsQ.poll();

                if (visited.contains(next))
                    continue;

                if (hasObject(obj, next)) {
                    closestObjCache.put(key, pathLength);
                    return pathLength;
                }

                visited.add(next);

                for (var adj : getAdjacentLocations(next)) {
                    if (!visited.contains(adj))
                        bfsQ.add(adj);
                }
            }
            pathLength++;
        }

        closestObjCache.put(key, Integer.MAX_VALUE);
        return Integer.MAX_VALUE;
    }

    private Set<Location> getAdjacentLocations(Location current) {
        int x = current.x;
        int y = current.y;

        Set<Location> adjSet = new HashSet<>();

        Location left = new Location(x - 1, y);
        Location right = new Location(x + 1, y);
        Location up = new Location(x, y - 1);
        Location down = new Location(x, y + 1);
        if (isAdjacent(current, left))
            adjSet.add(left);
        if (isAdjacent(current, right))
            adjSet.add(right);
        if (isAdjacent(current, up))
            adjSet.add(up);
        if (isAdjacent(current, down))
            adjSet.add(down);


        return adjSet;
    }

    @Override
    public void setAgPos(int ag, Location l) {
        // Set last position if agent 0
        this.lastPositions.set(ag, getAgPos(ag));
        super.setAgPos(ag, l);
    }

    // closed-world version
//    synchronized public boolean move(Move dir, int ag) throws Exception {
//        if (ag < 0) {
//            logger.warning("** Trying to move unknown agent!");
//            return false;
//        }
//        Location l = getAgPos(ag);
//        if (l == null) {
//            logger.warning("** We lost the location of agent " + (ag + 1) + "!"+this);
//            return false;
//        }
//        Location n = null;
//        switch (dir) {
//            case UP:
//                n =  new Location(l.x, l.y - 1);
//                break;
//            case DOWN:
//                n =  new Location(l.x, l.y + 1);
//                break;
//            case RIGHT:
//                n =  new Location(l.x + 1, l.y);
//                break;
//            case LEFT:
//                n =  new Location(l.x - 1, l.y);
//                break;
//        }
//
//        if (n != null && isFreeOfObstacle(n)) {
//            // if there is an agent there, move that agent
////            if (!hasObject(AGENT, n) || move(dir,getAgAtPos(n))) {
//                this.setAgPos(ag, n);
//                notifyListeners(ag, TypeEvent.MOVED, null);
//                return true;
////            }
//        }
//        return false;
//    }

    // Open-World version
    synchronized public boolean move(Move dir, int ag) throws Exception {
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
            notifyListeners(ag, TypeEvent.MOVED, null);
            return true;
        }

        return false;
    }

    public void addMapListener(MapEventListener listener) {
        this.mapEventListeners.add(listener);
        for(int i=0; i<3; i++){
            this.notifyListeners(i, TypeEvent.MOVED, null);
        }
    }


    private synchronized void notifyListeners(Integer agentId, TypeEvent typeEvent, Atom moveDirection) {
        Location agentLoc = getAgPos(agentId);

        if (moveDirection == null) {
            moveDirection = getLastLocation(agentId);
        }

        for (var listener : mapEventListeners)
            listener.agentMoved(new MapEvent(this, agentLoc, moveDirection, typeEvent, agentId), agentId);
    }

    public Atom getLastLocation(Integer agentId) {
        if (lastPositions.get(agentId) == null)
            return null;

        return getDirectionAtom(lastPositions.get(agentId), getAgPos(agentId));
    }

    private Atom getDirectionAtom(Location src, Location dst) {

        var delta = delta(src, dst);

        if (delta.x == 1)
            return ASSyntax.createAtom("right");
        if (delta.x == -1)
            return ASSyntax.createAtom("left");
        if (delta.y == -1)
            return ASSyntax.createAtom("up");
        if (delta.y == 1)
            return ASSyntax.createAtom("down");

        throw new NullPointerException("Huh?");
    }

    // Closed-World version
//    public Location delta(Location src, Location dst) {
//        Location delta = new Location(dst.x - src.x, dst.y - src.y);
//        if ((Math.abs(delta.x) != 1 && Math.abs(delta.y) != 1) || (delta.x == delta.y)) {
//            System.out.println("Invalid Direction? " + delta);
//            throw new NullPointerException();
//        }
//
//        return delta;
//    }

    // Open-world version
    public Location delta(Location src, Location dst) {
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

    // Closed-world version
//    private Literal getObsPercept(Location cur, int x, int y) {
//        Location delta = new Location(cur.x + x, cur.y + y);
//        Literal locAtom;
//        if (x == 0 && y == -1)
//            locAtom = ASSyntax.createAtom(NORTH);
//        else if (x == 0 && y == 1)
//            locAtom = ASSyntax.createAtom(SOUTH);
//        else if (x == 1 && y == 0)
//            locAtom = ASSyntax.createAtom(EAST);
//        else // if (x == -1 && y == 0)
//            locAtom = ASSyntax.createAtom(WEST);
//        if(inGrid(delta) && !isFreeOfObstacle(delta)){
//            return ASSyntax.createLiteral("obs", locAtom);
//        }
//        return ASSyntax.createLiteral("~obs", locAtom);
//    }

    // Open-world version
    private Literal getObsPercept(Location cur, int x, int y) {
        // Wrap-around si on sort de la grille
        int wrappedX = (cur.x + x + width) % width;
        int wrappedY = (cur.y + y + height) % height;
        Location delta = new Location(wrappedX, wrappedY);

        Literal locAtom;
        if (x == 0 && y == -1)
            locAtom = ASSyntax.createAtom(NORTH);
        else if (x == 0 && y == 1)
            locAtom = ASSyntax.createAtom(SOUTH);
        else if (x == 1 && y == 0)
            locAtom = ASSyntax.createAtom(EAST);
        else // if (x == -1 && y == 0)
            locAtom = ASSyntax.createAtom(WEST);

        if (!isFreeOfObstacle(delta)) {
            return ASSyntax.createLiteral("obs", locAtom);
        }
        return ASSyntax.createLiteral(Literal.LNeg, "obs", locAtom);
    }

    public boolean isAdjacent(Location firstLoc, Location secondLoc) {
        if (!isFreeOfObstacle(firstLoc) || !isFreeOfObstacle(secondLoc))
            return false;

        return firstLoc.distance(secondLoc) == 1;
    }

    public synchronized void signalInput(boolean val) {
        this.inputEnabled = val;
    }

}

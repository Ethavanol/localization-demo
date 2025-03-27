package localization.models;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import epistemic.DebugConfig;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import localization.MapEventListener;
import localization.perception.Terrain;
import localization.view.LocalizationMapView;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LocalizationMapModel extends GridWorldModel implements KeyListener {

    public static final int POSSIBLE = (int) Math.pow(2, 4);
    public static final int GOAL = (int) Math.pow(2, 3);
    public static final int RED_DISP = (int) Math.pow(2, 5);
    public static final int BLUE_DISP = (int) Math.pow(2, 6);
    private static final Terrain OBSTACLE = Terrain.OBSTACLE;
    private static final Terrain NONE = Terrain.NONE;
    private static final int AGENT_IDX = 0;
    private static final String NORTH = "up";
    private static final String SOUTH = "down";
    private static final String EAST = "left";
    private static final String WEST = "right";


    private List<MapEventListener> mapEventListeners;
    private List<Location> possibleLocations;
    private List<Location> goalLocations;

    private Location initialLocation;

    private Location lastPosition;
    private boolean inputEnabled = true;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private boolean usedNone = false;
    private final DebugConfig config;

    // Should we be using loc perceptions: percept(loc(0,-1)...) instead of percept(north,...)
    private static final boolean SHOULD_USE_REL_PERCEPTS = false;
    private static final boolean GENERATE_DIRS = true; // not needed when we generate larger maps

    public LocalizationMapModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        this.config = DebugConfig.getInstance();
        this.goalLocations = new ArrayList<>();
        this.mapEventListeners = new ArrayList<>();
        this.possibleLocations = new ArrayList<>();
    }

    public LocalizationMapModel(LocalizationMap map, LocalizationMapView.MapType mapType) {
        this(map.getWidth(), map.getHeight(), 1 + DebugConfig.getInstance().getExtraAgents().size());


        this.setAgPos(AGENT_IDX, map.getAgentStart());

//        var additionalAgents = DebugConfig.getInstance().getExtraAgents();
//
//        for (int i = 0; i < additionalAgents.size(); i++) {
//            Location loc = additionalAgents.get(i);
//            this.setAgPos(1 + i, loc);
//        }

        this.initialLocation = map.getAgentStart();

        MapMarker prev = null;
        for (var marker : map.getMarkers()) {
            if (marker.getType() == GOAL)
                goalLocations.add(marker.getLocation());

            this.add(marker.getType(), marker.getLocation().x, marker.getLocation().y);
            prev = marker;
        }

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

    public void addMapListener(MapEventListener listener) {
        this.mapEventListeners.add(listener);

        // Notify the listener of the current agent position
        this.notifyListeners(this.getAgPos(AGENT_IDX));
    }

    public Atom getLastDirectionAtom() {
        if (lastPosition == null)
            return NONE.getTerrainAtom();

        return getDirectionAtom(lastPosition, getAgPos(AGENT_IDX));
    }

    @Override
    public void setAgPos(int ag, Location l) {

        // Set last position if agent 0
        if (ag == AGENT_IDX)
            this.lastPosition = getAgPos(ag);

        super.setAgPos(ag, l);
    }

    private Location getNearestGoal(Location start) {
        int minDist = Integer.MAX_VALUE;
        Location minGoal = null;

        for (var goal : goalLocations) {
            int dist = goal.distance(start);
            if (dist < minDist) {
                minDist = dist;
                minGoal = goal;
            }
        }

        return minGoal;
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

    private Location findClosestGoal(Location curLocation) {
        // Return this position if it's a goal
        if (hasObject(GOAL, curLocation))
            return curLocation;

        Queue<Location> bfsQ = new LinkedList<>();
        Set<Location> visited = new HashSet<>();

        bfsQ.add(curLocation);
        int pathLength = 0;

        while (!bfsQ.isEmpty()) {
            int curNodes = bfsQ.size();
            for (int i = 0; i < curNodes; i++) {

                Location next = bfsQ.poll();

                if (visited.contains(next))
                    continue;

                if (hasObject(GOAL, next))
                    return next;

                visited.add(next);

                for (var adj : getAdjacentLocations(next)) {
                    if (!visited.contains(adj))
                        bfsQ.add(adj);
                }
            }
            pathLength++;
        }

        return null;
    }

    Map<List<Integer>, Integer> closestObjCache = new HashMap<>();

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


    private synchronized void addPossible() {
        for (var location : possibleLocations) {
            // For relative agent positions
            this.add(POSSIBLE, location);
        }

        this.view.getCanvas().invalidate();
    }

    private synchronized void clearPossible() {
        for (var location : possibleLocations)
            this.remove(POSSIBLE, location);

        this.possibleLocations.clear();

    }

    public synchronized void setPossible(List<Location> newPossible) {
        this.clearPossible();
        // Transform for true localization where we don't have absolute positions
//        List<Location> transformed = newPossible.stream().map(location ->  new Location(initialLocation.x + location.x, initialLocation.y + location.y)).collect(Collectors.toList());
        this.possibleLocations.addAll(newPossible);
        this.addPossible();
    }

    @Override
    public synchronized void keyTyped(KeyEvent e) {
        if (!inputEnabled) {
            logger.info("Waiting for agent to process previous input...");
            return;
        }

        if (e.getKeyChar() == 'v') {
            // Toggle settings panel
            boolean curVis = getView().getSettingsPanel().isVisible();
            getView().getSettingsPanel().setVisible(!curVis);
        }

        if (e.getKeyChar() == 'w')
            moveUp();
        else if (e.getKeyChar() == 'a')
            moveLeft();
        else if (e.getKeyChar() == 's')
            moveDown();
        else if (e.getKeyChar() == 'd')
            moveRight();
    }

    private synchronized void notifyListeners(Location agentLoc) {
        List<Literal> newPercepts = getPercepts(agentLoc);
        Atom moveDirection = getLastDirectionAtom();

        for (var listener : mapEventListeners)
            listener.agentMoved(new MapEvent(this, agentLoc, moveDirection));
    }

    public Map<Location, Terrain> getPerceptData() {
        var agentPos = getAgPos(0);

        int x = agentPos.x;
        int y = agentPos.y;

        // Get directional percepts
        Map<Location, Terrain> relPerceptData = new HashMap<>();

        relPerceptData.put(new Location(0, -1), getPerceptTerrain(agentPos.x, agentPos.y - 1));
        relPerceptData.put(new Location(0, 1), getPerceptTerrain(agentPos.x, agentPos.y + 1));
        relPerceptData.put(new Location(-1, 0), getPerceptTerrain(agentPos.x - 1, agentPos.y));
        relPerceptData.put(new Location(1, 0), getPerceptTerrain(agentPos.x + 1, agentPos.y));

        return relPerceptData;
    }

    private Terrain getPerceptTerrain(int x, int y) {
        Location loc = new Location(x, y);
        if (!inGrid(loc) || isFreeOfObstacle(loc))
            return NONE;

        return OBSTACLE;
    }

    private List<Location> getAllLocations() {
        List<Location> allLocations = new ArrayList<>();
        for (int x = 0; x < this.getWidth(); x++)
            for (int y = 0; y < this.getHeight(); y++)
                allLocations.add(new Location(x, y));
        return allLocations;
    }

    public List<Literal> dumpMapBeliefsToBB() {
        List<Literal> bels = new ArrayList<>();

//        countUniqueObjects = 0;
//        for (Location location : getAllLocations()) {
//            if (!inGrid(location))
//                continue;
//
//            if (!isFreeOfObstacle(location)) {
//                bels.add(getLocationLiteral(location).setNegated(Literal.LNeg));
//                continue;
//            }
//
//            bels.addAll(getLocationPercepts(location));
//            bels.add(getAdjacentBelief(location));
//            bels.addAll(getDirectionsToGoal(location));
//            bels.addAll(getDirectionsToDispensers(location));
//            bels.add(getGoalRel(location));
////            bels.addAll(getShortestPathDirs(location));
//        }
//
//        logger.info("number of unique objects: " + countUniqueObjects);

        Map<Location, List<Literal>> locationPercepts = new LinkedHashMap<>();
        Map<Location, Literal> adjBeliefs = new LinkedHashMap<>();
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
            adjBeliefs.put(location, getAdjacentBelief(location));

            var dirListTerm = getDirectionsToObj(location, GOAL);
            if (!dirListTerm.isEmpty())
                dirGoalBeliefs.put(location, dirListTerm);

//            Literal goalRelTerm = getGoalRel(location);
//            goalRelBeliefs.put(location, goalRelTerm);
        }

        for (var entry : locationPercepts.entrySet())
            if (!entry.getValue().isEmpty()) {
//                fileString.append(getBeliefASLString("Percept Mappings for " + entry.getKey(), entry.getValue()));
                bels.addAll(entry.getValue());
            }

        for (var entry : dirGoalBeliefs.entrySet())
            bels.addAll(entry.getValue());
        //            fileString.append(getBeliefASLString("Goal Direction Mappings for " + entry.getKey(), entry.getValue()));
        return bels;
    }

    private List<Literal> getShortestPathDirs(Location source) {
        List<Literal> shortestPathLits = new ArrayList<>();

        if (!inGrid(source) || !isFreeOfObstacle(source))
            return shortestPathLits;

        for (Location dest : getAllLocations()) {
            if (!inGrid(dest) || !isFreeOfObstacle(dest) || dest.equals(source))
                continue;

            Location west = new Location(source.x - 1, source.y);
            Location east = new Location(source.x + 1, source.y);
            Location north = new Location(source.x, source.y - 1);
            Location south = new Location(source.x, source.y + 1);

            int westPath = findPathTo(west, dest, new HashSet<>());
            int eastPath = findPathTo(east, dest, new HashSet<>());
            int northPath = findPathTo(north, dest, new HashSet<>());
            int southPath = findPathTo(south, dest, new HashSet<>());

            int minPath = Math.min(Math.min(Math.min(westPath, northPath), eastPath), southPath);

            if (minPath == Integer.MAX_VALUE) {
                logger.info("No path to dest!");
                continue;
            }

            if (westPath == minPath)
                shortestPathLits.add(ASSyntax.createLiteral("shortestPathDir", getLocationLiteral(source), getLocationLiteral(dest), ASSyntax.createAtom("left")));
            if (eastPath == minPath)
                shortestPathLits.add(ASSyntax.createLiteral("shortestPathDir", getLocationLiteral(source), getLocationLiteral(dest), ASSyntax.createAtom("right")));
            if (northPath == minPath)
                shortestPathLits.add(ASSyntax.createLiteral("shortestPathDir", getLocationLiteral(source), getLocationLiteral(dest), ASSyntax.createAtom("up")));
            if (southPath == minPath)
                shortestPathLits.add(ASSyntax.createLiteral("shortestPathDir", getLocationLiteral(source), getLocationLiteral(dest), ASSyntax.createAtom("down")));


            // Get shortest path to dest
            // Get direction 'dir' to next location on shortest path
            // ADd literal with (location, dest, dir)


        }

        return shortestPathLits;
    }

    private Map<Map.Entry<Location, Location>, Integer> cachePathSize = new HashMap<>();

    private int findPathTo(Location source, Location dest, Set<Location> visited) {
        if (source.equals(dest))
            return 0;

        if (visited.contains(source))
            return Integer.MAX_VALUE;

        visited.add(source);

        var entry = Map.entry(source, dest);

        if (cachePathSize.containsKey(entry))
            return cachePathSize.get(entry);

        int minPathLength = Integer.MAX_VALUE;

        for (var adj : getAdjacentLocations(source)) {

            if (!visited.contains(adj)) {
                int next = findPathTo(adj, dest, visited);
                if (next < minPathLength) {
                    minPathLength = next;
                }
            }
        }

        if (minPathLength == Integer.MAX_VALUE) {
            return minPathLength;
        }

        cachePathSize.put(entry, minPathLength + 1);
        return minPathLength + 1;

    }

    public String dumpMapBeliefs() {
        Map<Location, List<Literal>> locationPercepts = new LinkedHashMap<>();
        Map<Location, Literal> adjBeliefs = new LinkedHashMap<>();
        Map<Location, List<Literal>> dirGoalBeliefs = new LinkedHashMap<>();
        Map<Location, List<Literal>> dirDispBeliefs = new LinkedHashMap<>();
//        Map<Location, List<Literal>> dirDispBeliefs = new LinkedHashMap<>();
//        Map<Location, Literal> goalRelBeliefs = new LinkedHashMap<>();


//        var xVar = new VarTerm("X");
//        var yVar = new VarTerm("Y");
//        Literal locRange = new Rule(ASSyntax.createLiteral("range", ), body);

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
            adjBeliefs.put(location, getAdjacentBelief(location));

            if (GENERATE_DIRS) {
                var dirListTerm = getDirectionsToObj(location, GOAL);
                if (!dirListTerm.isEmpty())
                    dirGoalBeliefs.put(location, dirListTerm);

                dirListTerm = getDirectionsToObj(location, RED_DISP);
                if (!dirListTerm.isEmpty())
                    dirDispBeliefs.put(location, dirListTerm);
            }
//            Literal goalRelTerm = getGoalRel(location);
//            goalRelBeliefs.put(location, goalRelTerm);
        }

        fileString.append("\r\n");
        fileString.append("// Obstacle mappings\r\n");
        for (var entry : locationPercepts.entrySet())
            if (!entry.getValue().isEmpty())
                // fileString.append(getBeliefASLString("Percept Mappings for " + entry.getKey(), entry.getValue()));
                fileString.append(getBeliefASLString("", entry.getValue())); // Don't print comment before beliefs

        try {
            fileString.append(getBeliefASLString("", List.of(ASSyntax.parseRule("~obs(D) :- not(obs(D))."))));
            fileString.append("\n");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        if (!dirGoalBeliefs.isEmpty())
            fileString.append("// Direction mappings");
        for (var entry : dirGoalBeliefs.entrySet())
            fileString.append(getBeliefASLString("", entry.getValue()));
        for (var entry : dirDispBeliefs.entrySet())
            fileString.append(getBeliefASLString("", entry.getValue()));


        return fileString.toString();


//        return
////                getBeliefASLString("Locations", possibleLocs) +
////                        perceptString +
//                        getBeliefASLString("Adjacent Location Mappings", adjBeliefs.values()) +
////                getBeliefASLStringList("Dispenser Direction Mappings", dirDispBeliefs.values()) +
//                        goalString;
////                getBeliefASLString("Location Goal Rel. Mappings", goalRelBeliefs.values()) +

    }

    private List<Rule> createRange(int width, int height) {

        // var xList = new ListTermImpl();
        // var yList = new ListTermImpl();
        // for (var loc : allLocations) {
        //     if (!xList.contains(new NumberTermImpl(loc.x)))
        //         xList.add(new NumberTermImpl(loc.x));
        //     if (!yList.contains(new NumberTermImpl(loc.y)))
        //         yList.add(new NumberTermImpl(loc.y));
        // }

        // var unif = new Unifier();
        // unif.bind(new VarTerm("XList"), xList);
        // unif.bind(new VarTerm("YList"), yList);

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

    private Literal getGoalRel(Location location) {
        Location nearest = (getNearestGoal(location));
        Location delta = new Location(nearest.x - location.x, nearest.y - location.y);
        return ASSyntax.createLiteral("locGoalRel", getLocationLiteral(location), getLocationLiteral(delta));
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

    private String getBeliefASLStringList(String heading, Collection<List<Literal>> mapping) {
        StringBuilder builder = new StringBuilder();
        builder.append("// ").append(heading).append("\n");

        // Print beliefs one X coordinate at a time
        for (var belief : mapping)
            builder.append(getBeliefASLString("", belief));

        builder.append("\n");

        return builder.toString();
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

    @NotNull
    private List<Literal> getDirectionsToDispensers(Location location) {
        var redDispenserDirs = new ListTermImpl();
        redDispenserDirs.addAll(getNearestObjectDirections(location, RED_DISP));

        var blueDispenserDirs = new ListTermImpl();
        blueDispenserDirs.addAll(getNearestObjectDirections(location, BLUE_DISP));

        List<Literal> dispenserDirs = new ArrayList<>();
        dispenserDirs.add(ASSyntax.createLiteral("locDirToDispenser", getLocationLiteral(location), ASSyntax.createAtom("red"), redDispenserDirs));
        dispenserDirs.add(ASSyntax.createLiteral("locDirToDispenser", getLocationLiteral(location), ASSyntax.createAtom("blue"), blueDispenserDirs));
        return dispenserDirs;
    }

    private List<Literal> getLocationPercepts(Location location) {
        Literal locationLit = getLocationLiteral(location);
        List<Literal> locationPercepts = new ArrayList<>();

        // Get percepts for this location
        var percepts = getPercepts(location);


        for (Literal percept : percepts)
            if (SHOULD_USE_REL_PERCEPTS) {
                if (percept.getTerm(2) == OBSTACLE.getTerrainAtom()) {
                    locationPercepts.add(ASSyntax.createRule(ASSyntax.createLiteral("obs", percept.getTerm(0), percept.getTerm(1)), getLocationLiteral(location)));
                }
            } else {
                if (percept.getTerm(1) == OBSTACLE.getTerrainAtom()) {
                    locationPercepts.add(ASSyntax.createRule(ASSyntax.createLiteral("obs", percept.getTerm(0)), getLocationLiteral(location)));
                }
            }

        return locationPercepts;

        // Add Percept beliefs
//        var listTerm = new ListTermImpl();
//        listTerm.addAll(percepts);
//        return ASSyntax.createLiteral("locPercept", locationLit, listTerm);
    }

    private Literal getAdjacentBelief(Location location) {
        Literal locationLit = getLocationLiteral(location);
        var adjacent = getAdjacentLocations(location);

        var adjListTerm = new ListTermImpl();
        adjListTerm
                .addAll(adjacent.stream().map((adj) -> {
                    Literal adjLocLit = getLocationLiteral(adj);
                    Atom dirAtom = getDirectionAtom(adj, location);

                    return ASSyntax.createLiteral("adjacent", dirAtom, adjLocLit);
                }).collect(Collectors.toSet()));
        return ASSyntax.createLiteral("locAdjacent", locationLit, adjListTerm);
    }

    public Location delta(Location src, Location dst) {
        Location delta = new Location(dst.x - src.x, dst.y - src.y);
        if ((Math.abs(delta.x) != 1 && Math.abs(delta.y) != 1) || (delta.x == delta.y)) {
            logger.warning("Invalid Direction? " + delta);
            throw new NullPointerException();
        }

        return delta;
    }

    public Location getLastDirection() {
        if (lastPosition == null)
            return new Location(0, 0);
        return delta(lastPosition, getAgPos(AGENT_IDX));
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

    private Literal getLocationLiteral(Location location) {
        return ASSyntax.createLiteral("loc", ASSyntax.createNumber(location.x), ASSyntax.createNumber(location.y));
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

    public static LocalizationMapModel loadFromFile(LocalizationMapView.MapType mapType) {
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(mapType.getFileName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load map!", e);
        }
        LocalizationMap map = gson.fromJson(reader, LocalizationMap.class);

        return new LocalizationMapModel(map, mapType);
    }

    private LinkedList<Map<Location, Literal>> perms = new LinkedList<>();

    private void debugOnlyGenPerceptPerms(Location l) {
        if (perms.isEmpty()) {
            Map<Location, Literal> map = new HashMap<>();
            map.put(l, ASSyntax.createLiteral("percept", getLocationLiteral(l), OBSTACLE.getTerrainAtom()));
            perms.add(map);

            Map<Location, Literal> mapTwo = new HashMap<>();
            mapTwo.put(l, ASSyntax.createLiteral("percept", getLocationLiteral(l), NONE.getTerrainAtom()));
            perms.add(mapTwo);
            return;
        }

        HashSet<Map<Location, Literal>> newMaps = new HashSet<>();

        for (var entry : perms) {
            Map<Location, Literal> map = new HashMap<>(entry);
            Map<Location, Literal> mapTwo = new HashMap<>(entry);

            map.put(l, ASSyntax.createLiteral("percept", getLocationLiteral(l), OBSTACLE.getTerrainAtom()));
            mapTwo.put(l, ASSyntax.createLiteral("percept", getLocationLiteral(l), NONE.getTerrainAtom()));

            newMaps.add(map);
            newMaps.add(mapTwo);
        }

        perms.clear();
        perms.addAll(newMaps);
    }

    public List<Literal> getPercepts(Location agentPos) {
        Location left = new Location(-1, 0);
        Location right = new Location(1, 0);
        Location down = new Location(0, 1);
        Location up = new Location(0, -1);

        // Get directional percepts
        var arrList = new ArrayList<Literal>();

        if (config.useUniquePercepts()) {
            // Generate all unique perceptions
            for (int i = -5; i < 5; i++) {
                for (int j = -5; j < 5; j++) {
                    arrList.add(getDirPercept(agentPos, i, j));
                }
            }
        } else if (config.useMaxPercepts()) {
            if (perms.isEmpty() && !usedNone) {
                usedNone = true;
                debugOnlyGenPerceptPerms(left);
                debugOnlyGenPerceptPerms(right);
                debugOnlyGenPerceptPerms(up);
                debugOnlyGenPerceptPerms(down);
                System.out.println(perms.size());
            }

            if (perms.isEmpty()) {
                arrList.add(getDirPercept(agentPos, 0, -1));
                arrList.add(getDirPercept(agentPos, 0, 1));
                arrList.add(getDirPercept(agentPos, -1, 0));
                arrList.add(getDirPercept(agentPos, 1, 0));

            } else {
                var map = perms.removeFirst();
                arrList.add(map.get(left));
                arrList.add(map.get(right));
                arrList.add(map.get(up));
                arrList.add(map.get(down));
            }
        } else {
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
        }
        return arrList;
    }

    private int countUniqueObjects = 1;


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
                return ASSyntax.createLiteral("percept", getLocationLiteral(new Location(x, y)), OBSTACLE.getTerrainAtom());
        }

        Literal locAtom;

        // Should we be using loc perceptions: percept(loc(0,-1)...) instead of percept(north,...)
        if (SHOULD_USE_REL_PERCEPTS) {
            // Old -> Use actual rel location as percept
//            locAtom = getLocationLiteral(new Location(x, y));
//            locAtom = getLocationLiteral(new Location(x, y));
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

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private synchronized void move(Location delta) {
        Location agentPos = this.getAgPos(AGENT_IDX);

        agentPos.x += delta.x;
        agentPos.y += delta.y;

        if (this.inGrid(agentPos) && this.isFree(agentPos)) {
            this.setAgPos(0, agentPos);
            this.view.invalidate();
            notifyListeners(agentPos);
        }
    }

    public void moveLeft() {
        move(new Location(-1, 0));
    }

    public void moveDown() {
        move(new Location(0, 1));
    }

    public void moveUp() {
        move(new Location(0, -1));
    }

    public void moveRight() {
        move(new Location(1, 0));
    }

    /**
     * Are locations left/right/up/down adjacent cells?
     *
     * @param firstLoc
     * @param secondLoc
     * @return
     */
    public boolean isAdjacent(Location firstLoc, Location secondLoc) {
        if (!isFreeOfObstacle(firstLoc) || !isFreeOfObstacle(secondLoc))
            return false;

        return firstLoc.distance(secondLoc) == 1;
    }

    public LocalizationMapView getView() {
        return (LocalizationMapView) this.view;
    }

    public synchronized void signalInput(boolean val) {
        this.inputEnabled = val;
    }
}

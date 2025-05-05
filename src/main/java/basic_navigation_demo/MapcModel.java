package basic_navigation_demo;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;


public class MapcModel extends GridWorldModel {

    public static final int GOAL = 16;
    private static final String NORTH = "up";
    private static final String SOUTH = "down";
    private static final String EAST = "right";
    private static final String WEST = "left";
    private boolean inputEnabled = true;
    private Location lastPosition;
    private final Logger logger = Logger.getLogger(getClass().getName());


    private List<MapEventListener> mapEventListeners;

    private static final int AGENT_IDX = 0;
    // the grid size
    public static final int GSize = 5;

    Location lGoal = new Location(GSize - 2,GSize - 2);

    public MapcModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        this.mapEventListeners = new ArrayList<>();

        addWall(1, 2, 3, 2);

        randomAgentSpawn(w,h,nbAgs);

        add(GOAL, lGoal);
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
                    this.setAgPos(i,0,4);
                    notpositionned = false;
                }
            }
        }
    }

    @Override
    public void setAgPos(int ag, Location l) {
        // Set last position if agent 0
        if (ag == AGENT_IDX)
            this.lastPosition = getAgPos(ag);

        super.setAgPos(ag, l);
    }

    // Closed-World version
//    void move(String direction) {
//        Location r1 = getAgPos(0);
//        switch(direction){
//            case "up":
//                r1.y--;
//                break;
//            case "down":
//                r1.y++;
//                break;
//            case "left":
//                r1.x--;
//                break;
//            case "right":
//                r1.x++;
//                break;
//        }
//        if(!this.inGrid(r1)){
//            Atom moveDirection = ASSyntax.createAtom(direction);
//            notifyListeners(getAgPos(0), TypeEvent.FAILED, moveDirection);
//        }
//        if(this.isFree(r1)){
//            setAgPos(0, r1);
//            notifyListeners(getAgPos(0), TypeEvent.MOVED, null);
//        }
//    }

    // Open-World version
    void move(String direction) {
        Location r1 = getAgPos(0);
        int newX = r1.x;
        int newY = r1.y;

        switch (direction) {
            case "up":
                newY = (newY - 1 + GSize) % GSize;
                break;
            case "down":
                newY = (newY + 1) % GSize;
                break;
            case "left":
                newX = (newX - 1 + GSize) % GSize;
                break;
            case "right":
                newX = (newX + 1) % GSize;
                break;
        }

        Location newPos = new Location(newX, newY);

        if (this.isFree(newPos)) {
            setAgPos(0, newPos);
            notifyListeners(newPos, TypeEvent.MOVED, null);
        } else {
            Atom moveDirection = ASSyntax.createAtom(direction);
            notifyListeners(r1, TypeEvent.FAILED, moveDirection);
        }
    }

    public void addMapListener(MapEventListener listener) {
        this.mapEventListeners.add(listener);
        this.notifyListeners(this.getAgPos(0), TypeEvent.MOVED, null);
    }


    private synchronized void notifyListeners(Location agentLoc, TypeEvent typeEvent, Atom moveDirection) {
        if (moveDirection == null) {
            moveDirection = getLastLocation();
        }

        for (var listener : mapEventListeners)
            listener.agentMoved(new MapEvent(this, agentLoc, moveDirection, typeEvent));
    }

    public Atom getLastLocation() {
        if (lastPosition == null)
            return null;

        return getDirectionAtom(lastPosition, getAgPos(0));
    }

    private Atom getDirectionAtom(Location src, Location dst) {

        var delta = delta(src, dst, GSize, GSize);

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

    public Location delta(Location src, Location dst, int width, int height) {
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

    public List<Literal> getObsPercepts(Location agentPos) {
        // Get directional percepts
        var arrList = new ArrayList<Literal>();

        arrList.add(getObsPercept(agentPos, 0, -1));
        arrList.add(getObsPercept(agentPos, 0, 1));
        arrList.add(getObsPercept(agentPos, -1, 0));
        arrList.add(getObsPercept(agentPos, 1, 0));
        return arrList;
    }

    // Closed-World version
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
//        return ASSyntax.createLiteral(Literal.LNeg,"obs", locAtom);
//    }

    // Open-World version
    private Literal getObsPercept(Location cur, int x, int y) {
        // Wrap-around si on sort de la grille
        int wrappedX = (cur.x + x + GSize) % GSize;
        int wrappedY = (cur.y + y + GSize) % GSize;
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

        // On n'a plus besoin de inGrid() car on reste toujours dans la grille
        if (!isFreeOfObstacle(delta)) {
            return ASSyntax.createLiteral("obs", locAtom); // obstacle présent
        }
        return ASSyntax.createLiteral(Literal.LNeg, "obs", locAtom); // pas d'obstacle
    }

    public synchronized void signalInput(boolean val) {
        this.inputEnabled = val;
    }

}

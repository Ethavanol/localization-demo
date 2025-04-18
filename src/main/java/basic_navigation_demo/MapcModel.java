package basic_navigation_demo;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;
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

        this.setAgPos(AGENT_IDX, 0, 0);

        addWall(1, 2, 3, 2);

        add(GOAL, lGoal);
    }



    @Override
    public void setAgPos(int ag, Location l) {
        // Set last position if agent 0
        if (ag == AGENT_IDX)
            this.lastPosition = getAgPos(ag);

        super.setAgPos(ag, l);
    }

    void move(String direction) {
        Location r1 = getAgPos(0);
        switch(direction){
            case "up":
                r1.y--;
                break;
            case "down":
                r1.y++;
                break;
            case "left":
                r1.x--;
                break;
            case "right":
                r1.x++;
                break;
        }
        if(!this.inGrid(r1)){
            Atom moveDirection = ASSyntax.createAtom(direction);
            notifyListeners(getAgPos(0), TypeEvent.FAILED, moveDirection);
        }
        if(this.isFree(r1)){
            setAgPos(0, r1);
            notifyListeners(getAgPos(0), TypeEvent.MOVED, null);
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

    public Location delta(Location src, Location dst) {
        Location delta = new Location(dst.x - src.x, dst.y - src.y);
        if ((Math.abs(delta.x) != 1 && Math.abs(delta.y) != 1) || (delta.x == delta.y)) {
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


    private Literal getObsPercept(Location cur, int x, int y) {
        Location delta = new Location(cur.x + x, cur.y + y);
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
        return ASSyntax.createLiteral("~obs", locAtom);
    }

    public synchronized void signalInput(boolean val) {
        this.inputEnabled = val;
    }

}

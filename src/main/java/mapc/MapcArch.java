package mapc;

import jason.architecture.AgArch;
import jason.asSyntax.Literal;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapcArch extends AgArch {

    MapcModel model = null;
    MapcView view = null;

    int myId = -1;

    protected Logger logger = Logger.getLogger(MapcArch.class.getName());


    /** this version of perceive is used in local simulator. it gets
     the perception and updates the world model. only relevant percepts
     are leaved in the list of perception for the agent.
     */
    @Override
    public Collection<Literal> perceive() {
        Collection<Literal> per = super.perceive();
        try {
            if (per != null) {
                Iterator<Literal> ip = per.iterator();

            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in perceive!", e);
        }
        return per;
    }

//    void obstaclePerceived(int x, int y, Literal p) {
//        if (! model.hasObject(MapcModel.OBSTACLE, x, y)) {
//            model.add(MapcModel.OBSTACLE, x, y);
//        }
//    }

    public int getMyId() {
        if (myId < 0) {
            myId = getAgId(getAgName());
        }
        return myId;
    }

    public MapcModel getModel() {
        return model;
    }

    public static int getAgId(String agName) {
        return (Integer.parseInt(agName.substring(agName.length()-1))) - 1;
    }

}

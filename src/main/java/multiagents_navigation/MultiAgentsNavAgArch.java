package multiagents_navigation;

import epistemic_jason.architecture.AgArchEpistemic;
import jason.asSyntax.Literal;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiAgentsNavAgArch extends AgArchEpistemic {

    MultiAgentsNavModel model = null;

    int myId = -1;

    protected Logger logger = Logger.getLogger(MultiAgentsNavAgArch.class.getName());


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

    public int getMyId() {
        if (myId < 0) {
            myId = getAgId(getAgName());
        }
        return myId;
    }

    public MultiAgentsNavModel getModel() {
        return model;
    }

    public static int getAgId(String agName) {
        return (Integer.parseInt(agName.substring(agName.length()-1))) - 1;
    }

}

package basic_navigation_demo;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class MapcEnv extends Environment implements MapEventListener {

    private Queue<MapEvent> mapEventQueue;

    public static final Literal ag = Literal.parseLiteral("at(goal)");

    static Logger logger = Logger.getLogger(MapcEnv.class.getName());

    private MapcModel model;
    private MapcView view;

    @Override
    public void init(String[] args){
        this.mapEventQueue = new LinkedList<>();

        model = new MapcModel(5,5,1);

        view = new MapcView(model);
        model.setView(view);

        model.addMapListener(this);
        super.init(args);
    }


    @Override
    public void informAgsEnvironmentChanged(String... agents) {
        super.informAgsEnvironmentChanged(agents);
    }

    @Override
    public synchronized void agentMoved(MapEvent event) {
        this.mapEventQueue.add(event);
        // Disable input until agent is ready.
        model.signalInput(false);

        // Inform that agents need new percepts (otherwise there is a delay!)ù=

        this.informAgsEnvironmentChanged();
    }

    @Override
    public synchronized Collection<Literal> getPercepts(String agName) {
        // No change in perceptions if the agent hasn't moved
        // Also, keep current percepts if the agent is not done reasoning
        clearPercepts(agName);

        var curPercepts = super.getPercepts(agName);

        if (curPercepts == null)
            curPercepts = new ArrayList<>();

        // Add always-present percepts
        if (mapEventQueue.isEmpty() && curPercepts.isEmpty())
            return curPercepts;

        // If no events need to be processed, return null (no change in percepts)
        if (mapEventQueue.isEmpty())
            return null;

        if(model.getAgPos(0).equals(model.lGoal)){
            curPercepts.add(ag);
        }

        // Get next event to process
        MapEvent nextEvent = mapEventQueue.poll();
        if (nextEvent.getMoveDirectionAtom() != null){
            curPercepts.add(nextEvent.toDelEvent());
        }

        curPercepts.addAll(nextEvent.getObsPerceptions());



        return curPercepts;
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("[" + ag + "] doing: " + action);

        if (action.getFunctor().equals("move")) {
            String dir = action.getTerm(0).toString();
            System.out.println("moooove : "+ dir);
            model.move(dir);
        } else if (action.getFunctor().equals("submit")) {
            System.out.println("lessgo");
        }
        return true;
    }
}

package mapc;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapcEnv extends Environment implements MapEventListener {

    private HashMap<Integer, Queue<MapEvent>> mapEventQueue;

    public static final Literal ag = Literal.parseLiteral("at(goal)");

    static Logger logger = Logger.getLogger(MapcEnv.class.getName());

    private MapcModel model;
    private MapcView view;

    String agentName;

    @Override
    public void init(String[] args){
        this.mapEventQueue = new HashMap<>();

        MapcModel.loadFromFile(MapType.LOCALIZATION_20x20);

        agentName = args[1];

        view = new MapcView(MapType.LOCALIZATION_20x20);
        model = view.getModel();

        model.addMapListener(this);
        view.setVisible(true);
        super.init(args);
    }

    public MapcModel getModel() {
        return model;
    }

    @Override
    public void informAgsEnvironmentChanged(String... agents) {
        super.informAgsEnvironmentChanged(agents);
    }

    @Override
    public synchronized void agentMoved(MapEvent event, Integer agentId) {
        this.mapEventQueue
                .computeIfAbsent(agentId, k -> new LinkedList<>()) // crée une queue si elle n'existe pas
                .add(event);
        // Disable input until agent is ready.
        model.signalInput(false);

        // Inform that agents need new percepts (otherwise there is a delay!)
        this.informAgsEnvironmentChanged(getAgNameFromId(agentId));
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

        Location agentPos = model.getAgPos(getAgNbFromName(agName));

        for (Location goal : model.getGoalLocations()) {
            if (agentPos.equals(goal)) {
                curPercepts.add(ag);
                break;
            }
        }

        // Get next event to process
        MapEvent nextEvent = this.mapEventQueue.get(getAgNbFromName(agName)).poll();
        if(nextEvent == null){
            return curPercepts;
        }

        curPercepts.add(nextEvent.toDelEvent());
        curPercepts.addAll(nextEvent.getObsPerceptions());


        return curPercepts;
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("[" + ag + "] doing: " + action);

        boolean result = false;
        int agId = -10;
        try{
            agId = getAgNbFromName(ag);

            if (action.getFunctor().equals("move")) {
                String dir = action.getTerm(0).toString();
                switch (dir){
                    case "up":
                        result = model.move(MapcModel.Move.UP, agId);
                        break;
                    case "down":
                        result = model.move(MapcModel.Move.DOWN, agId);
                        break;
                    case "left":
                        result = model.move(MapcModel.Move.LEFT, agId);
                        break;
                    case "right":
                        result = model.move(MapcModel.Move.RIGHT, agId);
                        break;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing " + action + " for " + ag + " (ag code:"+agId+")", e);
        }
        return true;
    }

    public int getAgNbFromName(String agName) {
        if (model.getNbAgts() > 1){
            return (Integer.parseInt(agName.substring(agentName.length()))) - 1;
        } else {
            return 0;
        }
    }

    public String getAgNameFromId(int agId) {
        if (model.getNbAgts() > 1){
            return agentName + (agId+1);
        } else {
            return agentName;
        }
    }
}

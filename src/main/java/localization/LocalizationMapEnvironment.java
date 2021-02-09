package localization;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import localization.models.LocalizationMapModel;
import localization.models.MapEvent;
import localization.perception.AgentPerspectiveMap;
import localization.view.LocalizationMapView;

import java.util.*;

public class LocalizationMapEnvironment extends Environment implements MapEventListener {


    private static final String RED_ITEM = "red";
    private static final String BLUE_ITEM = "blue";
    // Hack to access from agent....
    public static LocalizationMapEnvironment instance;
    private final AgentPerspectiveMap observedMap;

    private final LocalizationMapView localizationMapView;
    private final LocalizationMapModel localizationMapModel;
    private final Queue<MapEvent> mapEventQueue;
    
    private final Set<String> backpack;

    public LocalizationMapEnvironment() {
        instance = this;
        this.mapEventQueue = new LinkedList<>();
        localizationMapView = new LocalizationMapView();
        localizationMapModel = localizationMapView.getModel();
        this.observedMap = new AgentPerspectiveMap();

        // Generate the map information beliefs based on the loaded map
        localizationMapModel.generateASL();


        localizationMapModel.addMapListener(this);
        localizationMapView.setVisible(true);
        backpack = new HashSet<>();
    }

    @Override
    public void init(String[] args) {
        super.init(args);
    }

    @Override
    public synchronized Collection<Literal> getPercepts(String agName) {
        // No change in perceptions if the agent hasn't moved
        // Also, keep current percepts if the agent is not done reasoning
        super.clearPercepts(agName);

        var curPercepts = super.getPercepts(agName);

        if (curPercepts == null)
            curPercepts = new ArrayList<>();

        // add persistent percepts
        curPercepts.addAll(getPersistentPercepts());

        // Add always-present percepts
        if (mapEventQueue.isEmpty() && curPercepts.isEmpty())
            return curPercepts;

        // If no events need to be processed, return null (no change in percepts)
        if (mapEventQueue.isEmpty())
            return null;


        // Get next event to process
        MapEvent nextEvent = mapEventQueue.poll();

        curPercepts.add(ASSyntax.createAtom("moved"));
        curPercepts.addAll(nextEvent.getPerceptions());
        curPercepts.add(ASSyntax.createLiteral("lastMove", nextEvent.getMoveDirectionAtom()));

        return curPercepts;
    }

    @Override
    public boolean executeAction(String agName, Structure act) {
        getLogger().info("Action: " + act.toString());

        if(act.getFunctor().equals("move"))
        {
            String dir = act.getTerm(0).toString();
            if(dir.equals("right")) localizationMapModel.moveRight();
            if(dir.equals("left")) localizationMapModel.moveLeft();
            if(dir.equals("up")) localizationMapModel.moveUp();
            if(dir.equals("down")) localizationMapModel.moveDown();
        }

        if(act.getFunctor().equals("pickUp")) {
            var loc = localizationMapModel.getAgPos(0);
            if (localizationMapModel.hasObject(LocalizationMapModel.RED_DISP, loc)) {
                backpack.add(RED_ITEM);
                localizationMapModel.remove(LocalizationMapModel.RED_DISP, loc);
            }

            if (localizationMapModel.hasObject(LocalizationMapModel.BLUE_DISP, loc)) {
                backpack.add(BLUE_ITEM);
                localizationMapModel.remove(LocalizationMapModel.BLUE_DISP,loc);
            }
        }

        if(act.getFunctor().equals("submit")) {
            if(backpack.contains(RED_ITEM))
                getLogger().info("Submit success! Congratulations!");
            else
            {
                getLogger().warning("Submit without item! Failure.");
                return false;
            }
        }

        return true;
    }

    private List<Literal> getPersistentPercepts() {
        List<Literal> persistPercepts = new ArrayList<>();

        persistPercepts.add(ASSyntax.createLiteral("modelObject", new ObjectTermImpl(localizationMapModel)));



        // Add dynamic map knowledge
//        persistPercepts.addAll(getModel().dumpMapBeliefsToBB());

        if (localizationMapView.getSettingsPanel().shouldAutoMove())
            persistPercepts.add(ASSyntax.createLiteral("autoMove"));

        return persistPercepts;
    }


    @Override
    public synchronized void agentMoved(MapEvent event) {
        this.mapEventQueue.add(event);

        // Disable input until agent is ready.
        getModel().signalInput(false);

        // Inform that agents need new percepts (otherwise there is a delay!)
        if (this.getEnvironmentInfraTier() != null)
            this.getEnvironmentInfraTier().informAgsEnvironmentChanged();
    }

    public LocalizationMapModel getModel() {
        return localizationMapModel;
    }
}

package localization;

// import epistemic.DebugConfig;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import localization.models.LocalizationMapModel;
import localization.models.MapEvent;
import localization.view.LocalizationMapView;

import java.util.*;

public abstract class LocalizationMapEnvironment extends Environment implements MapEventListener {

    private Queue<MapEvent> mapEventQueue;

    private LocalizationMapView localizationMapView;
    private LocalizationMapModel localizationMapModel;

    @Override
    public void init(String[] args) {
        this.mapEventQueue = new LinkedList<>();

        // Dump all map type beliefs

        LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_4x3);
        LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_5x5);
//        LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_10x10);
//        LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_20x20);
//        LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_30x30);
//        LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_40x40);
//        LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_50x50);
        // LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_100x100);



        localizationMapView = new LocalizationMapView(LocalizationMapView.MapType.LOCALIZATION_4x3);
        localizationMapModel = localizationMapView.getModel();

        localizationMapModel.addMapListener(this);
        // if(DebugConfig.getInstance().showGUI())
            localizationMapView.setVisible(true);
        super.init(args);

    }

    @Override
    public synchronized Collection<Literal> getPercepts(String agName) {
        // No change in perceptions if the agent hasn't moved
        // Also, keep current percepts if the agent is not done reasoning
        clearPercepts(agName);

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

    private List<Literal> getPersistentPercepts() {
        List<Literal> persistPercepts = new ArrayList<>();

        persistPercepts.add(ASSyntax.createLiteral("modelObject", new ObjectTermImpl(localizationMapModel)));

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

    @Override
    public final boolean executeAction(String agName, Structure act) {
        if(act.getFunctor().equals("move"))
        {
            String dir = act.getTerm(0).toString();
            if(dir.equals("right")) localizationMapModel.moveRight();
            if(dir.equals("left")) localizationMapModel.moveLeft();
            if(dir.equals("up")) localizationMapModel.moveUp();
            if(dir.equals("down")) localizationMapModel.moveDown();
            return true;
        }

        return executeAction(agName, act.getFunctor(), act);
    }

    public abstract boolean executeAction(String agName, String actionName, Structure action);
}

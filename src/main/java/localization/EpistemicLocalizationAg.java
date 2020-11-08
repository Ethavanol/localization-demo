package localization;

import epistemic.agent.EpistemicAgent;

public class EpistemicLocalizationAg extends EpistemicAgent {


    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        // Add map data
        // Changed this to on-the-fly (percepts) so that we can shift towards MAPC
//        for (var bel : LocalizationMapEnvironment.instance.getModel().dumpMapBeliefsToBB())
//            this.addInitialBel(bel);
    }
}

package localization;

import epistemic.agent.EpistemicAgent;
import jason.infra.centralised.CentralisedAgArch;

public class EpistemicLocalizationAg extends EpistemicAgent {


    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        var arch = this.getTS().getAgArch();

        while(arch != null && !(arch instanceof CentralisedAgArch))
            arch = arch.getNextAgArch();

        var myArch = (CentralisedAgArch) arch;
        LocalizationMapEnvironment localizationMapEnvironment = (LocalizationMapEnvironment) myArch.getEnvInfraTier().getUserEnvironment();

        for (var bel : localizationMapEnvironment.getModel().dumpMapBeliefsToBB())
            this.addInitialBel(bel);
    }
}

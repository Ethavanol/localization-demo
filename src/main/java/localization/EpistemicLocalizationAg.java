package localization;

import epistemic.agent.EpistemicAgent;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.local.LocalAgArch;

public class EpistemicLocalizationAg extends EpistemicAgent {


    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        var arch = this.getTS().getAgArch();

        while(arch != null && !(arch instanceof LocalAgArch))
            arch = arch.getNextAgArch();

        var myArch = (LocalAgArch) arch;
        LocalizationMapEnvironment localizationMapEnvironment = (LocalizationMapEnvironment) myArch.getEnvInfraTier().getUserEnvironment();

        for (var bel : localizationMapEnvironment.getModel().dumpMapBeliefsToBB())
            this.addInitialBel(bel);
    }
}

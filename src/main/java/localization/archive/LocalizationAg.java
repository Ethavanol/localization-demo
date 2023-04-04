package localization.archive;

import jason.asSemantics.Agent;
import localization.LocalizationAgArch;

/**
 * @deprecated Used for naive localization approach (no reasoner). Kept for reference.
 */
@Deprecated
public class LocalizationAg extends Agent {


    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        var arch = this.getTS().getAgArch();

        while(arch != null && !(arch instanceof LocalizationAgArch))
            arch = arch.getNextAgArch();

        var myArch = (LocalizationAgArch) arch;

        for (var bel : myArch.getEnvironment().getModel().dumpMapBeliefsToBB())
            this.addInitialBel(bel);
    }
}

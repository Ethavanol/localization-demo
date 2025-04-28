package mapc;

import jason.asSemantics.Agent;
import jason.infra.local.LocalAgArch;

public class MapcAgent extends Agent {

    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        var arch = this.getTS().getAgArch();

        while (arch != null && arch.getNextAgArch() != null && !(arch instanceof LocalAgArch))
            arch = arch.getNextAgArch();

        var myArch = (LocalAgArch) arch;
        MapcEnv mapcEnvironment = (MapcEnv) myArch.getEnvInfraTier().getUserEnvironment();

        for (var bel : mapcEnvironment.getModel().dumpMapBeliefsToBB())
            this.addInitialBel(bel);
    }
}

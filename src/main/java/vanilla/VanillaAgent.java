package vanilla;

import jason.asSemantics.Agent;
import jason.infra.local.LocalAgArch;
import localization.models.LocalizationMapModel;
import localization.view.LocalizationMapView;

public class VanillaAgent extends Agent {
    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        var arch = this.getTS().getAgArch();



        while (arch != null && arch.getNextAgArch() != null && !(arch instanceof LocalAgArch))
            arch = arch.getNextAgArch();

        var myArch = (LocalAgArch) arch;
        VanillaEnvironment localizationMapEnvironment = (VanillaEnvironment) myArch.getEnvInfraTier().getUserEnvironment();
        var mapModel = LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_5x5);
        // Setup fake model for evaluation of updates/queries
        // System.out.println("FAKE MODEL CREATION IS ACTIVE");
        // System.out.println("FAKE MODEL CREATION IS ACTIVE");
        // System.out.println("FAKE MODEL CREATION IS ACTIVE");
        // this.getTS().getEpistemic().setFakeLocalizationModel(localizationMapEnvironment.getModel().getWidth());


        // for (var bel : localizationMapEnvironment.getModel().dumpMapBeliefsToBB())
        //     this.addInitialBel(bel);
    }

}

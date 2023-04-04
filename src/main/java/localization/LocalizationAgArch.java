package localization;

import jason.architecture.AgArch;
import jason.infra.centralised.CentralisedAgArch;
import navigation.NavigationMapEnvironment;

/**
 * Used to signal to gui that input can be taken from user after reasoning cycle is complete.
 */
public class LocalizationAgArch extends AgArch {

    private NavigationMapEnvironment curEnv;

    @Override
    public void reasoningCycleFinished() {
        super.reasoningCycleFinished();
        // Signal input when finished running


        if(this.getTS().getC().getNbRunningIntentions() == 0)
            getEnvironment().getModel().signalInput(true);

    }

    public NavigationMapEnvironment getEnvironment()
    {
        if(curEnv != null)
            return curEnv;

        var cur = super.getFirstAgArch();

        while(cur != null)
        {
            if(cur instanceof CentralisedAgArch)
            {
                var env = ((CentralisedAgArch) cur).getEnvInfraTier().getUserEnvironment();
                if(env instanceof NavigationMapEnvironment)
                {
                    this.curEnv = (NavigationMapEnvironment) env;
                    break;
                }
            }
            cur = cur.getNextAgArch();
        }

        return curEnv;
    }
}

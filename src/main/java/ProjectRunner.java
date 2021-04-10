import jason.JasonException;
import jason.infra.centralised.RunCentralisedMAS;
import jason.util.Config;

import java.io.FileNotFoundException;

public class ProjectRunner {
    public static void main(String[] args) throws JasonException, FileNotFoundException {
        Config.get().setProperty(Config.START_WEB_MI, "false");

        // default to epistemic-agents.mas2j
        if(args.length == 0)
            args = new String[] {"epistemic-agents.mas2j"};

        RunCentralisedMAS.main(args);
    }
}

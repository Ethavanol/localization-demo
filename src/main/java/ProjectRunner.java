import jason.JasonException;
import jason.infra.local.RunLocalMAS;
import jason.util.Config;

import java.io.FileNotFoundException;

public class ProjectRunner {
    public static void main(String[] args) throws JasonException, FileNotFoundException {
        Config.get().setProperty(Config.START_WEB_MI, "false");

        // default to epistemic-agents.mas2j
        if(args.length == 0)
            args = new String[] {"simple_navigation.mas2j"};

        RunLocalMAS.main(args);
    }
}

import jason.JasonException;
import jason.infra.centralised.RunCentralisedMAS;

import java.io.FileNotFoundException;

public class ProjectRunner {
    public static void main(String[] args) throws JasonException, FileNotFoundException {
        RunCentralisedMAS.main(new String[] {"epistemic-agents.mas2j"});
    }
}

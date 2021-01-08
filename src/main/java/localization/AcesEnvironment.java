package localization;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.util.ArrayList;
import java.util.Collection;

public class AcesEnvironment extends Environment {


    // Hack to access from agent....
    public static AcesEnvironment instance;

    public AcesEnvironment() {
        instance = this;
    }

    @Override
    public void init(String[] args) {
        super.init(args);
    }

    @Override
    public synchronized Collection<Literal> getPercepts(String agName) {
        // No change in perceptions if the agent hasn't moved
        // Also, keep current percepts if the agent is not done reasoning
        super.clearPercepts(agName);

        var curPercepts = super.getPercepts(agName);

        if (curPercepts == null)
            curPercepts = new ArrayList<>();

//        curPercepts.add(ASSyntax.createLiteral("cards", ASSyntax.createString("Bob"), ASSyntax.createString("AA")));
//        curPercepts.add(ASSyntax.createLiteral("cards", ASSyntax.createString("Charlie"), ASSyntax.createString("AA")));
        curPercepts.add(ASSyntax.createLiteral("peekedCard", ASSyntax.createString("A")));
        return curPercepts;
    }

    @Override
    public boolean executeAction(String agName, Structure act) {
        return true;
    }
}

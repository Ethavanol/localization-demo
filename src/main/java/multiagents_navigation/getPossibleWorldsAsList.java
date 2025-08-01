package multiagents_navigation;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.List;

public class getPossibleWorldsAsList extends DefaultInternalAction {

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        // We only allow var terms that are unground.
        if (!args[0].isVar() || args[0].isGround()) {
            throw JasonException.createWrongArgument(this,"first argument must be an unground variable term");
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Term result = args[0];

        MultiAgentsNavAgent ag = (MultiAgentsNavAgent) ts.getAg();
        List<String> propositionsList = ag.getListPropsLocationsIA();
        ListTerm resList = new ListTermImpl();
        for (String proposition : propositionsList) {
            Literal litProposition = ASSyntax.parseLiteral(proposition);
            resList.add(litProposition);
        }
        return un.unifies(result, resList);
    }
}

package internal;

import epistemic.agent.EpistemicAgent;
import epistemic.distribution.formula.EpistemicFormula;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import localization.models.LocalizationMapModel;

import java.util.HashSet;
import java.util.Set;

public class test_query extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        var res = ts.getAg().getBB().getCandidateBeliefs(ASSyntax.createLiteral("modelObject", ASSyntax.createVar("Model")), null);

        if (!res.hasNext())
            return false;

        Literal modelObjLit = res.next();
        ObjectTerm modelObjTerm = (ObjectTerm) modelObjLit.getTerm(0);
        LocalizationMapModel localizationMapModel = (LocalizationMapModel) modelObjTerm.getObject();


        int num = -1;

        num = ((int) ((NumberTerm) args[0]).solve());
        Literal queryLit = (Literal) args[1];

        EpistemicAgent ag = (EpistemicAgent) ts.getAg();

        Set<EpistemicFormula> queries = new HashSet<>();

        int cnt = 0;

        for (var form : ag.getCandidateFormulas(EpistemicFormula.fromLiteral(queryLit)))
        {
            if (num == -1 || cnt < num)
            {
                queries.add(form);
                cnt ++;
            }

        }

        var res2 = ag.getEpistemicDistribution().evaluateFormulas(queries);
        return true;
    }
}

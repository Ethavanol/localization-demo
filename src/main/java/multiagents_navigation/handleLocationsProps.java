package multiagents_navigation;

import MAP.Direction;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.ArrayList;
import java.util.List;

public class handleLocationsProps extends DefaultInternalAction {

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        // We only allow var terms that are unground.
        if (!args[0].isVar() || args[0].isGround()) {
            throw JasonException.createWrongArgument(this,"first argument must be an unground variable term");
        }

        // check that arg 1 is a direction
        if (!args[1].isAtom()) {
            throw JasonException.createWrongArgument(this,
                    "second argument must be an atom representing a direction");
        }

        String dirStr = args[1].toString();
        boolean valid = false;
        for (Direction d : Direction.values()) {
            if (d.toString().equals(dirStr)) { // compare avec "up", "down", etc.
                valid = true;
                break;
            }
        }

        if (!valid) {
            throw JasonException.createWrongArgument(this,
                    "second argument must be one of: up, down, left, right");
        }

        if (!args[2].isList()) {
            throw JasonException.createWrongArgument(this,"third argument must be a list");
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        MultiAgentsNavAgent ag = (MultiAgentsNavAgent) ts.getAg();
        boolean openWorld = ag.getOpenWorld();
        int width = ag.getMapWidth();
        int height = ag.getMapHeight();

        Term result = args[0];
        Term dir = args[1];
        ListTerm listProps = (ListTerm) args[2];
        ListTerm resList = new ListTermImpl();

        for (Term proposition : listProps) {
            Literal litProposition = ASSyntax.parseLiteral(proposition.toString());
            Term termX = litProposition.getTerm(0);
            Term termY = litProposition.getTerm(1);

            int x = (termX.isNumeric()) ? (int) ((NumberTerm) termX).solve() : -1;
            int y = (termY.isNumeric()) ? (int) ((NumberTerm) termY).solve() : -1;

            switch (dir.toString()) {
                case "up":
                    y -= 1;
                    break;
                case "down":
                    y += 1;
                    break;
                case "left":
                    x -= 1;
                    break;
                case "right":
                    x += 1;
                    break;
            }

            if (openWorld) {
                x = (x + width) % width;
                y = (y + height) % height;
            } else {
                if (x < 0 || x >= width || y < 0 || y >= height) {
                    continue;
                }
            }

            Literal newLoc = ASSyntax.createLiteral("loc",
                    ASSyntax.createNumber(x),
                    ASSyntax.createNumber(y));

            resList.add(newLoc);
        }
        return un.unifies(result, resList);
    }
}

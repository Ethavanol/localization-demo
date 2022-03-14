package minesweeper;

import jason.NoValueException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.util.*;

public class MineSweeperEnvironment extends Environment {


    // Hack to access from agent....
    public static MineSweeperEnvironment instance;

    private List<Map.Entry<Integer, Integer>> clicked;
    private List<Map.Entry<Integer, Integer>> mines;
    private Map<Map.Entry<Integer, Integer>, Integer> mapHints;

    public MineSweeperEnvironment() {
        instance = this;
    }

    @Override
    public void init(String[] args) {
        super.init(args);
        this.clicked = new ArrayList<>();
        this.mines = new ArrayList<>();
        this.mapHints = new HashMap<>();
        createMap();
    }

    private void createMap()
    {
        int height = 3;
        int width = 3;
        int mines = 3;

        this.mines.add(loc(1,1));
        this.mines.add(loc(2,1));
        this.mines.add(loc(1,2));

        mapHints.put(loc(1, 1), -1);
        mapHints.put(loc(1, 2), -1);
        mapHints.put(loc(1, 3), 1);
        mapHints.put(loc(2, 1), -1);
        mapHints.put(loc(2, 2), 3);
        mapHints.put(loc(2, 3), 1);
        mapHints.put(loc(3, 1), 1);
        mapHints.put(loc(3, 2), 1);
        mapHints.put(loc(3, 3), 0);

        clicked.add(loc(3,3));
        clicked.add(loc(3,2));
        clicked.add(loc(2,3));


//        for (int r = 1; r <= height; r++) {
//            for (int c = 1; c <= width; c++) {
//
//            }
//        }

    }

    private Map.Entry<Integer, Integer> loc(int r, int c) {
        return new AbstractMap.SimpleEntry<>(r, c);
    }

    private boolean done = false;
    private boolean lost = false;

    @Override
    public synchronized Collection<Literal> getPercepts(String agName) {
        // No change in perceptions if the agent hasn't moved
        // Also, keep current percepts if the agent is not done reasoning
        super.clearPercepts(agName);

        var curPercepts = super.getPercepts(agName);

        if (curPercepts == null)
            curPercepts = new ArrayList<>();

        /**
         * clicked(2, 3).
         * clicked(3, 3).
         * clicked(3, 2).
         *
         * hint(3, 3, 0).
         * hint(3, 3, 0)
         */


        for (Map.Entry<Integer, Integer> click : this.clicked)
        {
            int r = click.getKey();
            int c = click.getValue();
            int hint = mapHints.get(click);

            // if hint == 0, click all neighbors
            // Copy francois' code


            curPercepts.add(ASSyntax.createLiteral("clicked", ASSyntax.createNumber(r), ASSyntax.createNumber(c)));
            curPercepts.add(ASSyntax.createLiteral("hint", ASSyntax.createNumber(r), ASSyntax.createNumber(c), ASSyntax.createNumber(hint)));
        }


        if(lost)
        {
            done = true;
            curPercepts.add(ASSyntax.createLiteral("lost"));
        }



        if(this.clicked.size() == 9 - 2)
        {
            // set done for next time
            done = true;
            curPercepts.add(ASSyntax.createLiteral("done"));
        }

        return curPercepts;
    }

    @Override
    public boolean executeAction(String agName, Structure act) {
        if(act.getFunctor().equals("click") && act.getArity() == 2)
        {
            try {
                int row = (int) ((NumberTerm) act.getTerm(0)).solve();
                int col = (int) ((NumberTerm) act.getTerm(1)).solve();
                this.clicked.add(loc(row, col));

                if(this.mines.contains(loc(row, col)))
                    lost = true;
            } catch (NoValueException e) {
                e.printStackTrace();
            }



        }
        return true;
    }
}

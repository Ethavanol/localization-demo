package navigation;

import epistemic_jason.asSemantics.AgentEpistemic;
import epistemic_jason.asSemantics.modelListener.ModelResponse;
import epistemic_jason.asSemantics.modelListener.World;
import epistemic_jason.formula.PropFormula;
import jason.asSemantics.Event;
import jason.asSemantics.Option;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import jason.environment.grid.Location;
import jason.infra.local.LocalAgArch;

import java.util.*;

public class NavAgent extends AgentEpistemic {

    NavEnv mapcEnv;

    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        var arch = this.getTS().getAgArch();

        while (arch != null && arch.getNextAgArch() != null && !(arch instanceof LocalAgArch))
            arch = arch.getNextAgArch();

        var myArch = (LocalAgArch) arch;
        mapcEnv = (NavEnv) myArch.getEnvInfraTier().getUserEnvironment();

        for (var bel : mapcEnv.getModel().dumpMapBeliefsToBB())
            this.addInitialBel(bel);
    }

    @Override
    public void eventModelApplied(Event event){
        // we update the directions of the worlds in the model only if the agent moved and his possibles locations changed
        if(event.getTrigger().getOperator().equals(Trigger.TEOperator.add) && event.getTrigger().getLiteral().toString().startsWith("on(")){
            try {
                ModelResponse model = this.getWorldsResponseModel();
                updateView(model.getWorlds());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void updateView(List<World> worlds) throws ParseException {
        List<Location> possibleLocations = new ArrayList<>();
        for (World w : worlds) {
            // we get the directions generated before
            Map<String, Boolean> propositions = w.getPropositions();
            String locValue = null;
            List<PropFormula> listNewProps = new ArrayList<>();
            for (String prop : propositions.keySet()) {
                if (prop.startsWith("loc(")) {
                    locValue = prop;
                }
            }
            possibleLocations.add(stringToLocation(locValue));
        }
        mapcEnv.getModel().setPossible(possibleLocations);
    }

    public Location stringToLocation(String location){
        String[] coordinates = location.substring(location.indexOf("(")+1, location.indexOf(")")).split(",");
        return new Location(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
    }


    @Override
    public Option selectOption(List<Option> options) {
        if (options != null && !options.isEmpty()) {
            return options.remove(0);
        } else {
            return null;
        }
    }
}

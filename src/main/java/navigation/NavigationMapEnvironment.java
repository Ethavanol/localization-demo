package navigation;

import jason.asSyntax.Structure;
import localization.LocalizationMapEnvironment;
import localization.models.LocalizationMapModel;

import java.util.HashSet;
import java.util.Set;

public class NavigationMapEnvironment extends LocalizationMapEnvironment {


    private static final String RED_ITEM = "red";
    private static final String BLUE_ITEM = "blue";

    private Set<String> backpack;

    public NavigationMapEnvironment() {
    }

    @Override
    public void init(String[] args) {
        backpack = new HashSet<>();
        super.init(args);
    }

    @Override
    public boolean executeAction(String agName, String action, Structure act) {
        getLogger().info("Action: " + act.toString());

        LocalizationMapModel localizationMapModel = getModel();

        if(action.equals("pickUp")) {
            var loc = localizationMapModel.getAgPos(0);
            if (localizationMapModel.hasObject(LocalizationMapModel.RED_DISP, loc)) {
                backpack.add(RED_ITEM);
                localizationMapModel.remove(LocalizationMapModel.RED_DISP, loc);
            }

            if (localizationMapModel.hasObject(LocalizationMapModel.BLUE_DISP, loc)) {
                backpack.add(BLUE_ITEM);
                localizationMapModel.remove(LocalizationMapModel.BLUE_DISP,loc);
            }
        }

        if(act.getFunctor().equals("submit")) {
            if(backpack.contains(RED_ITEM))
                getLogger().info("Submit success! Congratulations!");
            else
            {
                getLogger().warning("Submit without item! Failure.");
                return false;
            }
        }

        return true;
    }



}

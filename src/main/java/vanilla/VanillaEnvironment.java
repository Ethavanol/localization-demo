package vanilla;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.parser.ParseException;
import jason.environment.Environment;
import localization.models.LocalizationMapModel;
import localization.view.LocalizationMapView;

import java.util.Collection;
import java.util.List;

public class VanillaEnvironment extends Environment {
    public VanillaEnvironment() {
        super();
    }


    @Override
    public void informAgsEnvironmentChanged(String... agents) {
        super.informAgsEnvironmentChanged(agents);
    }

    @Override
    public Collection<Literal> getPercepts(String agName) {

        try {
            return List.of(
                    // ASSyntax.parseLiteral("loc(1, 1)")
                    ASSyntax.parseLiteral("asd")
                    // ASSyntax.parseLiteral("~asd")
            );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearPercepts() {
        super.clearPercepts();
    }

    @Override
    public boolean executeAction(String agName, Structure act) {
        return super.executeAction(agName, act);
    }

    public LocalizationMapModel getModel() {
        return LocalizationMapModel.loadFromFile(LocalizationMapView.MapType.LOCALIZATION_50x50);
    }
}

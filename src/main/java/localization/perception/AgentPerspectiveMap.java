package localization.perception;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;

import java.util.*;

public class AgentPerspectiveMap {
    private final Map<Location, Perception> observedMap;
    private Location curLocation;

    public AgentPerspectiveMap() {
        this.observedMap = new HashMap<>();
        this.curLocation = new Location(0,0);
    }

    public boolean agentMoved(Location movementDirection, Perception newPercept)
    {
        this.curLocation = new Location(curLocation.x + movementDirection.x, curLocation.y + movementDirection.y);
        var oldPercept = this.observedMap.put(curLocation, newPercept);

        // Return true if the previous value was null or the new percepts are different
        return oldPercept == null || !oldPercept.equals(newPercept);
    }
    public List<Literal> toMapBeliefData() {
        List<Literal> locationPercepts = getPercepts(curLocation);

        for(var locationEntry : observedMap.keySet())
        {
            locationPercepts.add(toLocationBeliefData(locationEntry));
        }

        return locationPercepts;
    }

    public Literal toLocationBeliefData(Location curLocation) {
        Literal locationLit = getLocationLiteral(curLocation);
        List<Literal> locationPercepts = getPercepts(curLocation);

        // Get percepts for this location
        var percepts = getPercepts(curLocation);

        // Add Percept beliefs
        var listTerm = new ListTermImpl();
        listTerm.addAll(percepts);
        return ASSyntax.createLiteral("locPercept", locationLit, listTerm);
    }

    private List<Literal> getPercepts(Location curLocation) {
        return observedMap.get(curLocation).toLiterals();
    }

    public static Literal getLocationLiteral(Location location) {
        return ASSyntax.createLiteral("location", ASSyntax.createNumber(location.x), ASSyntax.createNumber(location.y));
    }

}

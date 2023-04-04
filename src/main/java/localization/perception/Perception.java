package localization.perception;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static localization.perception.AgentPerspectiveMap.getLocationLiteral;

public class Perception {

    public static Location LEFT = new Location(-1,0);
    public static Location RIGHT = new Location(1,0);
    public static Location UP = new Location(0,-1);
    public static Location DOWN = new Location(0,1);

    private Map<Location, Terrain> perceptData;
    public Perception(Map<Location, Terrain> perceptData)
    {
        this.perceptData = new HashMap<>(perceptData);
    }

    public void addPercept(Location location, Terrain terrain)
    {
        this.perceptData.put(location, terrain);
    }

    public List<Literal> toLiterals() {
        List<Literal> literals = new ArrayList<>();
        for(var locationEntry : this.perceptData.entrySet())
        {
            var location = locationEntry.getKey();
            var terr = locationEntry.getValue();

            literals.add(ASSyntax.createLiteral("percept", getLocationLiteral(new Location(location.x, location.y)), terr.getTerrainAtom()));
        }
        return literals;
    }

    @Override
    public int hashCode() {
        return perceptData.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;

        if(!(obj instanceof Perception))
            return false;

        Perception otherP = (Perception) obj;

        return otherP.perceptData.equals(perceptData);
    }
}

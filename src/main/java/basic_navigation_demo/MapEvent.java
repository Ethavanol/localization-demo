package basic_navigation_demo;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;

import java.util.EventObject;
import java.util.List;


public class MapEvent extends EventObject {

    private final MapcModel source;
    private final Location newLocation;
    private final Atom moveDirection;
    private final TypeEvent typeEvent;

    public MapEvent(MapcModel source, Location newLocation, Atom direction, TypeEvent typeEvent) {
        super(source);
        this.source = source;
        this.newLocation = newLocation;
        this.moveDirection = direction;
        this.typeEvent = typeEvent;
    }

    public Location getNewLocation() {
        return newLocation;
    }

    public Atom getMoveDirection() {
        return source.getLastLocation();
    }
    public Atom getMoveDirectionAtom() {
        return moveDirection;
    }

    public List<Literal> getObsPerceptions() {
        return source.getObsPercepts(newLocation);
    }

    public Literal toDelEvent() {
        if(typeEvent == TypeEvent.MOVED){
            return ASSyntax.createLiteral("moved", this.moveDirection);
        } else {
            return ASSyntax.createLiteral("move_failed", this.moveDirection);
        }
    }

    public TypeEvent getTypeEvent() {
        return typeEvent;
    }

    @Override
    public MapcModel getSource() {
        return source;
    }
}

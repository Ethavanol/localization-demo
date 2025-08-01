package multiagents_navigation;

import MAP.Direction;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;

import java.util.EventObject;
import java.util.List;


public class MapEvent extends EventObject {

    private final MultiAgentsNavModel source;
    private final Location newLocation;
    private final Direction moveDirection;
    private final TypeEvent typeEvent;
    private final Integer agentId;

    public MapEvent(MultiAgentsNavModel source, Location newLocation, Direction direction, TypeEvent typeEvent, int ag) {
        super(source);
        this.source = source;
        this.newLocation = newLocation;
        this.moveDirection = direction;
        this.typeEvent = typeEvent;
        this.agentId = ag;
    }

    public Location getNewLocation() {
        return newLocation;
    }

    public Direction getMoveDirection() {
        return source.getLastLocation(agentId);
    }

    public Direction getMoveDirectionAtom() {
        return moveDirection;
    }

    public List<Literal> getObsPerceptions() {
        return source.getObsPercepts(this.agentId);
    }

    public List<Literal> getAgentsPerceptions() {
        return source.getAgentsPercepts(this.agentId);
    }

    public Literal toDelEvent() {
        if(typeEvent == TypeEvent.MOVED){
            if(this.moveDirection == null){
                return ASSyntax.createLiteral("moved");
            }
            return ASSyntax.createLiteral("moved", new Atom(this.moveDirection.toString()));
        } else {
            return ASSyntax.createLiteral("move_failed", new Atom(this.moveDirection.toString()));
        }
    }

    public TypeEvent getTypeEvent() {
        return typeEvent;
    }

    @Override
    public MultiAgentsNavModel getSource() {
        return source;
    }
}

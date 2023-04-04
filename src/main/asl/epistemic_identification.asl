{ include("epistemic_localization.asl") }

// Add agent possibilities to our model
range(agent(Agent))  :-  .member(Agent, ["Bob", "Charlie"]).

possible_locations("Bob", [location(1, 1), location(2,1)]).
possible_locations("Charlie", [location(1, 3), location(2,3)]).

isDelta(location(X1, Y1), location(X2, Y2), DeltaX, DeltaY) :- X2 == (X1 + DeltaX) & Y2 == (Y1 + DeltaY).

is_perceived(AgPoss, Possible, PerceiveX, PerceiveY) :- .member(LocOne, AgPoss) & .member(LocTwo, Possible) & isDelta(LocOne, LocTwo, PerceiveX, PerceiveY).

!identifyAgent(1,0).

+!identifyAgent(PerceiveX, PerceiveY)
    <-  .abolish(~agent(_)); // Remove old eliminations
        .setof(location(X, Y), possible(location(X, Y)), Possible); // Get all possible locations
        for(range(agent(Agent))) {
            ?possible_locations(Agent, AgPoss); // Get Agent's possible locations
            if(not is_perceived(Possible, AgPoss, PerceiveX, PerceiveY))
            {
                .print("Impossible: ", Agent);
                +~agent(Agent);
            };
        };
        ?agent(Ag);
        .print("The agent at (", PerceiveX, ", ", PerceiveY, ") has been identified as: ", Ag).

// Perceive agent B at PX, PY
// Get all agent possible locations. For each agent Ag's location L:
    // Is L at (PX, PY) away from any of our possible locations?
        // No: eliminate Ag possibility


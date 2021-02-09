//{ include("map_locations.asl") }

/*********************/
/* Better Generation */
/*********************/
range(location(X, Y))  :-  locPercept(location(X, Y), _).
range(percept(Direction, Object)) :- locPercept(_, Perceptions) & .member(percept(Direction, Object), Perceptions).
range(direction(Dir)) :- locDirToGoal(_, Dirs) & .member(Dir, Dirs).
range(goalRelative(Gx, Gy)) :- locGoalRel(location(X,Y), location(Gx, Gy)).
//range(adjacent(MoveDir, Prev)) :- locAdjacent(_, Adj) & .member(adjacent(MoveDir, Prev), Adj).


//adjacent(MoveDir, Prev) :- location(X, Y) & locAdjacent(location(X, Y), Adj) & .member(adjacent(MoveDir, Prev), Adj).
percept(Direction, Object) :- location(X, Y) & locPercept(location(X, Y), Perceptions) & .member(percept(Direction, Object), Perceptions).
direction(Dir) :- location(X, Y) & locDirToGoal(location(X,Y), Dirs) & .member(Dir, Dirs).
goalRelative(Gx, Gy) :- location(X, Y) & locGoalRel(location(X,Y), location(Gx, Gy)).

/************************/
/* END Model Generation */
/************************/

// The following plan runs when the agent moves and perceptions are updated
+moved
    <-  .print("I Moved.");
        !updateAdjacent; // Update the adjacent locations based on movement from our previous locations to further eliminate worlds
        !updatePrevious;
        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known


+!updateAdjacent
    :  not previousPossible(PrevList). // Get previous locations



// Update the reasoner with knowledge of our adjacent positions
+!updateAdjacent
    :  previousPossible(PrevPos) &
       lastMove(MoveDir)
    <-  .abolish(~location(_, _));
        for(locAdjacent(Loc, Adj) & not (.member(adjacent(MoveDir, Loc2), Adj) & .member(Loc2, PrevPos))) { +~Loc; }.

// Update the reasoner with knowledge of our adjacent positions
// OLD WAY with adj. locations
+!updateAdjacentOLD
    :  previousNotPossible(PrevNotPossList) & // Get previous locations
       previousPossible(PrevPos) &
       lastMove(MoveDir)

    <-  .abolish(~adjacent(_, _));
        // A little bit hacky since we don't have a way to express 'OR' knowledge. This is made more complex because we have multiple adjacent locations in each world.
        // What we have to do is find all adjacent(X, Pos) where Pos doesn't contain our movement and a prev position, and add all adjacent literals as false knowledge
        for(locAdjacent(Loc, Adj) & not (.member(adjacent(MoveDir, Loc2), Adj) & .member(Loc2, PrevPos))) { for(.member(NotAdj, Adj)) { +~NotAdj; }; }.

+!updateGUIPossible
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) & // get all possibilities from reasoner
        .setof(Dir, possible(direction(Dir)), AllDir)
    <-  .print("Possible Locations: ", Possible); // Print to agent log
        .print("Possible Directions: ", AllDir); // Print to agent log
        internal.update_best_move(AllDir);
        internal.update_possible(Possible). // Update GUI positions

+!updatePrevious
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) & // get all possibilities from reasoner
        .setof(location(X, Y), ~location(X, Y), NotPossible) // get all non-possibilities from reasoner
    <-  .abolish(previousPossible(_)); // Reset previous possibilities
        +previousPossible(Possible).

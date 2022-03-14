/*********************/
/*  Model Generation */
/*********************/
range(location(X, Y))  :-  locPercept(location(X, Y), _).
range(percept(Direction, Object)) :- locPercept(_, Perceptions) & .member(percept(Direction, Object), Perceptions).

percept(Direction, Object) :- location(X, Y) & locPercept(location(X, Y), Perceptions) & .member(percept(Direction, Object), Perceptions).

/************************/
/* END Model Generation */
/************************/

isAdjacent(Prev, Dir, Cur) :-
    locAdjacent(Cur, AdjList) &
    .member(adjacent(Dir, Prev), AdjList).



vals([percept(location(0,-1),none), percept(location(0,1), none), percept(location(1,0), none), percept(location(-1,0), none)]).

// The following plan runs when the agent moves and perceptions are updated
//@moved[atomic]
//+moved
//    : .length(L) == 0 |
//        (vals(L) & [H|T] = L)
//    <-  .print("I Moved.");
//        +H;
//        -vals(_);
//        +vals(T);
////        !updateAdjacent; // Update the adjacent locations based on movement from our previous locations to further eliminate worlds
////        !updatePrevious;
//        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known

@moved[atomic]
+moved
    <-  .print("I Moved.");
        internal.test_query(5, possible(take(X)));
        internal.test_query(50, possible(take(X)));
        internal.test_query(5, ~take(X));
        internal.test_query(50, ~take(X));
        internal.test_query(5, fake(X));
        internal.test_query(50, fake(X));
        !updateAdjacent; // Update the adjacent locations based on movement from our previous locations to further eliminate worlds
//        !updatePrevious;
        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known


+!updateAdjacent
    :  not previousPossible(PrevList). // Have not moved (no prev locations)

// Update the reasoner with knowledge of our adjacent positions
+!updateAdjacent
    :   previousPossible(PrevPos)
    <-  .abolish(~location(_, _));
        ?lastMove(MoveDir);
        .print(PrevPos);
        .print(CurrentPossible);
        for(range(location(X,Y)) & not(.member(Prev, PrevPos) & isAdjacent(Prev, MoveDir, location(X, Y)))) { +~location(X, Y); }.

+!updateGUIPossible
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) & // get all possibilities from reasoner
        .setof(Dir, possible(closest(dispenser(red), Dir)), AllDir)
    <-  .print("Possible Locations: ", Possible); // Print to agent log
        .print("Possible Directions: ", AllDir); // Print to agent log
        internal.update_best_move(AllDir);
        internal.update_possible(Possible). // Update GUI positions

+!updatePrevious
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) // get all possibilities from reasoner
    <-  .abolish(previousPossible(_)); // Reset previous possibilities
        +previousPossible(Possible).
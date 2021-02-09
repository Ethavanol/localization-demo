/*********************/
/* Better Generation */
/*********************/
range(location(X, Y))  :-  locPercept(location(X, Y), _).
range(percept(Direction, Object)) :- locPercept(_, Perceptions) & .member(percept(Direction, Object), Perceptions).
range(closest(dispenser(Type), Dir)) :- locDirToDispenser(_, Type, Dirs) & .member(Dir, Dirs).
range(closest(goal, Dir)) :- locDirToGoal(_, Dirs) & .member(Dir, Dirs).


percept(Direction, Object) :- location(X, Y) & locPercept(location(X, Y), Perceptions) & .member(percept(Direction, Object), Perceptions).
closest(dispenser(Type), Dir) :- location(X, Y) & locDirToDispenser(location(X, Y), Type, Dirs) & .member(Dir, Dirs).
closest(goal, Dir) :- location(X, Y) & locDirToGoal(location(X,Y), Dirs) & .member(Dir, Dirs).

/************************/
/* END Model Generation */
/************************/


isAdjacent(Prev, Dir, Cur) :-
    locAdjacent(Cur, AdjList) &
    .member(adjacent(Dir, Prev), AdjList).

// The following plan runs when the agent moves and perceptions are updated
@moved[atomic]
+moved
    <-  .print("I Moved.");
        !updateAdjacent; // Update the adjacent locations based on movement from our previous locations to further eliminate worlds
        !updatePrevious;
        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known

!completeTask.


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

+!completeTask
    <-  !navigate(dispenser(red));
        pickUp;
        !navigate(goal);
        submit.

+!navigate(Object)
    :   possible(closest(Object, Dir)) & Dir \== none
    <-  .print(Object, " in ", Dir);
        move(Dir);
        !navigate(Object).

+!navigate(Object)
    :   closest(Object, none)
    <-  .print("At ", Object).








//
//+!navigate(dispenser, Color)
//    :   dispenser(Color, none) // At dispenser
//    <-  .print("At dispenser ", Color).
//
//+!navigateGoal
//    :   possible(goal(Dir)) & Dir \== none
//    <-  .print("Goal in ", Dir);
//        move(Dir);
//        !navigateGoal.
//
//+!navigateGoal
//    :   goal(none) // At goal
//    <-  .print("At goal ").

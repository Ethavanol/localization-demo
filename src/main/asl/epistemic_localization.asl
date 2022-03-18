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



// w -> w'

// pre: location(X, Y)
// post: location(X - 1, Y)

possible(location(X - 1, Y))
    :- possible(location(X, Y)) & lastMove(left).
// location(1, 1) :- location(0, 1)
// location(1, 1) :- percept(up, block)


post(click(X, Y)) :- ~click(X, Y);
// click(1, 1) :- ~click(1, 1)




// V(w) => click(1,1) Francois
// V(w) => click(1,1)
post(click(1, 1)) :- ~click(1, 1) & clicked(1, 1);

// possible(location(0, 0)) ...
// 1. Find all worlds where this is true: WT
// possible(location(-1, 0)) ...
// 2. Find all worlds where these are true: WT2

// possible(location(0, 0)) -> possible(location(-1, 0)). NULL
// possible(location(1, 0)) -> possible(location(0, 0)). w10 -> w00


// V(w10) = V(w00)






post(location(X - 1, Y))
    :- location(X, Y) & lastMove(left).

possible(location(X + 1, Y))
    :- location(X, Y) & lastMove(right).

possible(location(X, Y - 1))
    :- location(X, Y) & lastMove(up).

possible(location(X, Y + 1))
    :- location(X, Y) & lastMove(down).

possible(location(X, Y))
    :- location(X, Y) & lastMove(none).


+!navigate(Object)
    :   possible(location(X, Y))
    <-  .print(Object, " in ", Dir);
        move(Dir);
        .wait(1500);
        !navigate(Object).


@moved[atomic]
+moved
    <-  .print("I Moved.");
        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known

+!updateGUIPossible
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) & // get all possibilities from reasoner
        .setof(Dir, possible(closest(dispenser(red), Dir)), AllDir)
    <-  .print("Possible Locations: ", Possible); // Print to agent log
        .print("Possible Directions: ", AllDir); // Print to agent log
        internal.update_best_move(AllDir);
        internal.update_possible(Possible). // Update GUI positions


// Evaluation testing
//vals([percept(location(0,-1),none), percept(location(0,1), none), percept(location(1,0), none), percept(location(-1,0), none)]).
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
////    Maybe too complex, as this may change the ranges
//      ontic(location(X + 1, Y))
//          :- location(X, Y). //& lastMove(right).



//@moved[atomic]
//+moved
//    <-  .print("I Moved.");
//        internal.test_query(5, possible(take(X)));
//        internal.test_query(50, possible(take(X)));
//        internal.test_query(5, ~take(X));
//        internal.test_query(50, ~take(X));
//        internal.test_query(5, fake(X));
//        internal.test_query(50, fake(X));
//        !updateAdjacent; // Update the adjacent locations based on movement from our previous locations to further eliminate worlds
////        !updatePrevious;
//        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known
//
//
//+!updateAdjacent
//    :  not previousPossible(PrevList). // Have not moved (no prev locations)
//
//// Update the reasoner with knowledge of our adjacent positions
//+!updateAdjacent
//    :   previousPossible(PrevPos)
//    <-  .abolish(~location(_, _));
//        ?lastMove(MoveDir);
//        .print(PrevPos);
//        .print(CurrentPossible);
//        for(range(location(X,Y)) & not(.member(Prev, PrevPos) & isAdjacent(Prev, MoveDir, location(X, Y)))) { +~location(X, Y); }.
//
//+!updatePrevious
//    :   .setof(location(X, Y), possible(location(X, Y)), Possible) // get all possibilities from reasoner
//    <-  .abolish(previousPossible(_)); // Reset previous possibilities
//        +previousPossible(Possible).



//+!updateGUIPossible
//    :   .setof(location(X, Y), possible(location(X, Y)), Possible) & // get all possibilities from reasoner
//        .setof(Dir, possible(closest(dispenser(red), Dir)), AllDir)
//    <-  .print("Possible Locations: ", Possible); // Print to agent log
//        .print("Possible Directions: ", AllDir); // Print to agent log
//        internal.update_best_move(AllDir);
//        internal.update_possible(Possible). // Update GUI positions
//

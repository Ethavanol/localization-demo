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

//possible(location(X - 1, Y))
//    :- location(X, Y) & (true | lastMove(left)).


+model(moved)
    :   ~location(4, _)
    <-  post(NewPer, location(X, Y) & locPercept(location(X + 1, Y), NewPer));
        effects(NewPer, location(X, Y) & locPercept(location(X + 1, Y), NewPer)).



+moved
    :   location(4, _)
    <-  post(NewPer, location(X, Y) & locPercept(location(X + 1, Y), NewPer));
        effects(NewPer, location(X, Y) & locPercept(location(X + 1, Y), NewPer)).




event(moved(right)) :- agent_moves_right.

// For event +moved(right)
pre(moved(right)) :- ~location(4, _) & true. // If not at edge
post(moved(right), NewPer, false) :- location(X, Y) & locPercept(location(X + 1, Y), NewPer).



event(moved_right(failed)) :- ~agent_moves_right.
pre(moved_right(failed)) :- location(4, _).





// For event +move_failed(right)
pre(move_failed(right)) :- location(4, _). // Only fails if at edge
// No post condition


~locationa(4,4).

event(move(right)) :- true. // | moved(right).
//pre(move(right)) :- (~locationa(4, _)) | ((X == 4) & (test | X >= 4)) . // If not at edge
//pre(move(right)) :- ~((~locationa(4, _)) & ~((X /= 4) & (test | X >= 4))) . // If not at edge
//pre(move(right)) :- ~((~locationa(4, _)) & (.member(X, [4]) & (test | X >= 4))) . // If not at edge
pre(move(right)) :- ~locationa(4, _) & (.member(X, [4]) & (test | X >= 4)) . // If not at edge
pre(move(right)) :- ~locationa(4, _) & (.member(X, [4]) & (test | X >= 4)) . // If not at edge


// old
//pre(move(right)) :- ~locationa(4, _) & not (false) | (.member(X, [4]) & (test | X >= 4)) . // If not at edge
post(move(right), NewPer, false) :- location(X, Y) & locPercept(location(X + 1, Y), NewPer).

post(move(right), l1) :- l_1.
post(move(right), l1) :- l_2.

// Alternatively:
// performed(move(right)) :- moved(right).
// condition(move(right)) :- moved(right) & ~percept(east, obstacle) & ~location(4, _).
// effects(move(right), [location(X, 23), ~location(X, Y)]) :- location(X, Y) & locPercept(location(X, Y), Perceptions).


// OR:
//event(move(right)) :- moved(right).
//move(right) :- moved(right) & ~percept(east, obstacle) & ~location(4, _).
//effects(move(right), [location(X, 23), ~location(X, Y)]) :- location(X, Y) & locPercept(location(X, Y), Perceptions).


// Best integration because of 'event' term overload: Event +E occurs in Jason
// In events set: +moved(right)
// pre(moved(right)) :- ~percept(east, obstacle) & ~location(4, _).
// effects(moved(right), [location(X, 23), ~location(X, Y)])
//      :- location(X, Y) & locPercept(location(X, Y), Perceptions)


//+moved(east)
//    : ~percept(east, obstacle) & ~location(4, _)
//    <- effects([location(X, 23), ~location(X, Y)], location(X, Y) & locPercept(location(X, Y), Perceptions)).


/*
Event syntax:

event(e) :- bb_cond. // As a modeller, determine whether event e is applicable to current update. Use BB only for bb_cond

pre(e) :- cond.  // Whether event e is applicable to current model

post(e, {Add Props}, {Remove Props}) :- cond. // Add and remove explicit propositions
post(e, {Replace Props}) :- cond. // Replace all existing props with new propositions. Shortcut for: post(e, V(new_w), V(old_w)/V(new_w))


Example:
event(move(right)) :- moved(right).
pre(move(right)) :- ~block(right) & ~location(4, 0). // If not blocked and not at edge
post(move(right), worldProps(X+1, Y)) :- location(X, Y).

event(move(fail)) :- moved(fail).
pre(move(right)) :- block(right) | location(4, 0).



*/



// DO this:
//possible(location(X - 1, Y))
//    :- possible(location(X, Y)) & lastMove(left).
// location(1, 1) :- location(0, 1)
// location(1, 1) :- percept(up, block)


//post(click(X, Y)) :- ~click(X, Y).
// click(1, 1) :- ~click(1, 1)




// V(w) => click(1,1) Francois
// V(w) => click(1,1)
//post(click(1, 1)) :- ~click(1, 1) & clicked(1, 1);

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

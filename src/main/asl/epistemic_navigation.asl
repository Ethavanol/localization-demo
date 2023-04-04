{ include("epistemic_localization.asl") }

/*********************/
/*  Model Generation */
/*********************/

// Same as single
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~loc(X1, Y1) :- range(loc(X1, Y1)) & loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).



range(closest(dispenser(Type), Dir)) :- locDirToDispenser(_, Type, Dirs) & .member(Dir, Dirs).
range(closest(goal, Dir)) :- locDirToGoal(_, Dirs) & .member(Dir, Dirs).

closest(dispenser(Type), Dir) :- location(X, Y) & locDirToDispenser(location(X, Y), Type, Dirs) & .member(Dir, Dirs).
closest(goal, Dir) :- location(X, Y) & locDirToGoal(location(X,Y), Dirs) & .member(Dir, Dirs).

/************************/
/* END Model Generation */
/************************/


!completeTask.

+!completeTask
    <-  !navigate(dispenser(red));
        pickUp;
        !navigate(goal);
        submit.

+!navigate(Object)
    :   possible(closest(Object, Dir)) & Dir \== none
    <-  .print(Object, " in ", Dir);
        move(Dir);
        .wait(1500);
        !navigate(Object).

+!navigate(Object)
    :   closest(Object, none)
    <-  .print("At ", Object).

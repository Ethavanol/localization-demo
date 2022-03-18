{ include("epistemic_localization.asl") }

/*********************/
/*  Model Generation */
/*********************/

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

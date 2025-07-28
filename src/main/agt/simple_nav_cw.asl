range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
range(locGoal(X,Y)) :- (.range(X,4,4) & .range(Y,0,0)).
range(locGoal(X,Y)) :- (.range(X,2,2) & .range(Y,3,4)).

range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

range(noneGoal) :- true.
noneGoal :- .findall(not(locGoal(X, Y)), range(locGoal(X, Y)), List) & .big_and(Y, List) & Y.
~noneGoal.

~loc(1,2).
~loc(2,2).

~loc(X1, Y1) :- loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).
~locGoal(X1, Y1) :- locGoal(X2, Y2) & (X1 \== X2 | Y1 \== Y2).

obs(right) :- loc(0,2).
obs(down) :- loc(1,1).
obs(down) :- loc(2,1).
obs(up) :- loc(1,3).
obs(up) :- loc(2,3).
obs(left) :- loc(3,2).

dir(right) :- right.
dir(down) :- down.
dir(up) :- up.
dir(left) :- left.

!nav.

// MOVE EVENTS
// CLOSED-WORLD VERSION

+on(moved(right)) : loc(X, Y) & X < 4
   <- -loc(X, Y);
      +loc(X + 1, Y).

+on(moved(left)) : loc(X, Y) & X > 0
   <- -loc(X, Y);
      +loc(X - 1, Y).

+on(moved(up)) : loc(X, Y) & Y > 0
   <- -loc(X, Y);
      +loc(X, Y - 1).

+on(moved(down)) : loc(X, Y) & Y < 4
   <- -loc(X, Y);
      +loc(X, Y + 1).

// If it's a closed-world, you want to know when you couldn't move

+on(move_failed(right)) : loc(4,Y).

+on(move_failed(left)) : loc(0,Y).

+on(move_failed(up)) : loc(X,0).

+on(move_failed(down)) : loc(X,4).

+on(~move_failed(D)) : not move_failed(D).

+on(obs(D)) : obs(D).
+on(~obs(D)) : not obs(D).

+on(~loc(X,Y)) : not loc(X,Y).
+on(~locGoal(X,Y)) : not locGoal(X,Y).

+on(~na(goal)) : not (loc(X,Y) & locGoal(X,Y)).


+!nav : at(goal)
    <- .print("At goal.").

+!nav : poss(dir(D) & locGoal(X2,Y2) & obj(X2,Y2))
    <- move(D);
       !checkIfGoal;
       !nav.

+!nav : dir(D)
    <- move(D);
       !checkIfGoal;
       !addTemporaryObjective;
       !nav.

+!nav : poss(dir(D))
    <- move(D);
       !checkIfGoal;
       !addTemporaryObjective;
       !nav.

+!addTemporaryObjective : loc(X,Y) & poss(locGoal(X2,Y2))
    <- +obj(X2,Y2).

+!addTemporaryObjective : true
    <- .print("not sure about where I am...").


+!checkIfGoal : not at(goal) & loc(X,Y) & poss(locGoal(X,Y))
    <-  -obj(X,Y);
        +~locGoal(X,Y).

+!checkIfGoal : not at(goal) & locGoal(X,Y) & poss(loc(X,Y))
    <-  +~loc(X,Y).

+!checkIfGoal : not at(goal)
    <-  +~na(goal);
        -~na(goal).


// here is an "AND" possible formula : it's gonna evaluate if there is a world that both contains loc(X,Y) and locGoal(X,Y)
+!checkIfGoal : poss(loc(X,Y) & locGoal(X,Y))
    <- .print("here check").

+!checkIfGoal : true
    <- .print("true").
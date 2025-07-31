range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
range(locGoal(X,Y)) :- (.range(X,4,4) & .range(Y,0,0)).
range(locGoal(X,Y)) :- (.range(X,2,2) & .range(Y,4,4)).
range(locGoal(X,Y)) :- (.range(X,1,1) & .range(Y,3,3)).

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


// MOVE EVENTS
// OPEN-WORLD VERSION

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

+on(moved(right)) : loc(X, Y) & X >= 4
   <- -loc(X, Y);
      +loc(0, Y).

+on(moved(left)) : loc(X, Y) & X <= 0
   <- -loc(X, Y);
      +loc(4, Y).

+on(moved(up)) : loc(X, Y) & Y <= 0
   <- -loc(X, Y);
      +loc(X, 4).

+on(moved(down)) : loc(X, Y) & Y >= 4
   <- -loc(X, Y);
      +loc(X, 0).

+on(obs(D)) : obs(D).
+on(~obs(D)) : not obs(D).

+on(~loc(X,Y)) : not loc(X,Y).
+on(~locGoal(X,Y)) : not locGoal(X,Y).

+on(~na(goal)) : not (loc(X,Y) & locGoal(X,Y)).


+!nav : at(goal)
    <- .print("At goal.").

//if the agent has a temporary objective he goes in the direction that leads to it
+!nav : poss(dir(D) & locGoal(X2,Y2) & obj(X2,Y2))
    <- move(D);
       !storeSameMove(D);
       !checkIfGoal;
       !nav.

//if the agent moved in the same direction 5 times he has to change of direction
+!nav : dir(D1) & samemove(D2,Nb) & Nb >= 5 & D1 \== D2
    <- move(D1);
       !storeSameMove(D1);
       !checkIfGoal;
       !tryAddTemporaryObjective;
       !nav.

+!nav : dir(D)
    <- move(D);
       !storeSameMove(D);
       !checkIfGoal;
       !tryAddTemporaryObjective;
       !nav.

+!nav : poss(dir(D1)) & samemove(D2,Nb) & Nb >= 5 & D1 \== D2
    <- move(D1);
       !storeSameMove(D1);
       !checkIfGoal;
       !tryAddTemporaryObjective;
       !nav.

+!nav : poss(dir(D))
    <- move(D);
       !storeSameMove(D);
       !checkIfGoal;
       !tryAddTemporaryObjective;
       !nav.


// Here we could directly do Loc(X,Y) & poss(locGoal(X2,Y2)) but this is not optimized as we will have to test each possible pair
+!tryAddTemporaryObjective : loc(X,Y)
    <- !addTemporaryObjective.

+!tryAddTemporaryObjective : true
    <- .print("not sure about where I am...").

+!addTemporaryObjective : poss(locGoal(X2,Y2))
    <- +obj(X2,Y2).

+!addTemporaryObjective : true
    <- .print("Huh ?! I know where I am but I have no goal possible !!!").

+!checkIfGoal : not at(goal) & loc(X,Y) & poss(locGoal(X,Y))
    <-  -obj(X,Y);
        +~locGoal(X,Y).

+!checkIfGoal : not at(goal) & locGoal(X,Y) & poss(loc(X,Y))
    <-  +~loc(X,Y).

+!checkIfGoal : not at(goal)
    <-  +~na(goal);
        -~na(goal).

// here we face another problem : in the case of the openworld, if the agent spawn on the top line,
// he will just keeps going right as he will keeps considering possible the fact that he is in 0,0.
// To fix this, we want to prevent him from doing 6th time the same move (as the grid is 5x5).

+!storeSameMove(X) : samemove(X,Nb)
    <- -samemove(X, Nb);
       +samemove(X, Nb+1).

+!storeSameMove(X)
    <- -samemove(_, Nb);
       +samemove(X, 1).

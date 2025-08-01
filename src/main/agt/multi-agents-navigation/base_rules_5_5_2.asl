range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
range(locGoal(1,4)) :- true.

range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

range(noneGoal) :- true.
noneGoal :- .findall(not(locGoal(X, Y)), range(locGoal(X, Y)), List) & .big_and(Y, List) & Y.
~noneGoal.

~loc(4,1).
~loc(2,2).
~loc(3,4).

~loc(X1, Y1) :- loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).


obs(right) :- loc(1,2) | loc(3,1) | loc(2,4).
obs(down) :- loc(2,1) | loc(4,0) | loc(3,3).
obs(up) :- loc(2,3) | loc(4,2) | loc(3,0).
obs(left) :- loc(3,2) | loc(0,1) | loc(4,4).

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

+on(~na(goal)) : not (loc(X,Y) & locGoal(X,Y)).


+!nav : at(goal)
    <- .print("At goal.").

//if the agent moved in the same direction 5 times he has to change of direction
+!nav : dir(D1) & samemove(D2,Nb) & Nb >= 5 & D1 \== D2
    <- move(D1);
       !storeSameMove(D1);
       !checkIfGoal;
       !nav.

+!nav : dir(D)
    <- move(D);
       !storeSameMove(D);
       !checkIfGoal;
       !nav.

+!nav : poss(dir(D1)) & samemove(D2,Nb) & Nb >= 5 & D1 \== D2
    <- move(D1);
       !storeSameMove(D1);
       !checkIfGoal;
       !nav.

+!nav : poss(dir(D))
    <- move(D);
       !storeSameMove(D);
       !checkIfGoal;
       !nav.

+!checkIfGoal : not at(goal)
    <-  +~na(goal);
        -~na(goal).

+!checkIfGoal
    <-  .print("I am arrived").

// here we face another problem : in the case of the openworld, if the agent spawn on the top line,
// he will just keeps going right as he will keeps considering possible the fact that he is in 0,0.
// To fix this, we want to prevent him from doing 6th time the same move (as the grid is 5x5).

+!storeSameMove(X) : samemove(X,Nb)
    <- -samemove(X, Nb);
       +samemove(X, Nb+1).

+!storeSameMove(X)
    <- -samemove(_, Nb);
       +samemove(X, 1).

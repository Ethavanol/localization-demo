range(loc(X,Y)) :- (.range(X,0,6) & .range(Y,0,6)).
range(locGoal(3,4)) :- true.

range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

range(noneGoal) :- true.
noneGoal :- .findall(not(locGoal(X, Y)), range(locGoal(X, Y)), List) & .big_and(Y, List) & Y.
~noneGoal.

~loc(3,2).
~loc(5,6).
~loc(1,5).

~loc(X1, Y1) :- loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).


obs(right) :- loc(2,2) | loc(4,6) | loc(0,5).
obs(left)  :- loc(4,2) | loc(6,6) | loc(2,5).
obs(down)  :- loc(3,1) | loc(5,5) | loc(1,4).
obs(up)    :- loc(3,3) | loc(5,0) | loc(1,6).

dir(right) :- right.
dir(down) :- down.
dir(up) :- up.
dir(left) :- left.

// MOVE EVENTS
// OPEN-WORLD VERSION

+on(moved(right)) : loc(X, Y) & X < 6
   <- -loc(X, Y);
      +loc(X + 1, Y).

+on(moved(left)) : loc(X, Y) & X > 0
   <- -loc(X, Y);
      +loc(X - 1, Y).

+on(moved(up)) : loc(X, Y) & Y > 0
   <- -loc(X, Y);
      +loc(X, Y - 1).

+on(moved(down)) : loc(X, Y) & Y < 6
   <- -loc(X, Y);
      +loc(X, Y + 1).

+on(moved(right)) : loc(X, Y) & X >= 6
   <- -loc(X, Y);
      +loc(0, Y).

+on(moved(left)) : loc(X, Y) & X <= 0
   <- -loc(X, Y);
      +loc(6, Y).

+on(moved(up)) : loc(X, Y) & Y <= 0
   <- -loc(X, Y);
      +loc(X, 6).

+on(moved(down)) : loc(X, Y) & Y >= 6
   <- -loc(X, Y);
      +loc(X, 0).

+on(obs(D)) : obs(D).
+on(~obs(D)) : not obs(D).

+on(~loc(X,Y)) : not loc(X,Y).

+on(~na(goal)) : not (loc(X,Y) & locGoal(X,Y)).

+!nav : at(goal)
    <- .print("At goal.").

+!nav : dir(D)
    <- move(D);
       !checkIfGoal;
       !nav.

+!nav : poss(dir(D))
    <- move(D);
       !checkIfGoal;
       !nav.

+!checkIfGoal : not at(goal)
    <-  +~na(goal);
        -~na(goal).

+!checkIfGoal
    <-  .print("I am arrived").

// communication between agents.




// here we face another problem : in the case of the openworld, if the agent spawn on the top line,
// he will just keeps going right as he will keeps considering possible the fact that he is in 0,0.
// To fix this, we want to prevent him from doing 6th time the same move (as the grid is 5x5).

/**
+!storeSameMove(X) : samemove(X,Nb)
    <- -samemove(X, Nb);
       +samemove(X, Nb+1).

+!storeSameMove(X)
    <- -samemove(_, Nb);
       +samemove(X, 1).
**/
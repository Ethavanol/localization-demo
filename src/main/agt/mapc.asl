range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

~loc(X1, Y1) :- loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).

~obs(D) :- not obs(D).

!nav.

// MOVE EVENTS

+on(moved(right)) : loc(X, Y) & X < 4
   <- -loc(X, Y);
      +loc(X + 1, Y).

+on(moved(right)) : loc(X, Y) & X >= 4
   <- -loc(X, Y);
      +loc(0, Y).

+on(moved(left)) : loc(X, Y) & X > 0
   <- -loc(X, Y);
      +loc(X - 1, Y).

+on(moved(left)) : loc(X, Y) & X <= 0
   <- -loc(X, Y);
      +loc(4, Y).

+on(moved(up)) : loc(X, Y) & Y > 0
   <- -loc(X, Y);
      +loc(X, Y - 1).

+on(moved(up)) : loc(X, Y) & Y <= 0
   <- -loc(X, Y);
      +loc(X, 4).

+on(moved(down)) : loc(X, Y) & Y < 4
   <- -loc(X, Y);
      +loc(X, Y + 1).

+on(moved(down)) : loc(X, Y) & Y >= 4
   <- -loc(X, Y);
      +loc(X, 0).

+on(~moved(D)) : ~moved(D).

+on(obs(D)) : obs(D).
+on(~obs(D)) : ~obs(D).

+!nav : at(goal)
    <- .print("At goal.").

+!nav : dir(D, goal)
    <- move(D);
       !nav.

+!nav : poss(dir(D, goal))
    <- move(D);
       !nav.
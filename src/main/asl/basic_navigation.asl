{ include("beliefsloc.asl") }
//agent(3).
!nav.

// MOVE EVENTS

// Remove all >= events if you want a closed-world
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

// MOVE FAILED EVENTS

//+on(move_failed(right)) : loc(4, Y).

//+on(move_failed(up)) : loc(X, 0).

//+on(move_failed(down)) : loc(X, 4).

//+on(move_failed(left)) : loc(0, Y).

//+on(~move_failed(D)) : ~move_failed(D).

+on(obs(D)) : obs(D).
+on(~obs(D)) : ~obs(D).

+!nav : at(goal)
    <- .print("At goal.").

+!nav : dir(D, goal) | poss(dir(D, goal))
    <- move(D);
       !nav.

range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
~loc(X1, Y1) :- range(loc(X1, Y1)) & loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).
~loc(1,2).
~loc(2,2).

/* Remove case of all locations being false */
range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

// Obstacle mappings
obs(left) :- loc(0,2).
obs(down) :- loc(1,1).
obs(up) :- loc(1,3).
obs(down) :- loc(2,1).
obs(up) :- loc(2,3).
obs(right) :- loc(3,2).

// No obstacles otherwise (closed-world)
~obs(D) :- not(obs(D)).

// Direction mappings
dir(down) :- loc(0,0).
dir(down) :- loc(0,1).
dir(down) :- loc(0,2).
dir(right) :- loc(0,3).
dir(right) :- loc(0,4).
dir(up) :- loc(0,4).
dir(left) :- loc(1,0).
dir(right) :- loc(1,0).
dir(down) :- loc(1,0).
dir(left) :- loc(1,1).
dir(right) :- loc(1,1).
dir(right) :- loc(1,3).
dir(right) :- loc(1,4).
dir(up) :- loc(1,4).
dir(right) :- loc(2,0).
dir(down) :- loc(2,0).
dir(right) :- loc(2,1).
dir(up) :- loc(2,4).
dir(down) :- loc(3,0).
dir(down) :- loc(3,1).
dir(down) :- loc(3,2).
dir(left) :- loc(3,3).
dir(left) :- loc(3,4).
dir(up) :- loc(3,4).
dir(left) :- loc(4,0).
dir(down) :- loc(4,0).
dir(left) :- loc(4,1).
dir(down) :- loc(4,1).
dir(left) :- loc(4,2).
dir(down) :- loc(4,2).
dir(left) :- loc(4,3).
dir(left) :- loc(4,4).
dir(up) :- loc(4,4).

// No directions otherwise (closed-world)
~dir(D) :- not(dir(D)).


// "On" plans for obstacles
+on(obs(Dir)) : obs(Dir).
+on(~obs(Dir)) : ~obs(Dir). 


// "On" plans for movement
+on(moved(right))
    : loc(X, Y) & X < 4
    <-  -loc(X, Y);
        +loc(X + 1, Y).

+on(moved(left))
    : loc(X, Y) & X > 0
    <-  -loc(X, Y);
        +loc(X - 1, Y).
        
+on(moved(up))
    : loc(X, Y) & Y > 0
    <-  -loc(X, Y);
        +loc(X, Y - 1).
        
+on(moved(down))
    : loc(X, Y) & Y < 4
    <-  -loc(X, Y);
        +loc(X, Y + 1).
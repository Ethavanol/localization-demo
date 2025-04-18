/** These are the beliefs generated for the map that are added automatically to the BB **/
/** This file is not loaded by the agent. It is just the output for debugging purposes and will be overwritten. **/
range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
//range(loc(none)).

//loc(none) :- ~loc(0,0) & ~loc(0,1) & ~loc(0,2) & ~loc(0,3) & ~loc(0,4) & ~loc(1,0) & ~loc(1,1) & ~loc(1,2) & ~loc(1,3) & ~loc(1,4) & ~loc(2,0) & ~loc(2,1) & ~loc(2,2) & ~loc(2,3) & ~loc(2,4) & ~loc(3,0) & ~loc(3,1) & ~loc(3,2) & ~loc(3,3) & ~loc(3,4) & ~loc(4,0) & ~loc(4,1) & ~loc(4,2) & ~loc(4,3) & ~loc(4,4).
//~loc(none).

range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

~loc(X1, Y1) :- loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).

~loc(1,2).
~loc(2,2).
~loc(3,2).

// Obstacle mappings
obs(left) :- loc(4,2).
obs(down) :- loc(1,1).
obs(up) :- loc(1,3).
obs(down) :- loc(2,1).
obs(up) :- loc(2,3).
obs(down) :- loc(3,1).
obs(up) :- loc(3,3).
obs(right) :- loc(0,2).

~obs(D) :- not obs(D).

// Direction mappings
dir(down,goal) :- loc(0,0).
dir(down,goal) :- loc(0,1).
dir(down,goal) :- loc(0,2).
dir(right,goal) :- loc(0,3).
dir(up,goal) :- loc(0,4).
dir(right,goal) :- loc(0,4).

dir(down,goal) :- loc(1,0).
dir(right,goal) :- loc(1,0).
dir(left,goal) :- loc(1,0).
dir(left,goal) :- loc(1,1).
dir(right,goal) :- loc(1,1).
dir(right,goal) :- loc(1,3).
dir(up,goal) :- loc(1,4).
dir(right,goal) :- loc(1,4).

dir(down,goal) :- loc(2,0).
dir(right,goal) :- loc(2,0).
dir(right,goal) :- loc(2,1).
dir(right,goal) :- loc(2,3).
dir(up,goal) :- loc(2,4).
dir(right,goal) :- loc(2,4).

dir(down,goal) :- loc(3,0).
dir(right,goal) :- loc(3,0).
dir(right,goal) :- loc(3,1).
dir(up,goal) :- loc(3,4).

dir(down,goal) :- loc(4,0).
dir(down,goal) :- loc(4,1).
dir(down,goal) :- loc(4,2).
dir(left,goal) :- loc(4,3).
dir(left,goal) :- loc(4,4).

/** These are the beliefs generated for the map that are added automatically to the BB **/
/** This file is not loaded by the agent. It is just the output for debugging purposes and will be overwritten. **/
range(loc(X,Y)) :- (.range(X,0,3) & .range(Y,0,2)).

~loc(1,1).
~loc(2,1).

// Obstacle mappings
obs(left) :- loc(0,1).
obs(down) :- loc(1,0).
obs(up) :- loc(1,2).
obs(down) :- loc(2,0).
obs(up) :- loc(2,2).
obs(right) :- loc(3,1).
~obs(D) :- not (obs(D)).

// Direction mappingsdir(right,goal) :- loc(0,0).
dir(down,goal) :- loc(0,0).
dir(down,goal) :- loc(0,1).
dir(right,goal) :- loc(0,2).
dir(right,goal) :- loc(1,0).
dir(right,goal) :- loc(2,0).
dir(left,goal) :- loc(2,2).
dir(up,goal) :- loc(3,1).
dir(left,goal) :- loc(3,2).
dir(up,goal) :- loc(3,2).
dir(down,disp) :- loc(0,0).
dir(down,disp) :- loc(0,1).
dir(right,disp) :- loc(0,2).
dir(left,disp) :- loc(1,0).
dir(right,disp) :- loc(1,0).
dir(right,disp) :- loc(2,0).
dir(left,disp) :- loc(2,2).
dir(right,disp) :- loc(2,2).
dir(down,disp) :- loc(3,0).
dir(down,disp) :- loc(3,1).

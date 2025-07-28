!start.

test(a).
test(b).
obs(right).
obs(down).
dir(right,goal) :- obs(right).
dir(left,goal) :- obs(down).
test(a,b,c,d).

//e :- dir(left,goal) & test(a,b,c,d).

+!start <-
    !goal.

+!goal : poss(left) <-
    .print("poss(a) option").

+!goal : obs(right) & X <-
    .print("poss(a) option").

+!goal : dir(D,goal) & test(a,b,c,d) <-
    .print("poss(a) option").

+!goal : test(a,X,c,Y)  <-
    .print("poss(a) option").

+!goal : test(a) | (dir(Y,goal) & test(a,b,c,d)) <-
    .print("a option").

+!goal : test(a) | (dir(D,goal) & test(A0,X,c,d)) <-
    .print("a option").

+!goal : test(X) & X \== b <-
    .print("X option").

+!goal : test(X) <-
    .print("X option").

+!goal : test(a)<-
    .print("X option").

+!goal : dir(D,goal) & D \== right <-
    .print("X option").


+!goal : test(a,X,Z,Y) <-
    .print("poss(a) option").

//+!goal : test(a,b,c,d) <-
//    .print("poss(a) option").



+!goal : dir(D,goal) <-
    .print("a option").


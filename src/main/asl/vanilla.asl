
range(loc(X,Y)) :- (.range(X,0,4) & .range(Y,0,4)).
~loc(X1, Y1) :- range(loc(X1, Y1)) & loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).
~loc(1,2).
~loc(2,2).

range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

!test.

// Obstacle mappings
obs(left) :- loc(0,2).
obs(down) :- loc(1,1).
obs(up) :- loc(1,3).
obs(down) :- loc(2,1).
obs(up) :- loc(2,3).
obs(right) :- loc(3,2).

// No obstacles otherwise (closed-world)
~obs(D) :- not (obs(D)).


// "On" plans for obstacles
+on(obs(Dir)) : obs(Dir).
+on(~obs(Dir)) : ~obs(Dir).

+!test
    : asd | ~asd.

//+!test  <- for(bel(X)) {
//    if( X > 5 ) {
//        .print(X);
//    };
//    +test;
//};
//.count(test, 1);
//.print("hi");
//-test.


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


//!test_query.
//!test_update.


//+!test_update
//    <-
//        internal.test_updates(test(1)).
//        internal.test_updates(test(50)).
//        internal.test_updates(test(100)).

//+!test_query
//    <-
//        internal.test_queries(test(1)).
//        internal.test_queries(test(50)).
//        internal.test_queries.


//+on(test(1))
//    : true.

//+on(test(Max))
//    : true & .range(X, 1, Max)
//    <- +test(X).

//+on(test(2))
//    : true.

//+on(test(10))
//    : loc(0,X) & .range(X, 1, 10)
//    <- +test(5).

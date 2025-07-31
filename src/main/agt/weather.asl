//we dont know if its raining.
//two possible world it's raining (raining = 1) / it's not raining (raining = 0)
//If we know its raining we want to put a jacket / take a umbrella
//Otherwise we just wanna go outside in tee
//many different ways to determine the possible worlds exist.
range(raining(X)) :- .range(X, 0, 1).

// The lines under allows to eliminate the worlds where no state is true (none)
range(none) :- true.
none :- .findall(not(raining(X)), range(raining(X)), List) & .big_and(X, List) & X.
~none.

// The line under declares mutual exclusivity : worlds with both raining(1) & raining(0) are not possible
~raining(X1) :- raining(X2) & (X1 \== X2).

/**

2)
range(raining) :- true.

3)
range(raining) :- true.
range(notRaining) :- true.

range(none) :- true.
none :- not raining & not notRaining.
~none.

~raining :- notRaining.
~notRaining :- raining.
**/

home.
!goOut.

+!preparateForGoingOut: poss(raining(1))
    <- !preparateForRain.

+!preparateForGoingOut
    <- !putATee.

+!goBackHome: not home
    <- .print("going back home");
       +home.

+!putATee: home
    <- .print("putting a tee shirt").

+!preparateForRain: home
    <- .print("putting a jacket");
       .print("taking my umbrella").

+!goOut: home
    <- !preparateForGoingOut;
       !leaveTheHouse;
       .wait(2000);
       !discoverItIsSunny.

+!leaveTheHouse : home
    <- .print("going out");
       -home. // -home; for the PAL +~home; for the DEL

+!discoverItIsSunny: not home
    <- .print("Oh it is not raining..");
       +~raining(1);
       !goBackHome;
       !preparateForGoingOut;
       !leaveTheHouse.




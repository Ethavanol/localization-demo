//we dont know if its raining.
//two possible world it's raining (raining = 1) / it's not raining (raining = 0)
//If we know its raining we want to put a jacket / take a umbrella
//Otherwise we just wanna go outside in tee

range(raining(X)) :- .range(X, 0, 1).
// The line under declares mutual exclusivity : worlds with both raining(1) & raining(0) are not possible
~raining(X1) :- raining(X2) & (X1 \== X2).
// Theoretically, the lines under should be useful to eliminate the worlds where no state is true (none)
// But it is not working to the moment, due to the mutual exclusivity rule (creates an infinity loop)
//range(raining(none)) :- true.
//The line under create an infinity loop for the moment.
//raining(none) :- ~raining(0) & ~raining(1).
//~raining(none).

home.
!goOut.

// If you write another plan with a guard raining(1) that is true,
// It will be chosen over the one with the guard poss(raining(1)), no matter the order of these plans in the code
// Guard priority : X >> poss(X) >> None

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

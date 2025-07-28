range(muda(X)) :- .range(X, 0, 1).
range(mudb(X)) :- .range(X, 0, 1).

range(nonemudA) :- true.
nonemudA :- .findall(not(muda(X)), range(muda(X)), List) & .big_and(Y, List) & Y.
~nonemudA.

range(nonemudB) :- true.
nonemudB :- .findall(not(mudb(X)), range(mudb(X)), List) & .big_and(Y, List) & Y.
~nonemudB.

~muda(X1) :- muda(X2) & (X1 \== X2).
~mudb(X1) :- mudb(X2) & (X1 \== X2).

+on(muddyb) : mudb(1).
+on(~muddyb) : not mudb(1).

!open_eyes.

+!open_eyes
    <- +~muddyb.
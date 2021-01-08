// We use 'range' to describe all values of Alice's hand.
range cards("Alice", Hand) :- .member(Hand, ["AA", "A8", "88"])
range cards("Bob", Hand) :- .member(Hand, ["AA", "A8", "88"])
range cards("Charlie", Hand) :- .member(Hand, ["AA", "A8", "88"])

cards("Alice", "")


// We use 'possible' to describe all values of Alice's Hand that are not impossible
possible cards("Alice", Hand)
    :-  range cards("Alice", Hand) &
        & not possible ~cards("Alice", Hand).

possible ~cards("Alice", Hand)
    :-  range cards("Alice", Hand) &
        & not possible cards("Alice", Hand).

// Issue 1: Infinite Recursion. First definition calls second definition which then calls the first, etc...
// Issue 2: Agent can not specify beliefs


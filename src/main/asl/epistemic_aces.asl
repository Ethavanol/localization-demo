range(cards("Bob", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
range(cards("Charlie", Hand)) :- .member(Hand, ["AA", "A8", "88"]).

// We use possible to denote that Alice's cards depends on B/C's cards and we will write rules about them
// We know it's dependent because it has multiple rules?
range(cards("Alice", Hand)) :- .member(Hand, ["AA", "A8", "88"]).


cards("Alice", "AA") :- cards("Bob", "88") & cards("Charlie", "88").
cards("Alice", "88") :- cards("Bob", "AA") & cards("Charlie", "AA").

~cards("Alice", "88") :- (cards("Bob", "A8") & cards("Charlie", "88")).
~cards("Alice", "88") :- (cards("Bob", "88") & cards("Charlie", "A8")).

~cards("Alice", "AA") :- (cards("Bob", "A8") & cards("Charlie", "AA"))
                            | (cards("Bob", "AA") & cards("Charlie", "A8")).


!determineCards.

+!perceive
    <-
        +cards("Bob", "AA");
        +cards("Charlie", "AA").
//        -possible(cards("Bob", "88"));
//        +possible(cards("Bob", "88"));
//        // All still possible (empty props)
//
//        +~possible(cards("Bob", "A8")).
//        +possible(cards("Bob", "A8"));
//        +~possible(cards("Bob", "88"));
//        +~possible(cards("Bob", "88"));
//        // Remove A8 for Bob
//
//        +~possible(cards("Bob", "AA"));
//        // Remove A8 for Bob
//
//        -possible(cards("Bob", "A8"));
//        +~possible(cards("Bob", "A8"));
//        +cards("Charlie", "AA");
//        -cards("Charlie", "AA").

+!determineCards
    <-  !perceive;
        !printPossible;
        !printKnowledge.

+!printPossible
    : .setof(Card, possible(cards("Alice", Card)), Cards)
    <-  .print("Possible Cards are: ", Cards).

+!printKnowledge
    : cards("Alice", Cards)
    <-  .print("Cards are: ", Cards).

+!printKnowledge
    : not cards("Alice", Cards)
    <-  .print("Unknown Cards").
test("wow", "wow")[necessary] :- true.

// This rule specifies the range of Bob's possible cards
cards("Bob", Hand)[possibly] :- .member(Hand, ["AA", "A8", "88"]).

// This rule specifies the range of Charlie's possible cards
cards("Charlie", Hand)[possibly] :- .member(Hand, ["AA", "A8", "88"]).

// We provide multiple rules for Alice's cards, which can depend based on Bob's and Charlie's cards
// Alice has two aces if Bob and Charlie both have two eights
cards("Alice", "AA")[necessary] :-
    cards("Bob", "88") & cards("Charlie", "88").

// Alice has two Eights if Bob and Charlie both have two Aces
cards("Alice", "88")[necessary] :-
    cards("Bob", "AA") & cards("Charlie", "AA").

cards("Alice", HandOne)[possibly]
    :-  ((cards("Bob", "A8") & cards("Charlie", "88")) |
        (cards("Bob", "88") & cards("Charlie", "A8"))) &
        .member(HandOne, ["AA", "A8"]).

cards("Alice", HandTwo)[possibly]
    :-  ((cards("Bob", "A8") & cards("Charlie", "AA")) |
        (cards("Bob", "AA") & cards("Charlie", "A8"))) &
        .member(HandTwo, ["88", "A8"]).

// If none of the above rules match for Alice, she can have any pair of cards
cards("Alice", Hand)[possibly] :-
    not cards("Alice", _) &
    .member(Hand, ["AA", "A8", "88"]).

!determineCards.

+!perceive
    <-  -possible(cards("Bob", "88"));
        -possible(cards("Bob", "AA"));
        +cards("Charlie", "AA").

+!determineCards
    : not tester("Alice", _)
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
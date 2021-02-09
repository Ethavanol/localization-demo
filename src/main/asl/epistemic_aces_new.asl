//test("wow", "wow")[necessary] :- true.
//
//// This rule specifies the range of Bob's possible cards
//cards("Bob", Hand)[possibly] :- .member(Hand, ["AA", "A8", "88"]).
//
//// This rule specifies the range of Charlie's possible cards
//cards("Charlie", Hand)[possibly] :- .member(Hand, ["AA", "A8", "88"]).
//
//// We provide multiple rules for Alice's cards, which can depend based on Bob's and Charlie's cards
//// Alice has two aces if Bob and Charlie both have two eights
//cards("Alice", "AA")[necessary] :-
//    cards("Bob", "88") & cards("Charlie", "88").
//
//// Alice has two Eights if Bob and Charlie both have two Aces
//cards("Alice", "88")[necessary] :-
//    cards("Bob", "AA") & cards("Charlie", "AA").
//
//cards("Alice", HandOne)[possibly]
//    :-  ((cards("Bob", "A8") & cards("Charlie", "88")) |
//        (cards("Bob", "88") & cards("Charlie", "A8"))) &
//        .member(HandOne, ["AA", "A8"]).
//
//cards("Alice", HandTwo)[possibly]
//    :-  ((cards("Bob", "A8") & cards("Charlie", "AA")) |
//        (cards("Bob", "AA") & cards("Charlie", "A8"))) &
//        .member(HandTwo, ["88", "A8"]).
//
//// If none of the above rules match for Alice, she can have any pair of cards
//cards("Alice", Hand)[possibly] :-
//    not cards("Alice", _) &
//    .member(Hand, ["AA", "A8", "88"]).
//
//
//
//possible(cards("Alice", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//possible(cards("Bob", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//possible(cards("Charlie", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//
//// Knowledge rules depending on other knowledge (i.e. 'necessary')
//cards("Alice", "AA") :- cards("Bob", "88") & cards("Charlie", "88").
//cards("Alice", "88") :- cards("Bob", "AA") & cards("Charlie", "AA").
//
//∼cards("Alice", "88") :- (cards("Bob", "A8") & cards("Charlie", "88"))
//                            | (cards("Bob", "88") & cards("Charlie", "A8")).
//
//∼cards("Alice", "AA") :- (cards("Bob", "A8") & cards("Charlie", "AA"))
//                            | (cards("Bob", "AA") & cards("Charlie", "A8")).
//
//
//
//
////
//
//

// B/C are always known
// B/C are independent (i.e. no restrictions) so we can safely create our base world set (i.e. B x C)
range(cards("Bob", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
range(cards("Charlie", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Bob2", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Charlie2", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Bob3", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Charlie3", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Bob4", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Charlie4", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Bob5", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Charlie5", Hand)) :- .member(Hand, ["AA", "A8", "88"]).


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
//        +cards("Bob", "AA");
//        +cards("Charlie", "AA").
        -possible(cards("Bob", "88"));
//        +possible(cards("Bob", "88"));
//        // All still possible (empty props)
//
        +~possible(cards("Bob", "A8")).
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
    : not tester("Alice", _)
    <-  !perceive;
        ?cards("Alice", C);
      -cards("Bob", "AA");
      +cards("Bob", "88");
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
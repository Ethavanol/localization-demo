//range(cards("Bob", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Charlie", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//
//// We use possible to denote that Alice's cards depends on B/C's cards and we will write rules about them
//// We know it's dependent because it has multiple rules?
//range(cards("Alice", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//
//
//cards("Alice", "AA") :- cards("Bob", "88") & cards("Charlie", "88").
//cards("Alice", "88") :- cards("Bob", "AA") & cards("Charlie", "AA").
//
//~cards("Alice", "88") :- (cards("Bob", "A8") & cards("Charlie", "88")).
//~cards("Alice", "88") :- (cards("Bob", "88") & cards("Charlie", "A8")).
//
//~cards("Alice", "AA") :- (cards("Bob", "A8") & cards("Charlie", "AA"))
//                            | (cards("Bob", "AA") & cards("Charlie", "A8")).
//
//
//!determineCards.
//
//+!perceive
//    <-
//        +cards("Bob", "AA");
//        +cards("Charlie", "AA").
////        -possible(cards("Bob", "88"));
////        +possible(cards("Bob", "88"));
////        // All still possible (empty props)
////
////        +~possible(cards("Bob", "A8")).
////        +possible(cards("Bob", "A8"));
////        +~possible(cards("Bob", "88"));
////        +~possible(cards("Bob", "88"));
////        // Remove A8 for Bob
////
////        +~possible(cards("Bob", "AA"));
////        // Remove A8 for Bob
////
////        -possible(cards("Bob", "A8"));
////        +~possible(cards("Bob", "A8"));
////        +cards("Charlie", "AA");
////        -cards("Charlie", "AA").
//
//+!determineCards
//    <-  !perceive;
//        !printPossible;
//        !printKnowledge.
//
//+!printPossible
//    : .setof(Card, possible(cards("Alice", Card)), Cards)
//    <-  .print("Possible Cards are: ", Cards).
//
//+!printKnowledge
//    : cards("Alice", Cards)
//    <-  .print("Cards are: ", Cards).
//
//+!printKnowledge
//    : not cards("Alice", Cards)
//    <-  .print("Unknown Cards").


// Specify the range of possible Hands for Alice, Bob, and Charlie
range(cards("Alice", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
range(cards("Bob", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
range(cards("Charlie", Hand)) :- .member(Hand, ["AA", "A8", "88"]).

// Valuation Rules that specify rules of the domain
cards("Alice", "AA") :- cards("Bob", "88") & cards("Charlie", "88").
cards("Alice", "88") :- cards("Bob", "AA") & cards("Charlie", "AA").
~cards("Alice", "88") :- (cards("Bob", "A8") & cards("Charlie", "88")) | (cards("Bob", "88") & cards("Charlie", "A8")).
~cards("Alice", "AA") :- (cards("Bob", "A8") & cards("Charlie", "AA")) | (cards("Bob", "AA") & cards("Charlie", "A8")).

!determineCards.

+!determineCards
    <-  .print("Closed Eyes Knowledge:");
        !reasonAboutKnowledge;
        .print("------");
        .print("Open Eyes Knowledge:");
        !openEyes;
        !reasonAboutKnowledge.

// Print knowledge (we are certain about our hand)
+!reasonAboutKnowledge
    <-  !printKnowledge("Alice");
        !printKnowledge("Bob");
        !printKnowledge("Charlie").

// If we don't know Player's cards, print all possibilities
+!printKnowledge(Player)
    :   not cards(Player, _)
    <-  .setof(Hand, possible(cards(Player, Hand)), PossibleHands) // Get all current possibilities for Player
        .print("Alice doesn't know ", Player, "'s hand, but the possibilities are ", PossibleHands).

+!printKnowledge(Player)
    :   cards(Player, Hand)
    <-  .print("Alice knows ", Player, "'s hand: ", Hand).

// Open our eyes and update our knowledge based on Bob's/Charlie's cards
// This plan adds knowledge of Bob's and Charlie's cards to our belief base
// This is typically done via perceptions, but we hard-code this knowledge for demonstration
+!openEyes
    <-  +cards("Bob", "AA");
        +cards("Charlie", "AA").
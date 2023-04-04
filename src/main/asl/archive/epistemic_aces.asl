// Add beliefs for all possible hands for Alice
possible(cards("Alice", "AA")).
possible(cards("Alice", "A8")).
possible(cards("Alice", "88")).

// Add beliefs for all possible hands for Bob
possible(cards("Bob", "AA")).
possible(cards("Bob", "A8")).
possible(cards("Bob", "88")).

// Add beliefs for all possible hands for Charlie
possible(cards("Charlie", "AA")).
possible(cards("Charlie", "A8")).
possible(cards("Charlie", "88")).

// Get all possible hands for Player using the current 'possible(card(...))' beliefs
all_possible(Player, HandList) :- .setof(Hand, possible(cards(Player, Hand)), HandList).

// We know that a Player has Hand (placeholder for actual value) if we consider only one hand possible for that Player
cards(Player, Hand) :-
    possible(cards(Player, Hand)) &
    not (possible(cards(Player, OtherHand)) & Hand \== OtherHand).

!determineCards.

// Set up our belief base with all of our possibilities
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
    <-  ?all_possible(Player, PossibleHands); // Get all current possibilities for Player
        .print("Alice doesn't know ", Player, "'s hand, but the possibilities are ", PossibleHands).

+!printKnowledge(Player)
    :   cards(Player, Hand)
    <-  .print("Alice knows ", Player, "'s hand: ", Hand).


// Open our eyes and update our knowledge based on Bob's/Charlie's cards
// This plan adds knowledge of Bob's and Charlie's cards to our belief base
// This is typically done via perceptions, but we hard-code this knowledge for demonstration
+!openEyes
    <-  +cards("Bob", "A8");
        -possible(cards("Bob", "AA")); // No longer possible
        -possible(cards("Bob", "88")); // No longer possible
        +cards("Charlie", "AA");
        -possible(cards("Charlie", "A8")); // No longer possible
        -possible(cards("Charlie", "88")); // No longer possible
        !updateKnowledgeFromOpenEyes. // Update knowledge of Alice's cards


// Remove any Possible Hands with Aces if Bob and Charlie have all 4 Aces
+!updateKnowledgeFromOpenEyes
    :   cards("Bob", "AA") & cards("Charlie", "AA")
    <-  +cards("Alice", "88");
        -possible(cards("Alice", "AA"));
        -possible(cards("Alice", "A8")).

// Remove any Possible Hands with Eights if Bob and Charlie have all 4 Eights
+!updateKnowledgeFromOpenEyes
    :   cards("Bob", "88") & cards("Charlie", "88")
    <-  +cards("Alice", "AA");
        -possible(cards("Alice", "88"));
        -possible(cards("Alice", "A8")).


// Remove AA if Bob and Charlie have 3 Aces
+!updateKnowledgeFromOpenEyes
    :   (cards("Bob", "AA") & cards("Charlie", "A8")) | (cards("Bob", "A8") & cards("Charlie", "AA"))
    <-  -possible(cards("Alice", "AA")).

// Remove 88 if Bob and Charlie have 3 Eights
+!updateKnowledgeFromOpenEyes
    :   (cards("Bob", "88") & cards("Charlie", "A8")) | (cards("Bob", "A8") & cards("Charlie", "88"))
    <-  -possible(cards("Alice", "88")).


// Do not eliminate any possibilities if Bob and Charlie both have A8
+!updateKnowledgeFromOpenEyes.
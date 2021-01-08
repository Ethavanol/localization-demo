/*********************/
/* Better Generation */
/*********************/
//location(X, Y)[possibly]
//  :-  locPercept(location(X, Y), _).
//
//percept(Direction, Object)[necessary] :- location(X, Y) & locPercept(location(X, Y), Perceptions) & .member(percept(Direction, Object), Perceptions).
//adjacent(MoveDir, Prev)[necessary] :- location(X, Y) & locAdjacent(location(X, Y), Adj) & .member(adjacent(MoveDir, Prev), Adj).
//direction(Dir)[necessary] :- location(X, Y) & locDirToGoal(location(X,Y), Dirs) & .member(Dir, Dirs).
//goalRelative(Gx, Gy)[necessary] :- location(X, Y) & locGoalRel(location(X,Y), location(Gx, Gy)).


/************************/
/* END Model Generation */
/************************/

// We know Alice has Hand (placeholder for actual value) if there is only one possible hand
cards("Alice", Hand) :-
    .setof(AliceHand, possible(cards("Alice", AliceHand)), PossibleHands) &
    .length(PossibleHands, 1) &
    PossibleHands = [Hand|_].

!initialize.

// Set up our belief base with all of our possibilities
+!initialize
    <-  .print("Stage 1: Closed Eyes");
        !addAllPossible;
        .print("------");
        .print("Stage 2: Open Eyes");
        !openEyes;
        .print("------");
        .print("Stage 3: Peeking");
        !peekCard.

// Add all possible cards
+!addAllPossible
    <-  +possible(cards("Alice", "AA"));
        +possible(cards("Alice", "A8"));
        +possible(cards("Alice", "88"));
        !reasonAboutKnowledge.

// Open our eyes and update our knowledge based on Bob's/Charlie's cards
+!openEyes
    <-  openEyes; // Perceive Bob's and Charlie's cards (these are added to the BB)
        !updateKnowledgeFromOpenEyes;
        !reasonAboutKnowledge.

+!peekCard
    :   cards("Alice", _)
    <-  .print("Alice knows her card, so she does not need to cheat :)").

+!peekCard
    :   not cards("Alice", _)
    <-  peekCard;
        !updateKnowledgePeekCard;
        !reasonAboutKnowledge.

// Print knowledge (we are certain about our hand)
+!reasonAboutKnowledge
    :  cards("Alice", Hand)
    <- .print("Alice knows her hand is ", Hand).

// Print all Hands that are possible
+!reasonAboutKnowledge
    :   not cards("Alice", _)
    <-  .setof(Hand, possible(cards("Alice", Hand)), PossibleHands);
        .print("Alice doesn't know her hand, but the possibilities are ", PossibleHands).


// Remove any Possible Hands with Aces if Bob and Charlie have all Aces
+!updateKnowledgeFromOpenEyes
    :   cards("Bob", "AA") & cards("Charlie", "AA")
    <-  -possible(cards("Alice", "AA"));
        -possible(cards("Alice", "A8")).

// Remove any Possible Hands with Eights if Bob and Charlie have all Eights
+!updateKnowledgeFromOpenEyes
    :   cards("Bob", "88") & cards("Charlie", "88")
    <-  -possible(cards("Alice", "88"));
        -possible(cards("Alice", "A8")).


+!updateKnowledgeFromOpenEyes
    <-  .print("Could not eliminate possibilities from Bob's/Charlie's hands").

// Remove AA and A8 when we see 4 Aces on the table
+!updateKnowledgePeekCard
    :   peekedCard("A") &
        ((cards("Bob", "AA") & cards("Charlie", "A8")) |
         (cards("Bob", "A8") & cards("Charlie", "AA")))
    <-  -possible(cards("Alice", "AA"));
        -possible(cards("Alice", "A8")).

// Remove A8 and 88 when we see 4 eights on the table
+!updateKnowledgePeekCard
    :   peekedCard("8") &
        ((cards("Bob", "88") & cards("Charlie", "A8")) |
         (cards("Bob", "A8") & cards("Charlie", "88")))
    <-  -possible(cards("Alice", "88"));
        -possible(cards("Alice", "A8")).

// Remove AA when we see 3 Aces on the table
+!updateKnowledgePeekCard
    :   peekedCard("A") &
        cards("Bob", "A8") & cards("Charlie", "A8")
    <-  -possible(cards("Alice", "AA")).

// Remove 88 when we see 3 eights on the table
+!updateKnowledgePeekCard
    :   peekedCard("8") &
        cards("Bob", "A8") & cards("Charlie", "A8")
    <-  -possible(cards("Alice", "88")).


+!updateKnowledgePeekCard
    <-  .print("Could not eliminate possibilities from peeked card hands").





//// We use 'range' to describe all possible values of Alice's hand.
//range(cards("Alice", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Bob", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//range(cards("Charlie", Hand)) :- .member(Hand, ["AA", "A8", "88"]).
//
//// Relation between knowledge (trivial in Vanilla Jason)
//cards("Alice", "AA") :- cards("Bob", "88") & cards("Charlie", "88").
//cards("Alice", "88") :- cards("Bob", "AA") & cards("Charlie", "AA").
//
//+one_eight
//    <-  -possible cards("Alice", "A8").
//
//+no_aces
//    <-  -possible cards("Alice", "AA");
//        -possible cards("Alice", "A8").
//
//!determineCards.
//
//// Knowledge
//+!determineCards
//    : cards("Alice", Hand)
//    <- .print("I know my cards are: ", Hand).
//
//// One possibility
//+!determineCards
//    : not cards("Alice", _) & possible cards("Alice", Hand)
//    <- .print("I don't know my cards. I know that one possible hand is: ", Hand).
//
//// All possibilities
//+!determineCards
//    : not cards("Alice", _) & .setof(Hand, possible cards("Alice", Hand), Hands) // get all possible hands
//    <- .print("I don't know my cards. All of my possible hands are ", Hands).
//
//
//
//// (Monotonic Propositions - Epistemic Logic)
//// Belief Revision Function:
//// Monotonic in this case means that if card(Alice, AA) is true, then it's impossible for us to know card(Alice, A8) and card(Alice, 88)
//// We therefore do not need to revise beliefs to remove conflicting knowledge
//
//
//// Use case 1: Add knowledge
//+know card(Alice, AA)
//
//// Belief Base Assertions (Alice AA)
//card(Alice, AA)
//not card(Alice, AA) == false // Assert that we have removed 'we don't know Alice AA'
//possible card(Alice, AA)
//
//// Assertions (Alice A8)
//not card(Alice, A8)
//~card(Alice, A8)
//not possible card(Alice, A8)
//possible ~card(Alice, A8)
//
//// Assertions (Alice 88)
//not card(Alice, 88)
//~card(Alice, 88)
//not possible card(Alice, 88)
//possible ~card(Alice, 88)
//
//
//
//
//// Add Knowledge: +card(Alice, AA)
//// 1. Remove conflicting possibilities: -possible card(Alice, A8), -possible card(Alice, 88)
//
//// Not necessary:
//// - Remove conflicting knowledge: ~card(Alice, AA). No need because 'know ~card(Alice, AA)' is false if card(Alice, AA) is possible
//// - Remove conflicting knowledge: -card(Alice, 88). This will never be true because of monotonic props.
//// - Remove conflicting knowledge: not card(Alice, AA). No need because this is inherent when adding card(Alice, AA)
//
//
//
//
//
//// In order to make 'possible' consistent, we need to define the following rules:
//possible(cards("Alice", Hand))
//    :-  range(cards("Alice", Hand)) &
//        not possible(~cards("Alice", Hand)).
//
//possible(~cards("Alice", Hand))
//    :-  range(cards("Alice", Hand)) &
//        not possible(cards("Alice", Hand)).
//
//// Issue 1: Circular rules. First definition calls second definition which then calls the first, etc... leading to stack overflow
//// Issue 2: Agent can
//
//
//!start.
//
//+!start
//    : possible(cards("Alice", Hand))
//    <- .print(Hand).
//
//
//// The following plan runs when the agent moves and perceptions are updated
//+moved_bad
//    <-  .print("I Moved.");
//        !updateAdjacent; // Update the adjacent locations based on movement from our previous locations to further eliminate worlds
//        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known
//
//
//+!updateAdjacent
//    :  not previousPossible(PrevList) // Get previous locations
//    <- !updatePrevious. // Set new previous locations
//
//
//// Update the reasoner with knowledge of our adjacent positions
//+!updateAdjacent
//    :  previousPossible(PrevList) & // Get previous locations
//       lastMove(MoveDir)
//    <-  .abolish(adjacent(_, _)); // Remove existing adjacent knowledge (it is no longer relevant for our new location)
//        .print("Moved (", MoveDir, ") from: ", PrevList);
//        for(.member(Prev, PrevList)) { +adjacent(MoveDir, Prev); };// Add adjacent knowledge
//        !updatePrevious. // Now that we no longer need the current previous locations, we update the list of possibilities
//
//+!updateGUIPossible
//    :   .setof(location(X, Y), possible(location(X, Y)), Possible) & // get all possibilities from reasoner
//        .setof(Dir, possible(direction(Dir)), AllDir)
//    <-  .print("Possible Locations: ", Possible); // Print to agent log
//        .print("Possible Directions: ", AllDir); // Print to agent log
//        internal.update_best_move(AllDir);
//        internal.update_possible(Possible). // Update GUI positions
//
//+!updatePrevious
//    :   .setof(location(X, Y), possible(location(X, Y)), Possible) // get all possibilities from reasoner
//    <-  .abolish(previousPossible(_)); // Reset previous possibilities
//        +previousPossible(Possible).
//

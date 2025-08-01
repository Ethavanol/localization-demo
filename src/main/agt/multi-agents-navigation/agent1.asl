range(loc(X,Y)) :- (.range(X,0,6) & .range(Y,0,6)).
range(locGoal(3,4)) :- true.

range(none) :- true.
none :- .findall(not(loc(X, Y)), range(loc(X, Y)), List) & .big_and(Y, List) & Y.
~none.

range(noneGoal) :- true.
noneGoal :- .findall(not(locGoal(X, Y)), range(locGoal(X, Y)), List) & .big_and(Y, List) & Y.
~noneGoal.

~loc(3,2).
~loc(5,6).
~loc(1,5).

~loc(X1, Y1) :- loc(X2, Y2) & (X1 \== X2 | Y1 \== Y2).


obs(right) :- loc(2,2) | loc(4,6) | loc(0,5).
obs(left)  :- loc(4,2) | loc(6,6) | loc(2,5).
obs(down)  :- loc(3,1) | loc(5,5) | loc(1,4).
obs(up)    :- loc(3,3) | loc(5,0) | loc(1,6).

dir(right) :- right.
dir(down) :- down.
dir(up) :- up.
dir(left) :- left.

// MOVE EVENTS
// OPEN-WORLD VERSION

+on(moved(right)) : loc(X, Y) & X < 6
   <- -loc(X, Y);
      +loc(X + 1, Y).

+on(moved(left)) : loc(X, Y) & X > 0
   <- -loc(X, Y);
      +loc(X - 1, Y).

+on(moved(up)) : loc(X, Y) & Y > 0
   <- -loc(X, Y);
      +loc(X, Y - 1).

+on(moved(down)) : loc(X, Y) & Y < 6
   <- -loc(X, Y);
      +loc(X, Y + 1).

+on(moved(right)) : loc(X, Y) & X >= 6
   <- -loc(X, Y);
      +loc(0, Y).

+on(moved(left)) : loc(X, Y) & X <= 0
   <- -loc(X, Y);
      +loc(6, Y).

+on(moved(up)) : loc(X, Y) & Y <= 0
   <- -loc(X, Y);
      +loc(X, 6).

+on(moved(down)) : loc(X, Y) & Y >= 6
   <- -loc(X, Y);
      +loc(X, 0).

+on(obs(D)) : obs(D).
+on(~obs(D)) : not obs(D).

+on(~loc(X,Y)) : not loc(X,Y).

+on(~na(goal)) : not (loc(X,Y) & locGoal(X,Y)).

+on(informations_agent_locs(List)) : .big_or(X, List) & X.

!nav.

@atomic
+!nav : communicating
    <- !communicate;
       !confirmCommunication;
       !sendWorldsAsList;
       !updateMyWorlds;
       !nav.

+!nav : at(goal)
    <- .print("At goal.").

+!nav : dir(D)
    <- move(D);
        .wait(1000);
       !checkIfGoal;
       !nav.

+!nav : poss(dir(D))
    <- move(D);
       .wait(1000);
       !checkIfGoal;
       !nav.


+ag(D)
   <-  +communicating;
       +perc(ag(D)).

+!communicate : perc(ag(D))
    <- .print("COMMUNICATING WITH AGENT2");
       .send(agent2,tell,agent(D));
       -perc(ag(D)).

+!communicate
    <- true.

+!confirmCommunication : confirm(D)
    <- .print("CONFIRMING COMMUNICATING");
       .send(agent2,tell,me(D));
       -confirm(D).

+!confirmCommunication
    <- true.

+!sendWorldsAsList : getworlds(D)
    <- .print("GETTING WORLDS");
       multiagents_navigation.getPossibleWorldsAsList(L);
       .send(agent2,tell,worlds(L,D));
       -getworlds(D).

+!sendWorldsAsList
    <- true.

+!updateMyWorlds : updateWorlds(D,L)
    <- .print("UPDATING WORLDS");
       multiagents_navigation.handleLocationsProps(N,D,L);
       +informations_agent_locs(N);
       -updateWorlds(D,L);
       -communicating.

+!updateMyWorlds
    <- true.


+!kqml_received(Src,tell,agent(D),Mid)
   <-  +confirm(D);
       -perc(ag(X)).

+!kqml_received(Src,tell,me(D),Mid)
   <- +getworlds(D).

+!kqml_received(Src,tell,worlds(L,D),Mid)
   <- +updateWorlds(D,L).

+!checkIfGoal : not at(goal)
    <-  +~na(goal);
        -~na(goal).

+!checkIfGoal
    <-  .print("I am arrived").




/**

// communication between agents.

@atomic
+ag(D)
    <-  +perc(ag(D));
        .send(agent1,tell,agent(D)).

+agent(right)[source(S)] : perc(ag(left))
    <-.send(Src,tell,me(right));
      -perc(ag(left)).

+agent(left)[source(S)] : perc(ag(right))
    <-.send(Src,tell,me(right));
      -perc(ag(right)).

+agent(up)[source(S)] : perc(ag(down))
    <- .send(S, tell, me(up));
       -perc(ag(down)).

+agent(down)[source(S)] : perc(ag(up))
    <- .send(S, tell, me(down));
       -perc(ag(up)).

+me(D)[source(S)]
    <- multiagents_navigation.getPossibleWorldsAsList(L);
       .send(S, tell, worlds(L, D)).

+worlds(L,D)[source(S)]
    <- multiagents_navigation.handleLocationsProps(N, D, L);
       +informations_agent_locs(N).

**/

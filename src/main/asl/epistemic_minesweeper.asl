// Given the map size, the number of mines, we generate the following grid combinations:
// This is only a subset of possibilities for a 3x3 grid with 3 mines (for the MVP). The true number of states would be: 9 choose 3

/*
    The range/valuation rules in this example generate the following worlds (which each represent 1 possible configuration of a Minesweeper board):
    w1 = [grid(1), hint(1,1,-1), hint(1,2,-1), hint(1,3,1), hint(2,1,-1), hint(2,2,3), hint(2,3,1), hint(3,1,1), hint(3,2,1), hint(3,3,0), mine(1,1), mine(1,2), mine(2,1)]
    w2 = [grid(2), hint(1,1,-1), hint(1,2,2), hint(1,3,-1), hint(2,1,2), hint(2,2,3), hint(2,3,1), hint(3,1,-1), hint(3,2,1), hint(3,3,0), mine(1,1), mine(1,3), mine(3,1)]
    w3 = [grid(3), hint(1,1,-1), hint(1,2,3), hint(1,3,-1), hint(2,1,-1), hint(2,2,3), hint(2,3,1), hint(3,1,1), hint(3,2,1), hint(3,3,0), mine(1,1), mine(1,3), mine(2,1)]
    w4 = [grid(4), hint(1,1,-1), hint(1,2,-1), hint(1,3,1), hint(2,1,3), hint(2,2,3), hint(2,3,1), hint(3,1,-1), hint(3,2,1), hint(3,3,0), mine(1,1), mine(1,2), mine(3,1)]
*/


/* Generated Data */
grid_perms(1,
    [[1, 1], [1, 2], [2, 1]],
    [hint(1, 1, -1), hint(1, 2, -1), hint(1, 3, 1), hint(2, 1, -1), hint(2, 2, 3), hint(2, 3, 1), hint(3, 1, 1), hint(3, 2, 1), hint(3, 3, 0)]).

grid_perms(2,
    [[1, 1], [1, 3], [3, 1]],
    [hint(1, 1, -1), hint(1, 2, 2), hint(1, 3, -1), hint(2, 1, 2), hint(2, 2, 3), hint(2, 3, 1), hint(3, 1, -1), hint(3, 2, 1), hint(3, 3, 0)]).

grid_perms(3,
    [[1, 1], [1, 3], [2, 1]],
    [hint(1, 1, -1), hint(1, 2, 3), hint(1, 3, -1), hint(2, 1, -1), hint(2, 2, 3), hint(2, 3, 1), hint(3, 1, 1), hint(3, 2, 1), hint(3, 3, 0)]).

grid_perms(4,
    [[1, 1], [1, 2], [3, 1]],
    [hint(1, 1, -1), hint(1, 2, -1), hint(1, 3, 1), hint(2, 1, 3), hint(2, 2, 3), hint(2, 3, 1), hint(3, 1, -1), hint(3, 2, 1), hint(3, 3, 0)]).


// All grid coordinates, for reference
all_locations(1,1).
all_locations(1,2).
all_locations(1,3).

all_locations(2,1).
all_locations(2,2).
all_locations(2,3).

all_locations(3,1).
all_locations(3,2).
all_locations(3,3).
/* End Generated Data */

/* Begin range/valuation rules */
range(grid(X)) :- grid_perms(X, _, _).
range(mine(X, Y)) :- all_locations(X, Y).
range(hint(X, Y, N)) :- all_locations(X, Y) & .member(N, [-1, 0, 1, 2]).

mine(X, Y) :- grid(P) & grid_perms(P, Mines, _) & .member([X, Y], Mines).
hint(X, Y, N) :- grid(P) & grid_perms(_, _, Hints) & .member(hint(X, Y, N), Hints).
/* End range/valuation rules */

possible(mine(X + 1, Y))
    :- possible(mine(X, Y)) & moved(right).

/* Jason Plans */
// Percepts are lost, done, clicked(X, Y), and hint(X, Y, N), which change when the agent performs click(X, Y)
!playMineSweeper.

+!playMineSweeper
    : lost
    <- .print("Boom! Loser!").

+!playMineSweeper
    : done
    <- .print("Winner").

// Pick a cell which we KNOW does not have a mine (done by the reasoner), and which hasn't been clicked,
+!playMineSweeper
    : ~mine(X, Y) & not clicked(X, Y)
    <-  click(X, Y);
        .wait(2000);
        !playMineSweeper.

+!playMineSweeper
    <- .print("Failure! No Mines found!").
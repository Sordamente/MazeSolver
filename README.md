# MazeSolver
This is a basic A* powered maze making and solving program, assigned to the controls rookies at GRT as a test.

If you would like to see the assignment itself go to:
* [Original Nifty Problem](http://nifty.stanford.edu/2008/blaheta-maze/cs2/prep-hwk.pdf)
* [GUI Extension](http://nifty.stanford.edu/2008/blaheta-maze/cs2/proj-spec.pdf)
* [A* Algorithm](http://nifty.stanford.edu/2008/blaheta-maze/a-star/a-star.pdf)

# Test case of note:
Using the maze,
```
############
#.#........#
#.#.######.#
#.#....#...#
#.###.*#.#.#
#...####.#.#
#.#.#..#.#.#
#.#.#.##.#.#
#o#......#.#
############
```
We can see that `Euclidian` and `Dijkstra` are unable to solve it.
This is because this maze contains false valleys, where the estimated distance to the end is decievingly small. You can see that in this case, after only 5 steps, the euclidian distance to the end becomes really small, even though there are still 30 more steps until the end is reached.
`Dijkstra` is useless in this case also because the path needs to stray very far from the starting position in order to reach the end. Because the distance to start is the only factor affecting fitness, points that stray too far are removed from the path, making it impossible to solve the maze in this way.

Interestingly, we can see that `Proximity` and `Diagonal` both finish while only checking 2 unnecessary moves (for a total of 37 checks on a 35 move path), and `Manhattan` completes the path with only 41 checks. These heuristical checks are so efficient because they were built for situations where you can only move in a limited number of directions.

# Conclusions
Due to the phrasing of the A* problem itself, and the potential curiosity of the user, the `Euclidian` and `Dijkstra` checks were included in the program. However, as evidenced by the test case above, they can be very faulty in some scenarios, and potentially yield incorrect results. I would recommend `Diagonal` or `Proximity`, for the following reasons, in no particular order:
* `Diagonal` finds approximately linear paths
* Both score consistently higher than the other algorithms
* `Proximity` is a more advanced form of `Manhattan`
* `Proximity` was built for 4 directional scenarios like our case
* `Diagonal` is the second most processor intensive task (behind `Dijkstra`)

Then again, there are many legitimate use cases for `Euclidian` or `Dijkstra`, so they aren't to be cast aside. It really depends on the situation, and what kind of rules are in play in the pathfinding task.
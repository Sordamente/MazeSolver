package com.tomer.maze;

import java.util.ArrayList;

public class Maze {
	private Node[] maze;
	private int width;
	private int height;
	public ArrayList<Node> checked;
	public ArrayList<Node> solution;
	
	public Maze(int width, int height, Node[] maze) {
		//Create a maze object
		this.width = width;
		this.height = height;
		this.maze = maze;
	}

	//Some miscellaneous getters for the maze
	public Node[] getMaze() { return this.maze; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	
	public void solve(String heuristic) {
		//Reset the solution arrays
		this.checked = null;
		this.solution = null;

		//Find the start in the maze
		Node start = new Node();
		for (int i = 0; i < this.height; i++)
			for (int j = 0; j < this.width; j++)
				if (this.maze[i * this.width + j].name == "START") { start.x = j; start.y = i; start.name = "START"; }
		
		//Find the end in the maze
		Node end = new Node();
		for (int i = 0; i < this.height; i++)
			for (int j = 0; j < this.width; j++)
				if (this.maze[i * this.width + j].name == "END") { end.x = j; end.y = i; end.name = "END"; }
		
		if (start.name != "START" || end.name != "END") return;

		ArrayList<Node> ports = new ArrayList<Node>();
		for (int i = 0; i < this.height; i++)
			for (int j = 0; j < this.width; j++)
				if (this.maze[i * this.width + j].name == "PORT") {
					Node n = Node.fromChar('@');
					n.x = j; n.y = i;
					ports.add(n);
				}

		//Probably the most important line
		//Use A* to solve the maze, and unload the solution into an ArrayList
		Node sol = aStar(start, end, ports, heuristic);
		if (sol == null) return;
		this.solution = new ArrayList<Node>();
		solution.add(start);
		for (Node n = sol; n.parent != null; n = n.parent) solution.add(n);
	}
	
	private Node aStar(Node start, Node end, ArrayList<Node> ports, String heuristic) {
		//Set up flexible arrays for A*
		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> close = new ArrayList<Node>();
		this.checked = new ArrayList<Node>();
		
		//Add in the first location
		open.add(start);
		
		//While there are potential solutions
		while (!open.isEmpty()) {
			//Find the node in open that is the closest to the end location (estimated using heuristics)
			int leastInd = 0;
			for (int i = 0; i < open.size(); i++) if (open.get(i).h < open.get(leastInd).h) leastInd = i;
			Node least = open.remove(leastInd); //Remove that node from open to stop it being checked again
			this.checked.add(least);

			//Now we want to check the nodes around that node
			//This both helps us find potentially better fits
			//And it helps us to create the path itself
			ArrayList<Node> successors = new ArrayList<Node>();
			
			//This is a condensed way of having four if statements checking the four squares around the node
			//Think of the for loop as just rotating a line around the node in 90 degree increments
			for (double i = 0; i < Math.PI * 2; i += Math.PI / 2) {
				Node child = new Node();
				
				//Using basic trig we know the position of the node at that angle
				//Casting it to an int knocks two birds with one stone
				//1. We can add the numbers together (because you can't normally add a double and an int)
				//2. We truncate the double, which is useful because Math.PI is only accurate to like 16 digits, so error is removed
				child.x = least.x + (int) (Math.cos(i));
				child.y = least.y + (int) (Math.sin(i));
				
				//Now we check that this box is within the constraints of the board and that it isn't a wall
				if (child.x < 0 || child.x > this.width - 1 || child.y < 0 || child.y > this.height - 1) continue;
				if (this.maze[child.y * this.width + child.x].name == "WALL") continue;
				
				//If it passes both tests it gets added to the list of nodes with which the next loop will deal
				child.parent = least;
				successors.add(child);

				//Check teleporters :)
				if (this.maze[child.y * this.width + child.x].name == "PORT") for (Node n : ports) {
					Node port = Node.fromChar('@');
					port.x = n.x; port.y = n.y; port.parent = child;
					successors.add(port);
				}
			}
			
			//For every child, figure out if it fits better than everything else; otherwise trash it
			point: for (Node suc : successors) {
				if (suc.x == end.x && suc.y == end.y) { this.checked.add(suc); return suc; } //We found what A* considers the best path!
				
				//First, if we've ever checked this point before, lets not try again.
				for (Node comp : close) if (comp.x == suc.x && comp.y == suc.y) continue point;
				
				//Otherwise, lets find the g and h values for the total fitness of the node.
				suc.g = least.g + 1; //Distance to start of path (this value is always accurate and not estimated)
				suc.h = this.approxDist(suc, end, heuristic); //Find the h value
				
				//Note that the total fitness value is calculated by adding g and h together.
				
				//Filter the children in order to weed out ones that have fitnesses that aren't useful
				//This means that we ignore the point if:
				//We are already waiting to check a point with those coordinates, and that point has better (or equal) fitness values
				//When this check is combined with the method of finding the best point in open, it kills three birds with one stone:
				//1. Heuristic tiebreakers. Total fitness checks happen before the point is added so we don't waste time checking useless points
				//2. No two points in the same location will ever be checked, ever. This constrains the number of checks to the number of acessible open spots in the maze at most
				//3. Only the best point is chosen to check each time. This way we, using anything but Dijkstra, we can arrive at the final path in an almost minimal number of checks
				for (Node comp : open) if (comp.h+comp.g <= suc.h+suc.g && comp.x == suc.x && comp.y == suc.y) continue point;
				
				//It passed! It is a potential candidate for point on the path, so lets put it in open
				open.add(suc);
			}
			
			//Put the open's last best fit away, to use as a marker for other childrens' fitnesses in future
			close.add(least);
		}
		
		return null; //No solution, sorry.
	}
	
	private int approxDist(Node pt1, Node pt2, String heuristic) {
		//To find h, there are many different methods (including ignoring it)
		//Included are four common ones and the default "Dijkstra" (glorified BFS)
		//In the case of this maze both Manhattan, Proximity, or Diagonal are probably best
		//This is because we can only move in four directions
		int diffX = Math.abs(pt2.x - pt1.x);
		int diffY = Math.abs(pt2.y - pt1.y);
		switch (heuristic) {
		//Manhattan is the sum of the difference in x and y positions
		case "Manhattan": return diffX + diffY;
		//Euclidian is the linear distance between the points
		//Euclidian is best when you can move in any direction
		case "Euclidian": return (int) (Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2)));
		//Diagonal is the maximum difference in x or y
		//Diagonal is best when you can move in eight directions, but in most test cases it works really well here too
		case "Diagonal": return Math.max(diffX, diffY);
		//Proximity is manhattan, but maxxes out at eight if that distance is too high
		case "Proximity": return Math.min(diffX + diffY, 8);
		//The default is almost never the most efficient, but it can solve some cases that no other heuristic methods can
		//Read the README for some interesting cases and exeptions
		default: return 0;
		}
	}

	public String toString() {
		//This function's basic purpose is to convert the maze into a string
		//This is done using the newline character to concatenate the maze's rows
		String fin = "";
		
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				String adder = this.maze[i * this.width + j].toString();
				fin += adder;
			}
			fin += "\n";
		}
		
		return fin;
	}
}

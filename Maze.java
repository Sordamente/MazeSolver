package com.tomer.maze;

import java.util.ArrayList;
import java.util.Scanner;

public class Maze {
	private Node[] maze;
	private int width;
	private int height;
	private String heuristic;
	public int count = 0;
	
	public Maze() {
		//Set up input from the CLI
		Scanner sc = new Scanner(System.in);
		
		//Ask the user for the maze's dimensions
		System.out.println("Enter the maze dimensions ('width height'), please :)");
		String[] dims = sc.nextLine().split(" ");
		this.maze = new Node[Integer.parseInt(dims[1]) * Integer.parseInt(dims[0])];
		this.width = Integer.parseInt(dims[0]);
		this.height = Integer.parseInt(dims[1]);
		
		//Take input line by line and add to the maze
		//Please note that there is little validating of inputs going on
		//It is assumed that the user is of good intention (in this case, it is likely to be true)
		System.out.println("Now, enter the maze row by row ('#' is wall, '.' is air, 'o' is start, '*' is end).");
		for (int i = 0; i < this.height; i++) {
			char[] strings = sc.nextLine().toCharArray();
			if (strings.length == this.width) for (int j = 0; j < this.width; j++) maze[i * this.width + j] = Node.fromChar(strings[j]);
			else { i--; System.out.println("Input didn't match desired width"); }
		}
		
		System.out.println("Select a heuristic mode for A* (Manhattan, Euclidian, Diagonal, Proximity). Defaults to Dijkstra");
		this.heuristic = sc.nextLine();
		
		//Close the input stream
		//If we don't do this it could stay open past this program's termination (kernel / implementation dependent)
		//However, this isn't a major concern and is included generally as a good habit
		sc.close();
	}
	
	public Node solve() {
		//Find the start in the maze
		Node start = new Node();
		for (int i = 0; i < this.height; i++)
			for (int j = 0; j < this.width; j++)
				if (this.maze[i * this.width + j].name == "START") { start.x = j; start.y = i; }
		
		//Find the end in the maze
		Node end = new Node();
		for (int i = 0; i < this.height; i++)
			for (int j = 0; j < this.width; j++)
				if (this.maze[i * this.width + j].name == "END") { end.x = j; end.y = i; }
		
		//We go backwards from end to start
		//This is because, if we want to animate pathfinding, A* has the end as its latest child
		//Therefore, we want to end at the start so that with each layer we move closer to the end
		return aStar(end, start);
	}
	
	private Node aStar(Node start, Node end) {
		//Set up flexible arrays for A*
		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> close = new ArrayList<Node>();
		
		//Add in the first location
		open.add(start);
		
		//While there are potential solutions
		while (!open.isEmpty()) {
			this.count++;
			//Find the node in open with the best fit on the final path
			int leastInd = 0;
			for (int i = 0; i < open.size(); i++) if (open.get(i).f < open.get(leastInd).f) leastInd = i;
			Node least = open.remove(leastInd); //Remove that node from open to stop it being selected again
			
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
				successors.add(child);
			}
			
			//For every child, figure out if it fits better than everything else; otherwise trash it
			point: for (Node suc : successors) {
				suc.parent = least;
				if (suc.x == end.x && suc.y == end.y) return suc; //We found what A* considers the best path!
				
				suc.g = least.g + 1; //Distance to start of path (this value is always accurate and not estimated)
				suc.h = this.approxDist(suc, end, this.heuristic); //Find the h value
				
				suc.f = suc.g + suc.h; //Total fitness value
				//Note that "f" is not a nessessary value to store because you can always find it using g and h
				//However, due to the number of times we use the total fitness, it is likely more efficient to only do that once per node
				
				//Filter the children in order to weed out ones that have fitnesses that aren't useful
				for (Node comp : close) if (comp.x == suc.x && comp.y == suc.y) continue point;
				for (Node comp : open) if (comp.f < suc.f || (comp.f == suc.f && comp.x == suc.x && comp.y == suc.y)) continue point;
				
				//It passed! It is a potential candidate for point on the path, so lets put it in open
				open.add(suc);
			}
			
			//Put the open's last best fit away, to use as a marker for other childrens' fitnesses in future
			close.add(least);
		}
		
		return null;
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
	
	public String toString(ArrayList<Node> path) {
		//This function's basic purpose is to convert the maze into a string
		//This is done using the newline character to concatenate the maze's rows
		//Also, the solved path is overlayed over the maze in the form of the letter P
		String fin = "";
		
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				String adder = this.maze[i * this.width + j].toString();
				if (path != null) for (Node n : path) if (n.x == j && n.y == i) adder = "P";
				fin += adder;
			}
			fin += "\n";
		}
		
		return fin;
	}
}

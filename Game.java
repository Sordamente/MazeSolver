package com.tomer.maze;

import java.util.ArrayList;

public class Game {
	private Maze maze;
	private Node sol;
	
	public Game() {
		//Create a new maze for this game instance
		this.maze = new Maze();
		
		//Solve the maze using an implementation of A*
		this.sol = this.maze.solve();
		
		//Determine if there is a solution
		if (this.sol == null) System.out.println("No Solution");
		else System.out.println("Solution Found");
		
		//Create an array with the solution
		//This is built to withstand no solution cases so that little validation is required
		ArrayList<Node> path = new ArrayList<Node>();
		if (this.sol != null) for (Node par = this.sol; par.parent != null; par = par.parent) path.add(par);
		
		//Print out the solved (or unsolved) maze
		System.out.println(this.maze.toString(path));
	}
	
	public static void main(String[] args) {
		//Create a new game instance when the program is run
		new Game();
	}
}

class Node {
	//Originally, the Node class was an enum. However, the A* algorithm needs custom values such as g and h and a parent.
	//As such, an enum will not suffice because you do not make instances of enums, and because of that the different objects will overwrite one another.
	//Note that it is possible to split the identity of the Node (WALL, etc) into its own enum, but for code conciseness and readability this is not done.
	
	public int x, y, f, g, h = 0;
	public Node parent = null;
	public String name = "";
	
	public String toString() {
		if (this.name == "WALL") return "#";
		if (this.name == "OPEN") return ".";
		if (this.name == "START") return "o";
		return "*";
	}
	
	public static Node fromChar(char type) {
		Node n = new Node();
		if (type == '#') n.name = "WALL";
		if (type == '.') n.name = "OPEN";
		if (type == 'o') n.name = "START";
		if (type == '*') n.name = "END";
		return n;
	}
}

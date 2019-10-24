package com.tomer.maze;

import java.awt.Color;

public class Node {
	//Originally, the Node class was an enum. However, the A* algorithm needs custom values such as g and h and a parent.
	//As such, an enum will not suffice because you do not make instances of enums, and because of that the different objects will overwrite one another.
	//Note that it is possible to split the identity of the Node (WALL, etc) into its own enum, but for code conciseness and readability this is not done.
	//UPDATE: it would now really difficult (not to mention inefficient) to split the class into separate Objects for each datatype
	
	public int x, y, g, h = 0; //Cartesian coordinates, distance to start, and estimated distance to end
	public Node parent = null; //Defaulted to null (b/c this is only necessary when finding a solution)
	public String name = "WALL"; //Default the node to a wall when if it is created using 'new Node();'

	public void airWallSwap() {
		//When you left click, we want to swap between nothing and a wall
		if (this.name == "WALL") this.name = "OPEN";
		else this.name = "WALL";
	}

	public void startEndRotate() {
		//When you right click, we want to rotate between the end, start, and a teleport pad
		if (this.name == "PORT") this.name = "START";
		else if (this.name == "START") this.name = "END";
		else this.name = "PORT";
	}

	public Color getColor() {
		//To display the node in the GUI
		if (this.name == "WALL") return Color.DARK_GRAY;
		if (this.name == "OPEN") return Color.WHITE;
		if (this.name == "END") return Color.MAGENTA;
		if (this.name == "PORT") return Color.CYAN;
		return Color.GREEN;
	}
	
	public String toString() {
		//When saving the maze as a string
		if (this.name == "WALL") return "#";
		if (this.name == "OPEN") return ".";
		if (this.name == "START") return "o";
		if (this.name == "PORT") return "@";
		return "*";
	}
	
	public static Node fromChar(char type) {
		//When importing the maze from a file
		Node n = new Node();
		if (type == '.') n.name = "OPEN";
		if (type == 'o') n.name = "START";
		if (type == '*') n.name = "END";
		if (type == '@') n.name = "PORT";
		return n;
	}
}

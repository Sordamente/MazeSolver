package com.tomer.maze;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JPanel;

public class MazeDrawer extends JPanel {
	private static final long serialVersionUID = 1L; //Added to satisfy java :)
	
	public MazeDrawer() {
		//Check for mouse presses. This is how we interact with the maze
		//If you left click, the node flips between WALL and OPEN
		//If you right click, it rotates through START, END, and OPEN
		//This is done instead of having WALL, OPEN, START and END in one big rotation because people mainly use WALL and OPEN and should only have to click once
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				if (MazeSolver.maze == null) return;
				//Find the side length of one square on the maze
				int squareDim = 500 / MazeSolver.maze.getWidth();
				//Calculate the index position of the node
				int xLoc = (int) (evt.getX() / squareDim);
				int yLoc = (int) (evt.getY() / squareDim);
				
				if (evt.getButton() == 1) MazeSolver.maze.getMaze()[yLoc * MazeSolver.maze.getWidth() + xLoc].airWallSwap(); //WALL and OPEN swap
				if (evt.getButton() == 3) MazeSolver.maze.getMaze()[yLoc * MazeSolver.maze.getWidth() + xLoc].startEndRotate(); //START, END, OPEN rotation
				if (MazeSolver.animating) { MazeSolver.playTimer.cancel(); MazeSolver.animating = false; } //Stop any animated solution
				MazeSolver.index = 0; //Reset the animation
				MazeSolver.frame.repaint(); //Redraw the maze
				MazeSolver.maze.solve(MazeSolver.mode); //Solve the maze
				MazeSolver.updateInfo(); //Update the info tooltip
			}
		});
	}

	public void paintComponent(Graphics g) {
		//Draw the maze
		super.paintComponent(g);
		if (MazeSolver.maze == null) return;
		
		//Find the width of one square
		int squareDim = MazeSolver.width / MazeSolver.maze.getWidth();

		//Draw the maze itself node by node
		for (Node n : MazeSolver.maze.getMaze()) drawNode(g, n, n.getColor(), squareDim);

		//If the maze hasn't been checked, return
		if (MazeSolver.maze.checked == null) return;

		//Draw the checking of the maze, node by node, up to the current index
		for (int i = 0; i < MazeSolver.maze.checked.size() && i < MazeSolver.index; i++) {
			Node n = MazeSolver.maze.checked.get(i);
			drawNode(g, n, Color.GRAY, squareDim);
		}

		//If there's no solution to the maze, or if the animation of pathfinding hasn't finished, return
		if (MazeSolver.index != MazeSolver.maze.checked.size() || MazeSolver.maze.solution == null) return;

		//Draw the path (in hot pink ;P)
		for (Node n : MazeSolver.maze.solution) drawNode(g, n, Color.PINK, squareDim);
	}

	public void drawNode(Graphics g, Node n, Color c, int dim) {
		//Draw the square node and then a cute little accent square inside
		g.setColor(c);
		g.fillRect(n.x*dim,n.y*dim,dim,dim);
		g.setColor(c.darker());
		g.fillRect(n.x*dim + dim/10,n.y*dim + dim/10,4*dim/5,4*dim/5);
	}
}

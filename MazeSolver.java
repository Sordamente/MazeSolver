package com.tomer.maze;

//The line of imports was much, much longer than this, and was therefore condensed into much fewer lines.
//In a scenario where compile times are important, these statements should not be condensed due to the time it takes to fetch them.
import java.awt.*;
import java.io.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

public class MazeSolver {
	public static JFrame frame; //The main window
	public static Maze maze; //The maze itself
	public static String mode = "Dijkstra"; //The heuristic mode
	public static JLabel info; //The label which, when hovered, gives us some stats about the maze's solution and a help guide
	public static int index = 0; //How far we are into animating the solution
	public static Timer playTimer; //The timer to animate the solution
	public static Boolean animating = false; //Are we animating
	public static int width = 512; //The width of the window. This stays approximately constant and the height changes much more
	
	public static void main(String[] args) {
		//Set up the window
		frame = new JFrame("Maze Solver");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//The maze drawing object
		MazeDrawer drw = new MazeDrawer();
		
		//The top row of buttons / input boxes / hover tooltips
		JPanel topBtns = new JPanel();
		topBtns.setLayout(new GridLayout(1,2));

		JPanel importPnl = new JPanel();
		importPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));

		info = new JLabel("Info (hover)");
		updateInfo();

        JButton importBtn = new JButton("Import");
        importBtn.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				//Ask to pick a file to import from
        		int returnVal = fc.showOpenDialog(frame);

    	        if (returnVal == JFileChooser.APPROVE_OPTION) {
					//Open the file, load it, and redraw the screen
    	            File file = fc.getSelectedFile();
					loadMazeFromFile(file.getAbsolutePath());
					frame.repaint();
    	        }
        	}
		});

		importPnl.add(info);
		importPnl.add(importBtn);

		JPanel sizeInp = new JPanel();
		sizeInp.setLayout(new FlowLayout(FlowLayout.LEFT));
		JTextField widInp = new JTextField("",3);
		JLabel xLabel = new JLabel("x");
		JTextField heiInp = new JTextField("",3);
		JButton sizeBtn = new JButton("Size");
        sizeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { //When we ask to resize the maze
				//Find requested width and height
				//We add 0 to make sure we get a number when parsing
				int wid = Integer.parseInt("0" + widInp.getText());
				int hei = Integer.parseInt("0" + heiInp.getText());
				if (wid == 0 || hei == 0) return; //If either one is not valid
				//The following line makes it so that the dimensions of a node are always a whole number
				//It's here to remove excess whitespace from the side of the window in cases where the width of the maze isnt a factor of the width of the screen
				width = Math.round(width / wid) * wid;
				frame.setSize(width,hei * width / wid + 100); //Reset size
				Maze temp = genEmptyMaze(wid, hei); //Create an empty maze
				if (maze == null) { maze = temp; return; }
				//Fill it with the contents of the last maze (so that we don't lose data when enlarging)
				for (Node n : maze.getMaze()) for (Node n1 : temp.getMaze()) if (n.x == n1.x && n.y == n1.y) n1.name = n.name;
				maze = temp;
				//Solve the maze, update the tooltip, reset the animation, and redraw the window
				maze.solve(mode);
				updateInfo();
				if (animating) { playTimer.cancel(); animating = false; }
				index = 0;
				frame.repaint();
        	}
		});

		sizeInp.add(widInp);
		sizeInp.add(xLabel);
		sizeInp.add(heiInp);
		sizeInp.add(sizeBtn);

		topBtns.add(sizeInp);
		topBtns.add(importPnl);

		//The buttons that lie on the bottom of the window
		JPanel bottomBtns = new JPanel();
		bottomBtns.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		String[] heuristics = {"Dijkstra", "Manhattan", "Diagonal", "Euclidian", "Proximity"};
		JComboBox<String> modes = new JComboBox<String>(heuristics);
		modes.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				//When I chose another mode to solve the maze with
				if (maze == null) return;
				mode = (String) modes.getSelectedItem();
				//Rinse, repeat to reset the window
				maze.solve(mode);
				updateInfo();
				if (animating) { playTimer.cancel(); animating = false; }
				index = 0;
				frame.repaint();
        	}
		});
		//We want the heuristic choices to take up just a small portion of the bottom part of the window, so we use a weighted form of GridLayout
		c.weightx = 0.08;
		c.gridx = 0;
		c.gridy = 0;
		bottomBtns.add(modes, c);

		//All the buttons having to do with animating the solution
		JPanel playPnl = new JPanel();
		playPnl.setLayout(new GridLayout(1,6));
		JButton rwnd = new JButton("<<");
		rwnd.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				if (maze == null || maze.checked == null) return;
				if (animating) { playTimer.cancel(); animating = false; }
				index = 0; frame.repaint(); //Reset to start
			}
		});
		JButton back = new JButton("<");
		back.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				if (maze == null || maze.checked == null || index == 0) return;
				if (animating) { playTimer.cancel(); animating = false; }
				index--; frame.repaint(); //Move one step back
			}
		});
		JButton play = new JButton("*");
		play.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				if (animating) { playTimer.cancel(); animating = false; return; }
				if (maze == null || maze.checked == null) return;
				playTimer = new Timer();
				playTimer.scheduleAtFixedRate(new PlayTask(), 250, 250); //One step forwards in a loop
				animating = true;
			}
		});
		JButton step = new JButton(">");
		step.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				if (maze == null || maze.checked == null || index == maze.checked.size()) return;
				if (animating) { playTimer.cancel(); animating = false; }
				index++; frame.repaint(); //One step forwards
			}
		});
		JButton frwd = new JButton(">>");
		frwd.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				if (maze == null || maze.checked == null) return;
				if (animating) { playTimer.cancel(); animating = false; }
				index = maze.checked.size(); frame.repaint(); //Fast forward to the end of the solution
			}
		});

		JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				if (maze == null) return;
				//Choose a file to save to (note, this has only been tested with the .txt extension)
				final JFileChooser fc = new JFileChooser();
        		int returnVal = fc.showSaveDialog(frame);

    	        if (returnVal == JFileChooser.APPROVE_OPTION) {
    	            File file = fc.getSelectedFile();
    	            try {
						//Make a new file (or don't if it already exists)
						file.createNewFile();
						FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
						//Write to the file
						fileWriter.write(maze.toString());
						fileWriter.close();
					} catch (IOException e1) {}
    	        }
        	}
		});

		playPnl.add(rwnd);
		playPnl.add(back);
		playPnl.add(play);
		playPnl.add(step);
		playPnl.add(frwd);
		playPnl.add(saveBtn);

		//Weight the bottom buttons
		c.weightx = 0.92;
		c.gridx = 1;
		c.gridy = 0;
		bottomBtns.add(playPnl, c);
		
		//Add all the panels to the frame
        frame.add(topBtns, BorderLayout.PAGE_START);
		frame.add(drw, BorderLayout.CENTER);
		frame.add(bottomBtns, BorderLayout.PAGE_END);

		//Set the frame width, and present it to the viewer
		frame.setSize(width,100);
		frame.setResizable(false);
		frame.setVisible(true);
	}

	public static void updateInfo() {
		//This tooltip supports HTML, yay! The parts are broken up here for readability.
		String instr1 = "<h3>Instructions:</h3> Create a maze (or resize one) by entering a size (width x height) and clicking 'Size', and save by, well, clicking 'Save'<br>";
		String instr2 = "Left click on a square to swap between wall (black) and air (white), and right click to rotate between air, start (green), and end (orange).<br>";
		String instr3 = (maze != null && maze.solution != null ? "<h3>Stats:</h3> Solving with the method "+mode+" took "+maze.checked.size()+" checks to solve a " + maze.solution.size() + " long path." : "<h3>This maze is not solvable.</h3>");
		info.setToolTipText("<html>"+instr1+instr2+instr3+"</html>");
	}

	public static Maze genEmptyMaze(int wid, int hei) {
		//Create an empty maze (nothing too special)
		Node[] temp = new Node[wid*hei];
		for (int i = 0; i < hei; i++) {
			for (int j = 0; j < wid; j++) {
				Node n = new Node();
				n.x = j; n.y = i;
				temp[i*wid+j] = n;
			}
		}
		return new Maze(wid, hei, temp);
	}
	
	public static Node[] nodesFromStringList(ArrayList<String> rows) {
		//Use a list of strings to create a list of nodes
		//This is to aid in the creation of a maze from a file
		Node[] fin = new Node[rows.size() * rows.get(0).length()];
		for (int i = 0; i < rows.size(); i++) {
			char[] strings = rows.get(i).toCharArray();

			for (int j = 0; j < strings.length; j++) {
				Node n = Node.fromChar(strings[j]);
				n.x = j; n.y = i;

				fin[i * strings.length + j] = n;
			}
		}
		return fin;
	}
	
	public static void loadMazeFromFile(String filename) {
		File file = new File(filename);
		Scanner sc;

		try {
			sc = new Scanner(file);

			//Read from the file and make a maze using the lines
			ArrayList<String> rows = new ArrayList<String>();
			while (sc.hasNextLine()) rows.add(sc.nextLine());
			maze = new Maze(rows.get(0).length(), rows.size(), nodesFromStringList(rows));

			//Update the window to reflect changes
			maze.solve(mode);
			width = Math.round(width/rows.get(0).length()) * rows.get(0).length();
			frame.setSize(width,rows.size() * width / rows.get(0).length() + 100);
			updateInfo();
			if (animating) { playTimer.cancel(); animating = false; }
			index = 0;
			frame.repaint();
		} catch (FileNotFoundException e) {}
	}
}

class PlayTask extends TimerTask {
    public void run() {
		//This runs every 250 milliseconds in order to animate the solution
        if (MazeSolver.maze == null || MazeSolver.maze.checked == null || MazeSolver.index == MazeSolver.maze.checked.size()) { MazeSolver.playTimer.cancel(); MazeSolver.animating = false; return; }
		MazeSolver.index++;
		MazeSolver.frame.repaint();
	}
}
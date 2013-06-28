import java.util.*; //For Scanner class
import java.io.*;   //To read input file

/**
 * Class to solve easy Sudoku puzzles.  Uses the iterative solution of creating a 3D "marking array."
 * It will find the possibilities in each square.  If a square has only one possible option, the board
 * will be updated and the possibilities table will be regenerated.  By looping through this enough times,
 * any board which can be completed with this straightforward one-step logical method will be solved.
 * @author Logan Gore
 */
public class Solver
{
	private final int SIZE = 9;
	private final int SQUARE_SIZE = 3;
	private int board[][];
	private boolean avail[][][];
	private int correct;
	private int pass;
	private int limit;
	
	/**
	 * Constructor to create a board solver
	 * @param filename the input file
	 * @param iterations the maximum number of iterations to take to solve the board
	 */
	public Solver(String filename, int iterations)
	{
		// Define the board as a 2D int array, the number of correct guesses to zero, and declare the Scanner object
		board = new int[SIZE][SIZE];
		correct = 0;
		pass = 0;
		limit = iterations;
		Scanner sc = null;
		
		// Try to initialize the Scanner for the filename
		try
		{
			sc = new Scanner(new File(filename));
		}
		// If there was an error opening the file for scanning, print the stack trace and exit with error code 1
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		//Initialize the board availabilities to everything being possible
		avail = new boolean[SIZE][SIZE][SIZE];
		for (int i = 0; i < avail.length; i++)
			for (int j = 0; j < avail[0].length; j++)
				for (int k = 0; k < avail[0][0].length; k++)
					avail[i][j][k] = true;
		
		//Read in the board from file
		//IMPORTANT NOTE: Correct input is assumed.  Values 1-9 are accepted, and any value less than or equal to zero represents blank.
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board[0].length; j++)
			{
				board[i][j] = sc.nextInt();
				if (board[i][j] > 0)
				{
					//Increment the number of completed spaces if the board was initialized to a value >= 0
					correct++;
					//For that space's availability array, set all values to false
					for (int k = 0; k < SIZE; k++)
						avail[i][j][k] = false;
				}
			}
	}
	
	/**
	 * Getter method
	 * @return the current pass number
	 */
	public int getPass()
	{
		return pass;
	}
	
	/**
	 * Update board possibilities by calling update() on each point.
	 */
	public void updateAll()
	{
		//Increment the pass counter to show them how many passes the algorithm took to complete the board
		pass++;
		System.out.println("Starting pass " + pass)
		for (int i = 0; i < SIZE; i++)
			for (int j = 0; j < SIZE; j++)
				update(i, j);
	}
	
	/**
	 * Update the point at (row, col).
	 * The method calls updateRow(), updateColumn(), and updateSquare(),
	 * then determines if the point at (row, col) has only one possibility.
	 * If it does, the game board will be updated.
	 * @param row the row of the point to update
	 * @param col the column of the point to update
	 */
	public void update(int row, int col)
	{
		updateRow(row, col);
		updateColumn(row, col);
		updateSquare(row, col);
		
		// Initialize the number of possible values for a point on this iteration to SIZE
		int possible = SIZE;
		int openVal;
		
		//For each value between 0 and SIZE...
		for (int val = 0; val < SIZE; val++)
		{
			//If the avail array for this point at that value is false, decrement the number of possible values for the point
			if (avail[row][col][val] == false)
				possible--;
			//Otherwise, this could possibly be the one 'open value' for that point
			else
				openVal = val;
		}
		
		//If there is only one possible value for this point, set it to the point and increment the number of completed squares
		if (possible == 1 && board[row][col] <= 0)
		{
			board[row][col] = openVal + 1;
			correct++;
		}
	}
	
	/**
	 * If the value at (row, col) is completed already, remove it as a possibility from other elements in the same row.
	 * @param row the row of the point to check
	 * @param col the column of the point to check
	 */
	private void updateRow(int row, int col)
	{
		int val = board[row][col];
		if (val <= 0) //The space is empty, return
			return;
		for (int j = 0; j < avail[0].length; j++)
			if (j != col)
				avail[row][j][val - 1] = false;
	}
	
	/**
	 * If the value at (row, col) is completed already, remove it as a possibility from other elements in the same column.
	 * @param row the row of the point to check
	 * @param col the column of the point to check
	 */
	private void updateColumn(int row, int col)
	{
		int val = board[row][col];
		if (val <= 0) //The space is empty, return
			return;
		for (int i = 0; i < avail.length; i++)
			if (i != row)
				avail[i][col][val - 1] = false;
	}
	
	/**
	 * If the value at (row, col) is completed already, remove it as a possibility from other elements in the same square.
	 * @param row the row of the point to check
	 * @param col the column of the point to check
	 */
	private void updateSquare(int row, int col)
	{
		int val = board[row][col];
		if (val <= 0) //The space is empty, return
			return;
		
		//The next two lines determine which "square" the point is in
		int squareRow = row / SQUARE_SIZE * SQUARE_SIZE;
		int squareCol = col / SQUARE_SIZE * SQUARE_SIZE;
		
		for (int i = squareRow; i < squareRow + SQUARE_SIZE; i++)
			for (int j = squareCol; j < squareCol + SQUARE_SIZE; j++)
				if (i != row && j != col)
					avail[i][j][val - 1] = false;
	}
	
	/**
	 * Print how many spaces of the board are currently complete.
	 * The format is the percent of the squares filled in rounded to the nearest integer.
	 * @return whether or not the board has been completed
	 */
	private boolean isFinished()
	{
		//Print out how complete the board is as a percentage
		double percent = ((double)correct) / 81 * 100;
		System.out.printf("%2.f%% complete.\n", percent);
		
		//First call method isComplete to show the user how close the solver is to finishing the board
		done = isComplete();
		
		//If the board is complete or the solver has iterated more times than its limit, return true
		if (done || pass > limit)
			return true;
		
		//Otherwise, return false
		return false;
	}
	
	/**
	 * If 81 squares have been marked correct, the board is complete.
	 * @return whether or not the board has been completed
	 */
	public boolean isComplete()
	{
		return correct == 81;
	}
	
	/**
	 * Prints the board to standard output
	 */
	public void printBoard()
	{
		for (int i = 0; i < board.length; i++)
		{
			for (int j = 0; j < board.length; j++)
				System.out.print(board[i][j] + " ");
			System.out.println();
		}
	}
	
	public static void main(String[] args)
	{
		//Define the maximum number of iterations the solver will take to solve the board
		int MAX_TRIES = 1000
	
		//If the user did not provide an input file, exit
		if (args.length < 1)
		{
			System.out.println("Usage: java Solver <inFile>");
			System.exit(1);
		}
		
		//Create an instance of a sudoku solver with the given input file name
		//The sudoku solver will only go through MAX_TRIES iterations to solve the board
		Solver solver = new Solver(args[0], MAX_TRIES);
		
		//While the game is not complete, update all possibilities
		while (!solver.isFinished())
			solver.updateAll();
		
		//If the solver was able to complete the board (instead of just iterating more than the given limit)
		if (solver.isComplete())
		{
			//Tell the user the board was completed
			System.out.println("Board complete!");
			System.out.println("Board completed in " + solver.getPass() + " passes.");
			
			//Print out the completed game board
			solver.printBoard();
		}
		else // No solution was found in time
		{
			//Tell the user a solution could not be found
			System.out.println("The board could not be solved in the given number of iterations.")
			System.out.println("This is as far as the solver was able to calculate:")
			
			//Print out how far the solver was able to calculate
			solver.printBoard();
		}
	}
}
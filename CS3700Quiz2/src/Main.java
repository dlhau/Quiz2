import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * David Hau
 * CS3700
 * 10/10/2018
 */

public class Main
{
	private static final Object CELL_LOCK = new Object();
	private static final Object UPDATE_LOCK = new Object();
	private static AtomicBoolean wait = new AtomicBoolean(false);
	private static int[][] homeGrid;
	
	//private static ArrayList<Thread> cell = new ArrayList<>();
	
	public static class GridCell implements Runnable
	{
		private int alive;
		private Phaser phaser;
		private int[][] grid;
		private int M, N, posL, posW;
		
		public GridCell(Phaser phaser, int[][] grid, int posL, int posW)
		{
			
			this.alive = ThreadLocalRandom.current().nextInt(0, 2);
			this.phaser = phaser;
			this.grid = grid;
			M = grid.length;
			N = grid[0].length;
			this.posL = posL;
			this.posW = posW;
			this.grid[posL][posW] = alive;
			
			synchronized(CELL_LOCK)
			{
				homeGrid[posL][posW] = alive;
				CELL_LOCK.notifyAll();
			}
			
			System.out.println("Current Cell[" + posL + "][" + posW + "] is " + alive);
			
		}
		
		public void run()
		{
			phaser.register();
			
			// Source: https://www.geeksforgeeks.org/program-for-conways-game-of-life/
            // Loop through every cell
			boolean cellChange = false;
            for(int l = 1; l < M - 1; l++)
            {
                for(int m = 1; m < N - 1; m++) 
                { 
                    // finding alive neighbors
                    int aliveNeighbors = 0; 
                    for (int i = -1; i <= 1; i++)
                    {
                    	for (int j = -1; j <= 1; j++)
                    	{
                    		aliveNeighbors += grid[l + i][m + j]; 
                    	}
                    }
                    
                    aliveNeighbors -= grid[l][m]; 
      
                    // Implementing the Rules of Life 
      
                    // Cell is lonely and dies
                    if ((grid[l][m] == 1) && (aliveNeighbors < 2))
                    {
                    	alive = 0;
                    	grid[l][m] = 0;
                    	cellChange = true;
                    }
                    // Cell dies due to over population 
                    else if ((grid[l][m] == 1) && (aliveNeighbors > 3))
                    {
                    	alive = 0;
                    	grid[l][m] = 0;
                    	cellChange = true;
                    }
                    // A new cell is born 
                    else if ((grid[l][m] == 0) && (aliveNeighbors == 3))
                    {
                    	alive = 1;
                        grid[l][m] = 1;
                        cellChange = true;
                    }
                    // Remains the same, do nothing
                }
            }
            
            if(cellChange)
            {
            	System.out.println("Cell[" + posL + "][" + posW + "] CHANGED to " + alive);
            }
            else
            {
            	System.out.println("Cell[" + posL + "][" + posW + "] is STAYING " + alive);
            }
            homeGrid = grid; // Update home grid
            
            System.out.println("Next Cell[" + posL + "][" + posW + "] will be " + alive);
			
			phaser.arriveAndDeregister();
		}
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		int M, N;
		Phaser phaser = new Phaser();
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter integer for M: ");
		M = keyboard.nextInt();
		System.out.println("M is: " + M);
		System.out.println();
		
		System.out.print("Enter integer for N: ");
		N = keyboard.nextInt();
		System.out.println("N is: " + N);
		System.out.println();
		
		homeGrid = new int[M][N];
		
		for(int i = 0; i < homeGrid.length; i++)
		{
			for(int j = 0; j < homeGrid[0].length; j++)
			{
				homeGrid[i][j] = 0;
			}
		}
		
		Thread[][] threads = new Thread[M][N];
		
		
		for(int i = 0; i < homeGrid.length; i++)
		{
			for(int j = 0; j < homeGrid[0].length; j++)
			{
				wait.set(false);
				threads[i][j] = new Thread(new GridCell(phaser, homeGrid, i, j));
				threads[i][j].start();
			}
		}
		
	}
}

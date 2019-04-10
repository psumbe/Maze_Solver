package hwMaze;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * This file needs to hold your solver to be tested. 
 * You can alter the class to extend any class that extends MazeSolver.
 * It must have a constructor that takes in a Maze.
 * It must have the solve() method that returns the datatype List<Direction>
 *   which will either be a reference to a list of steps to take or will
 *   be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver
{
	
	private ExecutorService servicePool;
	
    public StudentMTMazeSolver(Maze maze)
    {
        super(maze);
    }

    public List<Direction> solve()
    {
        // TODO: Implement your code here
    	
		List<Direction> directions = null;
		int processors = Runtime.getRuntime().availableProcessors();
		//System.out.println(processors);
		servicePool = Executors.newFixedThreadPool(processors);
    	LinkedList<DFS> tasks = new LinkedList<DFS>();
		List<Future<List<Direction>>> futures = new LinkedList<Future<List<Direction>>>();

		
		
		Choice firstChoice;
		try {
			firstChoice = firstChoice(maze.getStart());
			int size = firstChoice.choices.size();
			for(int index = 0; index < size; index++){
				Choice currChoice = follow(firstChoice.at, firstChoice.choices.peek());
				
				tasks.add(new DFS(currChoice, firstChoice.choices.pop()));
			}
			futures = servicePool.invokeAll(tasks);
		} catch (SolutionFound e2) {
			e2.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("InterruptedException in StudentMTMazeSolver");
		}
		
		
		servicePool.shutdown();
		
		for(Future<List<Direction>> direction : futures){
			try {
				
				if(direction.get() != null){
					directions = direction.get();
					
				}
			} catch (InterruptedException e) {
				System.out.println("InterruptedException in StudentMTMazeSolver 2");
			} catch (ExecutionException e) {
				System.out.println("ExecutionException in StudentMTMazeSolver");
			}
		}
		
		return directions;

    }
    
    private class DFS implements Callable<List<Direction>>{
		Choice head;
		Direction choiceDir;
		
		public DFS(Choice head, Direction choiceDir){
			this.head = head;
			this.choiceDir = choiceDir;
			
		}

		@Override
		public List<Direction> call() {
			// TODO Auto-generated method stub
			LinkedList<Choice> choiceStack = new LinkedList<Choice>();
	        Choice ch;
	        try
	        {
	            choiceStack.push(head);
	            
	            while (!choiceStack.isEmpty())
	            {
	                ch = choiceStack.peek();
	                if (ch.isDeadend())
	                {
	                    // backtrack.
	                    choiceStack.pop();
	                    if (!choiceStack.isEmpty()) choiceStack.peek().choices.pop();
	                    continue;
	                }
	                choiceStack.push(follow(ch.at, ch.choices.peek()));
	            }
	            // No solution found.
	            return null;
	        }
	        catch (SolutionFound e)
	        {
	            Iterator<Choice> iter = choiceStack.iterator();
	            LinkedList<Direction> solutionPath = new LinkedList<Direction>();
	            while (iter.hasNext())
	            {
	            	ch = iter.next();
	                solutionPath.push(ch.choices.peek());
	            }
	            solutionPath.push(choiceDir);
	            if (maze.display != null){
	            	
	            	maze.display.updateDisplay();
	            }
	            return pathToFullPath(solutionPath);
	        }
	        
		}

	}
    
}

package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Stack;
import java.util.Random;

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
	
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;
	
	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
	    }
		
	}
	
	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}
	
	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();
	
	// Here you can define your variables!
	public boolean foundEastWall = false;
	public boolean foundCorner = false;
	public Stack<Integer> queuedActions = new Stack<Integer>();
	public boolean moveForward = false;
	public int iterationCounter = 1000;
	public MyAgentState state = new MyAgentState();
	public int nextTurn = state.EAST;
	
	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
	
	public Action rightTurn(){
		state.agent_last_action = state.ACTION_TURN_RIGHT;
	    state.agent_direction = ((state.agent_direction + 1) % 4);
		return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
	}

	public Action goForward(){
		state.agent_last_action = state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}

	public Action leftTurn(){
		state.agent_last_action = state.ACTION_TURN_LEFT;
	    state.agent_direction = ((state.agent_direction - 1 +4) % 4);
		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
	}

	public Action doQueuedActions(){
		switch (queuedActions.pop()) 
		{
			case MyAgentState.EAST:
				return rightTurn();
			case MyAgentState.WEST:
				return leftTurn();
			case MyAgentState.NORTH:
				return goForward();
				//something went wrong if default is ran
			default:
				return null;
		}
	}

	public Action prepareUTurn(int agent_direction){
		if (agent_direction == state.EAST){
			queuedActions.push(MyAgentState.WEST);
			queuedActions.push(MyAgentState.NORTH);
			queuedActions.push(MyAgentState.WEST);
		}
		else
		{
			queuedActions.push(MyAgentState.EAST);
			queuedActions.push(MyAgentState.NORTH);
			queuedActions.push(MyAgentState.EAST);
		}
		return doQueuedActions();
	}


	@Override
	public Action execute(Percept percept) {
		
		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}
		
    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!
    	    	
    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);
    	
		
	    iterationCounter--;
	    if (iterationCounter==0)
	    	return NoOpAction.NO_OP;

	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);
	    
	    // State update based on the percept value and the last action
	    state.updatePosition((DynamicPercept)percept);
	    if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
	    }

	    if (dirt){
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
	    		//System.out.println("hittat smuts");
	    }
	    else
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);

	    state.printWorldDebug();

	    if(!foundEastWall)
	    {
	    	switch(state.agent_direction)
	    	{
	    		case MyAgentState.NORTH:
	    			return rightTurn();    			
	    		case MyAgentState.EAST:
	    			if(bump)
	    			{
	    				foundEastWall = true;
		    			return rightTurn();
	    			}
	    			else
	    			{
		    			return goForward();
	    			}		
	    		case MyAgentState.SOUTH:
	    			return leftTurn();		
	    		case MyAgentState.WEST:
	    			return rightTurn();
	    	}
	    }
	    // found east wall, look for south east corner
	    else if (!foundCorner && foundEastWall)
	    {
	    	if (bump)
	    	{
	    		foundCorner = true;
	    		return rightTurn();
	    	}
	    	else
	    	{
	    		return goForward();
	    	}
	    }

	  //  System.out.println("Selection based on percept");
	    // Next action selection based on the percept value
	  	if (dirt)
	    {
	    	System.out.println("DIRT -> choosing SUCK action!");
	    	state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    } 
	    else
	    {
	    	System.out.println("vi kollar åt  " + state.agent_direction);
	    	if(queuedActions.size() > 0)
	    	{
	    		System.out.println("doing queued actions, size is "+queuedActions.size() );
	    		return doQueuedActions();
	    	} 
	    	if (bump)
	    	{
	    		System.out.println("hitta bump");

	    		switch (state.agent_direction)
	    		{
	    			case MyAgentState.NORTH:
	    				if(home)
	    				{
	    					return rightTurn();
	    				}
	    				else
	    				{
	    					return leftTurn();
	    				}
			    	case MyAgentState.EAST:
			    		return prepareUTurn(MyAgentState.EAST);
	    			//found a west wall, we could either be home or in the middle of the snaking back up to home.
	    			case MyAgentState.WEST:
	    				if (home)
	    				{
	    					return NoOpAction.NO_OP;
	    				}
	    				else 
	    				{
	    					return prepareUTurn(MyAgentState.WEST);
	    					
	    				}
	    		}
	    	}
	    	/*else
	    	{	
	    		if(state.agent_last_action == state.ACTION_MOVE_FORWARD)
	    		{
	    			if(state.agent_direction == state.NORTH)
	    			{
		    			if(nextTurn == state.ACTION_TURN_LEFT)
		    			{
		    				state.agent_last_action = state.ACTION_TURN_LEFT;
		    				state.agent_direction = state.WEST;
		    				return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		    			}
		    				
		    			else
		    			{
		    				state.agent_last_action = state.ACTION_TURN_LEFT;
		    				state.agent_direction = state.WEST;
		    				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;	
		    			}
	    			}
	    		}
	    		else
	    			{
						state.agent_last_action=state.ACTION_MOVE_FORWARD;
	    				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	    			}
	    		}*/
	    }
	    state.agent_last_action=state.ACTION_MOVE_FORWARD;
	    return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;	
	}
}

public class MyVacuumAgent extends AbstractAgent {
    public MyVacuumAgent() {
    	super(new MyAgentProgram());
	}
}

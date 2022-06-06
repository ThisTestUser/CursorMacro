package com.thistestuser.cursormacro.player;

import java.awt.Robot;
import java.util.Random;
import java.util.TreeSet;

public abstract class Player
{
	public TreeSet<Integer> mousePress = new TreeSet<>();
	public TreeSet<Integer> keyPress = new TreeSet<>();
	public long startTime;
	public long expectedTime;
	
	/**
	 * Sets up the player by initializing listeners. This is only run once and at the beginning.
	 */
	public abstract void setup();
	
	/**
	 * Starts the instruction player. This method should start a separate thread.
	 */
	public abstract void start(boolean unpress);
	
	/**
	 * Stops the instruction player. This should send a signal to stop the thread.
	 */
	public abstract void stop();
	
	/**
	 * Check if the thread has terminated yet. If this is true, nothing is allowed to be run.
	 */
	public abstract boolean isRunning();
	
	/**
	 * Returns if the execution should stop. This is used to exit from instructions that may run forever.
	 */
	public abstract boolean shouldStop();

	public void registerMouse(boolean press, int button)
	{
		if(press)
			mousePress.add(button);
		else
			mousePress.remove(button);
	}
	
	public void registerKey(boolean press, int button)
	{
		if(press)
			keyPress.add(button);
		else
			keyPress.remove(button);
	}
	
	public void unpress(Robot robot)
	{
		for(Integer i : mousePress)
			robot.mouseRelease(i);
		for(Integer i : keyPress)
			robot.keyRelease(i);
	}
	
	/**
	 * Returns if there are currently instructions compiled here.
	 */
	public abstract boolean hasInstructions();
	
	/**
	 * Compiles with the code from the text pane.
	 */
	public abstract void compile(String instrs);
	
	/**
	 * Compiles the instruction list with randomization. Returns the string representation of the randomized result.
	 */
	public abstract String randomize(String instrs, Random random, long rndDelay, float maxPercent, boolean randomLoc, boolean removeData);
	
	/**
	 * Clears all compiled instructions.
	 */
	public abstract void clearInstructions();
	
	/**
	 * Gets the string representation to be displayed in the combo box.
	 */
	public abstract String getStringRepr();
	
	@Override
	public String toString()
	{
		return getStringRepr();
	}
}

package com.thistestuser.cursormacro.player;

import java.awt.Robot;
import java.util.Random;
import java.util.TreeSet;

public abstract class Player implements Runnable
{
	public TreeSet<Integer> mousePress = new TreeSet<>();
	public TreeSet<Integer> keyPress = new TreeSet<>();
	public long startTime;
	public long expectedTime;
	public boolean unpress;
	
	/**
	 * Sets up the player by initializing listeners. This is only run once and at the beginning.
	 */
	public abstract void setup();
	
	/**
	 * Runs the instruction player. This method should run in a separate thread.
	 */
	public abstract void run();

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
	 * Compiles the instruction list with randomization.
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

package com.thistestuser.cursormacro.instr;

import java.awt.Robot;

import com.thistestuser.cursormacro.player.Player;

public abstract class Instruction
{
	/**
	 * Parses the arguments. Should only happen once!
	 * @param args The arguments INCLUDING the instruction name (start reading at index 1!)
	 */
	public abstract void parseArguments(String[] args);
	
	/**
	 * Executes the instruction, which should be done after parsing arguments.
	 * This should add to player.expectedTime if the expected runtime is not 0 (delay is involved).
	 */
	public abstract void executeInstruction(Player player, Robot robot);
	
	/**
	 * Returns the instruction as string format (printing out arguments).
	 * This should match the input given at parseArguments.
	 */
	public abstract String asString();
}

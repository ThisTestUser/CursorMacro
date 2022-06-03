package com.thistestuser.cursormacro.instr;

import java.awt.Robot;

import com.thistestuser.cursormacro.player.Player;

public class KeyType extends Instruction
{
	private int action;
	private int key;
	
	public KeyType() {}
	
	public KeyType(int action, int key)
	{
		this.action = action;
		this.key = key;
	}
	
	@Override
	public void parseArguments(String[] args)
	{
		if(args.length != 3)
			throw new IllegalArgumentException("KEY requires two arguments");
		action = Integer.parseInt(args[1]);
		if(action != 0 && action != 1)
			throw new IllegalArgumentException("First argument must be 0 (press) or 1 (release)");
		key = Integer.parseInt(args[2]);
	}

	@Override
	public void executeInstruction(Player player, Robot robot)
	{
		if(action == 0)
		{
			robot.keyPress(key);
			player.registerKey(true, key);
		}else
		{
			robot.keyRelease(key);
			player.registerKey(false, key);
		}
	}

	@Override
	public String asString()
	{
		return "KEY " + action + " " + key;
	}
}

package com.thistestuser.cursormacro.instr;

import java.awt.Robot;

import com.thistestuser.cursormacro.player.Player;

public class MouseWheel extends Instruction
{
	private int scroll;
	
	public MouseWheel() {}
	
	public MouseWheel(int scroll)
	{
		this.scroll = scroll;
	}
	
	@Override
	public void parseArguments(String[] args)
	{
		if(args.length != 2)
			throw new IllegalArgumentException("MOUSEWHEEL only needs one argument");
		scroll = Integer.parseInt(args[1]);
	}

	@Override
	public void executeInstruction(Player player, Robot robot)
	{
		robot.mouseWheel(scroll);
	}

	@Override
	public String asString()
	{
		return "MOUSEWHEEL " + scroll;
	}
}

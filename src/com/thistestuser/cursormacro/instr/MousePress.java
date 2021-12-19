package com.thistestuser.cursormacro.instr;

import java.awt.event.MouseEvent;

import com.thistestuser.cursormacro.Player;

import java.awt.Robot;

public class MousePress extends Instruction
{
	private int action;
	private int button;
	
	public MousePress() {}
	
	public MousePress(int action, int button)
	{
		this.action = action;
		this.button = button;
	}
	
	@Override
	public void parseArguments(String[] args)
	{
		if(args.length != 3)
			throw new IllegalArgumentException("MOUSEPRESS requires two arguments");
		action = Integer.parseInt(args[1]);
		if(action != 0 && action != 1)
			throw new IllegalArgumentException("First argument must be 0 (press) or 1 (release)");
		button = Integer.parseInt(args[2]);
		if(button < 1)
			throw new IllegalArgumentException("Invaild button");
	}

	@Override
	public void executeInstruction(Player player, Robot robot)
	{
		int realButton = MouseEvent.getMaskForButton(button);
		if(action == 0)
		{
			robot.mousePress(realButton);
			player.registerMouse(true, realButton);
		}else
		{
			robot.mouseRelease(realButton);
			player.registerMouse(false, realButton);
		}
	}

	@Override
	public String asString()
	{
		return "MOUSEPRESS " + action + " " + button;
	}
}

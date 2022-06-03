package com.thistestuser.cursormacro.instr;

import java.awt.Robot;

import com.thistestuser.cursormacro.player.Player;

public class Delay extends Instruction
{
	private int delay;
	private int offset;
	
	public Delay() {}
	
	public Delay(int delay)
	{
		this.delay = delay;
	}
	
	@Override
	public void parseArguments(String[] args)
	{
		if(args.length < 2 || args.length > 3)
			throw new IllegalArgumentException("DELAY can only have 1 or 2 arg(s)");
		delay = Integer.parseInt(args[1]);
		if(args.length == 3)
			offset = Integer.parseInt(args[2]);
		if(delay + offset < 0)
			throw new IllegalArgumentException("Negative delay");
	}

	@Override
	public void executeInstruction(Player player, Robot robot)
	{
		try
		{
			Thread.sleep(Math.max(0, delay + offset - (System.currentTimeMillis() - player.expectedTime)));
		}catch(InterruptedException e)
		{
		}
		player.expectedTime += delay + offset;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	@Override
	public String asString()
	{
		return "DELAY " + delay + (offset == 0 ? "" : " " + offset);
	}
}

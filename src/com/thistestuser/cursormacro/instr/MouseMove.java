package com.thistestuser.cursormacro.instr;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;

import com.thistestuser.cursormacro.CursorMacro;
import com.thistestuser.cursormacro.Player;

public class MouseMove extends Instruction
{
	private int mouseX;
	private int mouseY;
	private int tolX;
	private int tolY;
	private int offX;
	private int offY;
	
	private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	
	public MouseMove() {}
	
	public MouseMove(int mouseX, int mouseY)
	{
		this.mouseX = Math.min(Math.max(0, mouseX), SCREEN_SIZE.width - 1);
		this.mouseY = Math.min(Math.max(0, mouseY), SCREEN_SIZE.height - 1);
	}

	@Override
	public void parseArguments(String[] args)
	{
		if(args.length != 3 && args.length != 5 && args.length != 7)
			throw new IllegalArgumentException("MOUSEMOVE can only have 3, 5, or 7 args");
		mouseX = Math.min(Math.max(0, Integer.parseInt(args[1])), SCREEN_SIZE.width - 1);
		mouseY = Math.min(Math.max(0, Integer.parseInt(args[2])), SCREEN_SIZE.height - 1);
		if(args.length > 3)
		{
			tolX = Integer.parseInt(args[3]);
			tolY = Integer.parseInt(args[4]);
			if(tolX < 0 || tolY < 0)
				throw new IllegalArgumentException("Negative tolerance values are not allowed");
			if(args.length > 5)
			{
				offX = Integer.parseInt(args[5]);
				offY = Integer.parseInt(args[6]);
			}
		}
	}

	@Override
	public void executeInstruction(Player player, Robot robot)
	{
		int x = mouseX + offX;
		int y = mouseY + offY;
		int count = 0;
		Point loc = CursorMacro.getMouseLocation();
		while((Math.abs(loc.x - x) > tolX || Math.abs(loc.y - y) > tolY) && count < 10)
		{
			CursorMacro.setMouseLocation(x, y);
			loc = CursorMacro.getMouseLocation();
			count++;
		}
	}
	
	public int getMouseX()
	{
		return mouseX;
	}
	
	public int getMouseY()
	{
		return mouseY;
	}
	
	public int getToleranceX()
	{
		return tolX;
	}
	
	public int getToleranceY()
	{
		return tolY;
	}
	
	public void setOffsetX(int offsetX)
	{
		offX = offsetX;
	}
	
	public void setOffsetY(int offsetY)
	{
		offY = offsetY;
	}

	@Override
	public String asString()
	{
		boolean seven = offX != 0 || offY != 0;
		boolean five = seven || tolX != 0 || tolY != 0;
		return "MOUSEMOVE " + mouseX + " " + mouseY
			+ (five ? " " + tolX + " " + tolY + (seven ? " " + offX + " " + offY : "") : "");
	}
}

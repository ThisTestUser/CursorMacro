package com.thistestuser.cursormacro.instr;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import com.thistestuser.cursormacro.Player;

public class MouseMove extends Instruction
{
	private int mouseX;
	private int mouseY;
	private int tolX;
	private int tolY;
	private int offX;
	private int offY;
	
	public MouseMove() {}
	
	public MouseMove(int mouseX, int mouseY)
	{
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	@Override
	public void parseArguments(String[] args)
	{
		if(args.length != 3 && args.length != 5 && args.length != 7)
			throw new IllegalArgumentException("MOUSEMOVE can only have 3, 5, or 7 args");
		mouseX = Integer.parseInt(args[1]);
		mouseY = Integer.parseInt(args[2]);
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
		int tempX = x;
		int tempY = y;
		int count = 0;
		int moveX = x * 4/5;
		int moveY = y * 4/5;
		robot.mouseMove(0, 0);
		robot.mouseMove(x, y);
		Point loc = MouseInfo.getPointerInfo().getLocation();
		while((Math.abs(loc.x - x) > tolX || Math.abs(loc.y - y) > tolY) && count < 25)
		{
			if(loc.x > x)
				tempX--;
			else if(loc.x < x)
				tempX++;
			if(loc.y > y)
				tempY--;
			else if(loc.y < y)
				tempY++;
			moveX = tempX * 4/5;
			moveY = tempY * 4/5;
			robot.mouseMove(0, 0);
			robot.mouseMove(moveX, moveY);
			loc = MouseInfo.getPointerInfo().getLocation();
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

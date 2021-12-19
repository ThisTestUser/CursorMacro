package com.thistestuser.cursormacro;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.thistestuser.cursormacro.instr.Instruction;

public class Player implements Runnable
{
	private List<Instruction> instructions;
	public TreeSet<Integer> mousePress = new TreeSet<>();
	public TreeSet<Integer> keyPress = new TreeSet<>();
	public CursorMacro parent;
	private long startTime;
	public long expectedTime;
	public boolean unpress;
	
	public Player(List<Instruction> instructions, CursorMacro parent, boolean unpress)
	{
		this.parent = parent;
		this.instructions = new ArrayList<>(instructions);
		this.unpress = unpress;
	}
	
	@Override
	public void run()
	{
		startTime = expectedTime = System.currentTimeMillis();
		try
		{
			for(int i = 0; i < instructions.size(); i++)
			{
				if(!parent.playing)
					break;
				Instruction instr = instructions.get(i);
				parent.stateLbl.setText("State: Executing " + (i + 1) + "/" + instructions.size());
				instr.executeInstruction(this, parent.robot);
			}
			long timeNow = System.currentTimeMillis();
			if(unpress)
			{
				for(Integer i : mousePress)
					parent.robot.mouseRelease(i);
				for(Integer i : keyPress)
					parent.robot.keyRelease(i);
			}
			if(parent.playing)
				parent.stateLbl.setText("State: Execution done in " + (timeNow - startTime) + " MS");
		}catch(Exception e)
		{
			if(parent.playing)
				parent.stateLbl.setText("State: Execution failed " + e.getClass().getName() + " (" + e.getMessage() + ")");
		}
	}
	
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
}

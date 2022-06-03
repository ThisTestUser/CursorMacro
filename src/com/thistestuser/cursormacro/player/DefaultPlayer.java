package com.thistestuser.cursormacro.player;

import java.util.Random;

import com.thistestuser.cursormacro.CursorMacro;
import com.thistestuser.cursormacro.instr.Instruction;
import com.thistestuser.cursormacro.instr.InstructionList;

public class DefaultPlayer extends Player
{
	private InstructionList list;
	public CursorMacro parent;
	
	public DefaultPlayer(CursorMacro parent)
	{
		list = new InstructionList();
		this.parent = parent;
	}
	
	@Override
	public void setup()
	{}
	
	@Override
	public void run()
	{
		startTime = expectedTime = System.currentTimeMillis();
		try
		{
			for(int i = 0; i < list.getInstructions().size(); i++)
			{
				if(!parent.playing)
					break;
				Instruction instr = list.getInstructions().get(i);
				parent.stateLbl.setText("State: Executing " + (i + 1) + "/" + list.getInstructions().size());
				instr.executeInstruction(this, parent.robot);
			}
			long timeNow = System.currentTimeMillis();
			if(unpress)
				unpress(parent.robot);
			if(parent.autoStopOption.isSelected())
			{
				parent.stopButton.doClick();
				return;
			}
			if(parent.playing)
				parent.stateLbl.setText("State: Execution done in " + (timeNow - startTime) + " MS");
		}catch(Exception e)
		{
			if(parent.playing)
				parent.stateLbl.setText("State: Execution failed " + e.getClass().getName() + " (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public boolean hasInstructions()
	{
		return !list.getInstructions().isEmpty();
	}

	@Override
	public void compile(String instrs)
	{
		list.compile(instrs);
	}

	@Override
	public String randomize(String instrs, Random random, long rndDelay, float maxPercent, boolean randomLoc,
		boolean removeData)
	{
		return list.randomize(instrs, random, rndDelay, maxPercent, randomLoc, removeData);
	}

	@Override
	public void clearInstructions()
	{
		list.getInstructions().clear();
	}

	@Override
	public String getStringRepr()
	{
		return "Default";
	}
}

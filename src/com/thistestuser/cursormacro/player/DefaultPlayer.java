package com.thistestuser.cursormacro.player;

import java.util.Random;

import com.thistestuser.cursormacro.CursorMacro;
import com.thistestuser.cursormacro.instr.Instruction;
import com.thistestuser.cursormacro.instr.InstructionList;

public class DefaultPlayer extends Player
{
	private InstructionList list;
	private final CursorMacro parent;
	private Thread execution;
	private volatile boolean playing;
	
	public DefaultPlayer(CursorMacro parent)
	{
		list = new InstructionList();
		this.parent = parent;
	}
	
	@Override
	public void setup()
	{}
	
	@Override
	public void start(boolean unpress)
	{
		playing = true;
		execution = new Thread()
		{
			@Override
			public void run()
			{
				startTime = expectedTime = System.currentTimeMillis();
				try
				{
					for(int i = 0; i < list.getInstructions().size(); i++)
					{
						if(!playing)
							break;
						Instruction instr = list.getInstructions().get(i);
						parent.stateLbl.setText("State: Executing " + (i + 1) + "/" + list.getInstructions().size());
						instr.executeInstruction(DefaultPlayer.this, parent.robot);
					}
					long timeNow = System.currentTimeMillis();
					if(unpress)
						unpress(parent.robot);
					if(parent.autoStopOption.isSelected())
					{
						parent.stopButton.doClick();
						return;
					}
					if(playing)
						parent.stateLbl.setText("State: Execution done in " + (timeNow - startTime) + " MS");
				}catch(Exception e)
				{
					if(playing)
						parent.stateLbl.setText("State: Execution failed " + e.getClass().getName() + " (" + e.getMessage() + ")");
				}finally
				{
					if(!playing)
						parent.resetToIdleState();
				}
			}
		};
		execution.start();
	}
	
	@Override
	public void stop()
	{
		execution.interrupt();
		playing = false;
	}
	
	@Override
	public boolean isRunning()
	{
		return execution != null && execution.isAlive();
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

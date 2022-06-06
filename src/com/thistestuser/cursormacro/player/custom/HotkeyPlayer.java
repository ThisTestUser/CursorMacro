package com.thistestuser.cursormacro.player.custom;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jnativehook.GlobalScreen;

import com.thistestuser.cursormacro.CursorMacro;
import com.thistestuser.cursormacro.instr.Instruction;
import com.thistestuser.cursormacro.instr.InstructionList;
import com.thistestuser.cursormacro.player.Player;

/**
 * This module acts as a hotkey listener. When a key is typed, the corresponding instructions are executed.
 * Only one set of instructions are allowed to be executed at a time.
 */
public class HotkeyPlayer extends Player
{
	private Map<Integer, InstructionList> keyBindings = new HashMap<>();
	private Map<Integer, InstructionList> mouseBindings = new HashMap<>();
	private final CursorMacro parent;
	private HotkeyListener listener;
	private Thread execution;
	private volatile boolean listening;
	private boolean unpress;
	
	public HotkeyPlayer(CursorMacro parent)
	{
		this.parent = parent;
	}
	
	@Override
	public void setup()
	{
		listener = new HotkeyListener(this);
		GlobalScreen.addNativeKeyListener(listener);
		GlobalScreen.addNativeMouseListener(listener);
	}
	
	@Override
	public void start(boolean unpress)
	{
		listening = true;
		this.unpress = unpress;
		parent.stateLbl.setText("State: Listening for key/mouse inputs");
	}
	
	@Override
	public void stop()
	{
		if(isRunning())
			execution.interrupt();
		listening = false;
	}
	
	@Override
	public boolean isRunning()
	{
		return execution != null && execution.isAlive();
	}
	
	@Override
	public boolean shouldStop()
	{
		return !listening;
	}
	
	public void activateKeyMacro(int keyCode)
	{
		if(!listening || isRunning())
			return;
		
		if(!keyBindings.containsKey(keyCode))
			return;
		
		InstructionList list = keyBindings.get(keyCode);
		String key = KeyEvent.getKeyText(keyCode);
		
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
						if(!listening)
							break;
						Instruction instr = list.getInstructions().get(i);
						parent.stateLbl.setText("State: Executing key " + key
							+ " " + (i + 1) + "/" + list.getInstructions().size());
						instr.executeInstruction(HotkeyPlayer.this, parent.robot);
					}
					long timeNow = System.currentTimeMillis();
					if(unpress)
						unpress(parent.robot);
					if(listening)
						parent.stateLbl.setText("State: Execution of key " + key
							+ " done in " + (timeNow - startTime) + " MS");
				}catch(Exception e)
				{
					if(listening)
						parent.stateLbl.setText("State: Execution of key " + key + " failed "
							+ e.getClass().getName() + " (" + e.getMessage() + ")");
				}finally
				{
					if(!listening)
						parent.resetToIdleState();
				}
			}
		};
		execution.start();
	}
	
	public void activateMouseMacro(int button)
	{
		if(!listening || isRunning())
			return;
		
		if(!mouseBindings.containsKey(button))
			return;
		
		InstructionList list = mouseBindings.get(button);
		
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
						if(!listening)
							break;
						Instruction instr = list.getInstructions().get(i);
						parent.stateLbl.setText("State: Executing mouse button " + button
							+ " " + (i + 1) + "/" + list.getInstructions().size());
						instr.executeInstruction(HotkeyPlayer.this, parent.robot);
					}
					long timeNow = System.currentTimeMillis();
					if(unpress)
						unpress(parent.robot);
					if(listening)
						parent.stateLbl.setText("State: Execution of mouse button " + button
							+ " done in " + (timeNow - startTime) + " MS");
				}catch(Exception e)
				{
					if(listening)
						parent.stateLbl.setText("State: Execution of mouse button " + button + " failed "
							+ e.getClass().getName() + " (" + e.getMessage() + ")");
				}finally
				{
					if(!listening)
						parent.resetToIdleState();
				}
			}
		};
		execution.start();
	}

	@Override
	public boolean hasInstructions()
	{
		return !keyBindings.isEmpty() || !mouseBindings.isEmpty();
	}

	@Override
	public void compile(String instrs)
	{
		keyBindings.clear();
		mouseBindings.clear();
		
		String[] instrsSplit = instrs.split("\\r?\\n");
		int idx = 0;
		while(idx < instrsSplit.length)
		{
			String instr = instrsSplit[idx];
			idx++;
			if(instr.startsWith("//") || instr.trim().isEmpty())
				continue;
			
			String[] instrSplit = instr.split(" ");
			if(instrSplit.length != 3 || !instrSplit[0].equals("ASSIGN"))
				throw new IllegalArgumentException("Expected ASSIGN with 2 arguments at line " + (idx + 1));
			
			if(instrSplit[1].equals("key"))
			{
				int id = Integer.parseInt(instrSplit[2]);
				if(keyBindings.containsKey(id))
					throw new IllegalArgumentException("Duplicate key " + id + " assigned");
				
				String combined = "";
				int oldIdx = idx;
				while(idx < instrsSplit.length && !instrsSplit[idx].startsWith("ASSIGN"))
				{
					combined += instrsSplit[idx] + '\n';
					idx++;
				}
				InstructionList list = new InstructionList();
				list.compile(combined, oldIdx);
				
				if(list.getInstructions().isEmpty())
					throw new IllegalArgumentException("Key binding " + id + " has no instructions at line " + oldIdx);
				keyBindings.put(id, list);
			}else if(instrSplit[1].equals("mouse"))
			{
				int button = Integer.parseInt(instrSplit[2]);
				if(mouseBindings.containsKey(button))
					throw new IllegalArgumentException("Duplicate mouse button " + button + " assigned");
				
				String combined = "";
				int oldIdx = idx;
				while(idx < instrsSplit.length && !instrsSplit[idx].startsWith("ASSIGN"))
				{
					combined += instrsSplit[idx] + '\n';
					idx++;
				}
				InstructionList list = new InstructionList();
				list.compile(combined, oldIdx);
				
				if(list.getInstructions().isEmpty())
					throw new IllegalArgumentException("Mouse binding " + button + " has no instructions at line " + oldIdx);
				mouseBindings.put(button, list);
			}else
				throw new IllegalArgumentException("Expected ASSIGN key or ASSIGN mouse at line " + (idx + 1));
		}
	}

	@Override
	public String randomize(String instrs, Random random, long rndDelay, float maxPercent, boolean randomLoc,
		boolean removeData)
	{
		String builder = "";
		String[] instrsSplit = instrs.split("\\r?\\n");
		int idx = 0;
		while(idx < instrsSplit.length)
		{
			String instr = instrsSplit[idx];
			idx++;
			if(instr.startsWith("//") || instr.trim().isEmpty())
			{
				builder += instr + '\n';
				continue;
			}
			
			String[] instrSplit = instr.split(" ");
			if(instrSplit.length != 3 || !instrSplit[0].equals("ASSIGN"))
				throw new IllegalArgumentException("Expected ASSIGN with 2 arguments at line " + (idx + 1));
			
			if(instrSplit[1].equals("key") || instrSplit[1].equals("mouse"))
			{
				builder += instr + '\n';
				String combined = "";
				int oldIdx = idx;
				while(idx < instrsSplit.length && !instrsSplit[idx].startsWith("ASSIGN"))
				{
					combined += instrsSplit[idx] + '\n';
					idx++;
				}
				InstructionList list = new InstructionList();
				builder += list.randomize(combined, random, rndDelay, maxPercent, randomLoc, removeData, oldIdx);
			}else
				throw new IllegalArgumentException("Expected ASSIGN key or ASSIGN mouse at line " + (idx + 1));
		}
		return builder;
	}

	@Override
	public void clearInstructions()
	{
		keyBindings.clear();
		mouseBindings.clear();
	}

	@Override
	public String getStringRepr()
	{
		return "Hotkey";
	}
}

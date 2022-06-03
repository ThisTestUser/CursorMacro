package com.thistestuser.cursormacro.instr;

import java.awt.Robot;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.thistestuser.cursormacro.instr.custom.CustomModule;
import com.thistestuser.cursormacro.player.Player;

public class Custom extends Instruction
{
	public static final Map<String, Class<? extends CustomModule>> customInstrs = new HashMap<>();
	private CustomModule module;
	
	@Override
	public void parseArguments(String[] args)
	{
		if(args.length < 2)
			throw new IllegalArgumentException("CUSTOM needs at least 1 arg");
		Class<? extends CustomModule> clazz = customInstrs.get(args[1]);
		if(clazz == null)
			throw new IllegalArgumentException("Custom instruction name not found!");
		try
		{
			module = clazz.newInstance();
			module.initialize(args);
		}catch(InstantiationException | IllegalAccessException e)
		{
			throw new IllegalStateException("Reflection error while initializating module: " + e.getMessage());
		}
	}

	@Override
	public void executeInstruction(Player player, Robot robot)
	{
		module.execute(player, robot);
	}
	
	public boolean randomize(Random random, boolean removeData, long rndDelay, float maxPercent)
	{
		return module.randomize(random, removeData, rndDelay, maxPercent);
	}
	
	public boolean hasStaticRuntime()
	{
		return module.hasStaticRuntime();
	}
	
	public int getDelay()
	{
		return module.getDelay();
	}
	
	public int getOffset()
	{
		return module.getOffset();
	}

	@Override
	public String asString()
	{
		return "CUSTOM " + module.argsAsString();
	}
}

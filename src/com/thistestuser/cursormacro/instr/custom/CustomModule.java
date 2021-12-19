package com.thistestuser.cursormacro.instr.custom;

import java.awt.Robot;
import java.util.Random;

import com.thistestuser.cursormacro.Player;

public abstract class CustomModule
{
	public abstract void initialize(String[] args);
	
	public abstract void execute(Player player, Robot robot);
	
	public abstract boolean randomize(Random random, boolean removeData, long rndDelay, float maxPercent);
	
	public abstract boolean hasStaticRuntime();
	
	public abstract int getDelay();
	
	public abstract int getOffset();
	
	public abstract String argsAsString();
}

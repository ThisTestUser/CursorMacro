package com.thistestuser.cursormacro.instr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InstructionList
{
	private List<Instruction> instructions = new ArrayList<>();
	
	public void compile(String args) throws IllegalArgumentException
	{
		compile(args, 0);
	}
	
	public void compile(String args, int offset) throws IllegalArgumentException
	{
		String[] instrsSplit = args.split("\\r?\\n");
		for(int i = 0; i < instrsSplit.length; i++)
		{
			String instr = instrsSplit[i];
			if(instr.startsWith("//") || instr.trim().isEmpty())
				continue;
			String[] instrSplit = instr.split(" ");
			instructions.add(getInstrForString(instrSplit, i + offset + 1));
		}
	}
	
	public String randomize(String instrs, Random random, long rndDelay, float maxPercent, boolean randomLoc, boolean removeData)
	{
		return randomize(instrs, random, rndDelay, maxPercent, randomLoc, removeData, 0);
	}
	
	public String randomize(String instrs, Random random, long rndDelay, float maxPercent, boolean randomLoc, boolean removeData, int offset)
	{
		String builder = "";
		String[] instrsSplit = instrs.split("\\r?\\n");
		int runtime = 0;
		int runtimeNoOffset = 0;
		
		//Randomizes particular sections only
		boolean sectionRandomize = false;
		boolean isRandomizingSection = false;
		//Balance mode - When EndSectionRand is reached, last delay is used to keep the runtime of the block constant
		boolean shouldBalance = false;
		int sectionTotalOffset = 0;
		
		for(int i = 0; i < instrsSplit.length; i++)
		{
			String argsRaw = instrsSplit[i];
			int line = i + offset + 1;
			if(argsRaw.startsWith("//") || argsRaw.trim().isEmpty())
			{
				if(argsRaw.equals("// SectionRandomize=true"))
					sectionRandomize = true;
				if(argsRaw.equals("// SectionRandomize=false"))
				{
					if(isRandomizingSection)
						throw new IllegalArgumentException("Cannot end section randomizer when there is "
							+ "an active session not ended at line " + line);
					sectionRandomize = false;
				}
				if(sectionRandomize)
				{
					if(argsRaw.equals("// StartSectionRand"))
					{
						if(isRandomizingSection)
							throw new IllegalArgumentException("Cannot start section randomizer again at line "
								+ line);
						isRandomizingSection = true;
						shouldBalance = false;
						sectionTotalOffset = 0;
					}
					if(argsRaw.equals("// StartSectionRandBalance"))
					{
						if(isRandomizingSection)
							throw new IllegalArgumentException("Cannot start section randomizer again at line "
								+ line);
						isRandomizingSection = true;
						if(random == null)
							shouldBalance = false;
						else
							shouldBalance = true;
						sectionTotalOffset = 0;
					}
					if(argsRaw.equals("// EndSectionRand"))
					{
						isRandomizingSection = false;
						shouldBalance = false;
						sectionTotalOffset = 0;
					}
				}
				if(!argsRaw.startsWith("// Actual runtime after randomizer (MS):")
					&& !argsRaw.startsWith("// Runtime (MS):"))
					builder += argsRaw + '\n';
				continue;
			}
			String[] argsSplit = argsRaw.split(" ");
			Instruction instr = getInstrForString(argsSplit, line);
			if(sectionRandomize && !isRandomizingSection)
			{
				if(instr instanceof Delay)
				{
					Delay delay = (Delay)instr;
					int delayNum = delay.getDelay();
					runtime += delayNum + delay.getOffset();
					runtimeNoOffset += delayNum;
				}else if(instr instanceof Custom && ((Custom)instr).hasStaticRuntime())
				{
					Custom custom = (Custom)instr;
					int delayNum = custom.getDelay();
					runtime += delayNum + custom.getOffset();
					runtimeNoOffset += delayNum;
				}
				builder += argsRaw + '\n';
				continue;
			}
			if(instr instanceof Delay)
			{
				Delay delay = (Delay)instr;
				int delayNum = delay.getDelay();
				if(random == null)
				{
					if(removeData)
						delay.setOffset(0);
					runtime += delayNum + delay.getOffset();
					runtimeNoOffset += delayNum;
				}else
				{
					long actualRndDelay = Math.min(rndDelay, Math.round(delayNum * maxPercent / 100));
					int delayOffset = Math.round((random.nextFloat() - 0.5F) * actualRndDelay * 2);
					delay.setOffset(delayOffset);
					runtime += delayNum + delayOffset;
					runtimeNoOffset += delayNum;
					if(sectionRandomize && isRandomizingSection && shouldBalance)
						sectionTotalOffset += delayOffset;
				}
			}else if(instr instanceof MouseMove)
			{
				MouseMove move = (MouseMove)instr;
				if(!randomLoc || random == null)
				{
					if(removeData)
					{
						move.setOffsetX(0);
						move.setOffsetY(0);
					}
				}else
				{
					if(move.getToleranceX() > 0)
						move.setOffsetX(Math.round(random.nextFloat() * move.getToleranceX()));
					if(move.getToleranceY() > 0)
						move.setOffsetY(Math.round(random.nextFloat() * move.getToleranceY()));
				}
			}else if(instr instanceof Custom)
			{
				Custom custom = (Custom)instr;
				custom.randomize(random, removeData, rndDelay, maxPercent);
				if(custom.hasStaticRuntime())
				{
					int delayNum = custom.getDelay();
					runtime += delayNum + custom.getOffset();
					runtimeNoOffset += delayNum;
					if(random != null && sectionRandomize && isRandomizingSection && shouldBalance)
						sectionTotalOffset += custom.getOffset();
				}
			}
			//Balance randomizer (if option is true)
			if(sectionRandomize && isRandomizingSection && shouldBalance)
			{
				int i2 = i + 1;
				while(i2 < instrsSplit.length)
				{
					String instrRaw2 = instrsSplit[i2];
					if(instrRaw2.startsWith("//") || instrRaw2.trim().isEmpty())
					{
						if(instrRaw2.equals("// EndSectionRand"))
						{
							if(!(instr instanceof Delay))
								throw new IllegalArgumentException("Cannot balance if delay does not precede "
									+ "end section at line " + (i2 + 1));
							sectionTotalOffset -= ((Delay)instr).getOffset();
							runtime -= ((Delay)instr).getOffset();
							if(((Delay)instr).getDelay() - sectionTotalOffset < 0)
								throw new IllegalArgumentException("Cannot balance because last delay is not "
									+ "long enough (" + sectionTotalOffset + " MS required).\n"
									+ "Try using another seed or extending the last delay. (line " + (i2 + 1) + ")");
							((Delay)instr).setOffset(-sectionTotalOffset);
							runtime += -sectionTotalOffset;
						}
					}else
						//Break if instruction found
						break;
					i2++;
				}
			}
			builder += instr.asString() + '\n';
		}
		if(runtime > 0)
		{
			builder += "// Runtime (MS): " + runtimeNoOffset + '\n';
			builder += "// Actual runtime after randomizer (MS): " + runtime + '\n';
		}
		return builder;
	}
	
	private Instruction getInstrForString(String[] argsSplit, int line)
	{
		Instruction instruction;
		switch(argsSplit[0])
		{
			case "MOUSEMOVE":
				instruction = new MouseMove();
				break;
			case "MOUSEPRESS":
				instruction = new MousePress();
				break;
			case "MOUSEWHEEL":
				instruction = new MouseWheel();
				break;
			case "DELAY":
				instruction = new Delay();
				break;
			case "KEY":
				instruction = new KeyType();
				break;
			case "CUSTOM":
				instruction = new Custom();
				break;
			default:
				throw new IllegalArgumentException("Unknown instruction at line " + line);
		}
		try
		{
			instruction.parseArguments(argsSplit);
		}catch(Exception e)
		{
			throw new IllegalArgumentException(e.getClass().getSimpleName()
				+ ": " + e.getMessage() + " (line " + line + ")");
		}
		return instruction;
	}
	
	public List<Instruction> getInstructions()
	{
		return instructions;
	}
}

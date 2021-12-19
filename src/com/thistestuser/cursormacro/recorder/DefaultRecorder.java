package com.thistestuser.cursormacro.recorder;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;

import org.jnativehook.GlobalScreen;
import com.thistestuser.cursormacro.CursorMacro;
import com.thistestuser.cursormacro.instr.Delay;
import com.thistestuser.cursormacro.instr.Instruction;

public class DefaultRecorder extends Recorder
{
	private MouseListener mouseListener;
	private KeyListener keyListener;
	private boolean recording;
	private long startTime;
	private long lastInstrTime;
	private List<Instruction> instructions = new ArrayList<>();
	private final CursorMacro parent;
	
	public DefaultRecorder(CursorMacro parent)
	{
		this.parent = parent;
	}
	
	@Override
	public void setup()
	{
		mouseListener = new MouseListener(this);
		keyListener = new KeyListener(this);
		GlobalScreen.addNativeMouseListener(mouseListener);
		GlobalScreen.addNativeMouseMotionListener(mouseListener);
		GlobalScreen.addNativeMouseWheelListener(mouseListener);
		GlobalScreen.addNativeKeyListener(keyListener);
	}
	
	@Override
	public boolean isRecording()
	{
		return recording;
	}
	
	@Override
	public void startRecording(int mouseDelay, boolean snap)
	{
		mouseListener.setMouseDelay(mouseDelay);
		mouseListener.setSnapMouse(snap);
		instructions.clear();
		recording = true;
		startTime = lastInstrTime = System.currentTimeMillis();
	}
	
	@Override
	public void stopRecording(JTextPane pane)
	{
		recording = false;
		String builder = "";
		for(Instruction instr : instructions)
			builder += instr.asString() + '\n';
		builder += "// Runtime (MS): " + (lastInstrTime - startTime);
		startTime = -1;
		pane.setText(builder);
	}
	
	public int getStopKey()
	{
		return parent.getStopKey();
	}
	
	public boolean isChoosingStopKey()
	{
		return parent.isChoosingStopKey();
	}
	
	public void setStopKey(int key)
	{
		parent.setStopKey(key);
	}
	
	public void addInstrWithDelay(Instruction instr)
	{
		if(System.currentTimeMillis() > lastInstrTime)
			instructions.add(new Delay((int)(System.currentTimeMillis() - lastInstrTime)));
		instructions.add(instr);
		lastInstrTime = System.currentTimeMillis();
		parent.stateLbl.setText("State: Recording (instruction " + instructions.size() + ")");
	}
	
	public List<Instruction> getInstructions()
	{
		return instructions;
	}
	
	public void pressStop()
	{
		parent.stopButton.doClick();
	}
	
	@Override
	public String getStringRepr()
	{
		return "Default";
	}
}

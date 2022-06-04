package com.thistestuser.cursormacro.recorder;

import java.awt.event.KeyEvent;

import org.jnativehook.keyboard.SwingKeyAdapter;

import com.thistestuser.cursormacro.instr.KeyType;

public class KeyListener extends SwingKeyAdapter
{
	private final DefaultRecorder recorder;
	
	public KeyListener(DefaultRecorder recorder)
	{
		this.recorder = recorder;
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() != recorder.getStopKey() && recorder.isRecording())
			recorder.addInstrWithDelay(new KeyType(0, e.getKeyCode()));
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		//Choose key handler (should not be active during recording or playing)
		if(recorder.isChoosingStopKey())
			recorder.setStopKey(e.getKeyCode());
		//Stop handler (always active)
		if(e.getKeyCode() == recorder.getStopKey())
		{
			recorder.pressStop();
			return;
		}
		if(recorder.isRecording())
			recorder.addInstrWithDelay(new KeyType(1, e.getKeyCode()));
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{}
}

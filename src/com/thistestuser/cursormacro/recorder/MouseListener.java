package com.thistestuser.cursormacro.recorder;

import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;

import com.thistestuser.cursormacro.instr.MouseMove;
import com.thistestuser.cursormacro.instr.MousePress;
import com.thistestuser.cursormacro.instr.MouseWheel;

public class MouseListener implements NativeMouseInputListener, NativeMouseWheelListener
{
	private final DefaultRecorder recorder;
	private long lastMS;
	private int mouseDelay;
	private boolean snap;
	private boolean skipped;
	
	public MouseListener(DefaultRecorder recorder)
	{
		this.recorder = recorder;
	}
	
	public void setMouseDelay(int mouseDelay)
	{
		this.mouseDelay = mouseDelay;
	}
	
	public void setSnapMouse(boolean snap)
	{
		this.snap = snap;
	}
	
	private int getButton(int button)
	{
		if(button == 3)
			return 2;
		if(button == 2)
			return 3;
		return button;
	}
	
	@Override
	public void nativeMousePressed(NativeMouseEvent e)
	{
		if(recorder.isRecording())
		{
			if(skipped)
				recorder.addInstrWithDelay(new MouseMove(e.getX(), e.getY()));
			recorder.addInstrWithDelay(new MousePress(0, getButton(e.getButton())));
		}
	}
	
	@Override
	public void nativeMouseReleased(NativeMouseEvent e)
	{
		if(recorder.isRecording())
		{
			if(skipped)
				recorder.addInstrWithDelay(new MouseMove(e.getX(), e.getY()));
			recorder.addInstrWithDelay(new MousePress(1, getButton(e.getButton())));
		}
	}
	
	@Override
	public void nativeMouseDragged(NativeMouseEvent e)
	{
		if(recorder.isRecording())
			if(snap || lastMS + mouseDelay > System.currentTimeMillis())
				skipped = true;
			else
			{
				recorder.addInstrWithDelay(new MouseMove(e.getX(), e.getY()));
				lastMS = System.currentTimeMillis();
				skipped = false;
			}
	}
	
	@Override
	public void nativeMouseMoved(NativeMouseEvent e)
	{
		if(recorder.isRecording())
			if(snap || lastMS + mouseDelay > System.currentTimeMillis())
				skipped = true;
			else
			{
				recorder.addInstrWithDelay(new MouseMove(e.getX(), e.getY()));
				lastMS = System.currentTimeMillis();
				skipped = false;
			}
	}
	
	@Override
	public void nativeMouseWheelMoved(NativeMouseWheelEvent e)
	{
		if(recorder.isRecording())
		{
			if(skipped)
				recorder.addInstrWithDelay(new MouseMove(e.getX(), e.getY()));
			recorder.addInstrWithDelay(new MouseWheel(e.getWheelRotation()));
		}
	}
	
	@Override
	public void nativeMouseClicked(NativeMouseEvent e)
	{}
}

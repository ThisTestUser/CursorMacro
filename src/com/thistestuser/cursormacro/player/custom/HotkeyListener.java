package com.thistestuser.cursormacro.player.custom;

import java.awt.event.KeyEvent;

import org.jnativehook.keyboard.SwingKeyAdapter;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class HotkeyListener extends SwingKeyAdapter implements NativeMouseInputListener
{
	private final HotkeyPlayer player;
	
	public HotkeyListener(HotkeyPlayer player)
	{
		this.player = player;
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		player.activateKeyMacro(e.getKeyCode());
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{}

	@Override
	public void nativeMouseClicked(NativeMouseEvent e)
	{}

	@Override
	public void nativeMousePressed(NativeMouseEvent e)
	{}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e)
	{
		player.activateMouseMacro(getButton(e.getButton()));
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent e)
	{}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e)
	{}
	
	private int getButton(int button)
	{
		if(button == 3)
			return 2;
		if(button == 2)
			return 3;
		return button;
	}
}

package com.thistestuser.cursormacro.player.custom;

import com.thistestuser.cursormacro.CursorMacro;

public class CustomPlayers
{
	public static void register(CursorMacro owner)
	{
		owner.players.add(new HotkeyPlayer(owner));
	}
}

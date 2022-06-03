package com.thistestuser.cursormacro.recorder;

import javax.swing.JTextPane;

public abstract class Recorder
{
	/**
	 * Sets up the recorder by initializing listeners. This is only run once and at the beginning.
	 */
	public abstract void setup();
	
	/**
	 * Returns if the recorder is recording.
	 */
	public abstract boolean isRecording();
	
	/**
	 * Starts the recording.
	 */
	public abstract void startRecording(int mouseDelay, boolean snap);
	
	/**
	 * Stops the recording, writing instructions to the pane field.
	 */
	public abstract void stopRecording(JTextPane pane);
	
	/**
	 * Gets the string representation to be displayed in the combo box.
	 */
	public abstract String getStringRepr();
	
	@Override
	public String toString()
	{
		return getStringRepr();
	}
}

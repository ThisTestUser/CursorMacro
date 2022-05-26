package com.thistestuser.cursormacro;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import com.thistestuser.cursormacro.instr.InstructionList;
import com.thistestuser.cursormacro.instr.custom.CustomInstructions;
import com.thistestuser.cursormacro.recorder.DefaultRecorder;
import com.thistestuser.cursormacro.recorder.Recorder;
import com.thistestuser.cursormacro.recorder.custom.CustomRecorders;

import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Robot;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class CursorMacro extends JFrame
{
	private JPanel contentPane;
	private JTextPane textPane;
	private JTextField seedField;
	private JTextField randomDelayField;
	private JTextField percentField;
	private JTextField recordField;
	private JLabel statusLbl;
	public JLabel stateLbl;
	public JButton stopButton;
	private JButton compileButton;
	private JButton applyButton;
	private JCheckBox snapMouseRecordBox;
	private String lastCompile;
	
	private InstructionList instrList = new InstructionList();
	public List<Recorder> recorders = new ArrayList<>();
	private Recorder activeRecorder;
	private Thread player;
	private Player playerRunnable;
	public boolean playing;
	public Robot robot;
	private JCheckBox unpressBox;
	private JCheckBox randomizeLocBox;
	private JLabel selectRecorderLbl;
	private JComboBox<Recorder> recorderSelect;
	private JCheckBox hasStopHotkey;
	private JButton chooseStopHotkey;
	public JCheckBox autoStopOption;
	private int stopKey = 27;
	private boolean choosingKey;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					CursorMacro frame = new CursorMacro();
					frame.setVisible(true);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public CursorMacro()
	{
		//Setup native library
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.WARNING);
		logger.setUseParentHandlers(false);
		try
		{
			GlobalScreen.registerNativeHook();
		}catch(NativeHookException e)
		{
			System.exit(1);
		}
		//Initialize recorders
		recorders.add(new DefaultRecorder(this));
		CustomRecorders.register(this);
		recorders.forEach(Recorder::setup);
		try
		{
			robot = new Robot();
		}catch(AWTException e)
		{
			System.exit(1);
		}
		CustomInstructions.register();
		
		setTitle("CursorMacro");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 550);
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(ClassNotFoundException | InstantiationException
			| IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.rowWeights = new double[]{0.0, 0.8, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.1};
		gbl_contentPane.columnWeights = new double[]{0.2, 1.0, 0.2};
		contentPane.setLayout(gbl_contentPane);
		
		JButton startButton = new JButton("Start");
		
		JButton recordButton = new JButton("Record");
		recordButton.setToolTipText("<html>"
            + "Press this button to record your keyboard and mouse."
            + "<br>" + "Warning: This will overwrite the macro you have." + "</html>");
		recordButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(recorderSelect.getSelectedIndex() == -1)
				{
					JOptionPane.showMessageDialog(CursorMacro.this, "You did not select a recorder!", "Error",
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(!textPane.getText().isEmpty())
				{
					int reply = JOptionPane.showConfirmDialog(CursorMacro.this, "You have macro code that will be wiped\n"
						+ "when the recording is finished. Continue?", "Warning", JOptionPane.YES_NO_OPTION);
					if(reply != JOptionPane.YES_OPTION)
						return;
				}
				int delay;
				try
				{
					delay = Integer.parseInt(recordField.getText());
					if(delay < 0)
						throw new NumberFormatException();
				}catch(NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(CursorMacro.this, "Invaild mouse interval", "Error",
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				recordButton.setEnabled(false);
				startButton.setEnabled(false);
				disableStopKeyChoose(false);
				activeRecorder = (Recorder)recorderSelect.getSelectedItem();
				activeRecorder.startRecording(delay, snapMouseRecordBox.isSelected());
				stateLbl.setText("State: Recording (instruction 0)");
			}
		});
		GridBagConstraints gbc_recordButton = new GridBagConstraints();
		gbc_recordButton.insets = new Insets(0, 0, 5, 5);
		gbc_recordButton.gridx = 0;
		gbc_recordButton.gridy = 0;
		contentPane.add(recordButton, gbc_recordButton);
		
		startButton.setToolTipText("Runs the compiled macro code. You must press compile first.");
		startButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!textPane.getText().equals(lastCompile))
				{
					int reply = JOptionPane.showConfirmDialog(CursorMacro.this, "You have changed your"
						+ " macro code since last compile,\nwhich will not be reflected in this run."
						+ " Continue?", "Warning", JOptionPane.YES_NO_OPTION);
					if(reply != JOptionPane.YES_OPTION)
						return;
				}
				stateLbl.setText("State: Executing 0/" + instrList.getInstructions().size());
				recordButton.setEnabled(false);
				startButton.setEnabled(false);
				compileButton.setEnabled(false);
				applyButton.setEnabled(false);
				disableStopKeyChoose(false);
				playing = true;
				playerRunnable = new Player(instrList.getInstructions(), CursorMacro.this, unpressBox.isSelected());
				player = new Thread(playerRunnable, "Macro Executor");
				player.start();
			}
		});
		GridBagConstraints gbc_startButton = new GridBagConstraints();
		gbc_startButton.insets = new Insets(0, 0, 5, 5);
		gbc_startButton.gridx = 1;
		gbc_startButton.gridy = 0;
		contentPane.add(startButton, gbc_startButton);
		
		stopButton = new JButton("Stop");
		stopButton.setToolTipText("Press the ESC key to stop recording or playing.");
		stopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(activeRecorder != null)
				{
					activeRecorder.stopRecording(textPane);
					activeRecorder = null;
				}
				playing = false;
				if(player != null)
				{
					player.interrupt();
					if(playerRunnable.unpress)
					{
						for(Integer i : playerRunnable.mousePress)
							robot.mouseRelease(i);
						for(Integer i : playerRunnable.keyPress)
							robot.keyRelease(i);
					}
					playerRunnable = null;
					player = null;
				}
				recordButton.setEnabled(true);
				startButton.setEnabled(!instrList.getInstructions().isEmpty());
				compileButton.setEnabled(true);
				applyButton.setEnabled(true);
				disableStopKeyChoose(true);
				stateLbl.setText("State: Idle");
			}
		});
		GridBagConstraints gbc_stopButton = new GridBagConstraints();
		gbc_stopButton.insets = new Insets(0, 0, 5, 0);
		gbc_stopButton.gridx = 2;
		gbc_stopButton.gridy = 0;
		contentPane.add(stopButton, gbc_stopButton);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.gridy = 1;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		
		compileButton = new JButton("Compile");
		compileButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				instrList.getInstructions().clear();
				try
				{
					instrList.compile(textPane.getText());
					SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss:SSS MM/dd/yyyy");
					if(instrList.getInstructions().isEmpty())
						statusLbl.setText("Status: Not compiled");
					else
					{
						statusLbl.setText("Status: Compiled at " + format.format(new Date()));
						lastCompile = textPane.getText();
					}
				}catch(IllegalArgumentException ex)
				{
					instrList.getInstructions().clear();
					JOptionPane.showMessageDialog(CursorMacro.this, ex.getMessage(), "Compile Error",
						JOptionPane.ERROR_MESSAGE);
					statusLbl.setText("Status: Failed to compile");
				}
				boolean hasInstr = !instrList.getInstructions().isEmpty();
				startButton.setEnabled(hasInstr);
			}
		});
		
		unpressBox = new JCheckBox("Unpress", true);
		unpressBox.setToolTipText("Unpresses all pressed keys at the end of execution.");
		GridBagConstraints gbc_unpressBox = new GridBagConstraints();
		gbc_unpressBox.insets = new Insets(0, 0, 5, 5);
		gbc_unpressBox.gridx = 0;
		gbc_unpressBox.gridy = 2;
		contentPane.add(unpressBox, gbc_unpressBox);
		
		GridBagConstraints gbc_compileButton = new GridBagConstraints();
		gbc_compileButton.insets = new Insets(0, 0, 5, 5);
		gbc_compileButton.gridx = 1;
		gbc_compileButton.gridy = 2;
		contentPane.add(compileButton, gbc_compileButton);
		
		JButton helpButton = new JButton("Help");
		helpButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String[] info = {"There are six instructions possible:",
					"MOUSEMOVE, MOUSEPRESS, MOUSEWHEEL, DELAY, KEY, and CUSTOM.",
					"MOUSEMOVE has two parameters, x and y, indicating the new location of the mouse.",
					"MOUSEMOVE can also have four additional parameters, the first two being offset x and offset y,",
					"which are the tolerances if the mouse is not exactly at the location.",
					"The last two parameters are added to x and y and are generated with the randomizer using the tolerance.",
					"MOUSEPRESS presses or releases the mouse, accepting 0 as press and 1 as release as the first argument,",
					"and the button ID for the second argument (1 is left click, 2 is middle click, 3 is right click).",
					"MOUSEWHEEL scrolls the mouse wheel and accepts an integer as the rotation parameter.",
					"DELAY adds a pause between clicks, and accepts a value and a random offset.",
					"KEY types the corresponding key, accepting 0 as press and 1 as release as the first argument,",
					"and the integer keycode of the key typed as the second argument.",
					"CUSTOM accepts an instruction name and its args. The instruction itself",
					"must be implemented and added to CustomInstructions (compiled with the macro).",
					"Comments are designated using // and are ignored by the compiler."};
				JOptionPane.showMessageDialog(CursorMacro.this, String.join("\n", info), "Help",
					JOptionPane.INFORMATION_MESSAGE);
			}
		});
		GridBagConstraints gbc_helpButton = new GridBagConstraints();
		gbc_helpButton.insets = new Insets(0, 0, 5, 0);
		gbc_helpButton.gridx = 2;
		gbc_helpButton.gridy = 2;
		contentPane.add(helpButton, gbc_helpButton);
		
		JLabel seedLbl = new JLabel("Seed:");
		seedLbl.setToolTipText("<html>"
            + "Seed used for the speed randomizer. Leaving this blank"
            + "<br>" + "will allow you to remove randomizer settings." + "</html>");
		GridBagConstraints gbc_seedLbl = new GridBagConstraints();
		gbc_seedLbl.anchor = GridBagConstraints.EAST;
		gbc_seedLbl.insets = new Insets(0, 0, 5, 5);
		gbc_seedLbl.gridx = 0;
		gbc_seedLbl.gridy = 3;
		contentPane.add(seedLbl, gbc_seedLbl);
		
		seedField = new JTextField();
		GridBagConstraints gbc_seedField = new GridBagConstraints();
		gbc_seedField.insets = new Insets(0, 0, 5, 5);
		gbc_seedField.fill = GridBagConstraints.HORIZONTAL;
		gbc_seedField.gridx = 1;
		gbc_seedField.gridy = 3;
		contentPane.add(seedField, gbc_seedField);
		seedField.setColumns(10);
		
		JButton generateButton = new JButton("Generate");
		generateButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				seedField.setText(String.valueOf(new Random().nextLong()));
			}
		});
		GridBagConstraints gbc_generateButton = new GridBagConstraints();
		gbc_generateButton.insets = new Insets(0, 0, 5, 0);
		gbc_generateButton.gridx = 2;
		gbc_generateButton.gridy = 3;
		contentPane.add(generateButton, gbc_generateButton);
		
		JLabel randomNumLbl = new JLabel("Random Delay (MS):");
		randomNumLbl.setToolTipText("The maximum possible offset to apply to each delay.");
		GridBagConstraints gbc_randomNumLbl = new GridBagConstraints();
		gbc_randomNumLbl.anchor = GridBagConstraints.EAST;
		gbc_randomNumLbl.insets = new Insets(0, 0, 5, 5);
		gbc_randomNumLbl.gridx = 0;
		gbc_randomNumLbl.gridy = 4;
		contentPane.add(randomNumLbl, gbc_randomNumLbl);
		
		randomDelayField = new JTextField();
		GridBagConstraints gbc_randomDelayField = new GridBagConstraints();
		gbc_randomDelayField.insets = new Insets(0, 0, 5, 5);
		gbc_randomDelayField.fill = GridBagConstraints.HORIZONTAL;
		gbc_randomDelayField.gridx = 1;
		gbc_randomDelayField.gridy = 4;
		contentPane.add(randomDelayField, gbc_randomDelayField);
		randomDelayField.setColumns(10);
		
		randomizeLocBox = new JCheckBox("Randomize Location", true);
		randomizeLocBox.setToolTipText("Mouse locations will be randomized based on the tolerance if non-zero.");
		GridBagConstraints gbc_randomizeLocBox = new GridBagConstraints();
		gbc_randomizeLocBox.insets = new Insets(0, 0, 5, 0);
		gbc_randomizeLocBox.gridx = 2;
		gbc_randomizeLocBox.gridy = 4;
		contentPane.add(randomizeLocBox, gbc_randomizeLocBox);
		
		JLabel percentageLbl = new JLabel("Max Percentage (0-100):");
		percentageLbl.setToolTipText("The offset delay will be clamped to this percentage of the delay.");
		GridBagConstraints gbc_percentageLbl = new GridBagConstraints();
		gbc_percentageLbl.anchor = GridBagConstraints.EAST;
		gbc_percentageLbl.insets = new Insets(0, 0, 5, 5);
		gbc_percentageLbl.gridx = 0;
		gbc_percentageLbl.gridy = 5;
		contentPane.add(percentageLbl, gbc_percentageLbl);
		
		percentField = new JTextField();
		GridBagConstraints gbc_percentField = new GridBagConstraints();
		gbc_percentField.insets = new Insets(0, 0, 5, 5);
		gbc_percentField.fill = GridBagConstraints.HORIZONTAL;
		gbc_percentField.gridx = 1;
		gbc_percentField.gridy = 5;
		contentPane.add(percentField, gbc_percentField);
		percentField.setColumns(10);
		
		applyButton = new JButton("Apply Delay");
		applyButton.setToolTipText("Applies the randomizer, or checks runtime if the seed is empty.");
		applyButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String seedText = seedField.getText();
				if(seedText.isEmpty())
				{
					int reply = JOptionPane.showConfirmDialog(CursorMacro.this, "Do you want to erase "
						+ "all randomizer data?\nPressing \"Yes\" erases all randomizer data."
						+ "\nPressing \"No\" keeps the randomizer data intact and only calculates the runtime."
						+ "\nPressing \"Cancel\" prevents anything from being run.",
						"Warning",
						JOptionPane.YES_NO_CANCEL_OPTION);
					if(reply != JOptionPane.YES_OPTION && reply != JOptionPane.NO_OPTION)
						return;
					try
					{
						textPane.setText(instrList.randomize(textPane.getText(), null, 0, 0,
							false, reply == JOptionPane.YES_OPTION));
						compileButton.doClick();
					}catch(IllegalArgumentException ex)
					{
						JOptionPane.showMessageDialog(CursorMacro.this, "Error while parsing instructions: "
							+ ex.getMessage(), "Randomizer Error", JOptionPane.ERROR_MESSAGE);
					}
					return;
				}
				int rndDelay = 0;
				float maxPercent = 0;
				try
				{
					rndDelay = Integer.parseInt(randomDelayField.getText());
					maxPercent = Float.parseFloat(percentField.getText());
					if(maxPercent < 0 || maxPercent > 100)
					{
						JOptionPane.showMessageDialog(CursorMacro.this, "Invaild percentage", "Error",
							JOptionPane.ERROR_MESSAGE);
						return;
					}
				}catch(NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(CursorMacro.this, "Invaild delay field or percentage", "Error",
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				try
				{
					Random random = new Random(Long.parseLong(seedText));
					boolean removeData = true;
					if(!randomizeLocBox.isSelected())
					{
						int reply = JOptionPane.showConfirmDialog(CursorMacro.this, "Do you want to erase "
							+ "mouse randomizer data?\nPressing \"Yes\" erases mouse randomizer data."
							+ "\nPressing \"No\" keeps the mouse randomizer data intact."
							+ "\nPressing \"Cancel\" prevents anything from being run.",
							"Warning",
							JOptionPane.YES_NO_CANCEL_OPTION);
						if(reply != JOptionPane.YES_OPTION && reply != JOptionPane.NO_OPTION)
							return;
						removeData = reply == JOptionPane.YES_OPTION;
					}
					textPane.setText(instrList.randomize(textPane.getText(), random, rndDelay, maxPercent,
						randomizeLocBox.isSelected(), removeData));
					compileButton.doClick();
				}catch(NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(CursorMacro.this, "Invaild long value for seed", "Error",
						JOptionPane.ERROR_MESSAGE);
				}catch(IllegalArgumentException ex)
				{
					JOptionPane.showMessageDialog(CursorMacro.this, "Error while parsing instructions: "
						+ ex.getMessage(), "Randomizer Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		GridBagConstraints gbc_applyButton = new GridBagConstraints();
		gbc_applyButton.insets = new Insets(0, 0, 5, 0);
		gbc_applyButton.gridx = 2;
		gbc_applyButton.gridy = 5;
		contentPane.add(applyButton, gbc_applyButton);
		
		JLabel dragOccLbl = new JLabel("Mouse Interval:");
		dragOccLbl.setToolTipText("<html>"
            + "The interval in MS between mouse location recordings."
            + "<br>" + "Before a click, a mouse location will always be recorded."
            + "<br>" + "Mouse intervals below 100 are NOT recommended due to limitations of the pointer."+ "</html>");
		GridBagConstraints gbc_dragOccLbl = new GridBagConstraints();
		gbc_dragOccLbl.anchor = GridBagConstraints.EAST;
		gbc_dragOccLbl.insets = new Insets(0, 0, 5, 5);
		gbc_dragOccLbl.gridx = 0;
		gbc_dragOccLbl.gridy = 6;
		contentPane.add(dragOccLbl, gbc_dragOccLbl);
		
		recordField = new JTextField("250");
		GridBagConstraints gbc_recordField = new GridBagConstraints();
		gbc_recordField.insets = new Insets(0, 0, 5, 5);
		gbc_recordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_recordField.gridx = 1;
		gbc_recordField.gridy = 6;
		contentPane.add(recordField, gbc_recordField);
		recordField.setColumns(10);
		
		snapMouseRecordBox = new JCheckBox("Snap Recording Mode");
		snapMouseRecordBox.setToolTipText("<html>"
            + "If this option is on, the mouse recorder will only"
            + "<br>" + "record mouse movements before a click or scroll." + "</html>");
		GridBagConstraints gbc_snapMouseBox = new GridBagConstraints();
		gbc_snapMouseBox.insets = new Insets(0, 0, 5, 0);
		gbc_snapMouseBox.gridx = 2;
		gbc_snapMouseBox.gridy = 6;
		contentPane.add(snapMouseRecordBox, gbc_snapMouseBox);
		
		selectRecorderLbl = new JLabel("Select Recorder:");
		selectRecorderLbl.setToolTipText("<html>"
            + "Here you can select a custom recorder."
            + "<br>" + "The default recorder records mouse and key actions."
            + "<br>" + "You can compile custom recorders that record in different ways."+ "</html>");
		GridBagConstraints gbc_selectRecorderLbl = new GridBagConstraints();
		gbc_selectRecorderLbl.anchor = GridBagConstraints.EAST;
		gbc_selectRecorderLbl.insets = new Insets(0, 0, 5, 5);
		gbc_selectRecorderLbl.gridx = 0;
		gbc_selectRecorderLbl.gridy = 7;
		contentPane.add(selectRecorderLbl, gbc_selectRecorderLbl);
		
		recorderSelect = new JComboBox<Recorder>();
		recorders.forEach(rec -> recorderSelect.addItem(rec));
		GridBagConstraints gbc_recorderSelect = new GridBagConstraints();
		gbc_recorderSelect.insets = new Insets(0, 0, 5, 5);
		gbc_recorderSelect.fill = GridBagConstraints.HORIZONTAL;
		gbc_recorderSelect.gridx = 1;
		gbc_recorderSelect.gridy = 7;
		contentPane.add(recorderSelect, gbc_recorderSelect);
		
		hasStopHotkey = new JCheckBox("Stop Hotkey", true);
		hasStopHotkey.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				chooseStopHotkey.setEnabled(hasStopHotkey.isSelected());
				if(!hasStopHotkey.isSelected())
				{
					choosingKey = false;
					chooseStopHotkey.setText("Key: " + KeyEvent.getKeyText(stopKey));
				}
			}
		});
		GridBagConstraints gbc_hasStopHotkey = new GridBagConstraints();
		gbc_hasStopHotkey.insets = new Insets(0, 0, 5, 5);
		gbc_hasStopHotkey.gridx = 0;
		gbc_hasStopHotkey.gridy = 8;
		contentPane.add(hasStopHotkey, gbc_hasStopHotkey);
		
		chooseStopHotkey = new JButton("Key: " + KeyEvent.getKeyText(stopKey));
		chooseStopHotkey.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				chooseStopHotkey.setEnabled(false);
				choosingKey = true;
				chooseStopHotkey.setText("Press a key");
			}
		});
		GridBagConstraints gbc_chooseStopHotkey = new GridBagConstraints();
		gbc_chooseStopHotkey.insets = new Insets(0, 0, 5, 5);
		gbc_chooseStopHotkey.gridx = 1;
		gbc_chooseStopHotkey.gridy = 8;
		contentPane.add(chooseStopHotkey, gbc_chooseStopHotkey);
		
		autoStopOption = new JCheckBox("Auto-Stop", false);
		autoStopOption.setToolTipText("Automatically exits execution mode when done.");
		GridBagConstraints gbc_autoStopOption = new GridBagConstraints();
		gbc_autoStopOption.insets = new Insets(0, 0, 5, 5);
		gbc_autoStopOption.gridx = 2;
		gbc_autoStopOption.gridy = 8;
		contentPane.add(autoStopOption, gbc_autoStopOption);
		
		statusLbl = new JLabel("Status: Not compiled");
		GridBagConstraints gbc_statusLbl = new GridBagConstraints();
		gbc_statusLbl.insets = new Insets(0, 0, 5, 0);
		gbc_statusLbl.gridx = 0;
		gbc_statusLbl.gridwidth = 3;
		gbc_statusLbl.gridy = 9;
		contentPane.add(statusLbl, gbc_statusLbl);
		
		stateLbl = new JLabel("State: Idle");
		GridBagConstraints gbc_stateLbl = new GridBagConstraints();
		gbc_stateLbl.gridx = 0;
		gbc_stateLbl.gridwidth = 3;
		gbc_stateLbl.gridy = 10;
		contentPane.add(stateLbl, gbc_stateLbl);
		
		startButton.setEnabled(false);
	}
	
	public boolean isChoosingStopKey()
	{
		return choosingKey;
	}
	
	public int getStopKey()
	{
		return hasStopHotkey.isSelected() ? stopKey : -1;
	}
	
	public void setStopKey(int key)
	{
		stopKey = key;
		disableStopKeyChoose(true);
	}
	
	public void disableStopKeyChoose(boolean enableButton)
	{
		choosingKey = false;
		chooseStopHotkey.setText("Key: " + KeyEvent.getKeyText(stopKey));
		chooseStopHotkey.setEnabled(enableButton);
	}
	
	public static native boolean setMouseLocation(int x, int y);
	
	public static native Point getMouseLocation();
	
	private static native void setupDPI();
	
	private static void loadNativeLibrary() throws Exception
	{
		File library = File.createTempFile("mouseutils", ".dll");
        library.deleteOnExit();
        try(InputStream input = CursorMacro.class.getResourceAsStream("/lib/mouseutils.dll"))
        {
            try(FileOutputStream out = new FileOutputStream(library))
            {
            	int bytes;
            	byte[] buffer = new byte[2048];
            	
            	while((bytes = input.read(buffer)) != -1)
            		out.write(buffer, 0, bytes);
            }	
        }
        System.load(library.getAbsolutePath());
	}
	
	static
	{
		try
		{
			loadNativeLibrary();
			setupDPI();
		}catch(Exception e)
		{
			throw new RuntimeException("Cannot load mouseutils library", e);
		}
	}
}

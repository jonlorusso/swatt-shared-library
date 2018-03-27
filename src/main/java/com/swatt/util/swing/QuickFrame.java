package com.swatt.util.swing;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class QuickFrame extends JFrame {
	private static final long	serialVersionUID	= 1L;

	public QuickFrame(String title, JComponent component) {
		this(title, component, 0, 0, -1, -1);
	}
	
	public QuickFrame(JComponent component) {
		this("Test Frame", component, 0, 0, -1, -1);
	}
	
	public QuickFrame(JComponent component, JMenuBar menuBar) {
		this("Test Frame", component, menuBar, 0, 0, -1, -1);
	}

	public QuickFrame(String title, JComponent component, int width, int height) {
		this(title, component, 0, 0, width, height);
	}

	public QuickFrame(JComponent component, int width, int height) {
		this("Test Frame", component, 0, 0, width, height);
	}	

	
	public QuickFrame(JComponent component, int x, int y, int width, int height) {
		this("Test Frame", component, x, y, width, height);
	}
	
	public QuickFrame(JComponent component, JMenuBar menuBar, int x, int y, int width, int height) {
		this("Test Frame", component, menuBar, x, y, width, height);
	}
	
	public QuickFrame(String title, JComponent component, int x, int y, int width, int height) {
		this(title, component, null, x, y, width, height);
	}
		
	public QuickFrame(String title, JComponent component, JMenuBar jmenuBar, int x, int y, int width, int height) {
		this(0, title, component, jmenuBar, x, y, width, height);
	}

	public QuickFrame(int screenNumber, String title, JComponent component) {
		this(screenNumber, title, component, 0, 0, -1, -1);
	}
	
	public QuickFrame(int screenNumber, JComponent component) {
		this(screenNumber, "Test Frame", component, 0, 0, -1, -1);
	}
	

	public QuickFrame(int screenNumber, String title, JComponent component, int width, int height) {
		this(screenNumber, title, component, 0, 0, width, height);
	}

	public QuickFrame(int screenNumber, String title, JComponent component, int x, int y, int width, int height) {
		this(screenNumber, title, component, null, 0, 0, width, height);
	}

	public QuickFrame(int screenNumber, String title, JComponent component, JMenuBar jmenuBar, int x, int y, int width, int height) {
		super(title);
		
		if (jmenuBar != null)
			setJMenuBar(jmenuBar);

		getContentPane().setLayout(new GridLayout(1,1));
		getContentPane().add(component);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		if (width < 0) {
			pack();
			Dimension screenSize = getToolkit().getScreenSize();
			int maxFrameHeight = screenSize.height - 25;
			
			Dimension frameSize = getSize();
			
			if (frameSize.height > maxFrameHeight)
				setSize(new Dimension(frameSize.width, maxFrameHeight));
			
		} else
			setSize(width, height);

		if (x >= 0) {
			Rectangle screen = SwingTools.getScreenBounds(screenNumber);
			setLocation(x + screen.x, y + screen.y);
		}

		setVisible(true);		
	}

}

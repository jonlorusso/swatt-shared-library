package com.swatt.util.swing;


import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import com.swatt.util.general.StringUtilities;

public class SwingTools {
	private static int controlBarHeight = 25; // We have to assume a Windows Control/Start bar
	private static LinkedList<String> fixedFontFamilies;
	
	private static int initX = 0;
	private static int topX = initX;
	private static int topY = 0;
	private static int topXinc = 35;
	private static int maxX = 600;
	private static int nextX = topX;
	private static int nextY = topY;
	private static int xInc = 20;
	private static int yInc = 20;

	public static Point getNextFrameLocation() {
		Point point = new Point(nextX, nextY);
		nextX += xInc;
		nextY += yInc;
		
		if (nextY > 500) {
			topX += topXinc;
			
			if (topX > maxX)
				topX = initX;
			
			nextX = topX;;
			nextY = topY;
		}
		
		return point;
	}
	
	public static void setFrameLocation(Frame frame) {
		Point point = getNextFrameLocation();
		frame.setLocation(point.x, point.y);
	}
	
	public static Color createOppositeColor(Color color) {
		int r = 255 - color.getRed();
		int g = 255 - color.getGreen();
		int b = 255 - color.getBlue();
		
		return new Color(r, g, b);
	}
	
	public static void setAllBorders(JComponent jComponent, Border border) { //useful for debugging GUIs
		jComponent.setBorder(border);
		
		if(jComponent instanceof Container) {
			Container container = (Container)jComponent;
			for(int i = 0; i < container.getComponents().length; i++) {
				Component childComponent = container.getComponents()[i];
				
				if(childComponent instanceof JComponent)
				setAllBorders((JComponent)childComponent, border);
			}
		}
	}
	
	public static void setAllFonts(Component component, Font font) {
		component.setFont(font);
		
		if(component instanceof Container) {
			Container container = (Container)component;
			for(int i = 0; i < container.getComponents().length; i++) {
				Component childComponent = container.getComponents()[i];
				
				setAllFonts(childComponent, font);
			}
		}
	}
	
	public static void setAllEnabled(Component component, boolean isEnabled) {
		component.setEnabled(isEnabled);
		
		if(component instanceof Container) {
			Container container = (Container)component;
			for(int i = 0; i < container.getComponents().length; i++) {
				Component childComponent = container.getComponents()[i];
				
				setAllEnabled(childComponent, isEnabled);
			}
		}
	}
	
	public static void setAllBackgrounds(Component component, Color color) {
		component.setBackground(color);		
		if(component instanceof Container) {
			Container container = (Container)component;
			for(int i = 0; i < container.getComponents().length; i++) {
				Component childComponent = container.getComponents()[i];
				
				setAllBackgrounds(childComponent, color);
			}
		}
	}
	
	public static void setAllInsideOpaque(Container container, boolean isOpaque) {	
		for(int i = 0; i < container.getComponents().length; i++) {
			Component childComponent = container.getComponents()[i];
			
			setAllOpaue(childComponent, isOpaque);
		}
	}
	
	public static void setAllOpaue(Component component, boolean isOpaque) {	
		if(component instanceof JComponent)
			((JComponent)component).setOpaque(isOpaque);
		
		if(component instanceof Container) {
			Container container = (Container)component;
			for(int i = 0; i < container.getComponents().length; i++) {
				Component childComponent = container.getComponents()[i];
				
				setAllOpaue(childComponent, isOpaque);
			}
		}
	}
	
	public static void setAllForegrounds(Component component, Color color) {
		component.setForeground(color);		
		if(component instanceof Container) {
			Container container = (Container)component;
			for(int i = 0; i < container.getComponents().length; i++) {
				Component childComponent = container.getComponents()[i];
				
				setAllForegrounds(childComponent, color);
			}
		}
	}
	
	public static void setupLookAndFeel() {
		try {
		    UIManager.setLookAndFeel(
		      UIManager.getSystemLookAndFeelClassName());
		    }
		catch (Throwable t) {
			try {
				UIManager.setLookAndFeel(
				  UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Throwable t2) {
			}

		}
	}

	public static void setLocation (Window parent, Window child) {
		setLocation(parent, child, SwingConstants.CENTER, 0.0);
	}

	public static void setLocation (Component parent, Window child) {			
		setLocation(parent, child, SwingConstants.CENTER, 0.0);
	}

	public static void setLocation (Component parent, Window child, int direction, double offset) {
		Window parentWindow = SwingUtilities.getWindowAncestor(child);

		if (parent == null) 	// Component isn't visible
			return;

		setLocation(parentWindow, child, direction, offset);
	}


	//sortDirection-> compass points (use SwingConstants)
	public static void setLocation (Window parent, Window child, int direction, double offset) { 
		if (child == null) return;

		Dimension childSize = child.getSize();
		
		//start out with the screen as the parent
		Dimension screenSize = child.getToolkit().getScreenSize();
		int centerX = screenSize.width/2;
		int centerY = screenSize.height/2;
		
        // if there's a real parent window use that instead of the screen
		if (parent != null) {
			Dimension parentSize = parent.getSize();
			centerX = parent.getX() + parentSize.width/2;
			centerY = parent.getY() + parentSize.height/2;
		}
		
		//Shout out to Pathagorus
		double widthOrHeightForDiagonalDirection = offset / Math.sqrt(2.0);
		
		switch (direction) {
			case SwingConstants.NORTH:	centerY -= offset;
										break;
			case SwingConstants.SOUTH:	centerY += offset;
										break;
			case SwingConstants.EAST:	centerX += offset;
										break;
			case SwingConstants.WEST:	centerX -= offset;
										break;
			case SwingConstants.NORTH_EAST:	centerX += widthOrHeightForDiagonalDirection;
											centerY -= widthOrHeightForDiagonalDirection;
											break;
			case SwingConstants.SOUTH_EAST:	centerX += widthOrHeightForDiagonalDirection;
											centerY += widthOrHeightForDiagonalDirection;
											break;
			case SwingConstants.NORTH_WEST:	centerX -= widthOrHeightForDiagonalDirection;
											centerY -= widthOrHeightForDiagonalDirection;
											break;
			case SwingConstants.SOUTH_WEST:	centerX -= widthOrHeightForDiagonalDirection;
											centerY += widthOrHeightForDiagonalDirection;
											break;
		}
		
		//now that we know what the center of the child should be on the screen, let's put the child there.
		int transformedChildX = centerX - (childSize.width/2);
		int transformedChildY = centerY - (childSize.height/2);
		
		child.setLocation(transformedChildX, transformedChildY);
		
		ensureWindowVisible(child);
	}
	

	//shrink if necessary, then translate->viewable
	public static void ensureWindowVisible(Window window) { 
		if (window == null) return;
		
		Dimension	windowSize = window.getSize();
		Dimension	screenSize = window.getToolkit().getScreenSize();

		int newWidth = windowSize.width;
		int newHeight = windowSize.height;
		int newX = window.getX();
		int newY = window.getY();

		// if the window is wider or taller than the screen, make it at least the size of the screen.
		if (newHeight > screenSize.height)
			newHeight = screenSize.height;
		
		if (newWidth > screenSize.width)
			newWidth = screenSize.width;
		
		//now that the window can fit on the screen, perform translations

		//if it's too far to the left or too high
		while (newX < 0) newX++;
		while (newY < 0) newY++;
		
		//if it's too far to the right or too low
		while (newX + newWidth > screenSize.width) newX--;
		while (newY + newHeight > screenSize.height) newY--;
		
		
		window.setSize(newWidth, newHeight);
		window.setLocation(newX, newY);

	}

	//like ensure window visible, but allows window to be partly offscreen.
	public static void ensureWindowUsable(Window window) { 
		if (window == null) return;
		
		Dimension	windowSize = window.getSize();
		Dimension	screenSize = window.getToolkit().getScreenSize();

		int newWidth = windowSize.width;
		int newHeight = windowSize.height;
		int newX = window.getX();
		int newY = window.getY();

		// if the window is wider or taller than the screen, make it at least the size of the screen.
		if (newHeight > screenSize.height)
			newHeight = screenSize.height;
		
		if (newWidth > screenSize.width)
			newWidth = screenSize.width;
		
		//now that the window can fit on the screen, perform translations

		//if it's too far to the left or too high
		while (newX < 0) newX++;
		while (newY < 0) newY++;
		
		//if it's too far to the right or too low
		while (newX + 100 > screenSize.width) newX--;
		while (newY + 100 > screenSize.height) newY--;
		
		
		window.setSize(newWidth, newHeight);
		window.setLocation(newX, newY);

	}


	public static Iterator<String> getFixedFontFamilies() {
		JTextArea textArea = new JTextArea();
		fixedFontFamilies = new LinkedList<String>();
		String fontFamilies[] = GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames ();
		Font font=null;
		FontMetrics metrics=null;
		for (int i=0; i < fontFamilies.length; i++) {
			font = new Font(fontFamilies[i], Font.PLAIN, 12);
			metrics=textArea.getFontMetrics(font);

			if(font.canDisplay('a') && metrics.charWidth('W')==metrics.charWidth('i'))
				fixedFontFamilies.add(fontFamilies[i]);
		}
		return fixedFontFamilies.iterator();
	}
		

	public static void showJPopupMenu(Component component, JPopupMenu popup, int x, int y) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		Dimension popupSize = popup.getPreferredSize();

		Point componentLocation = component.getLocationOnScreen();

		int tooFarRight = screenSize.width - (x + componentLocation.x + popupSize.width);
		
		if (tooFarRight < 0 )
			x += tooFarRight;	// Since tooFarRight is negative it moves it back
		
		int tooLow = screenSize.height - (y + componentLocation.y + popupSize.height + controlBarHeight);

		if (tooLow < 0 ) 
			y += tooLow;	// Since tooLow is negative it moves it up
	
		popup.show(component, x, y);
	}

	public static JFrame createJFrame(String title, Component component) {
		return createJFrame(title, component, 0, 0, -1, -1);
	}
	
	public static JFrame createJFrame(String title, Component component, int x, int y) {
		return createJFrame(title, component, x, y, -1, -1);
	}
			
	public static JFrame createJFrame(final String title, final Component component, final int x,  final int y,  final int width,  final int height) {
		final JFrame frame = new JFrame(title);

		invokeNow(new Runnable() {
			public void run() {
				frame.getContentPane().setLayout(new GridLayout(1,1));
				frame.getContentPane().add(component);
				
				frame.setLocation(x,y);
				
				if ( (width < 0) || (height < 0) ) {
					frame.pack();
					Dimension size = frame.getSize();
					if ( (size.width < 5) || (size.height < 5))
						frame.setSize(100,100);
				} else
					frame.setSize(width, height);
					
				frame.setVisible(true);
			}
		});

		return frame;
	}

	private static class RunnableWrapper implements Runnable {
		Runnable runnable;
		boolean complete = false;
		
		RunnableWrapper(Runnable runnable) {
			this.runnable = runnable;
			SwingUtilities.invokeLater(this);
		}

		public void run() {
			runnable.run();
			complete = true;
			synchronized(this) {
				notify();
			}
		}
	}
	
	public static void invokeNow(Runnable runnable) {
		RunnableWrapper runnableWrapper = new RunnableWrapper(runnable);

		synchronized(runnableWrapper) {
			try {
				while(!runnableWrapper.complete)
					runnableWrapper.wait();
			} catch (InterruptedException e) { }
		}
	}
	
	public static String getColorAsHex(Color color) {
		return StringUtilities.getByteAsHex(color.getRed()) +  StringUtilities.getByteAsHex(color.getGreen()) +  StringUtilities.getByteAsHex(color.getBlue());
	}
	
	public static String colorizeLabelText(String text, Color color) {
		if (color == null)
			return text;
		
		String hexColor = getColorAsHex(color);
		return "<FONT COLOR=" + hexColor + "> " + text + " </FONT>";
	}

	public static String htmlizeLabelText(String text) {
		return "<HTML><BODY> " + text + "</BODY></HTML>";
	}
	
	public static JSplitPane createTopBottomSplitPane(final JComponent comp1, final JComponent comp2, final double dividerLocation) {
		return createSplitPane(JSplitPane.VERTICAL_SPLIT, comp1, comp2, dividerLocation);
	}
	
	public static JSplitPane createLeftRightSplitPane(final JComponent comp1, final JComponent comp2, final double dividerLocation) {
		return createSplitPane(JSplitPane.HORIZONTAL_SPLIT, comp1, comp2, dividerLocation);
	}

	public static JSplitPane createSplitPane(int type, final JComponent comp1, final JComponent comp2, final double dividerLocation) {
		JSplitPane jsplitPane = new JSplitPane(type, comp1, comp2) {
			private static final long	serialVersionUID	= 1L;
			private boolean isFirstNonZero = true;
			private double dLocation = dividerLocation;
			
			public void setDividerLocation(int location) {
				if (isFirstNonZero && (location > 0)) {
					isFirstNonZero = false;
					super.setDividerLocation(location);
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setDividerLocation(dLocation);
						}
					});
				} else
					super.setDividerLocation(location);
			}
		};
		
		return jsplitPane;
	}
	
	public static void adjustFontSize(JPanel jpanel, int diff) {
		for (int i=0; i < jpanel.getComponentCount(); i++)
			adjustFontSize(jpanel.getComponent(i), diff);
	}
	
	public static void adjustFontSize(Container container, int diff) {
		if (container instanceof JPanel)
			adjustFontSize((JPanel) container, diff);
		else if (container instanceof JComponent)
			adjustFontSize((Component) container, diff);
		else {
			for (int i=0; i < container.getComponentCount(); i++)
				adjustFontSize(container.getComponent(i), diff);
		}
	}
	
	public static void setContentPane(JDialog jdialog, JComponent contentPane) {
		jdialog.getContentPane().setLayout(new GridLayout(1,1));
		jdialog.getContentPane().add(contentPane);
	}
	
	public static void setContentPane(JFrame jframe, JComponent contentPane) {
		jframe.getContentPane().setLayout(new GridLayout(1,1));
		jframe.getContentPane().add(contentPane);
	}
	
	public static void adjustFontSize(Component component, int diff) {
		Font originalFont = component.getFont();
		int originalSize = originalFont.getSize();
		int newSize = Math.max(originalSize + diff, 3);			// I guess 3 point is the smallest I'd really ever use
		Font newFont = originalFont.deriveFont(originalFont.getStyle(), newSize);
		component.setFont(newFont);
	}
	
//a test main...  keeping this around just in case we need to test this again later.
	public static void main(String args[]) {
		Color color = Color.GRAY;
		System.out.println("color.getRed(): " + color.getRed());
		System.out.println("color.getGreen(): " + color.getGreen());
		System.out.println("color.getBlue(): " + color.getBlue());
		System.out.println("getColorAsHex(color): " + getColorAsHex(color));
//		JFrame frame = new JFrame("testing utils");
//		frame.setVisible(true);
//		frame.setSize(50, 50);
//		setLocation(null, frame);
//		JFrame child = new JFrame ("child frame");
//		child.setVisible(true);
//		child.setSize(200,200);
//		setLocation (frame, child, SwingConstants.SOUTH_EAST, 1700.0);
	}
	
	public static Color makeTranslucent(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
	
	public static Box createVerticalBoxPanel() { return new Box(BoxLayout.Y_AXIS); }
	public static Box createHorizontalBoxPanel() { return new Box(BoxLayout.X_AXIS); }
	
	public static JRadioButton getSelectedRadioButton(ButtonGroup buttonGroup) {
		for (Enumeration<?> e=buttonGroup.getElements(); e.hasMoreElements(); ) {
			JRadioButton radioButton = (JRadioButton) e.nextElement();
			
			if (radioButton.getModel() == buttonGroup.getSelection()) 
				return radioButton;
		}
		
		return null;
	}

	public static int getSelectedRadioButtonIndex(ButtonGroup buttonGroup) {
		int position = 0;
		for (Enumeration<?> e=buttonGroup.getElements(); e.hasMoreElements(); position++) {
			JRadioButton radioButton = (JRadioButton) e.nextElement();
			
			if (radioButton.getModel() == buttonGroup.getSelection()) 
				return position;
		}
		
		return -1;
	}
	
	public static void makeAutoExit(Frame frame) {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { System.exit(0); }
		});
	}
	
	public static void recursivelyAddKeyListener(Component component, KeyListener keyListener) {
		component.addKeyListener(keyListener);
		
		if (component instanceof Container) {
			Container container = (Container) component;
			
			Component[] components = container.getComponents();
			
			for(int i = 0; i < components.length; i++) {
				try {
					if(components[i] instanceof Container) {
						recursivelyAddKeyListener((Container)components[i], keyListener);
						components[i].addKeyListener(keyListener);
					}
				} catch(Exception ex) {}
			}
		}
	}
	
	public static void makeAutoHideOnEscape(final JDialog jdialog) {
		KeyListener keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
					jdialog.setVisible(false);
				}
			}
		};
		
		jdialog.getRootPane().addKeyListener(keyListener);
		jdialog.getContentPane().addKeyListener(keyListener);
		recursivelyAddKeyListener(jdialog, keyListener);
	}
	
	public static int getInt(JTextComponent textComponent) {
		return getInt(textComponent, 0);
	}

	
	public static int getInt(JTextComponent textComponent, int defaultValue) {
		String text = textComponent.getText().trim();
	
		if (text.length() == 0)
			return defaultValue;
		
		return Integer.parseInt(textComponent.getText());
	}
	
	public static double getDouble(JTextComponent textComponent) {
		return getDouble(textComponent, 0);
	}

	
	public static double getDouble(JTextComponent textComponent, double defaultValue) {
		String text = textComponent.getText().trim();
	
		if (text.length() == 0)
			return defaultValue;
		
		return Double.parseDouble(textComponent.getText());
	}
	
	public static String getString(JTextComponent textComponent) {
		return getString(textComponent, "");
	}

	
	public static String getString(JTextComponent textComponent, String defaultValue) {
		String text = textComponent.getText().trim();
	
		if (text.length() == 0)
			return defaultValue;
		
		return text;
	}
	
	
	public static Cursor createCustomCursor(String cursorName, Applet applet, String imageFileName) {
		return createCustomCursor(cursorName, applet, imageFileName, new Point(1,1));
	}
	
	public static Cursor createCustomCursor(String cursorName, Applet applet, String imageFileName, Point hotspotPoint) {
		Image image = applet.getImage(applet.getCodeBase(), imageFileName);
		return createCustomCursor(cursorName, image, hotspotPoint);
	}
	
	public static Cursor createCustomCursor(String cursorName, Component rootComponent, String imageFileName) {
		return createCustomCursor(cursorName, rootComponent, imageFileName, new Point(1,1));
	}
	
	public static Cursor createCustomCursor(String cursorName, Component rootComponent, String imageFileName, Point hotspotPoint) {
		Image image = getImage(rootComponent, imageFileName);
		return createCustomCursor(cursorName, image, hotspotPoint);
	}
	
	public static Image getImage(Component rootComponent, String imageFileName) {
		//load the image with the Media Tracker
		MediaTracker tracker = new MediaTracker(rootComponent);
		Image image = new ImageIcon(imageFileName).getImage();
		tracker.addImage(image, 0);
		try {
			tracker.waitForID(0);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		return image;
	}
	
	public static Cursor createCustomCursor(String cursorName, Image image) {
		return createCustomCursor(cursorName, image, new Point(1,1));
	}

	public static Cursor createCustomCursor(String cursorName, Image image, Point hotSpotPoint) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.createCustomCursor(image, hotSpotPoint, cursorName);
	}
	
	public static JButton  createJButton(String label, ActionListener actionListener) {
		JButton jButton = new JButton(label);
		jButton.addActionListener(actionListener);
		return jButton;
	}
	
	public static final void appendLineLater(JTextArea textArea, String line) {
		appendLater(textArea, line + "\n");
	}
	
	public static final void appendLater(final JTextArea textArea, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(text);
			}
		});
	}
	
	public static final void setLater(final JTextComponent textComponent, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textComponent.setText(text);
			}
		});
	}
	
	public static GraphicsEnvironment getLocalGraphicsEnvironment() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment();
	}
	
	public static GraphicsDevice[] getScreenDevices() {
		return getLocalGraphicsEnvironment().getScreenDevices();
	}
	
	public static GraphicsDevice getScreenDevice(int screenNumber) {
		GraphicsDevice[] screenDevices = getScreenDevices();
		return screenDevices[screenNumber];
	}
	
	public static int getNumberOfScreens() {
		return getScreenDevices().length;
	}
	
	public static int getLastScreenIndex() {
		return getScreenDevices().length - 1;
	}
	
	public static GraphicsConfiguration getScreenGraphicsConfiguration(int screenNumber) {
		GraphicsDevice screenDevice = getScreenDevice(screenNumber);
		return screenDevice.getDefaultConfiguration();
	}
	
	public static Rectangle getScreenBounds(int screenNumber) {
		GraphicsConfiguration graphicsConfiguration = getScreenGraphicsConfiguration(screenNumber);
		return graphicsConfiguration.getBounds();
	}
	
	public static Rectangle getVirtualScreenBounds() {
		Rectangle virtualBounds = new Rectangle();
		
		for (int i=0; i < getNumberOfScreens(); i++)  
			virtualBounds = virtualBounds.union(getScreenBounds(i));
		
		return virtualBounds;
	}
	
	public static void analyzeScreen() {
		Rectangle virtualBounds = new Rectangle();

		GraphicsDevice gs[] = getScreenDevices();
		
		JFrame frame[][] = new JFrame[gs.length][];

		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			System.out.println("Device " + j + ": " + gd);
			GraphicsConfiguration[] gc = gd.getConfigurations();
			frame[j] = new JFrame[gc.length];

			for (int i = 0; i < gc.length; i++) {
				System.out.println("  Configuration " + i + ": " + gc[i]);
				System.out.println("    Bounds: " + gc[i].getBounds());
				virtualBounds = virtualBounds.union(gc[i].getBounds());
//				frame[j][i] = new JFrame("Config: " + i, gc[i]);
//				frame[j][i].setBounds(50, 50, 400, 100);
//				frame[j][i].setLocation((int) gc[i].getBounds().getX() + 50, (int) gc[i].getBounds().getY() + 50);
//				frame[j][i].getContentPane().add(new JTextArea("Config:\n" + gc[i]));
//				frame[j][i].setVisible(true);
				
				System.out.println("Config:\n" + gc[i]);
				
			}
			System.out.println("Overall bounds: " + virtualBounds);
		}
	}
	


	public static Window findWindow(Component comp) {
		while(comp != null) {
			if (comp instanceof Window)
				return (Window) comp;
			
			comp = comp.getParent();
		}
		return null;
	}
	
	public static JFrame findJFrame(Component comp) {
		Window window  = findWindow(comp);
		
		if ((window != null) && (window instanceof JFrame))
			return (JFrame) window;
		else
			return null;
	}
	
}

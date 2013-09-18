

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;

public class ConnectKGUI extends JFrame {
	/**
	 * 
	 */
	private JButton[][] buttons;
	private Color[] playerColors;
	private JLabel statusLabel;
	
	public ConnectKGUI(BoardModel model){
		super("ConnectK");
		setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());		
		setLayout(new BorderLayout());
		
		//make a status bar
		JPanel statusBar = new JPanel();
		statusBar.setPreferredSize(new Dimension(getWidth(), 16));
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		statusLabel = new JLabel("status");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusBar.add(statusLabel);
		add(statusBar, BorderLayout.SOUTH);
		
		//close program on closing window
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Create menu bar and add it to the JFrame
        CKMenuBar menuBar = new CKMenuBar(this);
        setJMenuBar(menuBar);

		initializeGame(model.width, model.height);
		setSize(500, 400 + menuBar.getHeight() + statusBar.getHeight());
        setVisible(true);
	}

	private void initializeGame(int width, int height){
		setStatus("");
		buttons = new JButton[width][height];
		playerColors = new Color[]{null, Color.RED, Color.BLUE};
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(height,width));
		for(int j=height-1; j>=0; --j){
			for(int i=0; i<width; ++i){
				JButton button = new JButton("" + i + ":" + j);
				buttons[i][j] = button;
				button.putClientProperty("x", new Integer(i));
				button.putClientProperty("y", new Integer(j));
				panel.add(button);
			}
		}
		add(panel, BorderLayout.CENTER);
	}
	
	public void setStatus(String s){
		statusLabel.setText(s);
	}
	
	public void placePiece(java.awt.Point p, byte player){
		buttons[p.x][p.y].setBackground(playerColors[player]); 
	}

	public void highlightSpaces(List<Point> winningSpaces) {
		for(Point p: winningSpaces)
			buttons[p.x][p.y].setVisible(!buttons[p.x][p.y].isVisible());
	}
	
	public void addButtonListener(ActionListener l){
		for(int i=0; i<buttons.length; ++i)
			for(int j=0; j<(buttons[0].length); ++j)
				buttons[i][j].addActionListener(l);
	}

	public void newGameDialog(ConnectKGUI parentFrame) {
		final JDialog ngDialog = new JDialog(this, "New Game");
		final ConnectKGUI pf = parentFrame;
		JPanel cp = new JPanel();
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
		JPanel rulesPanel = new JPanel();
		JPanel player1Panel = new JPanel();
		JPanel player2Panel = new JPanel();
		JPanel goPanel = new JPanel();
		cp.add(rulesPanel);
		cp.add(player1Panel);
		cp.add(player2Panel);
		cp.add(goPanel);
		rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.X_AXIS));
		player1Panel.setLayout(new BoxLayout(player1Panel, BoxLayout.X_AXIS));
		player2Panel.setLayout(new BoxLayout(player2Panel, BoxLayout.X_AXIS));
		goPanel.setLayout(new BoxLayout(goPanel, BoxLayout.X_AXIS));
		
		JLabel widthLabel = new JLabel("Width:");
		JLabel heightLabel = new JLabel("Height:");
		JLabel kLengthLabel = new JLabel("K:");
		JLabel gravityLabel = new JLabel("Gravity:");
		final JSpinner width = new JSpinner(new SpinnerNumberModel(7,1,99,1));
		final JSpinner height = new JSpinner(new SpinnerNumberModel(6,1,99,1));
		final JSpinner kLength = new JSpinner(new SpinnerNumberModel(4,1,99,1));
		final JSpinner gravity = new JSpinner(new SpinnerListModel(new String[]{"On", "Off"}));
		rulesPanel.add(widthLabel);
		rulesPanel.add(width);
		rulesPanel.add(heightLabel);
		rulesPanel.add(height);
		rulesPanel.add(kLengthLabel);
		rulesPanel.add(kLength);
		rulesPanel.add(gravityLabel);
		rulesPanel.add(gravity);
		
		final JFileChooser fc = new javax.swing.JFileChooser(new java.io.File("."));

		JRadioButton player1Human = new JRadioButton("Player 1 Human");
		player1Human.setActionCommand("player1Human");
		player1Human.setSelected(true);
		JRadioButton player1AI = new JRadioButton("Player 1 AI");
		player1AI.setActionCommand("player1AI");
		final ButtonGroup player1Type = new ButtonGroup();
		player1Type.add(player1Human);
		player1Type.add(player1AI);
		player1Panel.add(player1Human);
		player1Panel.add(player1AI);
		final JComboBox player1AIOptions = new JComboBox();
		final JComboBox player2AIOptions = new JComboBox();
		try {
			player1AIOptions.addItem((Class<? extends CKPlayer>) Class.forName("connectK.DummyAI"));
			player2AIOptions.addItem((Class<? extends CKPlayer>) Class.forName("connectK.DummyAI"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		player1Panel.add(player1AIOptions);
		
		JRadioButton player2Human = new JRadioButton("Player 2 Human");
		player2Human.setActionCommand("player2Human");
		JRadioButton player2AI = new JRadioButton("Player 2 AI");
		player2AI.setActionCommand("player2AI");
		player2AI.setSelected(true);
		final ButtonGroup player2Type = new ButtonGroup();
		player2Type.add(player2Human);
		player2Type.add(player2AI);
		player2Panel.add(player2Human);
		player2Panel.add(player2AI);
		
		player2Panel.add(player2AIOptions);
		
		fc.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					java.io.File file = ((JFileChooser) arg0.getSource()).getSelectedFile();
					Class aClass = Class.forName("connectK." + file.getName().split(".class")[0]);
					if(CKPlayer.class.isAssignableFrom(aClass)){
						Class<? extends CKPlayer> aiClass = aClass;
						player1AIOptions.addItem(aiClass);
						player1AIOptions.setSelectedItem(aiClass);
						player2AIOptions.addItem(aiClass);
						player2AIOptions.setSelectedItem(aiClass);
					} else{
						throw new ClassCastException("Selected class (" + file.getName() + ") does not extend CKPlayer.");
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		JButton addAI = new JButton("Add AI");
		addAI.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fc.showOpenDialog(pf);
			}
		});
		
		JButton newGame = new JButton("New Game");
		newGame.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final BoardModel model = BoardModel.newBoard((Integer) width.getValue(), (Integer) height.getValue(), (Integer) kLength.getValue(), gravity.getValue().equals("On"));
				CKPlayer player1 = null;
				CKPlayer player2 = null;
				if(player1Type.getSelection().getActionCommand().equals("player1Human"))
					player1 = new GUIPlayer((byte) 1, model);
				else
					try {
						player1 = ((Class<? extends CKPlayer>) player1AIOptions.getSelectedItem()).getConstructor(Byte.TYPE, BoardModel.class).newInstance((byte) 1, model);
					} catch (Exception e) {
						e.printStackTrace();
						
					}
				if(player2Type.getSelection().getActionCommand().equals("player2Human"))
					player2 = new GUIPlayer((byte) 2, model);
				else
					try {
						player2 = ((Class<? extends CKPlayer>) player2AIOptions.getSelectedItem()).getConstructor(Byte.TYPE, BoardModel.class).newInstance((byte) 2, model);
					} catch (Exception e) {
						e.printStackTrace();
					}
				
				ngDialog.dispose();
				pf.dispose();
				final ConnectKGUI gui = new ConnectKGUI(model);
				if(player1 instanceof GUIPlayer)
					gui.addButtonListener((GUIPlayer) player1);
				if(player2 instanceof GUIPlayer)
					gui.addButtonListener((GUIPlayer) player2);
				final CKPlayer player1Final = player1;
				final CKPlayer player2Final = player2;
				new Thread(){
					public void run(){(new ConnectK(model, player1Final, player2Final, gui)).play();}
				}.start();
			}
        });
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ngDialog.dispose();
			}
        });
		goPanel.add(addAI);
		goPanel.add(newGame);
		goPanel.add(cancel);
		ngDialog.setContentPane(cp);
		ngDialog.setSize(400,150);
		ngDialog.setVisible(true);
	}
	
	public static void main(String[] args){
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");//a cross platform L&F; other L&F had some compatibility issues
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		BoardModel model = BoardModel.newBoard(7, 6, 4, false);
		CKPlayer player1 = new GUIPlayer((byte) 1, model);
		CKPlayer player2 = null;
		if(args.length > 0){
			URLClassLoader classLoader;
			try {
				File file = new File(args[0]);
				if(file.getParent() == null)
					classLoader = URLClassLoader.newInstance(new URL[] { new File(".").toURI().toURL() });
				else
					classLoader = URLClassLoader.newInstance(new URL[] { file.getParentFile().toURI().toURL() });
				Class<?> cls = Class.forName(file.getName().split(".class")[0], true, classLoader);
				if(CKPlayer.class.isAssignableFrom(cls)){
					@SuppressWarnings("unchecked")
					Constructor<? extends CKPlayer> c = (Constructor<? extends CKPlayer>) cls.getConstructor(Byte.TYPE, BoardModel.class);
					player2 = c.newInstance((byte) 2, model);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(player2 == null)
			player2 = new GUIPlayer((byte) 2, model);
		ConnectKGUI gui = new ConnectKGUI(model);
		gui.addButtonListener((GUIPlayer) player1);
		if(player2 instanceof GUIPlayer)
			gui.addButtonListener((GUIPlayer) player2);
		(new ConnectK(model, player1, player2, gui)).play();
	}
}

class CKMenuBar extends JMenuBar{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6040805383728634794L;
	private ConnectKGUI pf;
	
	public CKMenuBar(ConnectKGUI parentFrame){
		pf = parentFrame;
		JMenu fileMenu;
        JMenuItem menuItem;
        // Build the File menu.
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        add(fileMenu);

        // attach a group of JMenuItems to it
        menuItem = new JMenuItem("New", KeyEvent.VK_N);
        menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pf.newGameDialog(pf);
			}
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
        menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
        });
        fileMenu.add(menuItem);
	}
}

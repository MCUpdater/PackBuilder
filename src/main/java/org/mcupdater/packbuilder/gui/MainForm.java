package org.mcupdater.packbuilder.gui;

import org.mcupdater.api.Version;
import org.mcupdater.model.*;
import org.mcupdater.util.ServerPackParser;

import java.util.List;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MainForm {

	private static MainForm instance;
	private JFrame frameMain;
	private JDesktopPane desktop;

	public MainForm() {
		instance = this;

		initGui();
		bindLogic();
		frameMain.setVisible(true);
	}

	private void initGui() {
		frameMain = new JFrame();
		frameMain.setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		frameMain.setTitle("MCU PackBuilder " + Version.VERSION + Version.BUILD_LABEL);
		frameMain.setBounds(100, 100, 1175, 592);
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menuBar = new JMenuBar();
		{
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			menuBar.add(fileMenu);
		}
		frameMain.setJMenuBar(menuBar);

		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.PAGE_AXIS));
		{
			JButton btnA = new JButton("Test 1");
			JButton btnB = new JButton("Test 2");
			JButton btnC = new JButton("Test 3");
			JButton btnD = new JButton("Test 4");
			panelLeft.add(btnA);
			panelLeft.add(btnB);
			panelLeft.add(btnC);
			panelLeft.add(btnD);
		}
		frameMain.add(panelLeft, BorderLayout.WEST);

		desktop = new JDesktopPane();
		frameMain.add(desktop, BorderLayout.CENTER);

		desktop.add(new TestInternal());
	}

	private void bindLogic() {

	}

	public static MainForm getInstance() {
		return instance;
	}

	private class TestInternal extends JInternalFrame {
		public TestInternal() {
			super("Test Window",true,true,true,true);
			setSize(600,300);
			setLocation(30, 30);
			this.setVisible(true);
			this.setLayout(new GridBagLayout());
			DefaultMutableTreeNode top;
			top = new DefaultMutableTreeNode("/");
			{
				List<RawServer> pack = ServerPackParser.loadFromURL("https://files.mcupdater.com/official_packs/MCU-Prime/ServerPack.xml");
				if (pack != null) {
					for (RawServer server : pack) {
						DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(server);
						for (IPackElement packElement : server.getPackElements()) {
							DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(packElement);
							serverNode.add(newNode);
							if (packElement instanceof Module) {
								if (((Module) packElement).hasSubmodules()) {
									for (GenericModule submod : ((Module) packElement).getSubmodules()) {
										newNode.add(new DefaultMutableTreeNode(submod));
									}
								}
								if (((Module) packElement).hasConfigs()) {
									for (ConfigFile config : ((Module) packElement).getConfigs()) {
										newNode.add(new DefaultMutableTreeNode(config));
									}
								}
							}
						}
						top.add(serverNode);
					}
				}
				pack = ServerPackParser.loadFromURL("http://files.mcupdater.com/example/forge.php?mc=1.10.2&forge=12.18.2.2125");
				if (pack != null) {
					for (RawServer server : pack) {
						DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(server);
						for (IPackElement packElement : server.getPackElements()) {
							DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(packElement);
							serverNode.add(newNode);
							if (packElement instanceof Module) {
								if (((Module) packElement).hasSubmodules()) {
									for (GenericModule submod : ((Module) packElement).getSubmodules()) {
										newNode.add(new DefaultMutableTreeNode(submod));
									}
								}
								if (((Module) packElement).hasConfigs()) {
									for (ConfigFile config : ((Module) packElement).getConfigs()) {
										newNode.add(new DefaultMutableTreeNode(config));
									}
								}
							}
						}
						top.add(serverNode);
					}
				}
			}
			GridBagConstraints scrollConstraints = new GridBagConstraints();
			scrollConstraints.fill = GridBagConstraints.BOTH;
			scrollConstraints.weightx = 0.25;
			scrollConstraints.weighty = 1;
			JTree tree = new JTree(top);
			tree.addTreeSelectionListener(e -> {
				Object userObject = ((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).getUserObject();
				if (userObject instanceof Module) {
					System.out.println(((Module) userObject).getId());
				} else {
					System.out.println(userObject.getClass());
				}
			});
			this.add(new JScrollPane(tree), scrollConstraints);
			GridBagConstraints x = new GridBagConstraints();
			x.gridwidth = 2;
			x.fill = GridBagConstraints.BOTH;
			x.weightx = 1;
			x.weighty = 1;
			JPanel panel = new JPanel();
			this.add(panel, x);
		}
	}
}

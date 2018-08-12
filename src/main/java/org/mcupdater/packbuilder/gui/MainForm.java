package org.mcupdater.packbuilder.gui;

import org.jdesktop.swingx.JXTable;
import org.mcupdater.api.Version;
import org.mcupdater.model.*;
import org.mcupdater.model.Module;
import org.mcupdater.util.ServerPackParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

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
		JPanel detailPanel = new JPanel();

		public TestInternal() {
			super("Test Window",true,true,true,true);
			setSize(600,300);
			setLocation(30, 30);
			this.setVisible(true);
			this.setLayout(new GridBagLayout());
			DefaultMutableTreeNode top;
			top = new DefaultMutableTreeNode("/");
			{
				List<RawServer> pack = ServerPackParser.loadFromURL("https://files.mcupdater.com/optional/ServerPack.xml");
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
				if (userObject instanceof GenericModule) {
					GenericModule selected = (GenericModule) userObject;
					System.out.println(selected.getId());
					detailPanel.removeAll();
					GroupLayout layout = new GroupLayout(detailPanel);
					detailPanel.setLayout(layout);
					GroupLayout.Group colLabel = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
					GroupLayout.Group colContent = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

					GroupLayout.Group rowID = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblID = new JLabel("ID:");
					JTextField txtID = new JTextField(selected.getId());
					colLabel.addComponent(lblID);
					colContent.addComponent(txtID);
					rowID.addComponent(lblID).addComponent(txtID);

					GroupLayout.Group rowName = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblName = new JLabel("Name:");
					JTextField txtName = new JTextField(selected.getName());
					colLabel.addComponent(lblName);
					colContent.addComponent(txtName);
					rowName.addComponent(lblName).addComponent(txtName);

					GroupLayout.Group rowMD5 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblMD5 = new JLabel("MD5:");
					JTextField txtMD5 = new JTextField(selected.getMD5());
					colLabel.addComponent(lblMD5);
					colContent.addComponent(txtMD5);
					rowMD5.addComponent(lblMD5).addComponent(txtMD5);

					GroupLayout.Group rowDepends = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblDepends = new JLabel("Depends:");
					JTextField txtDepends = new JTextField(selected.getDepends());
					colLabel.addComponent(lblDepends);
					colContent.addComponent(txtDepends);
					rowDepends.addComponent(lblDepends).addComponent(txtDepends);

					GroupLayout.Group rowModType = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblModType = new JLabel("ModType:");
					JTextField txtModType = new JTextField(selected.getModType().toString());
					colLabel.addComponent(lblModType);
					colContent.addComponent(txtModType);
					rowModType.addComponent(lblModType).addComponent(txtModType);

					GroupLayout.Group rowFilename = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblFilename = new JLabel("Filename:");
					JTextField txtFilename = new JTextField(selected.getFilename());
					colLabel.addComponent(lblFilename);
					colContent.addComponent(txtFilename);
					rowFilename.addComponent(lblFilename).addComponent(txtFilename);

					GroupLayout.Group rowMeta = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblMeta = new JLabel("Meta:");
					JXTable tblMeta = new JXTable(toTableModel(selected.getMeta()));

					colLabel.addComponent(lblMeta);
					colContent.addComponent(tblMeta);
					rowMeta.addComponent(lblMeta).addComponent(tblMeta);

					layout.setAutoCreateContainerGaps(true);
					layout.setAutoCreateGaps(true);
					layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(colLabel).addGroup(colContent));
					layout.setVerticalGroup(layout.createSequentialGroup().addGroup(rowID).addGroup(rowName).addGroup(rowMD5).addGroup(rowDepends).addGroup(rowModType).addGroup(rowFilename).addGroup(rowMeta));
					detailPanel.revalidate();
					detailPanel.repaint();
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
			this.add(new JScrollPane(detailPanel), x);
		}
	}

	private TableModel toTableModel(Map<String, String> nvpairs) {
		DefaultTableModel model = new DefaultTableModel(
				new Object[] { "Key", "Value" }, 0
		);
		for (Map.Entry<?,?> entry : nvpairs.entrySet()) {
			model.addRow(new Object[] { entry.getKey(), entry.getValue() });
		}
		return model;
	}
}

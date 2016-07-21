package org.mcupdater.packbuilder.gui;

import org.mcupdater.api.Version;

import javax.swing.*;
import java.awt.*;

public class MainForm {

	private static MainForm instance;
	private JFrame frameMain;

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

		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		panelLeft.setLayout(new BorderLayout(0, 0));
		{

		}
		frameMain.getContentPane().add(panelLeft, BorderLayout.WEST);
	}

	private void bindLogic() {

	}

	public static MainForm getInstance() {
		return instance;
	}
}

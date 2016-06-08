package org.mcupdater.packbuilder;

import org.mcupdater.packbuilder.gui.MainForm;
import org.mcupdater.packbuilder.gui.TextContextMenu;
import org.mcupdater.util.MCUpdater;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {

	public static void main(final String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		File basePath;
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			basePath = new File(new File(System.getenv("APPDATA")),".MCUpdater");
		} else if(System.getProperty("os.name").startsWith("Mac"))
		{
			basePath = new File(new File(new File(new File(System.getProperty("user.home")),"Library"),"Application Support"),"MCUpdater");
		}
		else
		{
			basePath = new File(new File(System.getProperty("user.home")),".MCUpdater");
		}
		final MCUpdater mcu = MCUpdater.getInstance(basePath);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						System.out.println("Installed L&F: " + info.getName());
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
					if (UIManager.getLookAndFeel().getName().equals("Metal")) {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					UIManager.addAuxiliaryLookAndFeel(new LookAndFeel() {
						private final UIDefaults defaults = new UIDefaults() {
							@Override
							public javax.swing.plaf.ComponentUI getUI(JComponent c) {
								if (c instanceof javax.swing.text.JTextComponent) {
									if (c.getClientProperty(this) == null) {
										c.setComponentPopupMenu(TextContextMenu.INSTANCE);
										c.putClientProperty(this, Boolean.TRUE);
									}
								}
								return null;
							}
						};
						@Override public UIDefaults getDefaults() { return defaults; }
						@Override public String getID() { return "TextContextMenu"; }
						@Override public String getName() { return getID(); }
						@Override public String getDescription() { return getID(); }
						@Override public boolean isNativeLookAndFeel() { return false; }
						@Override public boolean isSupportedLookAndFeel() { return true; }
					});
					new MainForm();
				} catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}
}

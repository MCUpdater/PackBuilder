package org.mcupdater.packbuilder.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.api.Version;
import org.mcupdater.util.MCUpdater;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainForm extends Application {

	public static Logger LOGGER;
	private static MainForm instance;
	//private JFrame frameMain;
	//private JTabbedPane desktop;

	public MainForm() {
		instance = this;
		this.LOGGER = Logger.getLogger("PackBuilder");
		this.LOGGER.setParent(MCUpdater.apiLogger);
		this.LOGGER.setUseParentHandlers(true);
		this.LOGGER.setLevel(Level.ALL);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		//frameMain.setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		//frameMain.setBounds(100, 100, 1175, 592);
		//frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//JMenuBar menuBar = new JMenuBar();
		//{
		//	JMenu fileMenu = new JMenu("File");
		//	fileMenu.setMnemonic(KeyEvent.VK_F);
		//	menuBar.add(fileMenu);
		//}
		//frameMain.setJMenuBar(menuBar);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainForm.fxml"));
		Pane root = fxmlLoader.load();

		Scene scene = new Scene(root, 1175, 592);
		primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("mcu-icon.png")));
		primaryStage.setTitle("MCU PackBuilder " + Version.VERSION + Version.BUILD_LABEL);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static MainForm getInstance() {
		return instance;
	}

	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}
}

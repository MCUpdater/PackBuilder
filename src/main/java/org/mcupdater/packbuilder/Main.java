package org.mcupdater.packbuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.mcupdater.Version;
import org.mcupdater.util.MCUpdater;

public class Main extends Application
{
	public static void main(String[] args) {
		OptionParser optParser = new OptionParser();
		OptionSet options = optParser.parse(args);
		MCUpdater.getInstance();
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainDialog.fxml"));
		Parent root = (Parent) loader.load();

		Scene scene = new Scene(root, 1175, 600);

		stage.setTitle("MCUpdater PackBuilder " + Version.VERSION);
		stage.setScene(scene);
		stage.getIcons().add(new Image(Main.class.getResourceAsStream("mcu-icon.png")));
		stage.show();
	}
}

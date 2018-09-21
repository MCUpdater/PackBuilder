package org.mcupdater.packbuilder.gui.wrappers;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.mcupdater.model.Import;
import org.mcupdater.model.Module;
import org.mcupdater.model.ServerList;
import org.mcupdater.packbuilder.gui.ModifiableElement;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;

import java.util.ArrayList;

public class ImportWrapper extends ModifiableElement {
	private Import element;
	private TextField fieldUrl = new TextField();
	private TextField fieldServerId = new TextField();
	private Button buttonTest = new Button("Test");
	private ListView listTest = new ListView();

	public ImportWrapper(Import source, GridPane grid, String mcVersion) {
		this.element = source;
		this.gui = grid;
		int row = 0;
		gui.addRow(row++, new Label("URL:"), fieldUrl);
		gui.addRow(row++, new Label("ID:"), fieldServerId);
		gui.add(new Separator(),0,row++,2,1);
		gui.addRow(row++, buttonTest);
		gui.add(listTest, 0, row++, 2, 1);
		for (Node child : gui.getChildren()) {
			if (!(child instanceof Label)){
				child.setDisable(true);
				if (!(child instanceof CheckBox)) {
					GridPane.setFillWidth(child, true);
				}
			}
		}
		reload();
		buttonTest.setOnAction(event -> {
			listTest.getItems().clear();
			ServerList sl = ServerPackParser.loadFromURL(fieldUrl.getText(), fieldServerId.getText(), mcVersion);
			for (Module mod : MCUpdater.getInstance().sortMods(new ArrayList<>(sl.getModules().values()))) {
				listTest.getItems().add(mod.getFriendlyName());
			}
		});
	}

	@Override
	public void saveUpdates() {
		super.saveUpdates();
		element.setUrl(fieldUrl.getText());
		element.setServerId(fieldServerId.getText());
	}

	@Override
	public void reload() {
		fieldUrl.setText(element.getUrl());
		fieldServerId.setText(element.getServerId());
	}
}

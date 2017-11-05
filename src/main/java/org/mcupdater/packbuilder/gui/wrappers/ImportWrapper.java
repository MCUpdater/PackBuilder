package org.mcupdater.packbuilder.gui.wrappers;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.mcupdater.model.Import;
import org.mcupdater.packbuilder.gui.ModifiableElement;

public class ImportWrapper extends ModifiableElement {
	private Import element;
	private TextField fieldUrl = new TextField();
	private TextField fieldServerId = new TextField();

	public ImportWrapper(Import source, GridPane grid) {
		this.element = source;
		this.gui = grid;
		int row = 0;
		gui.addRow(row++, new Label("URL:"), fieldUrl);
		gui.addRow(row++, new Label("ID:"), fieldServerId);
		for (Node child : gui.getChildren()) {
			if (!(child instanceof Label)){
				child.setDisable(true);
				if (!(child instanceof CheckBox)) {
					GridPane.setFillWidth(child, true);
				}
			}
		}
		reload();

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

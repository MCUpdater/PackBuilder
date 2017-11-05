package org.mcupdater.packbuilder.gui.wrappers;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.mcupdater.model.ConfigFile;
import org.mcupdater.packbuilder.gui.ModifiableElement;

public class ConfigFileWrapper extends ModifiableElement {
	private ConfigFile element;

	private TextField fieldUrl = new TextField();
	private TextField fieldPath = new TextField();
	private TextField fieldMd5 = new TextField();
	private CheckBox fieldNoOverwrite = new CheckBox();

	public ConfigFileWrapper(ConfigFile source, GridPane grid) {
		this.gui = grid;
		this.element = source;
		int row = 0;
		gui.addRow(row++, new Label("URL:"), fieldUrl);
		gui.addRow(row++, new Label("Path:"), fieldPath);
		gui.addRow(row++, new Label("MD5:"), fieldMd5);
		gui.addRow(row++, new Label("Do Not Overwrite:"), fieldNoOverwrite);
		for (Node child : gui.getChildren()) {
			if (!(child instanceof Label)) {
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
		element.setPath(fieldPath.getText());
		element.setMD5(fieldMd5.getText());
		element.setNoOverwrite(fieldNoOverwrite.isSelected());
	}

	@Override
	public void reload() {
		fieldUrl.setText(element.getUrl());
		fieldPath.setText(element.getPath());
		fieldMd5.setText(element.getMD5());
		fieldNoOverwrite.setSelected(element.isNoOverwrite());
	}
}

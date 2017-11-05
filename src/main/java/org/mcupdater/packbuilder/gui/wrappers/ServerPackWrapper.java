package org.mcupdater.packbuilder.gui.wrappers;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.mcupdater.model.ServerPack;
import org.mcupdater.packbuilder.gui.ModifiableElement;

public class ServerPackWrapper extends ModifiableElement {
	private ServerPack element;

	private TextField fieldXslt = new TextField();

	public ServerPackWrapper(ServerPack source, GridPane grid) {
		this.element = source;
		this.gui = grid;
		int row = 0;
		gui.addRow(row++, new Label("XSLT:"), fieldXslt);
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
		element.setXsltPath(fieldXslt.getText());
	}

	@Override
	public void reload() {
		fieldXslt.setText(element.getXsltPath());
	}
}

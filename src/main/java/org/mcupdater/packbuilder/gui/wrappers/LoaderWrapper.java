package org.mcupdater.packbuilder.gui.wrappers;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.mcupdater.model.Loader;
import org.mcupdater.packbuilder.gui.ModifiableElement;

import java.text.DecimalFormat;
import java.text.ParsePosition;

public class LoaderWrapper extends ModifiableElement {
	private Loader element;
	private ChoiceBox<String> fieldType = new ChoiceBox(FXCollections.observableArrayList(Loader.getValidTypes()));
	private TextField fieldVersion = new TextField();
	private TextField fieldLoadOrder = new TextField();
	private DecimalFormat format = new DecimalFormat("0");

	public LoaderWrapper(Loader source, GridPane grid, String mcVersion) {
		this.element = source;
		this.gui = grid;
		int row = 0;
		gui.addRow(row++, new Label("Type:"), fieldType);
		gui.addRow(row++, new Label("Version:"), fieldVersion);
		fieldLoadOrder.setTextFormatter( new TextFormatter<Object>(c ->{
			if (c.getControlNewText().isEmpty()) {
				return c;
			}

			ParsePosition parsePosition = new ParsePosition(0);
			Object object = format.parse( c.getControlNewText(), parsePosition);

			if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
				return null;
			} else {
				return c;
			}
		}));
		gui.addRow(row++, new Label("Load Order:"), fieldLoadOrder);
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
		element.setType(fieldType.getSelectionModel().getSelectedItem());
		element.setVersion(fieldVersion.getText());
		element.setLoadOrder(Integer.valueOf(fieldLoadOrder.getText()));
	}

	@Override
	public void reload() {
		fieldType.getSelectionModel().select(element.getType());
		fieldVersion.setText(element.getVersion());
		fieldLoadOrder.setText(String.valueOf(element.getLoadOrder()));
	}
}

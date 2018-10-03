package org.mcupdater.packbuilder.gui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public abstract class ModifiableElement {
	protected GridPane gui;

	public void saveUpdates() {
		updateLocks(true);
	}

	public void beginUpdates() {
		updateLocks(false);
	}

	public void cancelUpdates() {
		updateLocks(true);
		reload();
	}

	abstract public void reload();

	protected void updateLocks(boolean lock) {
		for (Node child : gui.getChildren()) {
			if (!(child instanceof Label)) {
				child.setDisable(lock);
			}
		}
	}

	protected Node loadResource(String resourceName) {
		return new ImageView(new Image(MainForm.class.getResourceAsStream(resourceName)));
	}

}

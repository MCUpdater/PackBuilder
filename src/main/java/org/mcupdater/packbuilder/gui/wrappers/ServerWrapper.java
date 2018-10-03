package org.mcupdater.packbuilder.gui.wrappers;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.mcupdater.model.RawServer;
import org.mcupdater.packbuilder.gui.ModifiableElement;

public class ServerWrapper extends ModifiableElement {
	private RawServer element;

	private TextField fieldServerId = new TextField();
	private CheckBox fieldAbstract = new CheckBox();
	private TextField fieldName = new TextField();
	private TextField fieldNewsUrl = new TextField();
	private TextField fieldIconUrl = new TextField();
	private TextField fieldVersion = new TextField();
	private TextField fieldAddress = new TextField();
	private CheckBox fieldGenerateList = new CheckBox();
	private CheckBox fieldAutoConnect = new CheckBox();
	private TextField fieldRevision = new TextField();
	private TextField fieldMainClass = new TextField();
	private TextField fieldLauncherType = new TextField();
	private TextField fieldLibOverrides = new TextField();
	private TextField fieldServerClass = new TextField();

	public ServerWrapper(RawServer source, GridPane grid) {
		this.element = source;
		this.gui = grid;
		int row = 0;
		gui.addRow(row++, new Label("ID:"), fieldServerId);
		gui.addRow(row++, new Label("Abstract:"), fieldAbstract);
		gui.addRow(row++, new Label("Name:"), fieldName);
		gui.addRow(row++, new Label("News URL:"), fieldNewsUrl);
		gui.addRow(row++, new Label("Icon URL:"), fieldIconUrl);
		gui.addRow(row++, new Label("MC Version:"), fieldVersion);
		gui.addRow(row++, new Label("Server Address:"), fieldAddress);
		gui.addRow(row++, new Label("Generate Server List:"), fieldGenerateList);
		gui.addRow(row++, new Label("AutoConnect:"), fieldAutoConnect);
		gui.addRow(row++, new Label("Revision:"), fieldRevision);
		gui.addRow(row++, new Label("Main Class:", loadResource("stop.png")), fieldMainClass);
		gui.addRow(row++, new Label("Server Class:", loadResource("stop.png")), fieldServerClass);
		gui.addRow(row++, new Label("Launcher Type:", loadResource("stop.png")), fieldLauncherType);
		gui.addRow(row++, new Label("Lib Overrides:", loadResource("stop.png")), fieldLibOverrides);
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
		this.element.setServerId(fieldServerId.getText());
		this.element.setFakeServer(fieldAbstract.isSelected());
		this.element.setName(fieldName.getText());
		this.element.setNewsUrl(fieldNewsUrl.getText());
		this.element.setIconUrl(fieldIconUrl.getText());
		this.element.setVersion(fieldVersion.getText());
		this.element.setAddress(fieldAddress.getText());
		this.element.setGenerateList(fieldGenerateList.isSelected());
		this.element.setAutoConnect(fieldAutoConnect.isSelected());
		this.element.setRevision(fieldRevision.getText());
		this.element.setMainClass(fieldMainClass.getText());
		this.element.setLauncherType(fieldLauncherType.getText());
		this.element.setRawOverrides(fieldLibOverrides.getText());
		this.element.setServerClass(fieldServerClass.getText());
	}

	@Override
	public void reload() {
		fieldServerId.setText(this.element.getServerId());
		fieldAbstract.setSelected(this.element.isFakeServer());
		fieldName.setText(this.element.getName());
		fieldNewsUrl.setText(this.element.getNewsUrl());
		fieldIconUrl.setText(this.element.getIconUrl());
		fieldVersion.setText(this.element.getVersion());
		fieldAddress.setText(this.element.getAddress());
		fieldGenerateList.setSelected(this.element.isGenerateList());
		fieldAutoConnect.setSelected(this.element.isAutoConnect());
		fieldRevision.setText(this.element.getRevision());
		fieldMainClass.setText(this.element.getMainClass());
		fieldLauncherType.setText(this.element.getLauncherType());
		fieldLibOverrides.setText(this.element.getRawOverrides());
		fieldServerClass.setText(this.element.getServerClass_Raw());
	}
}

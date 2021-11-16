package org.mcupdater.packbuilder.gui.wrappers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.model.*;
import org.mcupdater.model.Module;
import org.mcupdater.packbuilder.gui.ModifiableElement;
import org.mcupdater.util.CurseModCache;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.PathWalker;
import org.mcupdater.util.ServerDefinition;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ModuleWrapper extends ModifiableElement {
	private final String mcVersion;
	private TextField fieldName = new TextField();
	private TextField fieldId = new TextField();
	private TextField fieldDepends = new TextField();
	private ChoiceBox<ModSide> fieldSide = new ChoiceBox<>(FXCollections.observableArrayList(ModSide.values()));
	private TableView<PrioritizedURL> fieldUrls;
	private TextField fieldCurseProject = new TextField();
	private TextField fieldCurseFile = new TextField();
	private ChoiceBox<CurseProject.ReleaseType> fieldCurseType = new ChoiceBox<>(FXCollections.observableArrayList(CurseProject.ReleaseType.values()));
	private CheckBox fieldCurseAuto = new CheckBox();
	private TextField fieldPrefix = new TextField();
	private TextField fieldModPath = new TextField();
	private TextField fieldSize = new TextField();
	private CheckBox fieldRequired = new CheckBox();
	private CheckBox fieldIsDefault = new CheckBox();
	private ChoiceBox<ModType> fieldModType = new ChoiceBox<>(FXCollections.observableArrayList(ModType.values()));
	private CheckBox fieldInRoot = new CheckBox();
	private TextField fieldOrder = new TextField();
	private CheckBox fieldKeepMeta = new CheckBox();
	private TextField fieldLaunchArgs = new TextField();
	private TextField fieldJreArgs = new TextField();
	private TextField fieldMD5 = new TextField();
	private TableView<Map.Entry<String,String>> fieldMeta;
	HashMap<String,String> localMeta;
	private GenericModule element;

	public ModuleWrapper(GenericModule source, GridPane grid, String mcVersion) {
		this.element = source;
		this.gui = grid;
		this.mcVersion = mcVersion;
		int row = 0;
		fieldUrls = tableUrlListBuilder(element.getPrioritizedUrls());
		localMeta = element.getMeta();
		fieldMeta = tableMapBuilder();
		gui.addRow(row++, new Label("Name:"), fieldName);
		gui.addRow(row++, new Label("Mod ID:"), fieldId);
		gui.addRow(row++, new Label("Dependencies:"), fieldDepends);
		gui.addRow(row++, new Label("Side:"), fieldSide);
		VBox groupUrls = new VBox();
		{
			HBox groupUrlControls = new HBox();
			{
				Button addUrl = new Button("Add");
				addUrl.setOnAction(e -> {
					fieldUrls.getItems().add(new PrioritizedURL("",0));
				});
				Button deleteUrl = new Button("Delete");
				deleteUrl.setOnAction(e -> {
					if (fieldUrls.getSelectionModel().getSelectedIndex() >= 0) {
						fieldUrls.getItems().remove(fieldUrls.getSelectionModel().getSelectedIndex());
					}
				});
				groupUrlControls.getChildren().addAll(addUrl,deleteUrl);
			}
			groupUrls.getChildren().addAll(fieldUrls, groupUrlControls);
		}
		gui.addRow(row++, new Label("URLs:"), groupUrls);
		gui.addRow(row++, new Label("Curse"), new Separator(Orientation.HORIZONTAL));
		gui.addRow(row++, new Label("   Project ID:"), fieldCurseProject);
		gui.addRow(row++, new Label("   File:"), fieldCurseFile);
		gui.addRow(row++, new Label("   Release Type:"), fieldCurseType);
		gui.addRow(row++, new Label("   AutoUpgrade:"), fieldCurseAuto);
		Button btnUpdate = new Button("Update to newest release");
		gui.addRow(row++, new Label(""), btnUpdate);
		gui.addRow(row++, new Pane(), new Separator(Orientation.HORIZONTAL));
		gui.addRow(row++, new Label("Load Order Prefix:", loadResource("stop.png")), fieldPrefix);
		gui.addRow(row++, new Label("Mod Path:"), fieldModPath);
		gui.addRow(row++, new Label("Size:"), fieldSize);
		gui.addRow(row++, new Label("Required:"), fieldRequired);
		gui.addRow(row++, new Label("   Default:"), fieldIsDefault);
		gui.addRow(row++, new Label("Mod Type:"), fieldModType);
		gui.addRow(row++, new Label("   (Extract) In Root:"), fieldInRoot);
		gui.addRow(row++, new Label("   Order:"), fieldOrder);
		gui.addRow(row++, new Label("   (Jar) Keep META-INF:"), fieldKeepMeta);
		gui.addRow(row++, new Label("   Special Launch Args:", loadResource("stop.png")), fieldLaunchArgs);
		gui.addRow(row++, new Label("   Special Java Args:", loadResource("stop.png")), fieldJreArgs);
		gui.addRow(row++, new Label("MD5:"), fieldMD5);
		VBox groupMeta = new VBox();
		{
			HBox groupMetaControls = new HBox();
			{
				Button addMeta = new Button("Add");
				addMeta.setOnAction(e -> {
					localMeta.put("foo","bar");
					System.out.println(localMeta.toString());
					fieldMeta.setItems(FXCollections.observableArrayList(localMeta.entrySet()));
				});
				Button deleteMeta = new Button("Delete");
				deleteMeta.setOnAction(e -> {
					if (fieldMeta.getSelectionModel().getSelectedIndex() >= 0) {
						System.out.println("Before: " + localMeta.toString());
						localMeta.remove(fieldMeta.getSelectionModel().getSelectedItem().getKey());
						fieldMeta.getItems().remove(fieldMeta.getSelectionModel().getSelectedIndex());
						System.out.println("After: " + localMeta.toString());
					}
				});
				groupMetaControls.getChildren().addAll(addMeta,deleteMeta);
			}
			groupMeta.getChildren().addAll(fieldMeta, groupMetaControls);
		}
		gui.addRow(row++, new Label("Meta:"), groupMeta);
		Button btnReparse = new Button("Reparse Mod Info");
		btnReparse.setOnAction(event -> {
			CurseProject project = null;
			if (!fieldCurseProject.getText().isEmpty()) {
				int curseFile = fieldCurseFile.getText().isEmpty() ? -1 : Integer.valueOf(fieldCurseFile.getText());
				project = new CurseProject(fieldCurseProject.getText(), curseFile, fieldCurseType.getValue(), fieldCurseAuto.isSelected(), mcVersion);
			}
			Module parsed = null;
			try {
				parsed = Module.parseFile(project,fieldUrls.getItems());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (parsed.getCurseProject() != null) {
				fieldCurseFile.setText(String.valueOf(parsed.getCurseProject().getFile()));
			}
			if (!parsed.getId().startsWith("import")) {
				fieldName.setText(parsed.getName());
				fieldId.setText(parsed.getId());
				localMeta = (HashMap<String, String>) parsed.getMeta().clone();
				fieldMeta.setItems(FXCollections.observableArrayList(localMeta.entrySet()));
			}
			fieldSize.setText(String.valueOf(parsed.getFilesize()));
			fieldMD5.setText(parsed.getMD5());
		});
		gui.addRow(row++, btnReparse);
		btnUpdate.setOnAction(event -> {
			fieldCurseFile.setText("");
			btnReparse.fire();
		});

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

	private TableView<Map.Entry<String,String>> tableMapBuilder() {
		TableColumn<Map.Entry<String, String>, String> column1 = new TableColumn<>("Name");
		column1.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
		column1.setCellFactory(TextFieldTableCell.forTableColumn());
		column1.setOnEditCommit(t -> {
			String rowValue = (t.getTableView().getItems().get(t.getTablePosition().getRow())).getValue();
			localMeta.remove(t.getOldValue());
			localMeta.put(t.getNewValue(),rowValue);
			t.getTableView().setItems(FXCollections.observableArrayList(localMeta.entrySet()));
		});

		TableColumn<Map.Entry<String, String>, String> column2 = new TableColumn<>("Value");
		column2.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()));
		column2.setCellFactory(TextFieldTableCell.forTableColumn());
		column2.setOnEditCommit(t -> {
			String key = t.getTableView().getSelectionModel().getSelectedItem().getKey();
			System.out.println(key);
			(t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
			localMeta.put(key,t.getNewValue());
		});

		ObservableList<Map.Entry<String,String>> data = FXCollections.observableArrayList(localMeta.entrySet());
		final TableView<Map.Entry<String,String>> table = new TableView<>(FXCollections.observableArrayList(data));
		table.setFixedCellSize(25);
		table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(Bindings.size(table.getItems()).add(2.01)));
		table.setEditable(true);
		table.getColumns().addAll(column1, column2);
		return table;
	}

	private TableView<PrioritizedURL> tableUrlListBuilder(List<PrioritizedURL> source) {
		TableColumn<PrioritizedURL, String> column1 = new TableColumn<>("Priority");
		column1.setCellValueFactory(p -> new SimpleStringProperty(Integer.toString(p.getValue().getPriority())));
		column1.setCellFactory(TextFieldTableCell.forTableColumn());
		column1.setOnEditCommit(t -> {
			try {
				int priority = Integer.parseInt(t.getNewValue());
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setPriority(priority);
			} catch (Exception ex) {
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setPriority(0);
			}
			t.getTableView().refresh();
		});

		TableColumn<PrioritizedURL, String> column2 = new TableColumn<>("URL");
		column2.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getUrl()));
		column2.setCellFactory(TextFieldTableCell.forTableColumn());
		column2.setOnEditCommit(t -> {
			try {
				URL test = new URL(t.getNewValue());  // Convert to URL to validate
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setUrl(t.getNewValue());
			} catch (MalformedURLException e) {
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setUrl(t.getOldValue());
			}
			t.getTableView().refresh();
		});

		final TableView<PrioritizedURL> table = new TableView<>(FXCollections.observableArrayList(source));
		table.setFixedCellSize(25);
		table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(Bindings.size(table.getItems()).add(2.01)));
		table.setEditable(true);
		table.getColumns().addAll(column1, column2);
		return table;
	}

	@Override
	public void saveUpdates() {
		super.saveUpdates();
		element.setName(fieldName.getText());
		element.setId(fieldId.getText());
		element.setDepends(fieldDepends.getText());
		element.setSide(fieldSide.getValue());
		element.setUrls(fieldUrls.getItems());
		element.setLoadPrefix(fieldPrefix.getText());
		element.setPath(fieldModPath.getText());
		element.setFilesize(Long.parseLong(fieldSize.getText()));
		element.setRequired(fieldRequired.isSelected());
		element.setIsDefault(fieldIsDefault.isSelected());
		element.setModType(fieldModType.getValue());
		element.setInRoot(fieldInRoot.isSelected());
		element.setJarOrder(Integer.parseInt(fieldOrder.getText()));
		element.setKeepMeta(fieldKeepMeta.isSelected());
		element.setLaunchArgs(fieldLaunchArgs.getText());
		element.setJreArgs(fieldJreArgs.getText());
		element.setMD5(fieldMD5.getText());
		element.setMeta(localMeta);
		if (!fieldCurseProject.getText().isEmpty()) {
			int curseFile = fieldCurseFile.getText().isEmpty() ? -1 : Integer.valueOf(fieldCurseFile.getText());
			element.setCurseProject(new CurseProject(fieldCurseProject.getText(),curseFile,fieldCurseType.getValue(),fieldCurseAuto.isSelected(),this.mcVersion));
		} else {
			element.setCurseProject(null);
		}
	}

	@Override
	public void cancelUpdates() {
		super.cancelUpdates();
		fieldUrls.refresh();
		fieldMeta.refresh();
	}

	@Override
	public void reload() {
		fieldName.setText(element.getName());
		fieldId.setText(element.getId());
		fieldDepends.setText(element.getDepends());
		fieldSide.setValue(element.getSide());
		ObservableList<PrioritizedURL> items = FXCollections.observableArrayList(element.getPrioritizedUrls());
		fieldUrls.setItems(items);
		if(element.getCurseProject() != null) {
			fieldCurseProject.setText(element.getCurseProject().getProject());
			fieldCurseFile.setText(String.valueOf(element.getCurseProject().getFile()));
			fieldCurseType.setValue(element.getCurseProject().getReleaseType());
			fieldCurseAuto.setSelected(element.getCurseProject().getAutoUpgrade());
		} else {
			fieldCurseProject.setText("");
			fieldCurseFile.setText("");
			fieldCurseType.setValue(null);
			fieldCurseAuto.setSelected(false);
		}
		fieldPrefix.setText(element.getLoadPrefix());
		fieldModPath.setText(element.getPath());
		fieldSize.setText(String.valueOf(element.getFilesize()));
		fieldRequired.setSelected(element.getRequired());
		fieldIsDefault.setSelected(element.getIsDefault());
		fieldModType.setValue(element.getModType());
		fieldInRoot.setSelected(element.getInRoot());
		fieldOrder.setText(String.valueOf(element.getJarOrder()));
		fieldKeepMeta.setSelected(element.getKeepMeta());
		fieldLaunchArgs.setText(element.getLaunchArgs());
		fieldJreArgs.setText(element.getJreArgs());
		fieldMD5.setText(element.getMD5());
		localMeta = (HashMap<String, String>) element.getMeta().clone();
		fieldMeta.setItems(FXCollections.observableArrayList(localMeta.entrySet()));
	}

}

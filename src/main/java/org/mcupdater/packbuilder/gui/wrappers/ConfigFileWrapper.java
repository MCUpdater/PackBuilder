package org.mcupdater.packbuilder.gui.wrappers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.mcupdater.model.ConfigFile;
import org.mcupdater.model.PrioritizedURL;
import org.mcupdater.packbuilder.gui.ModifiableElement;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ConfigFileWrapper extends ModifiableElement {
	private ConfigFile element;

	private TableView<PrioritizedURL> fieldUrls;
	private TextField fieldPath = new TextField();
	private TextField fieldMd5 = new TextField();
	private CheckBox fieldNoOverwrite = new CheckBox();

	public ConfigFileWrapper(ConfigFile source, GridPane grid) {
		this.gui = grid;
		this.element = source;
		int row = 0;
		fieldUrls = tableUrlListBuilder(element.getPrioritizedUrls());
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
		element.setUrls(fieldUrls.getItems());
		element.setPath(fieldPath.getText());
		element.setMD5(fieldMd5.getText());
		element.setNoOverwrite(fieldNoOverwrite.isSelected());
	}

	@Override
	public void cancelUpdates() {
		super.cancelUpdates();
		fieldUrls.refresh();
	}

	@Override
	public void reload() {
		ObservableList<PrioritizedURL> items = FXCollections.observableArrayList(element.getPrioritizedUrls());
		fieldUrls.setItems(items);
		fieldPath.setText(element.getPath());
		fieldMd5.setText(element.getMD5());
		fieldNoOverwrite.setSelected(element.isNoOverwrite());
	}
}

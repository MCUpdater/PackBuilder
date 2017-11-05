package org.mcupdater.packbuilder.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Map;

public class BoundMapView<K,V> extends TableView<Map.Entry<K,V>> {

	public BoundMapView(ObservableMap<K, V> map, String col1Name, String col2Name) {
		TableColumn<Map.Entry<K, V>, K> colKey = new TableColumn<>(col1Name);
		colKey.setCellValueFactory(p -> {
			return new SimpleObjectProperty<K>(p.getValue().getKey());
		});

		TableColumn<Map.Entry<K, V>, V> colValue = new TableColumn<>(col2Name);
		colValue.setCellValueFactory(p -> {
			return new SimpleObjectProperty<V>(p.getValue().getValue());
		});

		ObservableList<Map.Entry<K, V>> items = FXCollections.observableArrayList(map.entrySet());

		this.setItems(items);
		this.getColumns().setAll(colKey, colValue);

	}
}
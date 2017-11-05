package org.mcupdater.packbuilder.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.mcupdater.model.*;
import org.mcupdater.packbuilder.gui.wrappers.*;
import org.mcupdater.util.ServerDefinition;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Document;

import javax.swing.tree.TreeNode;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainFormController {
	@FXML public Menu mnuFile;
	@FXML public TabPane tabContent;
	@FXML public MenuItem mnuFileNew;
	@FXML public MenuItem mnuFileOpen;
	@FXML public Menu mnuFileRecent;
	@FXML public MenuItem mnuFileClose;
	@FXML public MenuItem mnuFileSave;
	@FXML public MenuItem mnuFileSaveAs;
	@FXML public MenuItem mnuFileRevert;
	@FXML public MenuItem mnuFilePref;
	@FXML public MenuItem mnuFileQuit;
	@FXML public Menu mnuEdit;
	@FXML public MenuItem mnuEditUndo;
	@FXML public MenuItem mnuEditRedo;
	@FXML public MenuItem mnuEditCut;
	@FXML public MenuItem mnuEditCopy;
	@FXML public MenuItem mnuEditPaste;
	@FXML public MenuItem mnuEditDelete;
	@FXML public MenuItem mnuEditSelectAll;
	@FXML public MenuItem mnuEditUnselectAll;
	@FXML public MenuItem mnuHelpAbout;
	@FXML public Button btnA;
	@FXML public Button btnB;
	@FXML public Button btnC;
	@FXML public Button btnD;
	private Button tbEdit;
	private Button tbExport;
	private TreeView<Object> tree;

	@FXML
	public void createTab(ActionEvent actionEvent) {

	}

	public void realTab(ActionEvent actionEvent) {
		TextInputDialog prompt = new TextInputDialog("https://files.mcupdater.com/official_packs/TheSkyblockExperience/skyblock.xml");
		prompt.setTitle("Load ServerPack");
		prompt.setHeaderText("Enter the URL for your ServerPack XML:");
		prompt.setContentText("URL:");

		Optional<String> result = prompt.showAndWait();

		result.ifPresent(url -> {
			BorderPane detailWrapper = new BorderPane();
			GridPane detailPanel = new GridPane();
			detailPanel.setHgap(10);
			detailPanel.setVgap(10);
			final ModifiableElement[] detailElement = new ModifiableElement[1];
			detailPanel.setPadding(new Insets(5,10,0,10));
			ColumnConstraints col1 = new ColumnConstraints();
			ColumnConstraints col2 = new ColumnConstraints();
			col2.setHgrow(Priority.ALWAYS);
			detailPanel.getColumnConstraints().addAll(col1,col2);
			TreeItem<Object> top = TreeBuilder.loadFromUrl(url);
			tree = new TreeView<>(top);
			top.setExpanded(true);
			tree.getSelectionModel().selectedItemProperty().addListener(new TreeChangeListener(detailPanel, detailElement));
			ToolBar toolBar = new ToolBar();
			{
				tbEdit = new Button("Edit", loadResource("pencil.png"));
				tbEdit.setOnAction(event -> {
					onEdit(detailWrapper, detailElement[0], tree, toolBar);
				});
				tbExport = new Button( "Export", loadResource("page_code.png"));
				tbExport.setOnAction(event -> {
					generateXML(top);
				});
				Button tbDelete = new Button("Delete", loadResource("bin.png"));
				tbDelete.setOnAction(event -> {
					confimAndDelete(tree.getSelectionModel().getSelectedItem());
				});

				Label tbServer = new Label("Server:");
				Button tbNewServer = new Button("", loadResource("server_add.png"));
				Button tbFastServer = new Button("", loadResource("server_lightning.png"));
				HBox serverGroup = new HBox(tbServer, tbNewServer, tbFastServer);

				Label tbMod = new Label("Mod:");
				Button tbNewMod = new Button("", loadResource("package_add.png"));
				Button tbCurseMod = new Button("", loadResource("package_go.png"));
				Button tbLinkMod = new Button("", loadResource("package_link.png"));
				HBox moduleGroup = new HBox(tbMod, tbNewMod,tbCurseMod,tbLinkMod);

				Label tbSubmod = new Label("Submod:");
				Button tbNewSubmod = new Button("", loadResource("plugin_add.png"));
				Button tbCurseSubmod = new Button("", loadResource("plugin_go.png"));
				Button tbLinkSubmod = new Button("", loadResource("plugin_link.png"));
				HBox submoduleGroup = new HBox(tbSubmod, tbNewSubmod,tbCurseSubmod,tbLinkSubmod);

				Label tbConfig = new Label("Config:");
				Button tbNewConfig = new Button("", loadResource("page_white_add.png"));
				Button tbLinkConfig = new Button("", loadResource("page_white_link.png"));
				HBox configGroup = new HBox(tbConfig, tbNewConfig,tbLinkConfig);

				toolBar.getItems().addAll(tbEdit, tbExport, new Separator(Orientation.VERTICAL),tbDelete, serverGroup, moduleGroup, submoduleGroup, configGroup);
			}
			ScrollPane detailScroller = new ScrollPane(detailPanel);
			detailScroller.setFitToWidth(true);
			detailWrapper.setCenter(detailScroller);
			BorderPane content = new BorderPane(detailWrapper,toolBar,null,null, tree);
			Tab newTab = new Tab(url,content);
			tabContent.getTabs().add(newTab);
		});
	}

	private void confimAndDelete(TreeItem<Object> selectedItem) {
		if (selectedItem.getValue() instanceof ServerPack) {
			Alert information = new Alert(Alert.AlertType.ERROR, "You cannot delete the ServerPack!");
			information.showAndWait();
			return;
		}
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to delete \"" + selectedItem.getValue().toString() + "\"?");
		confirmation.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
			TreeItem parent = selectedItem.getParent();
			switch (selectedItem.getValue().getClass().toString()) {
				case "class org.mcupdater.model.RawServer":
					((ServerPack) parent.getValue()).getServers().remove(selectedItem.getValue());
					break;
				case "class org.mcupdater.model.Import":
				case "class org.mcupdater.model.Module":
					((RawServer) parent.getValue()).getPackElements().remove(selectedItem.getValue());
					break;
				case "class org.mcupdater.model.Submodule":
					((Module) parent.getValue()).getSubmodules().remove(selectedItem.getValue());
					break;
				case "class org.mcupdater.model.ConfigFile":
					((Module) parent.getValue()).getConfigs().remove(selectedItem.getValue());
					break;
				default:
					System.out.println(selectedItem.getValue().getClass().toString());
			}
			tree.getSelectionModel().select(parent);
			parent.getChildren().remove(selectedItem);
		});
	}

	private void onEdit(BorderPane detailWrapper, ModifiableElement modifiableElement, TreeView<Object> tree, ToolBar toolBar) {
		modifiableElement.beginUpdates();
		tree.setDisable(true);
		toolBar.setDisable(true);
		ToolBar toolBarInner = new ToolBar();
		Button tbSave = new Button("Save", loadResource("accept.png"));
		tbSave.setOnAction(event1 -> {
			modifiableElement.saveUpdates();
			modifiableElement.reload();
			tree.setDisable(false);
			toolBar.setDisable(false);
			detailWrapper.setTop(null);
		});
		Button tbCancel = new Button("Cancel", loadResource("cancel.png"));
		tbCancel.setOnAction(event1 -> {
			modifiableElement.cancelUpdates();
			modifiableElement.reload();
			tree.setDisable(false);
			toolBar.setDisable(false);
			detailWrapper.setTop(null);
		});
		toolBarInner.getItems().addAll(tbSave,tbCancel);
		detailWrapper.setTop(toolBarInner);
	}

	private void generateXML(TreeItem<Object> top) {
		BorderPane content = new BorderPane();
		TextArea xml = new TextArea();
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		try {
			ServerDefinition.generateServerPackHeaderXML(((ServerPack) top.getValue()).getXsltPath(), writer);
			for (TreeItem<Object> child : top.getChildren()) {
				if (child.getValue() instanceof RawServer) {
					RawServer server = (RawServer) child.getValue();
					ServerDefinition.generateServerHeaderXML(server, writer);
					List<Import> imports = new ArrayList<>();
					List<Module> modules = new ArrayList<>();
					for (IPackElement element : server.getPackElements()) {
						if (element instanceof Import) imports.add((Import) element);
						if (element instanceof Module) modules.add((Module) element);
					}
					ServerDefinition.generateServerDetailXML(writer, imports, modules, false);
					ServerDefinition.generateServerFooterXML(writer);
				}
			}
			ServerDefinition.generateServerPackFooterXML(writer);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		xml.setText(stringWriter.toString());
		content.setCenter(xml);
		Tab newTab = new Tab("Export", content);
		tabContent.getTabs().add(newTab);
	}

	private Node loadResource(String resourceName) {
		return new ImageView(new Image(getClass().getResourceAsStream(resourceName)));
	}

	public List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}

	public void testGenerateXML(ActionEvent actionEvent) throws Exception {
		ServerPack pack = ServerPackParser.loadFromURL("https://files.mcupdater.com/official_packs/TheSkyblockExperience/skyblock.xml");
		Document doc = ServerPackParser.readXmlFromUrl("https://files.mcupdater.com/official_packs/TheSkyblockExperience/skyblock.xml");
		XPath xPath = XPathFactory.newInstance().newXPath();
		String style = (String) xPath.compile("/processing-instruction('xml-stylesheet')").evaluate(doc, XPathConstants.STRING);
		int start = style.indexOf("href=\"");
		String href = style.substring(start+6, style.indexOf("\"",start+6));
		BorderPane content = new BorderPane();
		TextArea xml = new TextArea();
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		try {
			ServerDefinition.generateServerPackHeaderXML(href, writer);
			for (RawServer server : pack.getServers()) {
				ServerDefinition.generateServerHeaderXML(server, writer);
				List<Import> imports = new ArrayList<>();
				List<Module> modules = new ArrayList<>();
				for (IPackElement element : server.getPackElements()) {
					if (element instanceof Import) imports.add((Import) element);
					if (element instanceof Module) modules.add((Module) element);
				}
				ServerDefinition.generateServerDetailXML(writer, imports, modules, false );
				ServerDefinition.generateServerFooterXML(writer);
			}
			ServerDefinition.generateServerPackFooterXML(writer);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		xml.setText(stringWriter.toString());
		content.setCenter(xml);
		Tab newTab = new Tab("XML Test", content);
		tabContent.getTabs().add(newTab);
	}

	private class TreeChangeListener implements ChangeListener<TreeItem<Object>> {

		private final GridPane detailPanel;
		private final ModifiableElement[] current;

		public TreeChangeListener(GridPane parent, ModifiableElement[] currentElement) {
			this.detailPanel = parent;
			this.current = currentElement;
		}

		@Override
		public void changed(ObservableValue<? extends TreeItem<Object>> observable, TreeItem<Object> oldValue, TreeItem<Object> newValue) {
			if (newValue != null) {
				TreeItem<Object> treeItem = newValue;
				detailPanel.getChildren().clear();
				switch(treeItem.getValue().getClass().toString()) {
					case "class org.mcupdater.model.ServerPack":
						ServerPack pack = (ServerPack) treeItem.getValue();
						current[0] = new ServerPackWrapper(pack, detailPanel);
						break;
					case "class org.mcupdater.model.RawServer":
						RawServer server = (RawServer) treeItem.getValue();
						current[0] = new ServerWrapper(server, detailPanel);
						break;
					case "class org.mcupdater.model.Import":
						Import anImport = (Import) treeItem.getValue();
						current[0] = new ImportWrapper(anImport, detailPanel);
						break;
					case "class org.mcupdater.model.Module":
					case "class org.mcupdater.model.Submodule":
						GenericModule module = (GenericModule) treeItem.getValue();
						current[0] = new ModuleWrapper(module, detailPanel);
						break;
					case "class org.mcupdater.model.ConfigFile":
						ConfigFile config = (ConfigFile) treeItem.getValue();
						current[0] = new ConfigFileWrapper(config, detailPanel);
						break;
					default:
						System.out.println(treeItem.getValue().getClass().toString());
				}
			}
		}
	}
}

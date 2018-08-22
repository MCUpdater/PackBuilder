package org.mcupdater.packbuilder.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.mcupdater.api.Version;
import org.mcupdater.model.*;
import org.mcupdater.model.Module;
import org.mcupdater.mojang.VersionManifest;
import org.mcupdater.packbuilder.gui.wrappers.*;
import org.mcupdater.util.FastPack;
import org.mcupdater.util.ServerDefinition;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;

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
	private TreeView<IPackElement> tree;
	private HBox serverGroup;
	private HBox moduleGroup;
	private HBox submoduleGroup;
	private HBox configGroup;
	private HBox importGroup;

	@FXML
	public void createTab(ActionEvent actionEvent) {
		final TreeItem[] top = new TreeItem[1];
		final ModifiableElement[] detailElement = new ModifiableElement[1];
		final String[] title = new String[1];
		top[0] = new TreeItem<IPackElement>(new ServerPack("", Version.API_VERSION));
		title[0] = "New ServerPack";
		createTab(top, detailElement, title[0]);

	}

	public void realTab(ActionEvent actionEvent) {
		TextInputDialog prompt = new TextInputDialog("https://files.mcupdater.com/official_packs/TheSkyblockExperience/skyblock.xml");
		prompt.setTitle("Load ServerPack");
		prompt.setHeaderText("Enter the URL for your ServerPack XML:");
		prompt.setContentText("URL:");

		Optional<String> result = prompt.showAndWait();
		final TreeItem[] top = new TreeItem[1];
		final ModifiableElement[] detailElement = new ModifiableElement[1];
		final String[] title = new String[1];
		result.ifPresent(url -> {
					top[0] = TreeBuilder.loadFromUrl(url);
					title[0] = url;
					createTab(top, detailElement, title[0]);
				});
	}

	public void createTab(TreeItem<IPackElement>[] top, ModifiableElement[] detailElement, String text) {
		BorderPane detailWrapper = new BorderPane();
		GridPane detailPanel = new GridPane();
		detailPanel.setHgap(10);
		detailPanel.setVgap(10);
		detailPanel.setPadding(new Insets(5,10,0,10));
		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHgrow(Priority.ALWAYS);
		detailPanel.getColumnConstraints().addAll(col1,col2);
		tree = new TreeView<>(top[0]);
		top[0].setExpanded(true);
		tree.getSelectionModel().selectedItemProperty().addListener(new TreeChangeListener(detailPanel, detailElement));
		ToolBar toolBar = new ToolBar();
		{
			tbEdit = new Button("Edit", loadResource("pencil.png"));
			tbEdit.setOnAction(event -> {
				onEdit(detailWrapper, detailElement[0], tree, toolBar);
			});
			tbExport = new Button( "Export", loadResource("page_code.png"));
			tbExport.setOnAction(event -> {
				generateXML(top[0]);
			});
			Button tbDelete = new Button("Delete", loadResource("bin.png"));
			tbDelete.setOnAction(event -> {
				confimAndDelete(tree.getSelectionModel().getSelectedItem());
			});

			Label tbServer = new Label("Server:");
			Button tbNewServer = new Button("", loadResource("server_add.png"));
			tbNewServer.setOnAction(event -> {
				RawServer newServer = new RawServer();
				((ServerPack) top[0].getValue()).getServers().add(newServer);
				top[0].getChildren().add(new TreeItem<>(newServer));
			});
			Button tbFastServer = new Button("", loadResource("server_lightning.png"));
			tbFastServer.setOnAction(event -> {
				RawServer newServer = promptForFastPack(tree.getScene().getWindow());
				if(newServer != null) {
					top[0].getChildren().add(TreeBuilder.fromRawServer(newServer));
				}
			});
			serverGroup = new HBox(tbServer, tbNewServer, tbFastServer);

			Label tbImport = new Label( "Import:");
			Button tbNewImport = new Button("", loadResource("link_add.png"));
			tbNewImport.setOnAction(event -> {
				Import newImport = new Import();
				TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
				TreeItem<IPackElement> server;
				switch (currentItem.getValue().getClass().toString()) {
					case "class org.mcupdater.model.RawServer":
						server = currentItem;
						break;
					case "class org.mcupdater.model.Import":
					case "class org.mcupdater.model.Module":
						server = currentItem.getParent();
						break;
					case "class org.mcupdater.model.Submodule":
					case "class org.mcupdater.model.ConfigFile":
						server = currentItem.getParent().getParent();
						break;
					default:
						server = null;
				}
				((RawServer) server.getValue()).getPackElements().add(newImport);
				server.getChildren().add(new TreeItem<>(newImport));
			});
			importGroup = new HBox(tbImport,tbNewImport);

			Label tbMod = new Label("Mod:");
			Button tbNewMod = new Button("", loadResource("package_add.png"));
			tbNewMod.setOnAction(event -> {
				Module newModule = Module.createBlankModule();
				TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
				TreeItem<IPackElement> server;
				switch (currentItem.getValue().getClass().toString()) {
					case "class org.mcupdater.model.RawServer":
						server = currentItem;
						break;
					case "class org.mcupdater.model.Import":
					case "class org.mcupdater.model.Module":
						server = currentItem.getParent();
						break;
					case "class org.mcupdater.model.Submodule":
					case "class org.mcupdater.model.ConfigFile":
						server = currentItem.getParent().getParent();
						break;
					default:
						server = null;
				}
				((RawServer) server.getValue()).getPackElements().add(newModule);
				server.getChildren().add(new TreeItem<>(newModule));
			});
			Button tbCurseMod = new Button("", loadResource("package_go.png"));
			tbCurseMod.setOnAction(event -> {
				//TODO: Curse Feed Link
			});
			Button tbLinkMod = new Button("", loadResource("package_link.png"));
			tbLinkMod.setOnAction(event -> {
				//TODO: Download Link
			});
			moduleGroup = new HBox(tbMod, tbNewMod,tbCurseMod,tbLinkMod);

			Label tbSubmod = new Label("Submod:");
			Button tbNewSubmod = new Button("", loadResource("plugin_add.png"));
			tbNewSubmod.setOnAction(event -> {
				Submodule newModule = Submodule.createBlankSubmodule();
				TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
				TreeItem<IPackElement> module;
				switch (currentItem.getValue().getClass().toString()) {
					case "class org.mcupdater.model.Module":
						module = currentItem;
						break;
					case "class org.mcupdater.model.Submodule":
					case "class org.mcupdater.model.ConfigFile":
						module = currentItem.getParent();
						break;
					default:
						module = null;
				}
				((Module) module.getValue()).getSubmodules().add(newModule);
				module.getChildren().add(new TreeItem<>(newModule));
			});
			Button tbCurseSubmod = new Button("", loadResource("plugin_go.png"));
			tbCurseSubmod.setOnAction(event -> {
				//TODO: Curse Feed Link
			});
			Button tbLinkSubmod = new Button("", loadResource("plugin_link.png"));
			tbLinkSubmod.setOnAction(event -> {
				//TODO: Download Link
			});
			submoduleGroup = new HBox(tbSubmod, tbNewSubmod,tbCurseSubmod,tbLinkSubmod);

			Label tbConfig = new Label("Config:");
			Button tbNewConfig = new Button("", loadResource("page_white_add.png"));
			tbNewConfig.setOnAction(event -> {
				ConfigFile newConfigFile = new ConfigFile(new ArrayList<>(),"",false,"");
				TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
				TreeItem<IPackElement> module;
				switch (currentItem.getValue().getClass().toString()) {
					case "class org.mcupdater.model.Module":
						module = currentItem;
						break;
					case "class org.mcupdater.model.Submodule":
					case "class org.mcupdater.model.ConfigFile":
						module = currentItem.getParent();
						break;
					default:
						module = null;
				}
				((Module) module.getValue()).getConfigs().add(newConfigFile);
				module.getChildren().add(new TreeItem<>(newConfigFile));
			});
			Button tbLinkConfig = new Button("", loadResource("page_white_link.png"));
			tbLinkConfig.setOnAction(event -> {
				//TODO: Download Link
			});
			configGroup = new HBox(tbConfig, tbNewConfig,tbLinkConfig);
			serverGroup.setVisible(false);
			importGroup.setVisible(false);
			moduleGroup.setVisible(false);
			submoduleGroup.setVisible(false);
			configGroup.setVisible(false);

			toolBar.getItems().addAll(tbEdit, tbExport, new Separator(Orientation.VERTICAL),tbDelete, serverGroup, importGroup, moduleGroup, submoduleGroup, configGroup);
		}
		ScrollPane detailScroller = new ScrollPane(detailPanel);
		detailScroller.setFitToWidth(true);
		detailWrapper.setCenter(detailScroller);
		BorderPane content = new BorderPane(detailWrapper,toolBar,null,null, tree);
		Tab newTab = new Tab(text,content);
		tabContent.getTabs().add(newTab);
	}

	private RawServer promptForFastPack(Window parent) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("FastPack: Select path to scan");
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File source = chooser.showDialog(parent);
		if (source != null) {
			String baseUrl;
			{
				TextInputDialog prompt = new TextInputDialog();
				prompt.setTitle("Base URL");
				prompt.setHeaderText("Enter the Base URL for your ServerPack's downloads:");
				prompt.setContentText("URL:");

				Optional<String> result = prompt.showAndWait();
				if (result.isPresent()) {
					baseUrl = result.get();
				} else {
					return null;
				}
			}
			String mcVersion = "";
			{
				try {
					VersionManifest versions = VersionManifest.getCurrent(false);
					ChoiceDialog<VersionManifest.VersionInfo> versionPrompt = new ChoiceDialog<>(versions.getVersion(versions.getLatest().getRelease()), versions.getVersions());
					versionPrompt.setTitle("Minecraft version");
					versionPrompt.setHeaderText("Select the version of Minecraft that this ServerPack will target:");

					Optional<VersionManifest.VersionInfo> versionResult = versionPrompt.showAndWait();
					if (versionResult.isPresent()) {
						mcVersion = versionResult.get().getId();
					} else {
						return null;
					}

				} catch (IOException e) {
					e.printStackTrace();
					return null;
				} catch (VersionManifest.VersionNotFoundException e) {
					e.printStackTrace();
				}
			}
			String forgeVersion = "";
			{
				TextInputDialog forgePrompt = new TextInputDialog();
				forgePrompt.setTitle("Minecraft Forge version");
				forgePrompt.setHeaderText("Enter the Minecraft Forge version to be included (leave blank to not include Forge):");
				forgePrompt.setContentText("Version:");
				Optional<String> result = forgePrompt.showAndWait();
				if (result.isPresent() && !result.get().isEmpty()) {
					forgeVersion = result.get();
				}
			}
			ServerDefinition fastpack = FastPack.doFastPack("","","FastPack","fastpack","","net.minecraft.launchwrapper.Launch","about:blank","","1",false, mcVersion, source.toPath(), baseUrl,false);
			if (fastpack.hasLitemods && !fastpack.hasMod(fastpack.sortMods(), "liteloader")) {
				fastpack.addModule(new Module("LiteLoader", "liteloader", Arrays.asList(new PrioritizedURL("http://dl.liteloader.com/versions/com/mumfrey/liteloader/" + mcVersion + "/liteloader-" + mcVersion + ".jar", 0)), null, "", false, ModType.Library, 100, false, false, true, "", null, "CLIENT", "", null, "--tweakClass com.mumfrey.liteloader.launch.LiteLoaderTweaker", "", null, ""));
			}
			if (!forgeVersion.isEmpty()) {
				fastpack.addForge(mcVersion, forgeVersion);
			}
			List<Module> sortedModules = fastpack.sortMods();
			Map<String,String> issues = new HashMap<>();
			fastpack.assignConfigs(issues, false);
			if(issues.size() > 0) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Config matching may not be accurate");
				alert.setHeaderText("The following config files may not have assigned to the correct mods:");
				TableColumn<Map.Entry<String, String>, String> column1 = new TableColumn<>("Path");
				column1.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
				column1.setCellFactory(TextFieldTableCell.forTableColumn());

				TableColumn<Map.Entry<String, String>, String> column2 = new TableColumn<>("Module");
				column2.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()));
				column2.setCellFactory(TextFieldTableCell.forTableColumn());

				ObservableList<Map.Entry<String,String>> data = FXCollections.observableArrayList(issues.entrySet());
				final TableView<Map.Entry<String,String>> table = new TableView<>(FXCollections.observableArrayList(data));
				table.setFixedCellSize(25);
				table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(Bindings.size(table.getItems()).add(2.01)));
				table.setEditable(false);
				table.getColumns().addAll(column1, column2);

				table.setMaxWidth(Double.MAX_VALUE);
				table.setMaxHeight(Double.MAX_VALUE);

				alert.getDialogPane().setExpandableContent(table);
				alert.getDialogPane().setExpanded(true);
				alert.showAndWait();
			}
			RawServer newServer = new RawServer(fastpack.getServerEntry());
			newServer.getPackElements().addAll(fastpack.getImports());
			newServer.getPackElements().addAll(fastpack.sortMods());
			return newServer;
		}
		return null;
	}

	private void confimAndDelete(TreeItem<IPackElement> selectedItem) {
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

	private void onEdit(BorderPane detailWrapper, ModifiableElement modifiableElement, TreeView<IPackElement> tree, ToolBar toolBar) {
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
			tree.refresh();
		});
		Button tbCancel = new Button("Cancel", loadResource("cancel.png"));
		tbCancel.setOnAction(event1 -> {
			modifiableElement.cancelUpdates();
			modifiableElement.reload();
			tree.setDisable(false);
			toolBar.setDisable(false);
			detailWrapper.setTop(null);
			tree.refresh();
		});
		toolBarInner.getItems().addAll(tbSave,tbCancel);
		detailWrapper.setTop(toolBarInner);
	}

	private void generateXML(TreeItem<IPackElement> top) {
		BorderPane content = new BorderPane();
		TextArea xml = new TextArea();
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		try {
			ServerDefinition.generateServerPackHeaderXML(((ServerPack) top.getValue()).getXsltPath(), writer);
			for (TreeItem<IPackElement> child : top.getChildren()) {
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

	private class TreeChangeListener implements ChangeListener<TreeItem<IPackElement>> {

		private final GridPane detailPanel;
		private final ModifiableElement[] current;

		public TreeChangeListener(GridPane parent, ModifiableElement[] currentElement) {
			this.detailPanel = parent;
			this.current = currentElement;
		}

		@Override
		public void changed(ObservableValue<? extends TreeItem<IPackElement>> observable, TreeItem<IPackElement> oldValue, TreeItem<IPackElement> newValue) {
			if (newValue != null) {
				TreeItem<IPackElement> treeItem = newValue;
				detailPanel.getChildren().clear();
				switch(treeItem.getValue().getClass().toString()) {
					case "class org.mcupdater.model.ServerPack":
						ServerPack pack = (ServerPack) treeItem.getValue();
						current[0] = new ServerPackWrapper(pack, detailPanel);
						serverGroup.setVisible(true);
						importGroup.setVisible(false);
						moduleGroup.setVisible(false);
						submoduleGroup.setVisible(false);
						configGroup.setVisible(false);
						break;
					case "class org.mcupdater.model.RawServer":
						RawServer server = (RawServer) treeItem.getValue();
						current[0] = new ServerWrapper(server, detailPanel);
						serverGroup.setVisible(true);
						importGroup.setVisible(true);
						moduleGroup.setVisible(true);
						submoduleGroup.setVisible(false);
						configGroup.setVisible(false);
						break;
					case "class org.mcupdater.model.Import":
						Import anImport = (Import) treeItem.getValue();
						current[0] = new ImportWrapper(anImport, detailPanel);
						serverGroup.setVisible(true);
						importGroup.setVisible(true);
						moduleGroup.setVisible(true);
						submoduleGroup.setVisible(false);
						configGroup.setVisible(false);
						break;
					case "class org.mcupdater.model.Module":
					case "class org.mcupdater.model.Submodule":
						GenericModule module = (GenericModule) treeItem.getValue();
						current[0] = new ModuleWrapper(module, detailPanel);
						serverGroup.setVisible(true);
						importGroup.setVisible(true);
						moduleGroup.setVisible(true);
						submoduleGroup.setVisible(true);
						configGroup.setVisible(true);
						break;
					case "class org.mcupdater.model.ConfigFile":
						ConfigFile config = (ConfigFile) treeItem.getValue();
						current[0] = new ConfigFileWrapper(config, detailPanel);
						importGroup.setVisible(true);
						moduleGroup.setVisible(true);
						submoduleGroup.setVisible(true);
						configGroup.setVisible(true);
						break;
					default:
						System.out.println(treeItem.getValue().getClass().toString());
						serverGroup.setVisible(false);
						importGroup.setVisible(false);
						moduleGroup.setVisible(false);
						submoduleGroup.setVisible(false);
						configGroup.setVisible(false);
				}
			}
		}
	}
}

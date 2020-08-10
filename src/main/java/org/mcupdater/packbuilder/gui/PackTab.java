package org.mcupdater.packbuilder.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.mcupdater.downloadlib.DownloadUtil;
import org.mcupdater.model.*;
import org.mcupdater.mojang.VersionManifest;
import org.mcupdater.packbuilder.gui.wrappers.*;
import org.mcupdater.util.FastPack;
import org.mcupdater.util.PathWalker;
import org.mcupdater.util.ServerDefinition;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PackTab extends Tab {

	private Button tbEdit;
	private Button tbExport;
	private TreeView<IPackElement> tree;
	private HBox serverGroup;
	private HBox moduleGroup;
	private HBox submoduleGroup;
	private HBox configGroup;
	private HBox importGroup;

	public PackTab(TreeItem<IPackElement>[] top, ModifiableElement[] detailElement, String text, MainFormController controller) {
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
				controller.generateXMLPreview(top[0]);
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
					top[0].getChildren().add(TreeBuilder.getRawServerElement(newServer));
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
					case "class org.mcupdater.model.Loader":
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
				server.setExpanded(true);
			});
			Button tbForgeImport = new Button("", loadResource("forge_add.png"));
			tbForgeImport.setOnAction(event -> {
				Loader newLoader = new Loader();
				TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
				TreeItem<IPackElement> server;
				switch (currentItem.getValue().getClass().toString()) {
					case "class org.mcupdater.model.RawServer":
						server = currentItem;
						break;
					case "class org.mcupdater.model.Import":
					case "class org.mcupdater.model.Loader":
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
				TextInputDialog forgeDialog = new TextInputDialog();
				forgeDialog.setTitle("Add import");
				forgeDialog.setHeaderText("Enter Forge version number (ex. 1.14.4-28.0.11)");
				forgeDialog.setContentText("Version:");

				Optional<String> result = forgeDialog.showAndWait();
				result.ifPresent(version -> {
					newLoader.setType("Forge");
					newLoader.setVersion(forgeDialog.getResult());
					newLoader.setLoadOrder(0);
					((RawServer) server.getValue()).getPackElements().add(newLoader);
					server.getChildren().add(new TreeItem<>(newLoader));
					server.setExpanded(true);
					((RawServer) server.getValue()).setMainClass(newLoader.getILoader().getMainClassClient());
				});
			});
			importGroup = new HBox(tbImport,tbNewImport,tbForgeImport);

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
					case "class org.mcupdater.model.Loader":
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
				server.setExpanded(true);
			});
			Button tbCurseMod = new Button("", loadResource("package_go.png"));
			tbCurseMod.setTooltip(new Tooltip("Add Mod from CurseForge"));
			tbCurseMod.setOnAction(event -> {
				TextInputDialog curseImport = new TextInputDialog();
				curseImport.setTitle("Add from CurseForge");
				curseImport.setHeaderText("Enter URL from CurseForge");
				curseImport.setContentText("URL:");

				Optional<String> result = curseImport.showAndWait();
				result.ifPresent(stringUrl -> {
					try {
						URL url = new URL(stringUrl);
						if (!isCurseURL(url)) {
							Alert alert = new Alert(Alert.AlertType.ERROR,"Invalid CurseForge URL!",ButtonType.OK);
							alert.show();
						} else {
							String[] parts = url.getPath().split("\\/");
							Module newModule = Module.createBlankModule();
							TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
							TreeItem<IPackElement> server;
							switch (currentItem.getValue().getClass().toString()) {
								case "class org.mcupdater.model.RawServer":
									server = currentItem;
									break;
								case "class org.mcupdater.model.Import":
								case "class org.mcupdater.model.Loader":
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
							newModule.setName(parts[3]);
							newModule.setId(parts[3]);
							newModule.setRequired(true);
							if (parts.length > 5) {
								newModule.setCurseProject(new CurseProject(parts[3], Integer.valueOf(parts[5])));
							} else {
								newModule.setCurseProject(new CurseProject(parts[3], ((RawServer) server.getValue()).getVersion()));
							}
							Module parsedMod = Module.parseFile(newModule.getCurseProject(), newModule.getPrioritizedUrls());
							if (parsedMod != null) {
								newModule.setName(parsedMod.getName());
								newModule.setId(parsedMod.getId());
								newModule.setCurseProject(parsedMod.getCurseProject());
								newModule.setMD5(parsedMod.getMD5());
								newModule.setFilesize(parsedMod.getFilesize());
								newModule.setMeta(parsedMod.getMeta());
							}
							((RawServer) server.getValue()).getPackElements().add(newModule);
							server.getChildren().add(new TreeItem<>(newModule));
							server.setExpanded(true);
						}
					} catch (MalformedURLException e) {
						Alert alert = new Alert(Alert.AlertType.ERROR,"Invalid URL!",ButtonType.OK);
						alert.show();
					} catch (Exception e) {
						Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),ButtonType.OK);
						alert.show();
					}
				});
			});
			Button tbLinkMod = new Button("", loadResource("package_link.png"));
			tbLinkMod.setOnAction(event -> {
				TextInputDialog curseImport = new TextInputDialog();
				curseImport.setTitle("Add from URL");
				curseImport.setHeaderText("Enter URL to add");
				curseImport.setContentText("URL:");

				Optional<String> result = curseImport.showAndWait();
				result.ifPresent(stringUrl -> {
					TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
					TreeItem<IPackElement> server;
					switch (currentItem.getValue().getClass().toString()) {
						case "class org.mcupdater.model.RawServer":
							server = currentItem;
							break;
						case "class org.mcupdater.model.Import":
						case "class org.mcupdater.model.Loader":
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
					//TODO: Download and process
					final File tmp;
					final Path path;
					try {
						tmp = File.createTempFile("import", ".jar");
						DownloadUtil.get(new URL(stringUrl), tmp);
						tmp.deleteOnExit();
						path = tmp.toPath();
						if( Files.size(path) == 0 ) {
							System.out.println("!! got zero bytes from " + stringUrl);
							return;
						}
						Module newModule = (Module) PathWalker.handleOneFile(new ServerDefinition(), tmp, stringUrl);
						((RawServer) server.getValue()).getPackElements().add(newModule);
						server.getChildren().add(new TreeItem<>(newModule));
						server.setExpanded(true);
					} catch (IOException e) {
						System.out.println("!! Unable to download " + stringUrl);
						return;
					}
				});
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
				module.setExpanded(true);
			});
			Button tbCurseSubmod = new Button("", loadResource("plugin_go.png"));
			tbCurseSubmod.setOnAction(event -> {
				TextInputDialog curseImport = new TextInputDialog();
				curseImport.setTitle("Add from CurseForge");
				curseImport.setHeaderText("Enter URL from CurseForge");
				curseImport.setContentText("URL:");

				Optional<String> result = curseImport.showAndWait();
				result.ifPresent(stringUrl -> {
					try {
						URL url = new URL(stringUrl);
						if (!isCurseURL(url)) {
							Alert alert = new Alert(Alert.AlertType.ERROR,"Invalid CurseForge URL!",ButtonType.OK);
							alert.show();
						} else {
							String[] parts = url.getPath().split("\\/");
							Submodule newModule = Submodule.createBlankSubmodule();
							TreeItem<IPackElement> currentItem = tree.getSelectionModel().getSelectedItem();
							TreeItem<IPackElement> module;
							TreeItem<IPackElement> server;
							switch (currentItem.getValue().getClass().toString()) {
								case "class org.mcupdater.model.Module":
									module = currentItem;
									server = currentItem.getParent();
									break;
								case "class org.mcupdater.model.Submodule":
								case "class org.mcupdater.model.ConfigFile":
									module = currentItem.getParent();
									server = currentItem.getParent().getParent();
									break;
								default:
									module = null;
									server = null;
							}
							newModule.setName(parts[3]);
							newModule.setId(parts[3]);
							newModule.setRequired(true);
							if (parts.length > 5) {
								newModule.setCurseProject(new CurseProject(parts[3], parts[5]));
							} else {
								newModule.setCurseProject(new CurseProject(parts[3], ((RawServer) server.getValue()).getVersion()));
							}
							Module parsedMod = Module.parseFile(newModule.getCurseProject(), newModule.getPrioritizedUrls());
							if (parsedMod != null) {
								newModule.setName(parsedMod.getName());
								newModule.setId(parsedMod.getId());
								newModule.setCurseProject(parsedMod.getCurseProject());
								newModule.setMD5(parsedMod.getMD5());
								newModule.setFilesize(parsedMod.getFilesize());
								newModule.setMeta(parsedMod.getMeta());
							}
							((Module) module.getValue()).getSubmodules().add(newModule);
							module.getChildren().add(new TreeItem<>(newModule));
							module.setExpanded(true);
						}
					} catch (MalformedURLException e) {
						Alert alert = new Alert(Alert.AlertType.ERROR,"Invalid URL!",ButtonType.OK);
						alert.show();
					} catch (Exception e) {
						Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),ButtonType.OK);
						alert.show();
					}
				});
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
				module.setExpanded(true);
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
		this.setContent(content);
		this.setText(text);
	}

	private boolean isCurseURL(URL url) {
		return url.getHost().equals("www.curseforge.com") && (url.getPath().contains("mc-mods"));
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
				fastpack.addModule(new Module("LiteLoader", "liteloader", Arrays.asList(new PrioritizedURL("http://dl.liteloader.com/versions/com/mumfrey/liteloader/" + mcVersion + "/liteloader-" + mcVersion + ".jar", 0)), null, 100000, "", false, ModType.Library, 100, false, false, true, "", null, "CLIENT", "", null, "--tweakClass com.mumfrey.liteloader.launch.LiteLoaderTweaker", "", null, ""));
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

	private Node loadResource(String resourceName) {
		return new ImageView(new Image(getClass().getResourceAsStream(resourceName)));
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
				RawServer rawServer;
				switch (treeItem.getValue().getClass().toString()) {
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
						rawServer = (treeItem.getParent().getValue() instanceof RawServer ? (RawServer) treeItem.getParent().getValue() : (RawServer) treeItem.getParent().getParent().getValue());
						current[0] = new ImportWrapper(anImport, detailPanel, rawServer.getVersion());
						serverGroup.setVisible(true);
						importGroup.setVisible(true);
						moduleGroup.setVisible(true);
						submoduleGroup.setVisible(false);
						configGroup.setVisible(false);
						break;
					case "class org.mcupdater.model.Loader":
						Loader anLoader = (Loader) treeItem.getValue();
						rawServer = (treeItem.getParent().getValue() instanceof RawServer ? (RawServer) treeItem.getParent().getValue() : (RawServer) treeItem.getParent().getParent().getValue());
						current[0] = new LoaderWrapper(anLoader, detailPanel, rawServer.getVersion());
						serverGroup.setVisible(true);
						importGroup.setVisible(true);
						moduleGroup.setVisible(true);
						submoduleGroup.setVisible(false);
						configGroup.setVisible(false);
						break;
					case "class org.mcupdater.model.Module":
					case "class org.mcupdater.model.Submodule":
						GenericModule module = (GenericModule) treeItem.getValue();
						rawServer = (treeItem.getParent().getValue() instanceof RawServer ? (RawServer) treeItem.getParent().getValue() : (RawServer) treeItem.getParent().getParent().getValue());
						current[0] = new ModuleWrapper(module, detailPanel, rawServer.getVersion());
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

package org.mcupdater.packbuilder.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.mcupdater.api.Version;
import org.mcupdater.model.*;
import org.mcupdater.util.FastPack;
import org.mcupdater.util.ServerDefinition;
import org.mcupdater.util.ServerPackParser;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainFormController {
	@FXML public BorderPane paneTop;
	@FXML public Menu mnuFile;
	@FXML public TabPane tabContent;
	@FXML public MenuItem mnuFileNew;
	@FXML public MenuItem mnuFileOpen;
	@FXML public MenuItem mnuFileImport;
	@FXML public MenuItem mnuFileClose;
	@FXML public MenuItem mnuFileSave;
	@FXML public MenuItem mnuFileSaveAs;
	@FXML public MenuItem mnuFileRevert;
//	@FXML public MenuItem mnuFilePref;
	@FXML public MenuItem mnuFileQuit;
/*
	@FXML public Menu mnuEdit;
	@FXML public MenuItem mnuEditUndo;
	@FXML public MenuItem mnuEditRedo;
	@FXML public MenuItem mnuEditCut;
	@FXML public MenuItem mnuEditCopy;
	@FXML public MenuItem mnuEditPaste;
	@FXML public MenuItem mnuEditDelete;
	@FXML public MenuItem mnuEditSelectAll;
	@FXML public MenuItem mnuEditUnselectAll;
*/
	@FXML public MenuItem mnuHelpAbout;
	@FXML public Button btnA;
	@FXML public Button btnB;
	@FXML public Button btnC;
	@FXML public Button btnD;

	@FXML
	public void createTab(ActionEvent actionEvent) {
		final TreeItem[] top = new TreeItem[1];
		final ModifiableElement[] detailElement = new ModifiableElement[1];
		final String[] title = new String[1];
		top[0] = new TreeItem<IPackElement>(new ServerPack("", Version.API_VERSION));
		title[0] = "New ServerPack";
		Tab newTab = new PackTab(top, detailElement, title[0], this);
		tabContent.getTabs().add(newTab);
	}

	public void generateXMLPreview(TreeItem<IPackElement> top) {
		BorderPane content = new BorderPane();
		TextArea xml = new TextArea();
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		writeXML(top, writer);
		xml.setText(stringWriter.toString());
		content.setCenter(xml);
		Tab newTab = new Tab("Export", content);
		tabContent.getTabs().add(newTab);
	}

	private void writeXML(TreeItem<IPackElement> top, BufferedWriter writer) {
		try {
			ServerDefinition.generateServerPackHeaderXML(((ServerPack) top.getValue()).getXsltPath(), writer);
			for (TreeItem<IPackElement> child : top.getChildren()) {
				if (child.getValue() instanceof RawServer) {
					RawServer server = (RawServer) child.getValue();
					ServerDefinition.generateServerHeaderXML(server, writer);
					List<Import> imports = new ArrayList<>();
					List<Loader> loaders = new ArrayList<>();
					List<Module> modules = new ArrayList<>();
					for (IPackElement element : server.getPackElements()) {
						if (element instanceof Import) imports.add((Import) element);
						if (element instanceof Loader) loaders.add((Loader) element);
						if (element instanceof Module) modules.add((Module) element);
					}
					ServerDefinition.generateServerDetailXML(writer, imports, loaders, modules, false);
					ServerDefinition.generateServerFooterXML(writer);
				}
			}
			ServerDefinition.generateServerPackFooterXML(writer);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}

	public void doOpen(ActionEvent actionEvent) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Load ServerPack");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
		File selected = chooser.showOpenDialog(paneTop.getScene().getWindow());
		if (selected != null) {
			try {
				final TreeItem[] top = new TreeItem[1];
				final ModifiableElement[] detailElement = new ModifiableElement[1];
				final String[] title = new String[1];
				top[0] = TreeBuilder.loadFromUrl(selected.toURI().toURL().toString());
				title[0] = selected.getName();
				Tab newTab = new PackTab(top, detailElement, title[0], this);
				newTab.getProperties().put("LocalFile",selected.getAbsolutePath());
				tabContent.getTabs().add(newTab);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	public void doSave(ActionEvent actionEvent) {
		Tab selectedItem = tabContent.getSelectionModel().getSelectedItem();
		if (selectedItem.getProperties().containsKey("LocalFile")) {
			doActualSave(new File((String) selectedItem.getProperties().get("LocalFile")), ((TreeView)((BorderPane) selectedItem.getContent()).getLeft()).getRoot());
		} else {
			doSaveAs(actionEvent);
		}
	}

	public void doSaveAs(ActionEvent actionEvent) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Save ServerPack");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
		File selected = chooser.showSaveDialog(paneTop.getScene().getWindow());
		if (selected != null) {
			Tab selectedItem = tabContent.getSelectionModel().getSelectedItem();
			doActualSave(selected, ((TreeView)((BorderPane) selectedItem.getContent()).getLeft()).getRoot());
			selectedItem.getProperties().put("LocalFile", selected.getAbsolutePath());
		}
	}

	private void doActualSave(File localFile, TreeItem top) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(localFile));
			writeXML(top, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doImport(ActionEvent actionEvent) {
		TextInputDialog prompt = new TextInputDialog("");
		prompt.setTitle("Import");
		prompt.setHeaderText("Enter URL for CurseForge modpack:");
		prompt.setContentText("URL:");

		Optional<String> result = prompt.showAndWait();
		final TreeItem[] top = new TreeItem[1];
		final ModifiableElement[] detailElement = new ModifiableElement[1];
		final String[] title = new String[1];
		result.ifPresent(url -> {
			ServerDefinition definition = FastPack.doImport(url,"Imported Pack","import","","net.minecraft.launchwrapper.Launch","","",false,false);
			RawServer rawServer = new RawServer(definition.getServerEntry());
			rawServer.getPackElements().addAll(definition.getImports());
			rawServer.getPackElements().addAll(definition.sortMods());
			top[0] = TreeBuilder.fromRawServer(rawServer);
			title[0] = url;
			Tab newTab = new PackTab(top, detailElement, title[0], this);
			tabContent.getTabs().add(newTab);
		});

	}

	/* Below this line is test code that can be removed before release */
	//TODO: Remove test code before release

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
			Tab newTab = new PackTab(top, detailElement, title[0], this);
			tabContent.getTabs().add(newTab);
		});
	}

	public void testGenerateXML(ActionEvent actionEvent) throws Exception {
		ServerPack pack = ServerPackParser.loadFromURL("https://files.mcupdater.com/official_packs/TheSkyblockExperience/skyblock.xml", true);
		BorderPane content = new BorderPane();
		TextArea xml = new TextArea();
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		try {
			ServerDefinition.generateServerPackHeaderXML(pack.getXsltPath(), writer);
			for (Server server : pack.getServers()) {
				if (server instanceof RawServer) {
					ServerDefinition.generateServerHeaderXML(server, writer);
					List<Import> imports = new ArrayList<>();
					List<Loader> loaders = new ArrayList<>();
					List<Module> modules = new ArrayList<>();
					for (IPackElement element : ((RawServer) server).getPackElements()) {
						if (element instanceof Import) imports.add((Import) element);
						if (element instanceof Loader) loaders.add((Loader) element);
						if (element instanceof Module) modules.add((Module) element);
					}
					ServerDefinition.generateServerDetailXML(writer, imports, loaders, modules, false);
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
		Tab newTab = new Tab("XML Test", content);
		tabContent.getTabs().add(newTab);
	}

}

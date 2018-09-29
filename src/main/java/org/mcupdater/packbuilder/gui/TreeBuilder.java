package org.mcupdater.packbuilder.gui;

import javafx.scene.control.TreeItem;
import org.mcupdater.api.Version;
import org.mcupdater.model.*;
import org.mcupdater.model.Module;
import org.mcupdater.util.ServerPackParser;

public class TreeBuilder {

	public static TreeItem<IPackElement> loadFromUrl(String sourceUrl){
		TreeItem<IPackElement> root = new TreeItem<>(ServerPackParser.loadFromURL(sourceUrl, true));
		if (root.getValue() != null) {
			ServerPack pack = (ServerPack) root.getValue();
			for (Server server : pack.getServers()) {
				if (server instanceof RawServer) {
					RawServer rawServer = (RawServer) server;
					TreeItem<IPackElement> serverNode = getRawServerElement(rawServer);
					root.getChildren().add(serverNode);
				}
			}
		}
		return root;
	}

	public static TreeItem<IPackElement> fromRawServer(RawServer server){
		ServerPack pack = new ServerPack("", Version.API_VERSION);
		TreeItem<IPackElement> root = new TreeItem<>(pack);
		TreeItem<IPackElement> serverNode = getRawServerElement(server);
		root.getChildren().add(serverNode);
		return root;
	}

	public static TreeItem<IPackElement> getRawServerElement(RawServer server) {
		TreeItem<IPackElement> serverNode = new TreeItem<>(server);
		for (IPackElement packElement : server.getPackElements()) {
			TreeItem<IPackElement> newNode = new TreeItem<>(packElement);
			serverNode.getChildren().add(newNode);
			if (packElement instanceof Module) {
				if (((Module) packElement).hasSubmodules()) {
					for (GenericModule submod : ((Module) packElement).getSubmodules()) {
						newNode.getChildren().add(new TreeItem<>(submod));
					}
				}
				if (((Module) packElement).hasConfigs()) {
					for (ConfigFile config : ((Module) packElement).getConfigs()) {
						newNode.getChildren().add(new TreeItem<>(config));
					}
				}
			}
		}
		return serverNode;
	}
}

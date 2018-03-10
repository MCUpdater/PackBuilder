package org.mcupdater.packbuilder.gui;

import javafx.scene.control.TreeItem;
import org.mcupdater.model.*;
import org.mcupdater.util.ServerPackParser;

import java.util.List;

public class TreeBuilder {

	public static TreeItem<IPackElement> loadFromUrl(String sourceUrl){
		TreeItem<IPackElement> root = new TreeItem<>(ServerPackParser.loadFromURL(sourceUrl));
		if (root.getValue() != null) {
			ServerPack pack = (ServerPack) root.getValue();
			for (RawServer server : pack.getServers()) {
				TreeItem<IPackElement> serverNode = fromRawServer(server);
				root.getChildren().add(serverNode);
			}
		}
		return root;
	}

	public static TreeItem<IPackElement> fromRawServer(RawServer server) {
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

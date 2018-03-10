package org.mcupdater.test;

import org.mcupdater.model.curse.feed.*;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class TestCurseFeed {
	public static void main(String[] args) {
		long tsStart = System.currentTimeMillis();
		Feed complete = FeedImporter.getFeed(true);
		System.out.println("Feed import time: " + (System.currentTimeMillis() - tsStart) / 1000D + " sec");
		complete.getProjects().stream().filter(p -> p.getCategorySection().getName().equals("Mods")).filter(p -> p.getGameVersionLatestFiles().stream().anyMatch(f -> f.getGameVesion().equals("1.12.2"))).sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName())).forEach(p -> {
			//System.out.println(p.getName() + ": " + "https://minecraft.curseforge.com/projects/" + p.getId() + "/files/" + p.getGameVersionLatestFiles().stream().filter(f -> f.getGameVesion().equals("1.12.2")).findFirst().get().getProjectFileID() + "/download");
			System.out.println(p.getName() + ": " + p.getDownloadCount().intValue());
		});
	}
}

package com.markineo.hmachines.util;

import java.util.UUID;

import org.bukkit.Bukkit;

import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderUtil {
	public static String getOfflinePlayerPlaceholder(UUID uuid, String placeholder) {
        String replaced = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), "%" + placeholder + "%");
        return replaced;
    }
}

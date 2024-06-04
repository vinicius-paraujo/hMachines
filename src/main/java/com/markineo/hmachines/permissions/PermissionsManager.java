package com.markineo.hmachines.permissions;


import java.util.UUID;
import java.util.concurrent.ExecutionException;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class PermissionsManager {
    private static LuckPerms luckPerms;

    public PermissionsManager() {
        PermissionsManager.luckPerms = LuckPermsProvider.get();
    }

    /**
     ** Verifica se um jogador possui uma permissão no proxy.
     * 
     * @param uuid A UUID do jogador.
     * @param permission A permissão em questão.
     **/
    public static boolean playerHasProxyPermission(UUID uuid, String permission) {
        try {
            User user = luckPerms.getUserManager().loadUser(uuid).get();
            
            return user == null ? false : user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean playerHasProxyPermission(String playerName, String permission) {
        UUID uuid = getPlayerUUIDbyName(playerName);
        
        return uuid == null ? false : playerHasProxyPermission(uuid, permission);
    }
    
    public static User getUser(String UUIDorName) {
        try {
            UUID uuid = UUID.fromString(UUIDorName);

            return luckPerms.getUserManager().loadUser(uuid).get();
        } catch (IllegalArgumentException e) {
            try {
                UUID uuid = getPlayerUUIDbyName(UUIDorName);
                if (uuid != null) {
                    return luckPerms.getUserManager().loadUser(uuid).get();
                } else {
                    return null;
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getPlayerExactName(UUID playerUUID) {
    	try {
			return luckPerms.getUserManager().loadUser(playerUUID).get().getUsername();
    	} catch (InterruptedException | ExecutionException e) {
    		e.printStackTrace();
    		return null;
    	}
    }

    public static UUID getPlayerUUIDbyName(String playerName) {
        try {
            return luckPerms.getUserManager().lookupUniqueId(playerName).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}

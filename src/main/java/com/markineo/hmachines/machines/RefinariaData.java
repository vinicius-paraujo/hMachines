package com.markineo.hmachines.machines;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Location;

public class RefinariaData {
    private int refinariaId;
    private String refinariaKey;
    
    private UUID ownerUUID;
    private UUID worldUUID;
    
    private boolean active;
    
    private String combustivelAtualKey;
    private int combustivelAmount;
    private int combustivelMax;
    
    private Location location;
    
    public RefinariaData(int refinariaId, String refinariaKey, UUID ownerUUID, boolean active, String combustivelAtualKey, int combustivelAmount, int combustivelMax, Location location, UUID worldUUID) {
        this.refinariaId = refinariaId;
        this.refinariaKey = refinariaKey;
        this.ownerUUID = ownerUUID;
        this.active = active;
        this.combustivelAtualKey = combustivelAtualKey;
        this.combustivelAmount = combustivelAmount;
        this.combustivelMax = combustivelMax;
        this.location = location;
        this.worldUUID = worldUUID;
    }

    public int getRefinariaId() {
        return refinariaId;
    }

    public String getRefinariaKey() {
        return refinariaKey;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean isActive() {
        return active;
    }
    
    public void changeActive(boolean state) {
    	active = state;
    	
    	try {
			MachinesManager.updateRefinariaColumn(refinariaId, "active", state);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public String getCombustivelAtualKey() {
        return combustivelAtualKey;
    }
    
    public void changeCombustivelType(String key) {
    	combustivelAtualKey = key;
    	
    	try {
			MachinesManager.updateRefinariaColumn(refinariaId, "combustivel_atual_key", key);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public int getCombustivelAmount() {
        return combustivelAmount;
    }
   
    public void fastChangeCombustivelAmount(int size) {
    	combustivelAmount = size;
    }
    
    public void changeCombustivelAmount(int size) {
    	combustivelAmount = size;
    	
    	try {
			MachinesManager.updateRefinariaColumn(refinariaId, "combustivel_amount", size);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public int getCombustivelMax() {
        return combustivelMax;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }
}

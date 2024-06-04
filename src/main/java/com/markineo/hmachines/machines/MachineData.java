package com.markineo.hmachines.machines;

import java.sql.SQLException;
import java.util.UUID;

public class MachineData {
	private int machineId;
	private String machineKey;
	private UUID ownerUUID;
	
	private boolean active;
	private boolean broken;
	
	private int litrosAtuais;
	private int litrosMax;
	private int stacks;
	
	public MachineData(int machineId, String machineKey, UUID ownerUUID, boolean active, boolean broken, int litrosAtuais, int litrosMax, int stacks) {
		this.machineId = machineId;
		this.machineKey = machineKey;
		this.ownerUUID = ownerUUID;
		this.active = active;
		this.broken = broken;
		this.litrosAtuais = litrosAtuais;
		this.litrosMax = litrosMax;
		this.stacks = stacks;
	}
	
	public int getMachineId() {
		return machineId;
	}
	
	public String getMachineKey() {
		return machineKey;
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
			MachinesManager.updateMachineColumn(machineId, "active", state);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
	
	public boolean isBroken() {
		return broken;
	}
	
	public void setBrokenState(boolean state) {
		broken = state;
		
		try {
			MachinesManager.updateMachineColumn(machineId, "broken", state);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getLitros() {
		return litrosAtuais;
	}
	
	public void updateLitrosAmount(int size) {
		litrosAtuais = size;
    	
    	try {
			MachinesManager.updateMachineColumn(machineId, "litros_atuais", size);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void fastUpdateLitrosAmount(int size) {
		litrosAtuais = size;
	}
	
	public int getLitrosMax() {
		return litrosMax;
	}
	
	public void updateCapacidade(int capacidade) {
		litrosMax = capacidade;
		
		try {
			MachinesManager.updateMachineColumn(machineId, "litros_max", capacidade);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getStacks() {
		return stacks;
	}
	
	public void updateStacks(int stacks) {
		this.stacks = stacks;
		
		try {
			MachinesManager.updateMachineColumn(machineId, "stacks", stacks);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

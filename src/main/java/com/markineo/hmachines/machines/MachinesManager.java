package com.markineo.hmachines.machines;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.util.DatabaseManager;

public class MachinesManager {
	private static Logger logger = Machines.getPluginLogger();
	
	private static HashMap<Location, MachineData> serverMachines;
	private static HashMap<Location, RefinariaData> serverRefinarias;
	
	public MachinesManager() {
		MachinesManager.serverMachines = new HashMap<>();
		MachinesManager.serverRefinarias = new HashMap<>();
	}
	
	/* 
	 * 
	 * SETUP
	 * 
	 */
	public static void setupRefinarias() throws SQLException {
	    Connection connection = null;

	    try {
	        connection = DatabaseManager.getConnection();

	        String sql = "SELECT * FROM playersRefinarias";
	        ResultSet rs = DatabaseManager.executeQueryRs(connection, sql);

	        while (rs.next()) {
	            int refinariaId = rs.getInt("refinaria_id");
	            String refinariaKey = rs.getString("refinaria_key");
	            
	            UUID ownerUUID = UUID.fromString(rs.getString("owner_uuid"));
	            
	            boolean active = false;
	            
	            String combustivelAtualKey = rs.getString("combustivel_atual_key");
	            
	            int combustivelAmount = rs.getInt("combustivel_amount");
	            int combustivelMax = rs.getInt("combustivel_max");
	            
	            double posX = rs.getDouble("pos_x");
	            double posY = rs.getDouble("pos_y");
	            double posZ = rs.getDouble("pos_z");
	            
	            UUID worldUUID = UUID.fromString(rs.getString("world_uuid"));
	            World world = Bukkit.getWorld(worldUUID);

	            Location location = new Location(world, posX, posY, posZ);

	            RefinariaData refinaria = new RefinariaData(refinariaId, refinariaKey, ownerUUID, active, combustivelAtualKey, combustivelAmount, combustivelMax, location, worldUUID);

	            serverRefinarias.put(location, refinaria);
	        }

	        logger.info("As refinarias foram carregadas com sucesso do banco de dados.");

	    } finally {
	        if (connection != null) {
	            connection.close();
	        }
	    }
	}
	
	public static void setupMachines() throws SQLException {
	    Connection connection = null;

	    try {
	        connection = DatabaseManager.getConnection();

	        String sql = "SELECT * FROM playersMachines";
	        ResultSet rs = DatabaseManager.executeQueryRs(connection, sql);

	        while (rs.next()) {
	            int machineId = rs.getInt("machine_id");
	            
	            String machineKey = rs.getString("machine_key");
	            
	            boolean broken = rs.getBoolean("broken");
	            boolean active = false;
	            
	            UUID ownerUUID = UUID.fromString(rs.getString("owner_uuid"));
	            
	            int litrosAtuais = rs.getInt("litros_atuais");
	            int litrosMax = rs.getInt("litros_max");
	            int stacks = rs.getInt("stacks");
	            
	            double posX = rs.getDouble("pos_x");
	            double posY = rs.getDouble("pos_y");
	            double posZ = rs.getDouble("pos_z");
	            UUID worldUUID = UUID.fromString(rs.getString("world_uuid"));

	            World world = Bukkit.getWorld(worldUUID);

	            Location location = new Location(world, posX, posY, posZ);
	            MachineData machine = new MachineData(machineId, machineKey, ownerUUID, active, broken, litrosAtuais, litrosMax, stacks);

	            serverMachines.put(location, machine);
	        }
	        
	        logger.info("As máquinas foram carregadas com sucesso do banco de dados.");

	    } finally {
	        if (connection != null) {
	            connection.close();
	        }
	    }
	}
	
	/*
	 * 
	 * SAVE
	 * 
	 */
	public static void saveAllMachines() {
	    if (serverMachines.isEmpty()) {
	        return;
	    }

	    Set<Location> locations = new HashSet<>(serverMachines.keySet());

	    locations.forEach(location -> {
	        MachineData machine = serverMachines.get(location);
	        
	        try {
	            registerMachineDatabase(location, machine);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        
	        serverMachines.remove(location);
	    });
	}
	
	public static void saveAllRefinarias() {
	    if (serverRefinarias.isEmpty()) {
	        return;
	    }

	    Set<Location> locations = new HashSet<>(serverRefinarias.keySet());

	    locations.forEach(location -> {
	        RefinariaData refinaria = serverRefinarias.get(location);
	        
	        try {
	            registerRefinariaDatabase(location, refinaria);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        
	        serverRefinarias.remove(location);
	    });
	}
	
	public static void registerRefinaria(Location location, RefinariaData refinaria) {
		serverRefinarias.put(location, refinaria);
		
		try {
			registerRefinariaDatabase(location, refinaria);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void registerRefinariaDatabase(Location location, RefinariaData refinariaData) throws SQLException {
	    Connection connection = null;

	    try {
	        connection = DatabaseManager.getConnection();

	        int refinariaId = refinariaData.getRefinariaId();
	        String refinariaKey = refinariaData.getRefinariaKey();
	        UUID ownerUUID = refinariaData.getOwnerUUID();
	        boolean active = refinariaData.isActive();
	        String combustivelAtualKey = refinariaData.getCombustivelAtualKey();
	        int combustivelAmount = refinariaData.getCombustivelAmount();
	        int combustivelMax = refinariaData.getCombustivelMax();
	        double posX = location.getX();
	        double posY = location.getY();
	        double posZ = location.getZ();
	        UUID worldUUID = location.getWorld().getUID();

	        String query = "INSERT INTO playersRefinarias (refinaria_id, refinaria_key, owner_uuid, active, combustivel_atual_key, combustivel_amount, combustivel_max, pos_x, pos_y, pos_z, world_uuid) " +
	                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
	                       "ON DUPLICATE KEY UPDATE " +
	                       "refinaria_key = VALUES(refinaria_key), owner_uuid = VALUES(owner_uuid), active = VALUES(active), " +
	                       "combustivel_atual_key = VALUES(combustivel_atual_key), combustivel_amount = VALUES(combustivel_amount), " +
	                       "combustivel_max = VALUES(combustivel_max), pos_x = VALUES(pos_x), pos_y = VALUES(pos_y), " +
	                       "pos_z = VALUES(pos_z), world_uuid = VALUES(world_uuid);";

	        DatabaseManager.executeQuery(connection, query, refinariaId, refinariaKey, ownerUUID.toString(), active, combustivelAtualKey, combustivelAmount, combustivelMax, posX, posY, posZ, worldUUID.toString());
	    } finally {
	        if (connection != null) {
	            connection.close();
	        }
	    }
	}
	
	public static void deleteRefinaria(Location location, RefinariaData refinaria) {
		serverRefinarias.remove(location);
		
		try {
			deleteRefinariaDatabase(refinaria);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteMachine(Location location, MachineData machine) {
		serverMachines.remove(location);
		
		try {
			deleteMachineDatabase(machine);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void registerMachine(Location location, MachineData machine) {
		serverMachines.put(location, machine);
		
		try {
			registerMachineDatabase(location, machine);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void registerMachineDatabase(Location location, MachineData machineData) throws SQLException {
	    Connection connection = null;

	    try {
	        connection = DatabaseManager.getConnection();

	        int id = machineData.getMachineId();

	        String machineKey = machineData.getMachineKey();
	        UUID ownerUUID = machineData.getOwnerUUID();

	        int litrosAtuais = machineData.getLitros();
	        int litrosMax = machineData.getLitrosMax();
	        int stacks = machineData.getStacks();

	        double posX = location.getX();
	        double posY = location.getY();
	        double posZ = location.getZ();

	        UUID worldUUID = location.getWorld().getUID();

	        String query = "INSERT INTO playersMachines (machine_id, machine_key, owner_uuid, litros_atuais, litros_max, stacks, pos_x, pos_y, pos_z, world_uuid) " +
	                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
	                       "ON DUPLICATE KEY UPDATE " +
	                       "machine_key = VALUES(machine_key), owner_uuid = VALUES(owner_uuid), litros_atuais = VALUES(litros_atuais), " +
	                       "litros_max = VALUES(litros_max), stacks = VALUES(stacks), pos_x = VALUES(pos_x), pos_y = VALUES(pos_y), " +
	                       "pos_z = VALUES(pos_z), world_uuid = VALUES(world_uuid);";

	        DatabaseManager.executeQuery(connection, query, id, machineKey, ownerUUID.toString(), litrosAtuais, litrosMax, stacks, posX, posY, posZ, worldUUID.toString());
	    } finally {
	        if (connection != null) {
	            connection.close();
	        }
	    }
	}
	
	public static int getNextMachineId() throws SQLException {
		Connection connection = null;
		
		try {
			connection = DatabaseManager.getConnection();
			
			ResultSet rs = DatabaseManager.executeQueryRs(connection, "SELECT machine_id FROM playersMachines ORDER BY machine_id DESC LIMIT 1;");
			
			if (rs == null || !rs.next()) {
				return 1;
			}
			
			return rs.getInt("machine_id") + 1;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	public static int getNextRefinariaId() throws SQLException {
		Connection connection = null;
		
		try {
			connection = DatabaseManager.getConnection();
			
			ResultSet rs = DatabaseManager.executeQueryRs(connection, "SELECT refinaria_id FROM playersRefinarias ORDER BY refinaria_id DESC LIMIT 1;");
			
			if (rs == null || !rs.next()) {
				return 1;
			}
			
			return rs.getInt("refinaria_id") + 1;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	private static void deleteRefinariaDatabase(RefinariaData refinaria) throws SQLException {
	    Connection connection = null;

	    try {
	        connection = DatabaseManager.getConnection();

	        int refinariaId = refinaria.getRefinariaId();
	        
	        String query = "DELETE FROM playersRefinarias WHERE refinaria_id=?;";

	        DatabaseManager.executeQuery(connection, query, refinariaId);
	    } finally {
	        if (connection != null) {
	            connection.close();
	        }
	    }
	}
	
	private static void deleteMachineDatabase(MachineData machineData) throws SQLException {
	    Connection connection = null;

	    try {
	        connection = DatabaseManager.getConnection();

	        int machineId = machineData.getMachineId();
	        
	        String query = "DELETE FROM playersMachines WHERE machine_id=?;";

	        DatabaseManager.executeQuery(connection, query, machineId);
	    } finally {
	        if (connection != null) {
	            connection.close();
	        }
	    }
	}

	public static void updateRefinariaColumn(int refinariaId, String column, Object value) throws SQLException {
        Connection connection = null;

        try {
            connection = DatabaseManager.getConnection();
            
            String query = "UPDATE playersRefinarias SET " + column + " = ? WHERE refinaria_id = ?";
            DatabaseManager.executeQuery(connection, query, value, refinariaId);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
	
	public static void updateMachineColumn(int machineId, String column, Object value) throws SQLException {
        Connection connection = null;

        try {
            connection = DatabaseManager.getConnection();
            
            String query = "UPDATE playersMachines SET " + column + " = ? WHERE machine_id = ?";
            DatabaseManager.executeQuery(connection, query, value, machineId);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
	
	public static boolean existMachineInRaio(Location location, int raio) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (int dx = -raio; dx <= raio; dx++) {
            for (int dy = -raio; dy <= raio; dy++) {
                for (int dz = -raio; dz <= raio; dz++) {
                    Location currentLocation = new Location(world, x + dx, y + dy, z + dz);

                    if (getMachineAt(currentLocation) != null) {
                    	return true;
                    }
                }
            }
        }

        return false;
    }
	
	public static boolean existsPlayerInRaio(Location location, int raio) {
	    return Bukkit.getOnlinePlayers().stream().anyMatch(player -> player.getLocation().distance(location) <= raio);
	}
	
	public static Location getMachineInRaio(Location location, int raio) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (int dx = -raio; dx <= raio; dx++) {
            for (int dy = -raio; dy <= raio; dy++) {
                for (int dz = -raio; dz <= raio; dz++) {
                    Location currentLocation = new Location(world, x + dx, y + dy, z + dz);

                    if (getMachineAt(currentLocation) != null) {
                        return currentLocation;
                    }
                }
            }
        }

        return null;
    }
	
	public static MachineData getMachineAt(Location location) {
		return serverMachines.get(location);
	}
	
	public static RefinariaData getRefinariaAt(Location location) {
		return serverRefinarias.get(location);
	}
}

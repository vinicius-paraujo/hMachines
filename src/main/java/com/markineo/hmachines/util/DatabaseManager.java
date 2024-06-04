package com.markineo.hmachines.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;

import com.markineo.hmachines.Machines;

public class DatabaseManager {
	private static Logger logger = Machines.getPluginLogger();
	private static BasicDataSource dataSource = new BasicDataSource();
	
	private static String host = FileManager.getDatabaseConfig().getString("database.host");
	private static int port = FileManager.getDatabaseConfig().getInt("database.port");
	private static String database = FileManager.getDatabaseConfig().getString("database.database");
	private static String user = FileManager.getDatabaseConfig().getString("database.user");
	private static String password = FileManager.getDatabaseConfig().getString("database.password");
	
	private static String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?characterEncoding=UTF-8";;
	
	public DatabaseManager() {
		configureConnection();
		
		try {
			setupTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void configureConnection() {
		 try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	    } catch (ClassNotFoundException e) {
	        logger.severe("MySQL JDBC Driver not found. Include it in your library path.");
	        e.printStackTrace();
	        
	        return;
	    }

		dataSource.setUrl(url);
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		dataSource.setMinIdle(5);
		dataSource.setMaxIdle(10);
		dataSource.setMaxTotal(20);
		
		Duration duration = Duration.ofSeconds(5);
		dataSource.setMaxWait(duration);
	}
	
	public static Connection getConnection() throws SQLException {
		int retries = 5;
        while (retries > 0) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("Erro ao obter conexão: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                retries--;
            }
        }
        throw new SQLException("Falha ao obter conexão após várias tentativas.");
	}
	
	public static ResultSet executeQueryRs(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            return statement.executeQuery();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            return null;
        }
    }
	
	public static void executeQuery(Connection connection, String sql, Object... parameters) {
        try {
             PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	private static void setupTables() throws SQLException {
		Connection connection = null;
		
		try {
			connection = getConnection();
			
			try (Statement statement = connection.createStatement()) {
				String playersMachinesTable = "CREATE TABLE IF NOT EXISTS playersMachines ("
                		+ "machine_id INT AUTO_INCREMENT PRIMARY KEY,"
						+ "machine_key VARCHAR(100) NOT NULL,"
						+ "active BOOLEAN,"
						+ "broken BOOLEAN,"
                		+ "owner_uuid VARCHAR(36) NOT NULL,"
                        + "litros_atuais INT NOT NULL,"
                		+ "litros_max INT NOT NULL,"
                        + "stacks INT NOT NULL,"
                        + "pos_x DOUBLE NOT NULL,"
                        + "pos_y DOUBLE NOT NULL,"
                        + "pos_z DOUBLE NOT NULL,"
                        + "world_uuid VARCHAR(36) NOT NULL"
                        + ")";
                statement.executeUpdate(playersMachinesTable);
                
                String playersRefinariasTable = "CREATE TABLE IF NOT EXISTS playersRefinarias ("
                        + "refinaria_id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "refinaria_key INT NOT NULL,"
                        + "owner_uuid VARCHAR(36),"
                        + "active BOOLEAN,"
                        + "combustivel_atual_key VARCHAR(100),"
                        + "combustivel_amount INT NOT NULL,"
                        + "combustivel_max INT NOT NULL,"
                        + "pos_x DOUBLE NOT NULL,"
                        + "pos_y DOUBLE NOT NULL,"
                        + "pos_z DOUBLE NOT NULL,"
                        + "world_uuid VARCHAR(36) NOT NULL"
                        + ")";
                statement.executeUpdate(playersRefinariasTable);
			}
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	};
}

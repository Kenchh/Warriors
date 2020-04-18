package me.rey.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPoolManager {

	private final Plugin plugin;
	
	private HikariDataSource dataSource;
	
	private String hostname, database, username, password;
	private int port;
	
	private int minimumConnections, maximumConnections;
	private long connectionTimeout;
	private String testQuery;
	
	public ConnectionPoolManager(Plugin plugin) {
		this.plugin = plugin;
		init();
		setupPool();
	}
	
	public void init() {
		
		ConfigurationSection mysql = plugin.getConfig().getConfigurationSection("mysql");
		this.hostname = mysql.getString("host");
		this.port = mysql.getInt("port");
		this.username = mysql.getString("username");
		this.database = mysql.getString("database");
		this.password = mysql.getString("password");
		
		minimumConnections = mysql.getInt("minConnections");
		maximumConnections = mysql.getInt("maxConnections");
		connectionTimeout = mysql.getLong("connectionTimeout");
		testQuery = mysql.getString("testQuery");
		
	}
	
	public void setupPool() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(
				
				"jdbc:mysql://" +
				hostname +
				":" +
				port +
				"/" +
				database
				
				);
		
		config.setDriverClassName("com.mysql.jdbc.Driver");
		config.setUsername(username);
		config.setPassword(password);
		config.setMinimumIdle(minimumConnections);
		config.setMaximumPoolSize(maximumConnections);
		config.setConnectionTimeout(connectionTimeout);
		config.setConnectionTestQuery(testQuery);
		dataSource = new HikariDataSource(config);
		
	}

	
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public void close(Connection conn, PreparedStatement ps, ResultSet res) {
		if(conn != null) try { conn.close(); } catch (SQLException ignored) {}
		if(ps != null) try { ps.close(); } catch (SQLException ignored) {}
		if(res != null) try { res.close(); } catch (SQLException ignored) {}
	}
	
	public void closePool() {
		if(dataSource !=null && !dataSource.isClosed()) {
			dataSource.close();
		}
	}
	
}

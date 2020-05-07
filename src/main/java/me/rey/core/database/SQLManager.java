package me.rey.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.Build.BuildSet;
import me.rey.core.utils.Text;

public class SQLManager {
	
	private final Warriors plugin;
	private final ConnectionPoolManager pool;
	private final String playerDataTable;
	
	public SQLManager(Warriors plugin) {
		this.plugin = plugin;
		pool = new ConnectionPoolManager(plugin);
		this.playerDataTable = this.plugin.getConfig().getConfigurationSection("mysql").getString("player_data_table");
		makeTable();
	}
	
	private void makeTable() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			String jsonList = "";
			String[] classNames = new String[ClassType.values().length];
			int count = 0;
			for(ClassType type : ClassType.values()) {
				jsonList += ", " + type.name().toLowerCase() + "_buildset JSON";
				classNames[count] = type.name().toLowerCase() + "_buildset";
				count++;
			}
			
			/*
			 * CREATING WHOLE TABLE
			 */
			String statement = "CREATE TABLE IF NOT EXISTS `" + playerDataTable + "` " +
					"(" + 
					"uuid TEXT" + jsonList +
					")";
			
			conn = pool.getConnection();
			ps = conn.prepareStatement(statement);
			ps.executeUpdate();
			
			
			/*
			 * ADDING ROW IF NOT EXIST
			 */
			for(String name : classNames) {

				String sql = "SELECT * FROM " + this.playerDataTable;
				rs = ps.executeQuery(sql);
				ResultSetMetaData metaData = rs.getMetaData();
			  	int rowCount = metaData.getColumnCount();
			  
				boolean isMyColumnPresent = false;
				String myColumnName = name;
				for (int i = 1; i <= rowCount; i++) {
					  if (myColumnName.equals(metaData.getColumnName(i))) {
					    isMyColumnPresent = true;
					  }
				}

				if (!isMyColumnPresent) {
				  String myColumnType = "JSON";
				  ps.executeUpdate("ALTER TABLE " + this.playerDataTable + " ADD " + myColumnName + " " + myColumnType);
				  Text.log(Warriors.getInstance(), "Sucessfully created column: " + name);
				} else {
					Text.log(Warriors.getInstance(), "Skipped creation of column: " + name);
				}
				
			}
			
			Text.log(Warriors.getInstance(), "Sucessfully executed statement: " + statement);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, rs);
		}
	}
	
	public void onDisable() {
		pool.closePool();
	}
	
	public boolean playerExists(UUID uuid) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			
			String stmt = "SELECT * FROM " + playerDataTable + " WHERE uuid=?";
			ps = conn.prepareStatement(stmt);
			
			ps.setString(1, uuid.toString());
			rs = ps.executeQuery();
			
			if(rs.next()) {
				return true;
			} 
			
			return false;
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, rs);
		}
		
		return false;
	}
	
	
	public void createPlayer(UUID uuid) {
		Connection conn = null;
		PreparedStatement ps = null, insert = null;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			
			String stmt = "SELECT * FROM " + playerDataTable + " WHERE uuid=?";
			ps = conn.prepareStatement(stmt);
			
			ps.setString(1, uuid.toString());
			rs = ps.executeQuery();
			
			rs.next();
			if(!playerExists(uuid)) {
				String stmt2 = "INSERT INTO " + playerDataTable
						+ "(uuid,chain_buildset,diamond_buildset,iron_buildset,gold_buildset,leather_buildset) VALUE(?,?,?,?,?,?)";
				insert = conn.prepareStatement(stmt2);
				insert.setString(1, uuid.toString());
				insert.setString(2, null);
				insert.setString(3, null);
				insert.setString(4, null);
				insert.setString(5, null);
				insert.setString(6, null);
				insert.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, rs);
			pool.close(null, insert, null);
		}
		
	}
	
	public BuildSet getPlayerBuilds(UUID uuid, ClassType classType) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			
			String stmt = "SELECT * FROM " + playerDataTable + " WHERE uuid=?";
			ps = conn.prepareStatement(stmt);
			
			ps.setString(1, uuid.toString());
			rs = ps.executeQuery();
			
			while(rs.next()) {
				
				if(rs.getObject(classType.name().toLowerCase()+"_buildset") == null) return new BuildSet();
				
				JSONObject obj = (JSONObject) new JSONParser().parse(rs.getString(classType.name().toLowerCase()+"_buildset"));
				
				ArrayList<Build> builds = new ArrayList<Build>();
				for(Object o : obj.keySet()) {
					String name = (String) o; 
					JSONObject input = (JSONObject) obj.get(name);
					
					HashMap<Ability, Integer> abilities = new HashMap<Ability, Integer>();
					JSONObject abJson = (JSONObject) input.get("abilities");
					for(Object id : abJson.keySet()) {
						for(Ability a : Warriors.getInstance().getAbilitiesInCache()) {
							if(Long.parseLong((String) id) == a.getIdLong()) {
								abilities.put(a, ((Long) abJson.get(id)).intValue());
							}
						}
					}
					
					Build build = new Build(name, UUID.fromString((String) input.get("uuid")), ((Long) input.get("position")).intValue() , abilities);
					build.setCurrentState((boolean) input.get("selected"));
					builds.add(build);
				}
				
				Build[] finalBuilds = builds.toArray(new Build[builds.size()]);
				
				BuildSet bs = new BuildSet(finalBuilds);
				return bs;
				
			}
						
		} catch (NullPointerException e) {
			return new BuildSet();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, rs);
		}
		
		return new BuildSet();
	}
	
	
	public void setPlayerData(UUID uuid, String column, Object data) {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = pool.getConnection();
			
			String stmt = "UPDATE " + playerDataTable + " SET " + column + "=?  WHERE uuid=?";
			ps = conn.prepareStatement(stmt);
			
			ps.setObject(1, data);
			ps.setString(2, uuid.toString());
			ps.executeUpdate();
						
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
	}
	
	public void createPlayerBuild(UUID uuid, Build b, ClassType classType) {
		
		createPlayer(uuid);
		
		BuildSet bs = this.getPlayerBuilds(uuid, classType);
		bs.add(b);
		
		HashMap<String, HashMap<String, Object>> list = new HashMap<>();
		for(Build build : bs.getArrayList()) {
			HashMap<Long, Integer> query = new HashMap<Long, Integer>();
			
			for(Ability ability : build.getAbilities().keySet()) {
				query.put(ability.getIdLong(), build.getAbilities().get(ability));
			}
			
			HashMap<String, Object> toPut = new HashMap<String, Object>();
			toPut.put("position", build.getPosition());
			toPut.put("uuid", build.getUniqueId().toString());
			toPut.put("abilities", query);
			toPut.put("selected", build.getCurrentState());
			
			
			list.put(build.getRawName(), toPut);
		}
		
		
		this.setPlayerData(uuid, classType.name().toLowerCase()+"_buildset", new JSONObject(list).toJSONString());
	}
	
	public void deletePlayerBuild(UUID uuid, Build b, ClassType classType) {
		
		createPlayer(uuid);
		
		if(this.getPlayerBuilds(uuid, classType).contains(b)) {
		
			BuildSet bs = this.getPlayerBuilds(uuid, classType);
			bs.remove(b);
			
			HashMap<String, HashMap<String, Object>> list = new HashMap<>();
			for(Build build : bs.getArrayList()) {
				HashMap<Long, Integer> query = new HashMap<Long, Integer>();
				
				for(Ability ability : build.getAbilities().keySet()) {
					query.put(ability.getIdLong(), build.getAbilities().get(ability));
				}
				
				HashMap<String, Object> toPut = new HashMap<String, Object>();
				toPut.put("position", build.getPosition());
				toPut.put("uuid", build.getUniqueId().toString());
				toPut.put("abilities", query);
				toPut.put("selected", build.getCurrentState());
				
				list.put(build.getRawName(), toPut);
			}
			
			this.setPlayerData(uuid, classType.name().toLowerCase()+"_buildset", new JSONObject(list).toJSONString());
			
		}
		
	}
	
	public void saveBuild(UUID uuid, Build b, ClassType classType) {
		
		createPlayer(uuid);
		
		BuildSet bs = this.getPlayerBuilds(uuid, classType);
		
		HashMap<String, HashMap<String, Object>> list = new HashMap<>();
		for(Build build : bs.getArrayList()) {
			if(build.getUniqueId().equals(b.getUniqueId())){
				build = b;
			}
			
			HashMap<Long, Integer> query = new HashMap<Long, Integer>();
			
			for(Ability ability : build.getAbilities().keySet()) {
				query.put(ability.getIdLong(), build.getAbilities().get(ability));
			}
			
			HashMap<String, Object> toPut = new HashMap<String, Object>();
			toPut.put("position", build.getPosition());
			toPut.put("uuid", build.getUniqueId().toString());
			toPut.put("abilities", query);
			toPut.put("selected", build.getCurrentState());
			
			list.put(build.getRawName(), toPut);
		}
		
		this.setPlayerData(uuid, classType.name().toLowerCase()+"_buildset", new JSONObject(list).toJSONString());
		
	}
	
	public Map<UUID, HashMap<ClassType, BuildSet>> loadAllBuilds(){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			
			String stmt = "SELECT * FROM " + playerDataTable + " WHERE uuid IS NOT NULL";
			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();
			HashMap<UUID, HashMap<ClassType, BuildSet>> toReturn = new HashMap<>();
			
			while(rs.next()) {
				
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				HashMap<ClassType, BuildSet> builds = new HashMap<ClassType, Build.BuildSet>();
				
				for(ClassType classType : ClassType.values()) builds.put(classType, this.getPlayerBuilds(uuid, classType));
				
				toReturn.put(uuid, builds);
			}
						
			return toReturn;
		} catch (NullPointerException e) {
			return new HashMap<UUID, HashMap<ClassType, BuildSet>>();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, rs);
		}
		
		return new HashMap<UUID, HashMap<ClassType, BuildSet>>();
	}

}
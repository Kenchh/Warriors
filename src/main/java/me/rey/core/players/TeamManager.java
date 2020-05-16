package me.rey.core.players;

import java.util.ArrayList;
import java.util.HashMap;

public class TeamManager {
	
	private static HashMap<User, PlayerTeam> teams = new HashMap<>();
	
	public static HashMap<User, PlayerTeam> getTeams(){
		return teams;
	}
	
	public static PlayerTeam createTeam(User self, PlayerTeam team) {
		if(team == null) return team;
		teams.put(self, team);
		return team;
	}
	
	public static void deleteTeam(User user) {
		teams.remove(user);
	}
	
	public static PlayerTeam getPlayerTeam(User user) {
		PlayerTeam team = null;
		for(User u : teams.keySet())
			if(u.getUniqueId().equals(user.getUniqueId()))
				team = teams.get(u);
		
		return team == null ? createTeam(user, new PlayerTeam(user)) : team;
	}

	public static class PlayerTeam {
		
		private ArrayList<User> team;
		private User self;
		
		public PlayerTeam(User self, User... teammates) {
			team = new ArrayList<User>();
			this.self = self;
			
			if(teammates != null)
				for(User u : teammates)
					team.add(u);
		}
		
		public User getSelf() {
			return self;
		}
		
		public ArrayList<User> getMembers(){
			return team;
		}
		
		public PlayerTeam removeMember(User user) {
			if(hasMember(user))
				this.team.remove(this.getMatchingUser(user));
			return this;
		}
		
		public PlayerTeam addMember(User user) {
			if(!hasMember(user) && TeamManager.getPlayerTeam(user) == null)
				this.team.add(user);
			return this;
		}
		
		public boolean hasMember(User user) {
			return getMatchingUser(user) != null;
		}
		
		private User getMatchingUser(User user) {
			for(User u : team)
				if(u.getUniqueId().equals(user.getUniqueId())) return u;
			return null;
		}
	}

}

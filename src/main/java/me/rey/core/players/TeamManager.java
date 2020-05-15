package me.rey.core.players;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TeamManager {
	
	private static Set<Team> teams = new HashSet<>();
	
	public static Set<Team> getTeams(){
		return teams;
	}
	
	public static Team getPlayerTeam(User user) {
		for(Team t : teams)
			if(t.hasMember(user))
				return t;
		return null;
	}

	public class Team {
		
		ArrayList<User> team;
		
		public Team(User... players) {
			team = new ArrayList<User>();
			
			if(players != null)
				for(User u : players)
					team.add(u);
		}
		
		public ArrayList<User> getMembers(){
			return team;
		}
		
		public Team removeMember(User user) {
			if(hasMember(user))
				this.team.remove(this.getMatchingUser(user));
			return this;
		}
		
		public Team addMember(User user) {
			if(!hasMember(user) && TeamManager.getPlayerTeam(user) == null)
				this.team.add(user);
			return this;
		}
		
		public boolean hasMember(User user) {
			return getMatchingUser(user) != null;
		}
		
		public User getMatchingUser(User user) {
			for(User u : team)
				if(u.getUniqueId().equals(user.getUniqueId())) return u;
			return null;
		}
	}

}

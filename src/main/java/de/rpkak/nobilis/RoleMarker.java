package de.rpkak.nobilis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class RoleMarker {
	private final long roleId;
	private final List<Long> less = new ArrayList<Long>();
	private String nickname = "%";

	public RoleMarker(Role role) {
		roleId = role.getIdLong();
	}

	public RoleMarker(ResultSet sqlResult) throws SQLException {
		roleId = sqlResult.getLong(1);
		String lessStr = sqlResult.getString(2);
		nickname = sqlResult.getString(3);

		if (lessStr.length() != 2) {
			for (String oneLess : lessStr.substring(1, lessStr.length() - 1).split(",")) {
				less.add(Long.parseLong(oneLess));
			}

		}
	}

	public void store(Guild guild) throws SQLException {
		ResultSet resultSet = Main.sqlite.executeWithResult("SELECT * FROM roles_of_server_" + guild.getId());
		boolean is = false;
		while (resultSet.next()) {
			if (resultSet.getLong(1) == roleId) {
				is = true;
				break;
			}
		}
		if (is) {
			if (!(resultSet.getString(2).equals(
					"[" + String.join(",", less.stream().map(l -> l.toString()).collect(Collectors.toSet())) + "]")
					&& resultSet.getString(3).equals(nickname))) {
				Main.sqlite.executeWithoutResult("UPDATE roles_of_server_" + guild.getId() + " SET less = \"["
						+ String.join(",", less.stream().map(l -> l.toString()).collect(Collectors.toSet()))
						+ "]\", nickname = \"" + nickname + "\" WHERE roleid = " + roleId);
			}
		} else {
			Main.sqlite.executeWithoutResult("INSERT INTO roles_of_server_" + guild.getId() + " VALUES(" + roleId
					+ ", \"[" + String.join(",", less.stream().map(l -> l.toString()).collect(Collectors.toSet()))
					+ "]\", \"" + nickname + "\")");
		}
	}

	public long getRoleId() {
		return roleId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public List<Long> getLess() {
		return less;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RoleMarker other = (RoleMarker) obj;
		if (roleId == other.roleId) {
			return true;
		}
		return false;
	}

}

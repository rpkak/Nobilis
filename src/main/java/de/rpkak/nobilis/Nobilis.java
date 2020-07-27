package de.rpkak.nobilis;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import guru.nidi.graphviz.attribute.Arrow;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

public class Nobilis {
	private static List<RoleMarker> getRoleMarkers(Guild guild) throws SQLException {
		ResultSet resultSet = Main.sqlite.executeWithResult("SELECT * FROM roles_of_server_" + guild.getId());
		List<RoleMarker> roleMarkers = new ArrayList<RoleMarker>();
		while (resultSet.next()) {
			roleMarkers.add(new RoleMarker(resultSet));
		}
		return roleMarkers;
	}

	private static RoleMarker checkRole(Role role, MessageChannel channel) throws SQLException {
		Main.sqlite.executeWithoutResult("CREATE TABLE IF NOT EXISTS roles_of_server_" + role.getGuild().getId()
				+ " (roleid INTEGER PRIMARY KEY, less TEXT, nickname TEXT)");
		RoleMarker roleMarker = null;
		for (RoleMarker sRoleMarker : getRoleMarkers(role.getGuild())) {
			if (sRoleMarker.getRoleId() == role.getIdLong()) {
				roleMarker = sRoleMarker;
				break;
			}
		}
		if (roleMarker == null) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Not added");
			builder.setColor(Color.RED);
			builder.setDescription("The role " + role.getAsMention() + " is not added yet.");
			channel.sendMessage(builder.build()).queue();
		}
		return roleMarker;
	}

	public static void add(Role role, MessageChannel channel) throws SQLException {
		Main.sqlite.executeWithoutResult("CREATE TABLE IF NOT EXISTS roles_of_server_" + role.getGuild().getId()
				+ " (roleid INTEGER PRIMARY KEY, less TEXT, nickname TEXT)");
		RoleMarker roleMarker = new RoleMarker(role);
		if (getRoleMarkers(role.getGuild()).contains(roleMarker)) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Already there");
			builder.setColor(Color.BLUE);
			builder.setDescription("The role (" + role.getAsMention() + ") is already added.");
			channel.sendMessage(builder.build()).queue();
		} else {
			roleMarker.store(role.getGuild());
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Added");
			builder.setColor(Color.GREEN);
			builder.setDescription("The role (" + role.getAsMention() + ") is added now.");
			channel.sendMessage(builder.build()).queue();
		}
	}

	public static void rm(Role role, MessageChannel channel) throws SQLException {
		Main.sqlite.executeWithoutResult("CREATE TABLE IF NOT EXISTS roles_of_server_" + role.getGuild().getId()
				+ " (roleid INTEGER PRIMARY KEY, less TEXT, nickname TEXT)");
		boolean right = false;
		for (RoleMarker roleMarker : getRoleMarkers(role.getGuild())) {
			if (roleMarker.getRoleId() == role.getIdLong()) {
				right = true;
				break;
			}
		}

		if (!right) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Not there");
			builder.setColor(Color.BLUE);
			builder.setDescription("The role (" + role.getAsMention() + ") was not added and can't be removed.");
			channel.sendMessage(builder.build()).queue();
		} else {
			Main.sqlite.executeWithoutResult(
					"DELETE FROM roles_of_server_" + role.getGuild().getId() + " WHERE roleid = " + role.getId());
			for (RoleMarker roleMarker : getRoleMarkers(role.getGuild())) {
				if (roleMarker.getLess().contains(role.getIdLong())) {
					roleMarker.getLess().remove(role.getIdLong());
					roleMarker.store(role.getGuild());
				}
			}
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Removed");
			builder.setColor(Color.GREEN);
			builder.setDescription("The role (" + role.getAsMention() + ") is removed now.");
			channel.sendMessage(builder.build()).queue();
		}
	}

	public static void setNickname(Role role, String newNickname, MessageChannel channel) throws SQLException {
		RoleMarker roleMarker = checkRole(role, channel);
		if (roleMarker != null) {
			String oldNickname = roleMarker.getNickname();
			roleMarker.setNickname(newNickname);
			roleMarker.store(role.getGuild());

			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Nickname set");
			builder.setColor(Color.GREEN);
			builder.setDescription("Nickname changed from \"" + oldNickname + "\" to \"" + newNickname + "\".");
			channel.sendMessage(builder.build()).queue();
		}
	}

	public static void give(Role role, Member member, MessageChannel channel) throws SQLException {
		RoleMarker roleMarker = checkRole(role, channel);
		if (roleMarker != null) {
			Guild guild = role.getGuild();

			List<RoleMarker> allRoles = new ArrayList<RoleMarker>();
			allRoles.add(roleMarker);
			while (!allRoles.isEmpty()) {
				RoleMarker thisRoleMarker = allRoles.remove(0);
				for (long lessId : thisRoleMarker.getLess()) {
					allRoles.add(checkRole(guild.getRoleById(lessId), channel));
				}
				guild.addRoleToMember(member, guild.getRoleById(thisRoleMarker.getRoleId())).queue();
			}
			member.modifyNickname(roleMarker.getNickname().replaceAll("%", member.getUser().getName())).queue();

			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Gave role");
			builder.setColor(Color.GREEN);
			builder.setDescription("Gave \"" + role.getAsMention() + "\" to \"" + member.getAsMention() + "\".");
			channel.sendMessage(builder.build()).queue();
		}
	}

	public static void addSubRole(Role role, Role subRole, MessageChannel channel) throws SQLException {
		RoleMarker roleMarker = checkRole(role, channel);
		RoleMarker subRoleMarker = checkRole(subRole, channel);
		if (roleMarker != null && subRoleMarker != null) {

			if (roleMarker.getLess().contains(subRole.getIdLong())) {
				EmbedBuilder builder = new EmbedBuilder();
				builder.setAuthor("Already added");
				builder.setColor(Color.BLUE);
				builder.setDescription("The role or \"" + subRole.getAsMention() + "\" is already a subrole of \""
						+ role.getAsMention() + "\".");
				channel.sendMessage(builder.build()).queue();
				return;
			}

			List<Long> subSubRoleMarkerIds = new ArrayList<Long>();
			List<Long> tempSubRoleMarkerIds = new ArrayList<Long>();

			subSubRoleMarkerIds.add(subRoleMarker.getRoleId());
			tempSubRoleMarkerIds.add(subRoleMarker.getRoleId());

			while (!tempSubRoleMarkerIds.isEmpty()) {
				long tempSubRoleMarkerId = tempSubRoleMarkerIds.remove(0);
				RoleMarker tempSubRoleMarker = checkRole(role.getGuild().getRoleById(tempSubRoleMarkerId), channel);

				subSubRoleMarkerIds.addAll(tempSubRoleMarker.getLess());
				tempSubRoleMarkerIds.addAll(tempSubRoleMarker.getLess());
			}

			if (subSubRoleMarkerIds.contains(role.getIdLong())) {
				EmbedBuilder builder = new EmbedBuilder();
				builder.setAuthor("Can't add subrole");
				builder.setColor(Color.RED);
				builder.setDescription("The role or a subrole of \"" + subRole.getAsMention() + "\" is equals to \""
						+ role.getAsMention() + "\".");
				channel.sendMessage(builder.build()).queue();
			} else {
				roleMarker.getLess().add(subRole.getIdLong());
				roleMarker.store(role.getGuild());

				EmbedBuilder builder = new EmbedBuilder();
				builder.setAuthor("Added subrole");
				builder.setColor(Color.GREEN);
				builder.setDescription(
						"Added subrole \"" + subRole.getAsMention() + "\" to \"" + role.getAsMention() + "\".");
				channel.sendMessage(builder.build()).queue();
			}
		}
	}

	public static void removeSubRole(Role role, Role subRole, MessageChannel channel) throws SQLException {
		RoleMarker roleMarker = checkRole(role, channel);
		RoleMarker subRoleMarker = checkRole(subRole, channel);
		if (roleMarker != null && subRoleMarker != null) {
			if (roleMarker.getLess().remove(subRoleMarker.getRoleId())) {
				roleMarker.store(role.getGuild());
				EmbedBuilder builder = new EmbedBuilder();
				builder.setAuthor("Removed subrole");
				builder.setColor(Color.GREEN);
				builder.setDescription(
						"Removed subrole \"" + subRole.getAsMention() + "\" from \"" + role.getAsMention() + "\".");
				channel.sendMessage(builder.build()).queue();
			} else {
				EmbedBuilder builder = new EmbedBuilder();
				builder.setAuthor("Can't remove subrole");
				builder.setColor(Color.BLUE);
				builder.setDescription("The role \"" + subRole.getAsMention() + "\" is no subrole of \""
						+ role.getAsMention() + "\".");
				channel.sendMessage(builder.build()).queue();
			}
		}
	}

	public static void plotHierarchy(MessageChannel channel, Guild guild) throws SQLException, IOException {
		Main.sqlite.executeWithoutResult("CREATE TABLE IF NOT EXISTS roles_of_server_" + guild.getId()
				+ " (roleid INTEGER PRIMARY KEY, less TEXT, nickname TEXT)");
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Color.GREEN);
		builder.setAuthor("Hierarchy");

		List<RoleMarker> roleMarkers = getRoleMarkers(guild);

		if (roleMarkers.isEmpty()) {
			builder.setFooter("[There is no added role yet.]");
			channel.sendMessage(builder.build()).queue();
		} else {
			MutableGraph graph = mutGraph("Hierarchy").setDirected(true).use((ingraph, ctx) -> {
				ctx.linkAttrs().add(Arrow.NONE);
				ctx.graphAttrs().add(Rank.dir(RankDir.BOTTOM_TO_TOP));
				ctx.graphAttrs().add(guru.nidi.graphviz.attribute.Color.rgb(54, 57, 63).background());
				ctx.nodeAttrs().add(guru.nidi.graphviz.attribute.Color.rgb(47, 49, 54).fill());
				ctx.nodeAttrs().add(Style.FILLED);
//				ctx.nodeAttrs().add(guru.nidi.graphviz.attribute.Color.WHITE);
				List<MutableNode> nodes = roleMarkers.stream().map(rm -> {
					Role role = guild.getRoleById(rm.getRoleId());
					MutableNode node = mutNode(role.getName());
//					node.add(Shape.RECTANGLE);
					node.add(Label.html("<font face=\"Arial\" color=\"#"
							+ (role.getColorRaw() == 536870911 ? "8E9297" : Integer.toHexString(role.getColorRaw()))
							+ "\">" + role.getName() + "</font>"));
					return node;
				}).collect(Collectors.toList());
				ingraph.add(nodes);
				for (int i = 0; i < roleMarkers.size(); i++) {
					for (int l = 0; l < roleMarkers.size(); l++) {
						if (roleMarkers.get(i).getLess().contains(roleMarkers.get(l).getRoleId())) {
							nodes.get(l).addLink(nodes.get(i));
						}
					}
				}
			});
//			File file = new File(
//					"hierarchy_" + guild.getName() + "(" + guild.getId() + ")_at_" + System.currentTimeMillis(), "png");
//			Graphviz.fromGraph(graph).width(1000).render(Format.PNG).toFile(file);
//			builder.setImage(file.getPath());
//			new Attachment(id, url, proxyUrl, fileName, size, height, width, jda)
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Graphviz.fromGraph(graph).width(1000).render(Format.PNG).toOutputStream(baos);
			builder.setImage("attachment://hierarchy.png");
			channel.sendFile(baos.toByteArray(), "hierarchy.png").embed(builder.build()).queue();
		}
	}

	public static void info(Role role, MessageChannel channel) throws SQLException {
		RoleMarker roleMarker = checkRole(role, channel);
		if (roleMarker != null) {
			Guild guild = role.getGuild();
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Info");
			builder.setDescription("of " + role.getAsMention());
			builder.setColor(Color.GREEN);
			builder.addField("Nickname", roleMarker.getNickname(), false);
			List<Role> subRoles = roleMarker.getLess().stream().map(id -> guild.getRoleById(id))
					.collect(Collectors.toList());
			builder.addField("Direct subroles",
					subRoles.isEmpty() ? "[None]"
							: String.join(", ", subRoles.stream().map(Role::getName).collect(Collectors.toList())),
					false);

			List<Long> subRoleMarkerIds = new ArrayList<Long>();
			List<Long> tempRoleMarkerIds = new ArrayList<Long>();

			subRoleMarkerIds.add(roleMarker.getRoleId());
			tempRoleMarkerIds.add(roleMarker.getRoleId());

			while (!tempRoleMarkerIds.isEmpty()) {
				long tempRoleMarkerId = tempRoleMarkerIds.remove(0);
				RoleMarker tempRoleMarker = checkRole(role.getGuild().getRoleById(tempRoleMarkerId), channel);

				for (long id : tempRoleMarker.getLess()) {
					if (!subRoleMarkerIds.contains(id)) {
						subRoleMarkerIds.addAll(tempRoleMarker.getLess());
						tempRoleMarkerIds.addAll(tempRoleMarker.getLess());
					}
				}
			}

			builder.addField("All subroles",
					subRoleMarkerIds.isEmpty() ? "[None]"
							: String.join(", ", subRoleMarkerIds.stream().map(guild::getRoleById).map(Role::getName)
									.collect(Collectors.toList())),
					false);

			channel.sendMessage(builder.build()).queue();
		}
	}

	public static void onRoleDelete(Role role) throws SQLException {
		Main.sqlite.executeWithoutResult("CREATE TABLE IF NOT EXISTS roles_of_server_" + role.getGuild().getId()
				+ " (roleid INTEGER PRIMARY KEY, less TEXT, nickname TEXT)");
		boolean right = false;
		for (RoleMarker roleMarker : getRoleMarkers(role.getGuild())) {
			if (roleMarker.getRoleId() == role.getIdLong()) {
				right = true;
				break;
			}
		}
		if (right) {
			Main.sqlite.executeWithoutResult(
					"DELETE FROM roles_of_server_" + role.getGuild().getId() + " WHERE roleid = " + role.getId());
			for (RoleMarker roleMarker : getRoleMarkers(role.getGuild())) {
				if (roleMarker.getLess().contains(role.getIdLong())) {
					roleMarker.getLess().remove(role.getIdLong());
					roleMarker.store(role.getGuild());
				}
			}
		}
	}
}
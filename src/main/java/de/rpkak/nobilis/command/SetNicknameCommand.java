package de.rpkak.nobilis.command;

import java.awt.Color;
import java.util.List;

import de.rpkak.dbu.command.manage.GuildCommand;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetNicknameCommand extends GuildCommand {

	public SetNicknameCommand() {
		super("Set Nickname", new String[] { "set-nickname", "setnickname", "snn", "sn" },
				"Changes or sets a new nickname for a role.\n```\n| set-nickname @[role] [nickname]\n| setnickname @[role] [nickname]\n| snn @[role] [nickname]\n| sn @[role] [nickname]\n```\nFor the nickname ``%`` is a placeholder for the username.\nIf the nickname have a space in it, you must put it in quotation (``set-nickname @Vip \"[Vip] %\"``) marks.\n",
				Permission.ADMINISTRATOR);
	}

	@Override
	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
		if (commandContent.size() < 2) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("Wrong Argument");
			builder.setDescription("You need to have more arguments");
			builder.setColor(Color.RED);
			event.getChannel().sendMessage(builder.build()).queue();
			return;
		}
		String roleStr = commandContent.get(0);
		boolean right = roleStr.startsWith("<@&") && roleStr.endsWith(">");
		if (right) {
			try {
				Role role = guild.getRoleById(roleStr.substring(3, roleStr.length() - 1));
				if (role == null) {
					right = false;
				} else {
					Nobilis.setNickname(role, commandContent.get(1), event.getChannel());
				}
			} catch (NumberFormatException e) {
				right = false;
				e.printStackTrace();
			}
		}
		if (!right) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("No role");
			builder.setDescription("\"" + roleStr + "\" is not a role.");
			builder.setColor(Color.RED);
			event.getChannel().sendMessage(builder.build()).queue();
		}
	}
}

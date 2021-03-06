package de.rpkak.nobilis.command;

import java.awt.Color;
import java.util.List;

import de.rpkak.dbu.command.manage.GuildCommand;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AddCommand extends GuildCommand {

	public AddCommand() {
		super("Add", new String[] { "add", "append", "a" },
				"After this Command, I'm abled to manage a new role for you.\n```\n| add @[role]\n| append @[role]\n| a @[role]\n```",
				Permission.ADMINISTRATOR);
	}

//	@Override
//	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
//		boolean right = !commandContent.isEmpty();
//		String roleStr = "";
//		if (right) {
//			roleStr = commandContent.get(0);
//			right = roleStr.startsWith("<@&") && roleStr.endsWith(">");
//			if (right) {
//				try {
//					Role role = guild.getRoleById(roleStr.substring(3, roleStr.length() - 1));
//					if (role == null) {
//						right = false;
//					} else {
//						Nobilis.add(role, event.getChannel());
//					}
//				} catch (NumberFormatException e) {
//					right = false;
//				}
//			}
//		}
//		if (!right) {
//			EmbedBuilder builder = new EmbedBuilder();
//			builder.setAuthor("No role");
//			builder.setDescription("\"" + roleStr + "\" is not a role.");
//			builder.setColor(Color.RED);
//			event.getChannel().sendMessage(builder.build()).queue();
//		}
//	}

	@Override
	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
		if (!event.getMessage().getMentionedRoles().isEmpty()) {
			Nobilis.add(event.getMessage().getMentionedRoles().get(0), event.getChannel());
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("No role");
			builder.setDescription("You need to write a role as mention.");
			builder.setColor(Color.RED);
			event.getChannel().sendMessage(builder.build()).queue();
		}
	}

}

package de.rpkak.nobilis.command;

import java.awt.Color;
import java.util.List;

import de.rpkak.dbu.command.manage.GuildCommand;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemoveSubroleCommand extends GuildCommand {

	public RemoveSubroleCommand() {
		super("Remove Subrole", new String[] { "rmsr", "rs", "removesubrole", "remove-subrole" },
				"Removes a subrole from a role\n```\n| remove-subrole @[role] @[subrole]\n| removesubrole @[role] @[subrole]\n| rmsr @[role] @[subrole]\n| rs @[role] @[subrole]\n```",
				Permission.ADMINISTRATOR);
	}

	@Override
	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
		if (event.getMessage().getMentionedRoles().size() >= 2) {
			Nobilis.removeSubRole(event.getMessage().getMentionedRoles().get(0),
					event.getMessage().getMentionedRoles().get(1), event.getChannel());
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("No role");
			builder.setDescription("You need to write a role as mention.");
			builder.setColor(Color.RED);
			event.getChannel().sendMessage(builder.build()).queue();
		}
	}

}

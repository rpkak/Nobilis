package de.rpkak.nobilis.command;

import java.awt.Color;
import java.util.List;

import de.rpkak.dbu.command.manage.GuildCommand;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AddSubroleCommand extends GuildCommand {

	public AddSubroleCommand() {
		super("Add Subrole", new String[] { "as", "asr", "addsubrole", "add-subrole" },
				"Adds a new subrole to a role\n```\n| add-subrole @[role] @[subrole]\n| addsubrole @[role] @[subrole]\n| asr @[role] @[subrole]\n| as @[role] @[subrole]\n```",
				Permission.ADMINISTRATOR);
	}

	@Override
	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
		if (event.getMessage().getMentionedRoles().size() >= 2) {
			Nobilis.addSubRole(event.getMessage().getMentionedRoles().get(0),
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

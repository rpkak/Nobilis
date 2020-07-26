package de.rpkak.nobilis.command;

import java.awt.Color;
import java.util.List;

import de.rpkak.dbu.command.manage.GuildCommand;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GiveCommand extends GuildCommand {

	public GiveCommand() {
		super("Give", new String[] { "give", "g" },
				"Gives a role with the nickname and all the subroles to a member\n```\n| give @[role] @[member]\n| g @[role] @[member]\n```",
				Permission.ADMINISTRATOR);
	}

	@Override
	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
		if (event.getMessage().getMentionedRoles().isEmpty()) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("No role");
			builder.setDescription("You need to write a role as mention.");
			builder.setColor(Color.RED);
			event.getChannel().sendMessage(builder.build()).queue();
			return;
		}
		if (event.getMessage().getMentionedMembers().size() == 1) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("No role");
			builder.setDescription("You need to write a member/user as mention.");
			builder.setColor(Color.RED);
			event.getChannel().sendMessage(builder.build()).queue();
			return;
		}
		Nobilis.give(event.getMessage().getMentionedRoles().get(0),
				event.getMessage().getMentionedMembers(guild).get(1), event.getChannel());
	}

}

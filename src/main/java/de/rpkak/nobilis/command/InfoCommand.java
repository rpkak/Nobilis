package de.rpkak.nobilis.command;

import java.awt.Color;
import java.util.List;

import de.rpkak.dbu.command.manage.GuildCommand;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InfoCommand extends GuildCommand {

	public InfoCommand() {
		super("Info", new String[] { "info", "i" }, "Shows you some information about the role\n```\n| info\n| i\n```");
	}

	@Override
	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
		if (!event.getMessage().getMentionedRoles().isEmpty()) {
			Nobilis.info(event.getMessage().getMentionedRoles().get(0), event.getChannel());
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor("No role");
			builder.setDescription("You need to write a role as mention.");
			builder.setColor(Color.RED);
			event.getChannel().sendMessage(builder.build()).queue();
		}
	}

}

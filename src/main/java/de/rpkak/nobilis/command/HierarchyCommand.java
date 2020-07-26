package de.rpkak.nobilis.command;

import java.util.List;

import de.rpkak.dbu.command.manage.GuildCommand;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HierarchyCommand extends GuildCommand {

	public HierarchyCommand() {
		super("Hierarchy", new String[] { "hierarchy", "hi" },
				"Shows you the hierarchy of the guild's roles\n```\n| hierarchy\n| hi\n```");
	}

	@Override
	public void onServerCommand(MessageReceivedEvent event, Guild guild, List<String> commandContent) throws Throwable {
		Nobilis.plotHierarchy(event.getChannel(), guild);
	}

}

package de.rpkak.nobilis;

import java.awt.Color;
import java.io.IOException;
import java.sql.SQLException;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;

import de.rpkak.dbu.Utils;
import de.rpkak.dbu.command.HelpCommand;
import de.rpkak.dbu.command.PermissionCommand;
import de.rpkak.dbu.listeners.CommandListener;
import de.rpkak.dbu.sqlite.SQLite;
import de.rpkak.nobilis.command.AddCommand;
import de.rpkak.nobilis.command.AddSubroleCommand;
import de.rpkak.nobilis.command.GiveCommand;
import de.rpkak.nobilis.command.HierarchyCommand;
import de.rpkak.nobilis.command.InfoCommand;
import de.rpkak.nobilis.command.RemoveCommand;
import de.rpkak.nobilis.command.RemoveSubroleCommand;
import de.rpkak.nobilis.command.SetNicknameCommand;
import de.rpkak.nobilis.listener.RoleRemoveListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.utils.JDALogger;

public class Main {
	private static final Logger LOG = JDALogger.getLog(Main.class);

	public static SQLite sqlite;

	public static void main(String[] args) {
		try {
			DefaultShardManagerBuilder shardManagerBuilder = DefaultShardManagerBuilder
					.createDefault(System.getenv("token"));
			sqlite = new SQLite("nobilis_data.db");

			CommandListener commandListener = new CommandListener("<@!727801109502099457>");

			EmbedBuilder perms = new EmbedBuilder();
			perms.setAuthor("Permissions");
			perms.setColor(Color.YELLOW);
			perms.setDescription("If I have all following permissions I can manage your roles perfectly for you.");
			perms.addField(Permission.MANAGE_ROLES.getName(), "for giving the roles to members", false);
			perms.addField("Highter Role",
					"The bot needs to have a role that is higher (equals is not enough) than all roles he should manage.\nTo make a role higher you can go to the server settings / roles and make the bot higher using drag'n'drop.",
					false);
			perms.addField(Permission.NICKNAME_MANAGE.getName() + " (Only for my nickname feature)",
					"for giving members the nickname of the role", false);
			perms.addField("Highter Role than member (Only for my nickname feature)",
					"The bot needs to have a role that is higher (equals is not enough) than all roles a member has where he should change the nickname.\nTo make a role higher you can go to the server settings / roles and make the bot higher using drag'n'drop.",
					false);

			commandListener.addCommand(new PermissionCommand(perms));
			commandListener.addCommand(new HierarchyCommand());
			commandListener.addCommand(new InfoCommand());
			commandListener.addCommand(new AddCommand());
			commandListener.addCommand(new RemoveCommand());
			commandListener.addCommand(new SetNicknameCommand());
			commandListener.addCommand(new GiveCommand());
			commandListener.addCommand(new AddSubroleCommand());
			commandListener.addCommand(new RemoveSubroleCommand());
			commandListener.addCommand(new HelpCommand(commandListener,
					"A subrole **A** of a role **B** is a role that is always given to a member when the role **B** is given to it.\nExample:\n```\n| add @A\n| add @B\n| add-subrole @B @A\n| give @B @a_member\n```\nAfter this the member **a_member** not only has role **B** but he also has role **A**."));

			shardManagerBuilder.addEventListeners(commandListener, new RoleRemoveListener());

			shardManagerBuilder.setStatus(OnlineStatus.ONLINE);
			ShardManager shardManager = shardManagerBuilder.build();

			sqlite.connect();

			Utils.setExit(() -> {
				try {
					if (sqlite != null) {
						sqlite.disconnect();
					}
					if (shardManager != null) {
						shardManager.setStatus(OnlineStatus.OFFLINE);
						shardManager.shutdown();
					}
				} catch (SQLException e) {
					LOG.error("Error while exit", e);
				}
			});
			Utils.checkConsole();
		} catch (IOException | LoginException | IllegalArgumentException | SQLException e) {
			Utils.exit("setup", e);
		}
	}

}

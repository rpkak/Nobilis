package de.rpkak.nobilis.listener;

import de.rpkak.dbu.Utils;
import de.rpkak.nobilis.Nobilis;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleRemoveListener extends ListenerAdapter {
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		Utils.tryCatch(() -> Nobilis.onRoleDelete(event.getRole()), event.getRole().getGuild().getSystemChannel(),
				"in onRoleDelete", event.getRole().getGuild());
	}
}

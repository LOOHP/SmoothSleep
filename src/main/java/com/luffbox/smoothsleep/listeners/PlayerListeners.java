package com.luffbox.smoothsleep.listeners;

import com.luffbox.smoothsleep.PlayerData;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.WorldData;
import com.luffbox.smoothsleep.lib.ConfigHelper;
import com.luffbox.smoothsleep.tasks.UpdateNotifyTask;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class PlayerListeners implements Listener {

	private final SmoothSleep pl;
	public PlayerListeners(SmoothSleep plugin) { pl = plugin; }

	@EventHandler
	public void join(PlayerJoinEvent e) {
		if (pl.data.worldEnabled(e.getPlayer().getWorld())) {
			pl.data.addPlayer(e.getPlayer()).update();
		}
		if (SmoothSleep.hasUpdate && pl.data.config.getBoolean(ConfigHelper.GlobalSettingKey.UPDATE_NOTIFY)) {
			if (e.getPlayer().hasPermission(SmoothSleep.PERM_NOTIFY)) {
				UpdateNotifyTask unt = new UpdateNotifyTask(pl, e.getPlayer());
				unt.runTaskLater(pl, 60);
			}
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent e) { pl.data.removePlayer(e.getPlayer()); }

	@EventHandler
	public void changeWorld(PlayerChangedWorldEvent e) {
		World to = e.getPlayer().getWorld();
		PlayerData pd = pl.data.getPlayerData(e.getPlayer());
		boolean needData = pl.data.worldEnabled(to);
		if (pd == null) { // If true, player data is null
			// If player data is needed, create it; otherwise, do nothing
			if (needData) pl.data.addPlayer(e.getPlayer()).update();
		} else if (needData) { // If true, pd can't be null and we need data, so update the data
			pd.update();
		} else { // If we get here, it means pd isn't null, but we don't need data, so we remove it
			pl.data.removePlayer(e.getPlayer());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void enterBed(PlayerBedEnterEvent e) {
		if (!pl.isEnabled()) { return; }
		World w = e.getPlayer().getWorld();
		if (!pl.data.worldEnabled(w)) { return; }
		WorldData wd = pl.data.getWorldData(w);
		if (wd == null) { SmoothSleep.logWarning("An error occurred while handing PlayerBedEnterEvent. Missing WorldData."); return; }
		PlayerData pd = pl.data.getPlayerData(e.getPlayer());
		if (pd == null) { SmoothSleep.logWarning("An error occurred while handling PlayerBedEnterEvent. Missing PlayerData."); return; }
		if (wd.isNight()) {
			pd.getTimers().resetAll();
			wd.startSleepTick();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void leaveBed(PlayerBedLeaveEvent e) {
		if (!pl.isEnabled()) { return; }
		final World w = e.getPlayer().getWorld();
		if (!pl.data.worldEnabled(w)) { return; }
		final WorldData wd = pl.data.getWorldData(w);
		if (wd == null) { SmoothSleep.logWarning("An error occurred while handing PlayerBedLeaveEvent. Missing WorldData."); return; }
		final PlayerData pd = pl.data.getPlayerData(e.getPlayer());
		if (pd == null) { SmoothSleep.logWarning("An error occurred while handling PlayerBedLeaveEvent. Missing PlayerData."); return; }

		(new BukkitRunnable() {
			@Override
			public void run() {
				pd.wake();
				Set<Player> sleepers = wd.getSleepers();
				sleepers.remove(pd.getPlayer());
				if (sleepers.isEmpty()) {
					wd.stopSleepTick();
				} else {
					SmoothSleep.logDebug("Bed leave: Other players still sleeping");
				}
			}
		}).runTaskLater(pl, 0L);
	}

}

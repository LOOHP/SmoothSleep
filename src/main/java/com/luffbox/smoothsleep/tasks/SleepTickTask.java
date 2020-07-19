package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.PlayerData;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.WorldData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * This task only runs during the night while players are sleeping.
 * It handles stepping ticks and cancels itself when it's done.
 */
public class SleepTickTask extends BukkitRunnable {

	private final SmoothSleep pl;
	private final WorldData wd;

	public SleepTickTask(SmoothSleep plugin, WorldData worldData) {
		pl = plugin;
		wd = worldData;
	}

	@Override
	public void run() {
		Set<Player> sleepers = wd.getSleepers();
		if (!pl.data.isPluginEnabled() || sleepers.isEmpty()) {
			for (PlayerData pd : wd.getPlayerData()) {
				if (pd == null) continue;
				pd.clearActionBar();
				pd.hideBossBar();
			}
			cancel();
			return;
		}
		boolean isNight = wd.isNight();
		long time = wd.getTime();
		if (isNight) { wd.timestep(); }
		if (time > SmoothSleep.SLEEP_TICKS_END - 2500 && time < SmoothSleep.SLEEP_TICKS_END + 500) { wd.clearWeather(); }
		if (!isNight || sleepers.isEmpty()) { cancel(); }
	}
}

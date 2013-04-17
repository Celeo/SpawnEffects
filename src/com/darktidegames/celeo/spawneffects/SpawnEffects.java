package com.darktidegames.celeo.spawneffects;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * <b>SpawnEffects</b>
 * 
 * @author Celeo
 */
public class SpawnEffects extends JavaPlugin
{

	/** The region in which the effects are applied */
	private String spawnRegion = "";
	/** All possible effects we can pull from to add to the player */
	private List<PotionEffectType> effects = new ArrayList<PotionEffectType>();
	/** WorldGuard connection */
	private WorldGuardPlugin wg = null;

	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
	}

	@Override
	public void onEnable()
	{
		load();
		Plugin test = getServer().getPluginManager().getPlugin("WorldGuard");
		if (test == null)
		{
			getLogger().severe("WorldGuard not detected on the server. Shutting this plugin down!");
			getServer().getPluginManager().disablePlugin(this);
		}
		else
			wg = (WorldGuardPlugin) test;
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				for (Player player : getServer().getOnlinePlayers())
					if (getRegionSet(player.getLocation()).contains(spawnRegion))
						refreshEffects(player);
			}
		}, 100L, 100L);
		getLogger().info("Enabled");
	}

	/**
	 * For each effect type in the list, apply to the player for 15 seconds.
	 * 
	 * @param player
	 *            Player
	 */
	private void refreshEffects(Player player)
	{
		for (PotionEffectType type : effects)
			player.addPotionEffect(type.createEffect(15 * 20 * (int) (1 / type.getDurationModifier()), 1));
	}

	/**
	 * 
	 * @param location
	 *            Location
	 * @return List of String region Ids
	 */
	private List<String> getRegionSet(Location location)
	{
		return wg.getRegionManager(location.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(location));
	}

	private void load()
	{
		reloadConfig();
		spawnRegion = getConfig().getString("spawnRegion", "spawn");
		String p = "";
		for (PotionEffectType effect : PotionEffectType.values())
		{
			if (effect == null)
				continue;
			p = "effects." + effect.getName().toLowerCase();
			if (getConfig().isSet(p) && getConfig().getBoolean(p))
				effects.add(effect);
		}
		getLogger().info("Settings loaded from configuration file");
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		getLogger().info("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (player.isOp() || player.hasPermission("spawneffects.reload"))
			{
				load();
				player.sendMessage("§aSettings reloaded from configuration");
			}
			else
				player.sendMessage("§cYou cannot use that command");
		}
		else
			load();
		return true;
	}

}
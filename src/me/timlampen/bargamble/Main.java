package me.timlampen.bargamble;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	ArrayList<String> filenames = new ArrayList<String>();
	HashMap<String, BarInventory> invs = new HashMap<String, BarInventory>();
	HashMap<String, BarInventory> invname = new HashMap<String, BarInventory>();
	public static Economy economy = null;
  
	@Override
	public void onDisable(){
		saveDefaultConfig();
	}
	@Override
	public void onEnable(){
		saveDefaultConfig();
		if(!setupEconomy()){
			System.out.println("BarGamble - Disabling due to lack of Vault Dependency.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
		for(String s : getConfig().getConfigurationSection("inventories").getKeys(false)){
			filenames.add(s);
			BarInventory bi = new BarInventory(this, s);
    	}
	}
  
	public boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = (Economy)economyProvider.getProvider();
		}
		return economy != null;
	}
  //.
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if((sender instanceof Player)){
			Player player = (Player)sender;
			if(command.getName().equalsIgnoreCase("bar")){
				if(args.length==1 && !args[0].contains("config") && !args[0].contains("list") && !args[0].contains("reload")){
					if(!player.getInventory().contains(Material.POTION)){
					String s = args[0];
					if(invs.containsKey(args[0])){
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("nobar")));
						return false;
					}
					if(player.hasPermission(getConfig().getString("inventories." + s.replace(".yml", "") + ".inv.perm"))){
						invs.get(args[0]).construct(player);
					}
					else{
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("noperm")));
					}
				}
				else{
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("cooldown")));
					}
				}
				else if(args.length==0){
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("syntaxerror")));
				}
				else if(args[0].equalsIgnoreCase("list")){
					for(String s : filenames){
						s = s.replace(".yml", "");
						player.sendMessage(ChatColor.DARK_AQUA + "- " + ChatColor.GREEN + s);
					}
				}
				else if(args[0].equalsIgnoreCase("reload")){
					if(player.hasPermission("bar.reload")){
						reloadConfig();
					    for(String s : getConfig().getConfigurationSection("inventories").getKeys(false)){
					    	s = s.toLowerCase();
					    	filenames.add(s);
					    	BarInventory bi = new BarInventory(this, s);
					    	}
						player.sendMessage(ChatColor.GREEN + "BarGamble Config Reloaded");
					}
					else{
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("noperm")));
					}
				}
				else{
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("syntaxerror")));
				}
			}
		}
		else{
			if(command.getName().equalsIgnoreCase("bar")){
				if(args.length>0){
					saveDefaultConfig();
					reloadConfig();
					filenames.clear();
					invs.clear();
					invname.clear();
				    for(String s : getConfig().getConfigurationSection("inventories").getKeys(false)){
				    	filenames.add(s);
				    	BarInventory bi = new BarInventory(this, s);
				    	}
					sender.sendMessage("Config reloaded");
				}
			}
		}
		return true;
	}
}

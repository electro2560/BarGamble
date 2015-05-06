package me.timlampen.bargamble;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EventListener implements Listener{
	Main plugin;
	public EventListener(Main instance){
		this.plugin = instance;
		}
	String success;
	String error;
	String nospace;
	String nomoney;
	String buy;
	
	@EventHandler
	public void onConsume(PlayerItemConsumeEvent event){
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		Random ran = new Random();
		if(item!=null && item.getType()==Material.POTION && item.hasItemMeta() && item.getItemMeta().hasLore()){
			for(String line : item.getItemMeta().getLore()){
				line = ChatColor.stripColor(line);
				if(line.contains("From shop:")){
					String shop = line.replace("From shop: ", "");
					BarInventory inv = plugin.invs.get(shop);
					int reward = inv.rewards.get(item);
					int chance = inv.chances.get(item);
					int r = ran.nextInt(100)+1;
					if(r <= chance){
						EconomyResponse dep = Main.economy.depositPlayer(player, reward);
						if(dep.transactionSuccess()){
							runnable(player, true, inv, item);
							success = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("success").replace("%money%", Main.economy.format(dep.amount)));
							player.sendMessage(success);
							break;
						}
						else{
							error = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("error"));
							player.sendMessage(error);
							break;
						}
					}
					else{
						runnable(player, false, inv, item);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lose")));
						break;
					}
				}
			}
		}
	}
  
	@EventHandler
	public void onClick(InventoryClickEvent event){
		Player player = (Player)event.getWhoClicked();
		if(event.getCurrentItem()!=null && event.getClickedInventory()!=null && event.getCurrentItem().hasItemMeta()){
			for(String s : plugin.invname.keySet()){
				if(event.getClickedInventory().getName().equals(s)){
					BarInventory inv = plugin.invname.get(s);
					event.setCancelled(true);
					if(player.getInventory().firstEmpty()==-1){
						nospace = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("nospace"));
						player.sendMessage(nospace);
						player.closeInventory();
						player.updateInventory();
						return;
					}
					else{
						EconomyResponse r = Main.economy.withdrawPlayer(player, inv.prices.get(event.getCurrentItem()));
						if(r.transactionSuccess()){
							ItemStack item = event.getCurrentItem();
							buy = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("buy").replace("%name%", item.getItemMeta().getDisplayName()).replace("%amt%", Main.economy.format(r.amount)));
							player.sendMessage(buy);
							player.getInventory().setItem(player.getInventory().firstEmpty(), item);
							player.closeInventory();
						}
						else{
							player.closeInventory();
							nomoney = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("nomoney"));
							player.sendMessage(nomoney);
						}
					}
				}
			}
		}
	}
	HashMap<UUID, Integer> counter = new HashMap<UUID, Integer>();
	public void runnable(final Player player, final boolean win, final BarInventory inv, final ItemStack item){
		final Random ran = new Random();
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){

			@Override
			public void run(){
				if(counter.containsKey(player.getUniqueId()) && counter.get(player.getUniqueId())<6){
					counter.put(player.getUniqueId(), counter.get(player.getUniqueId())+1);
					for(int i = 0; i<10; i++){
						int x = ran.nextInt(3)+player.getLocation().getBlockX();
						int y = ran.nextInt(3)+player.getLocation().getBlockY();
						int z = ran.nextInt(3)+player.getLocation().getBlockZ();
						if(!win){
								player.getWorld().playEffect(new Location(player.getWorld(), x, y, z), Effect.getByName(inv.loseeffect.get(item)), 2004);
						}
						else{
							player.getWorld().playEffect(new Location(player.getWorld(), x, y, z), Effect.getByName(inv.wineffect.get(item)), 2004);
						}
					}
					runnable(player, win, inv, item);
				}
				else if(!counter.containsKey(player.getUniqueId())){
					counter.put(player.getUniqueId(), 1);
					for(int i = 0; i<10; i++){
						int x = ran.nextInt(3)+player.getLocation().getBlockX();
						int y = ran.nextInt(3)+player.getLocation().getBlockY();
						int z = ran.nextInt(3)+player.getLocation().getBlockZ();
						if(!win){
								player.getWorld().playEffect(new Location(player.getWorld(), x, y, z), Effect.getByName(inv.loseeffect.get(item)), 2004);
						}
						else{
							player.getWorld().playEffect(new Location(player.getWorld(), x, y, z), Effect.getByName(inv.wineffect.get(item)), 2004);
						}
					}
					runnable(player, win, inv, item);
				}
				else{
					counter.remove(player.getUniqueId());
				}
				
			}}, 5);
	}
}

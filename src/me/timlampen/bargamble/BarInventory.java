package me.timlampen.bargamble;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BarInventory {
	Main p;
	String title;
	HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
	HashMap<ItemStack, Integer> prices = new HashMap<ItemStack, Integer>();
	HashMap<ItemStack, Integer> rewards = new HashMap<ItemStack, Integer>();
	HashMap<ItemStack, Integer> chances = new HashMap<ItemStack, Integer>();
	HashMap<ItemStack, String> loseeffect = new HashMap<ItemStack, String>();
	HashMap<ItemStack, String> wineffect = new HashMap<ItemStack, String>();
	String filename;
	FileConfiguration config;
	int size;
	public BarInventory(Main p, String filename){
		filename = filename.replace(".yml", "");
		this.filename = filename;
		this.p = p;
		config = p.getConfig();
		load();
	}
	
	public void load(){
		size = config.getInt("inventories." + filename + ".inv.rows")*9;
		title = ChatColor.translateAlternateColorCodes('&', config.getString("inventories." + filename + ".inv.name"));
		for(String s : config.getConfigurationSection("inventories." + filename + ".items").getKeys(false)){
			int slot = Integer.parseInt(s);
			int cost = config.getInt("inventories." + filename + ".items." + s + ".price");
			int reward = config.getInt("inventories." + filename + ".items." + s + ".reward");
			int chance = config.getInt("inventories." + filename + ".items." + s + ".chance");
			ItemStack is = new ItemStack(Material.POTION);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("inventories." + filename + ".items." + s + ".name")));
			ArrayList<String> lore = new ArrayList<>();
			lore.add(ChatColor.GREEN + "Buy: $" + convertMoney(cost));
			lore.add(ChatColor.DARK_AQUA + "Chance of winning: " + chance + "/100");
			lore.add(ChatColor.DARK_AQUA + "Potential win: $" + convertMoney(reward));
			for(String str : config.getStringList("inventories." + filename + ".items." + s + ".info")){
				lore.add(ChatColor.translateAlternateColorCodes('&', str));
			}
			lore.add("From shop: " + filename);
			im.setLore(lore);
			is.setItemMeta(im);
			items.put(slot, is);
			prices.put(is, cost);
			chances.put(is, chance);
			rewards.put(is, reward);
			loseeffect.put(is, config.getString("inventories." + filename + ".items." + s + ".loseeffect"));
			wineffect.put(is, config.getString("inventories." + filename + ".items." + s + ".wineffect"));
		}
		p.invs.put(filename, this);
		p.invname.put(title, this);
	}
	
	public void construct(Player player){
		Inventory inv = Bukkit.createInventory(player, size, title);
		for(Integer i : items.keySet()){
			inv.setItem(i-1, items.get(i));
		}
		player.openInventory(inv);
	}
	 public String convertMoney(double amt){
		 DecimalFormat df = new DecimalFormat("#.###");
		 String s = "";
		 Double thou = new Double("1000");
		 Double mill = new Double("1000000");
		 Double bill = new Double("1000000000");
		 Double tril = new Double("1000000000000");
		 Double quad = new Double("1000000000000000");
		 Double quin = new Double("1000000000000000000");
		 if(amt>=quin){
			 amt = amt/quin;
			 s = df.format(amt) + "quin";
		 }
		 else if(amt>=quad){
			 amt = amt/quad;
			 s = df.format(amt) + "quad";
		 }
		 else if(amt>=tril){
			 amt = amt/tril;
			 s = df.format(amt) + "tril";
		 }
		 else if(amt>=bill){
			 amt = amt/bill;
			 s = df.format(amt) + "bil";
		 }
		 else if(amt>=mill){
			 amt = amt/mill;
			 s = df.format(amt) + "mil";
		 }
		 else if(amt>=thou){
			 amt = amt/thou;
			 s = df.format(amt) + "k";
		 }
		 else{
			 s = df.format(amt) + "";
		 }
		 return s;
	 }
}

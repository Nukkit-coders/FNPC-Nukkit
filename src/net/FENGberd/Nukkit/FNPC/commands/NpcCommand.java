package net.FENGberd.Nukkit.FNPC.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import net.FENGberd.Nukkit.FNPC.Main;
import net.FENGberd.Nukkit.FNPC.npc.CommandNPC;
import net.FENGberd.Nukkit.FNPC.npc.NPC;
import net.FENGberd.Nukkit.FNPC.npc.ReplyNPC;
import net.FENGberd.Nukkit.FNPC.npc.TeleportNPC;
import net.FENGberd.Nukkit.FNPC.utils.RegisteredNPC;
import net.FENGberd.Nukkit.FNPC.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class NpcCommand extends Command {
	public NpcCommand() {
		super("fnpc", " ", " ");
		this.setPermission("FNPC.command.fnpc");
	}

	@Override
	public boolean execute(CommandSender sender,String commandLabel,String[] args) {
		if (!this.testPermission(sender)) {
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		try {
			NPC npc;
			switch(args[0]) {
			case "type":
				{
					final String[] data={TextFormat.GREEN + "========== " + TextFormat.YELLOW + "FNPC Type List" + TextFormat.GREEN + " =========="};
					Main.getRegisteredNpcs().values().forEach(npcF -> data[0] += "\n" + TextFormat.YELLOW + npcF.name + TextFormat.WHITE + " - " + TextFormat.AQUA + npcF.description);
					sender.sendMessage(data[0]);
				}
				break;
			case "add":
				if (args.length < 4) {
					return false;
				}
				if (sender instanceof Player) {
					if (NPC.pool.getOrDefault(args[2], null) != null) {
						sender.sendMessage("[NPC] " + TextFormat.RED + "An NPC with the same ID already exists");
						break;
					}
					args[1] = args[1].toLowerCase();
					RegisteredNPC npcClass = Main.getRegisteredNpcClass(args[1].toLowerCase());
					if (npcClass == null) {
						sender.sendMessage("[NPC] " + TextFormat.RED + "The specified type does not exist, use the /fnpc type to see the available types");
					} else {
						Player sender_ = Utils.cast(sender);
						npc = npcClass.npcClass.getConstructor(String.class,String.class,double.class,double.class,double.class,Item.class).newInstance(args[2], args[3], sender_.x, sender_.y, sender_.z, sender_.getInventory().getItemInHand());
						npc.level = sender_.getLevel().getFolderName();
						npc.spawnToAll();
						npc.save();
						sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC created successfully");
					}
				} else {
					sender.sendMessage("[NPC] " + TextFormat.RED + "Please use this command in game");
				}
				break;
			case "transfer":
				if (args.length < 4) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else if (npc instanceof TeleportNPC) {
					HashMap<String, Object> data = new HashMap<>();
					data.put("ip", args[2]);
					data.put("port", args[3]);
					((TeleportNPC) npc).setTeleport(data);
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC inter-server delivery settings are successful");
				} else {
					sender.sendMessage("[NPC] " + TextFormat.RED + "The NPC is not a transport NPC");
				}
				break;
			case "remove":
				if (args.length < 2) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else {
					npc.close();
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC removed");
				}
				break;
			case "reset":
				if (args.length < 2) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
					break;
				}
				if (npc instanceof TeleportNPC) {
					((TeleportNPC) npc).setTeleport(new HashMap<>());
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC transmission point removed successfully");
				} else if (npc instanceof CommandNPC) {
					((CommandNPC) npc).command=new ArrayList<>();
					npc.save();
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC command cleared successfully");
				} else if (npc instanceof ReplyNPC) {
					((ReplyNPC) npc).chat = new ArrayList<>();
					npc.save();
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC session data cleared successfully");
				} else {
					sender.sendMessage("[NPC] " + TextFormat.YELLOW + "The NPC does not have a resettable attribute");
				}
				break;
			case "teleport":
				if (args.length < 2) {
					return false;
				}
				if (sender instanceof Player) {
					npc = NPC.pool.getOrDefault(args[1], null);
					if (npc == null) {
						sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
					} else if (npc instanceof TeleportNPC) {
						((TeleportNPC) npc).setTeleport((Player) sender);
						sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC transmission point is set successfully");
					} else {
						sender.sendMessage("[NPC] " + TextFormat.RED + "The NPC is not a transport NPC");
					}
				} else {
					sender.sendMessage("[NPC] " + TextFormat.RED + "Please use this command in game");
				}
				break;
			case "command":
				if (args.length < 3) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else if (npc instanceof CommandNPC) {
					String cmd = "";
					switch(args[2]) {
					case "add":
						if (args.length < 4) {
							return false;
						}
						for(int i = 3; i < args.length; i++) {
							cmd += args[i] + (i != args.length -1 ? " " : "");
						}
						((CommandNPC) npc).addCommand(cmd);
						sender.sendMessage("[NPC] " + TextFormat.GREEN + "Command added");
						break;
					case "remove":
						if (args.length < 4) {
							return false;
						}
						for(int i = 3; i < args.length; i++) {
							cmd += args[i] + (i !=args.length -1 ? " " : "");
						}
						if (((CommandNPC) npc).removeCommand(cmd)) {
							sender.sendMessage("[NPC] " + TextFormat.GREEN + "Command removed");
						} else {
							sender.sendMessage("[NPC] " + TextFormat.RED + "Adding command failed");
						}
						break;
					case "list":
						final String[] data = { TextFormat.GREEN + "=== NPC Help ===\n" };
						((CommandNPC) npc).command.forEach(cmdData -> data[0] += TextFormat.YELLOW + cmdData + "\n");
						sender.sendMessage(data[0]);
						break;
					default:
						return false;
					}
				} else {
					sender.sendMessage("[NPC] " + TextFormat.RED + "This NPC is not command NPC");
				}
				break;
			case "chat":
				if (args.length < 3) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else if (npc instanceof ReplyNPC) {
					String chat = "";
					switch(args[2]) {
					case "add":
						if (args.length < 4) {
							return false;
						}
						for(int i = 3; i < args.length; i++) {
							chat += args[i] + (i != args.length -1 ? " " : "");
						}
						((ReplyNPC) npc).addChat(chat);
						sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC session data was added successfully");
						break;
					case "remove":
						if (args.length < 4) {
							return false;
						}
						for(int i = 3; i < args.length; i++) {
							chat += args[i] + (i != args.length -1 ? " " : "");
						}
						if (((ReplyNPC) npc).removeChat(chat)) {
							sender.sendMessage("[NPC] " + TextFormat.GREEN + "NPC session data removed successfully");
						} else {
							sender.sendMessage("[NPC] " + TextFormat.RED + "Adding data failed");
						}
						break;
					default:
						return false;
					}
				} else {
					sender.sendMessage("[NPC] " + TextFormat.RED + "The NPC is not a reply NPC");
				}
				break;
			case "name":
				if (args.length < 3) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else {
					npc.setName(args[2]);
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "NameTag changed");
				}
				break;
			case "skin":
				if (args.length < 3) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else {
					npc.setPNGSkin(args[2]);
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "Skin changed");
				}
				break;
			case "item":
				if (args.length < 3) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else {
					String[] itemData = args[2].split(":");
					npc.setHandItem(Item.get(Integer.parseInt(itemData[0]), Integer.parseInt(itemData.length < 2 ? "0" : itemData[1])));
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "Item replaced");
				}
				break;
			case "tphere":
				if (args.length < 2) {
					return false;
				}
				npc = NPC.pool.getOrDefault(args[1], null);
				if (npc == null) {
					sender.sendMessage("[NPC] " + TextFormat.RED + "There is no such NPC");
				} else if (sender instanceof Player) {
					npc.teleport(Utils.cast(sender));
					sender.sendMessage("[NPC] " + TextFormat.GREEN + "Transfered successfully");
				} else {
					sender.sendMessage("[NPC] " + TextFormat.RED + "Please use this command in game");
				}
				break;
			default:
			case "help":
				sender.sendMessage(TextFormat.GREEN + "=== NPC Help ===\n" +
					TextFormat.GREEN + "All commands must be preceded by /fnpc\n" +
					TextFormat.YELLOW + "add <Type> <ID> <Name> - Add an NPC\n" +
					TextFormat.YELLOW + "type - List all available NPC types\n" +
					TextFormat.YELLOW + "remove <ID> - Remove an NPC\n" +
					TextFormat.YELLOW + "skin <ID> <File> - Set NPC's skin\n" +
					TextFormat.YELLOW + "name <ID> <Name> - Set NPC's name\n" +
					TextFormat.YELLOW + "command <ID> <add/remove> <Command> - Add or remove command for NPC\n" +
					TextFormat.YELLOW + "command <ID> list - List NPC's commands\n" +
					TextFormat.YELLOW + "tphere <ID> - Teleport NPC to your position\n" +
					TextFormat.YELLOW + "teleport <ID> - Set NPC transfer destination to your location\n" +
					TextFormat.YELLOW + "transfer <ID> <IP> <Port> - Set up NPC cross-over delivery\n" +
					TextFormat.YELLOW + "reset <ID> - Reset NPC settings\n" +
					TextFormat.YELLOW + "chat <ID> <add/remove> <Chat> - Add or remove NPC conversation data\n" +
					TextFormat.YELLOW + "item <ID> <Item[:Damage]> - Set up NPC hand items\n" +
					TextFormat.YELLOW + "help - See help");
				break;
			}
		}
		catch(Exception e) {
			sender.sendMessage("[NPC] " + TextFormat.RED + "Unknown error occurred");
			e.printStackTrace();
		}
		return true;
	}
}
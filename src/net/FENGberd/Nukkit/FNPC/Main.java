package net.FENGberd.Nukkit.FNPC;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import net.FENGberd.Nukkit.FNPC.commands.NpcCommand;
import net.FENGberd.Nukkit.FNPC.npc.CommandNPC;
import net.FENGberd.Nukkit.FNPC.npc.NPC;
import net.FENGberd.Nukkit.FNPC.npc.ReplyNPC;
import net.FENGberd.Nukkit.FNPC.npc.TeleportNPC;
import net.FENGberd.Nukkit.FNPC.tasks.QuickSystemTask;
import net.FENGberd.Nukkit.FNPC.utils.RegisteredNPC;
import net.FENGberd.Nukkit.FNPC.utils.Utils;

import java.util.HashMap;

@SuppressWarnings("unused")
public class Main extends cn.nukkit.plugin.PluginBase implements cn.nukkit.event.Listener
{
	private static Main obj=null;
	private static HashMap<String,RegisteredNPC> registeredNPC=new HashMap<>();

	public static Main getInstance()
	{
		return Main.obj;
	}

	public static HashMap<String,RegisteredNPC> getRegisteredNpcs()
	{
		return registeredNPC;
	}

	public static RegisteredNPC getRegisteredNpcClass(String name)
	{
		RegisteredNPC npc=Main.registeredNPC.getOrDefault(name.toLowerCase(),null);
		if(npc==null)
		{
			return null;
		}
		return npc;
	}

	public static void unregisterNpc(String name)
	{
		Main.registeredNPC.remove(name.toLowerCase());
	}

	public static boolean registerNpc(String name,String description,Class npcClass)
	{
		return Main.registerNpc(name,description,npcClass,false);
	}

	public static boolean registerNpc(String name,String description,Class npcClass,boolean force)
	{
		name=name.toLowerCase();
		if(NPC.class.isAssignableFrom(npcClass) && ! npcClass.isInterface() && (Main.registeredNPC.getOrDefault(name,null)==null || force))
		{
			Main.registeredNPC.put(name,new RegisteredNPC(Utils.cast(npcClass),name,description));
			NPC.reloadUnknownNPC();
			return true;
		}
		return false;
	}

	@Override
	public void onEnable()
	{
		if(Main.obj==null)
		{
			Main.obj=this;
			Main.registerNpc("normal","Normal NPC (no actual function)",NPC.class,true);
			Main.registerNpc("reply","Reply-type NPC (using /fnpc chat)",ReplyNPC.class,true);
			Main.registerNpc("command","Command NPC (using /fnpc command)",CommandNPC.class,true);
			Main.registerNpc("teleport","Transport NPC (using /fnpc teleport or /fnpc transfer)",TeleportNPC.class,true);
		}
		NPC.init();
		QuickSystemTask quickSystemTask=new QuickSystemTask(this);
		this.getServer().getCommandMap().register("FNPC",new NpcCommand());
		
		this.getServer().getPluginManager().registerEvents(this,this);
		this.getServer().getScheduler().scheduleRepeatingTask(quickSystemTask,1);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerMove(cn.nukkit.event.player.PlayerMoveEvent event)
	{
		NPC.playerMove(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDataPacketReceive(DataPacketReceiveEvent event)
	{

			NPC.packetReceive(event.getPlayer(),event.getPacket());
	}
	

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerJoin(cn.nukkit.event.player.PlayerJoinEvent event)
	{
		NPC.spawnAllTo(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityLevelChange(cn.nukkit.event.entity.EntityLevelChangeEvent event)
	{
		if(event.getEntity() instanceof cn.nukkit.Player)
		{
			NPC.spawnAllTo(Utils.cast(event.getEntity()),event.getTarget());
		}
	}
}
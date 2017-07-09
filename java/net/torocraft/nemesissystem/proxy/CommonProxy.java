package net.torocraft.nemesissystem.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.torocraft.nemesissystem.handlers.AttackHandler;
import net.torocraft.nemesissystem.handlers.ChunkLoadHandler;
import net.torocraft.nemesissystem.handlers.DeathHandler;
import net.torocraft.nemesissystem.handlers.SetAttackTargetHandler;
import net.torocraft.nemesissystem.handlers.SpawnHandler;
import net.torocraft.nemesissystem.handlers.UpdateHandler;
import net.torocraft.nemesissystem.network.MessageOpenNemesisGui;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent e) {

	}

	public void init(FMLInitializationEvent e) {
		SpawnHandler.init();
		UpdateHandler.init();
		AttackHandler.init();
		ChunkLoadHandler.init();
		DeathHandler.init();
		SetAttackTargetHandler.init();
		initPackets();
	}

	private void initPackets() {
		int packetId = 0;
		MessageOpenNemesisGui.init(packetId++);
	}

	public void postInit(FMLPostInitializationEvent e) {

	}
}

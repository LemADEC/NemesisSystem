package net.torocraft.nemesissystem;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent e) {

	}

	public void init(FMLInitializationEvent e) {
		SpawnHandler.init();
		UpdateHandler.init();
	}

	public void postInit(FMLPostInitializationEvent e) {

	}
}

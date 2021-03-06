package net.torocraft.nemesissystem;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NemesisConfig {

	private static final String CATEGORY = "NemesisSystem Settings";
	private static Configuration config;
	private static final String[] DEFAULT_MOB_LIST = {
			"minecraft:zombie",
			"minecraft:zombie_pigman",
			"minecraft:zombie_villager",
			"minecraft:stray",
			"minecraft:husk",
			"minecraft:skeleton"
	};

	public static int NEMESIS_LIMIT = 16;
	public static int BOOK_DROP_CHANCE_BODY_GUARD = 10;
	public static int BOOK_DROP_CHANCE_MOB = 40;
	public static boolean DISCOVERY_ENABLED = true;
	public static String[] MOB_WHITELIST = DEFAULT_MOB_LIST;
	public static int NEMESIS_SETTLE_RADIUS = 5000;

	public static void init(File configFile) {
		if (config == null) {
			config = new Configuration(configFile);
			loadConfiguration();
		}
	}

	private static void loadConfiguration() {
		try {
			DISCOVERY_ENABLED =
					config.getBoolean("DISCOVERY_ENABLED", CATEGORY, true,
							"Enable the Nemesis Discovery System");

			NEMESIS_LIMIT =
					config.getInt("NEMESIS_LIMIT", CATEGORY, 16, 4, 256,
							"Maximum number of nemeses in each dimension");

			BOOK_DROP_CHANCE_BODY_GUARD =
					config.getInt("BOOK_DROP_CHANCE_BODY_GUARD", CATEGORY, 10, -1, 1000,
							"Chance a body guard will drop a discovery book (1 out of n)");

			BOOK_DROP_CHANCE_MOB =
					config.getInt("BOOK_DROP_CHANCE_MOB", CATEGORY, 40, -1, 1000,
							"Chance a mob will drop a discovery book (1 out of n)");

			MOB_WHITELIST =
					config.getStringList("MOB_WHITELIST", CATEGORY, DEFAULT_MOB_LIST,
							"Mobs that will be used to create nemeses. (Must extend EntityCreature)");

			NEMESIS_SETTLE_RADIUS =
					config.getInt("NEMESIS_SETTLE_RADIUS", CATEGORY, 5000, 500, 100000,
							"The distance a nemesis will settle within from the origin when there are not enough nemeses");

			config.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equalsIgnoreCase(NemesisSystem.MODID)) {
			loadConfiguration();
		}
	}
}

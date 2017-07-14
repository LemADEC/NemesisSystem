package net.torocraft.nemesissystem.handlers;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.torocraft.nemesissystem.NemesisSystem;
import net.torocraft.nemesissystem.registry.NemesisRegistry;
import net.torocraft.nemesissystem.util.BehaviorUtil;
import net.torocraft.nemesissystem.util.EntityDecorator;
import net.torocraft.nemesissystem.registry.Nemesis;
import net.torocraft.nemesissystem.registry.NemesisRegistryProvider;
import net.torocraft.nemesissystem.util.NemesisActions;
import net.torocraft.nemesissystem.util.NemesisUtil;
import net.torocraft.nemesissystem.util.SpawnUtil;

public class SpawnHandler {

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new SpawnHandler());
	}

	@SubscribeEvent
	public void handleSpawn(EntityJoinWorldEvent event) {
		if (event.getEntity().world.isRemote || !(event.getEntity() instanceof EntityCreature) || !NemesisUtil.isNemesisClassEntity(event.getEntity())) {
			return;
		}

		NemesisActions.handleRandomPromotions(event.getWorld(), (EntityCreature) event.getEntity());

		if (event.getEntity().getTags().contains(NemesisSystem.TAG_NEMESIS)) {
			Nemesis nemesis = NemesisUtil.loadNemesisFromEntity(event.getEntity());
			if (nemesis == null || !nemesis.isSpawned()) {
				System.out.println(nemesis == null ? "UNKNOWN" : nemesis.getNameAndTitle() + " has already been despawned");
				//event.setCanceled(true);
			}else{
				System.out.println(nemesis.getNameAndTitle() + " has not left the battle grounds yet!");
				nemesis.setUnloaded(null);
				NemesisRegistryProvider.get(event.getWorld()).update(nemesis);
			}
			return;
		}

		if (event.getEntity().getTags().contains(NemesisSystem.TAG_BODY_GUARD)) {
			return;
		}

		Nemesis nemesis = getNemesisForSpawn(event);

		if (nemesis == null) {
			return;
		}

		replaceEntityWithNemesis((EntityCreature)event.getEntity(), nemesis);
	}

	private void replaceEntityWithNemesis(EntityCreature entity, Nemesis nemesis) {
		entity.setDead();
		spawnNemesis(entity.world, entity.getPosition(), nemesis);
	}

	public static void spawnNemesis(World world, BlockPos pos, Nemesis nemesis) {
		if(nemesis.isLoaded() || nemesis.isDead() || nemesis.isSpawned()){
			return;
		}
		EntityCreature nemesisEntity = SpawnUtil.getEntityFromString(world, nemesis.getMob());

		EntityDecorator.decorate(nemesisEntity, nemesis);
		SpawnUtil.spawnEntityLiving(world, nemesisEntity, pos, 1);

		spawnBodyGuard(nemesisEntity, nemesis);
		nemesisAnnounceEffects(nemesisEntity);

		nemesis.setSpawned(nemesisEntity.getEntityId());
		nemesis.setUnloaded(null);
		NemesisRegistryProvider.get(world).update(nemesis);
	}

	private static void nemesisAnnounceEffects(EntityCreature nemesisEntity) {
		World world = nemesisEntity.world;

		if (canSeeSky(nemesisEntity)) {
			world.addWeatherEffect(new EntityLightningBolt(nemesisEntity.world, nemesisEntity.posX, nemesisEntity.posY, nemesisEntity.posZ, true));
		}

		// TODO sound horn

	}

	private static boolean canSeeSky(Entity e) {
		return e.world.canSeeSky(new BlockPos(e.posX, e.posY + (double) e.getEyeHeight(), e.posZ));
	}

	private static void spawnBodyGuard(EntityLiving entity, Nemesis nemesis) {

		// TODO high level nemeses spawn other nemesis in their body guard

		// TODO add body guard ranks? (different armor, ai, weapons)

		int count = 5 + nemesis.getLevel() * 5;

		for (int i = 0; i < count; i++) {
			EntityCreature bodyGuard = new EntityZombie(entity.getEntityWorld());
			bodyGuard.addTag(NemesisSystem.TAG_BODY_GUARD);
			bodyGuard.getEntityData().setUniqueId(NemesisSystem.NBT_NEMESIS_ID, nemesis.getId());
			equipBodyGuard(bodyGuard);
			SpawnUtil.spawnEntityLiving(entity.getEntityWorld(), bodyGuard, entity.getPosition(), 10);
			BehaviorUtil.setFollowSpeed(bodyGuard, 1.5);
		}
	}

	private static void equipBodyGuard(EntityCreature bodyGuard) {
		int color = 0xffffff;
		// TODO change weapon base on rank, or nemesis boss title, or trait?
		bodyGuard.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		bodyGuard.setItemStackToSlot(EntityEquipmentSlot.HEAD, colorArmor(new ItemStack(Items.LEATHER_HELMET, 1), color));
		bodyGuard.setItemStackToSlot(EntityEquipmentSlot.CHEST, colorArmor(new ItemStack(Items.LEATHER_CHESTPLATE, 1), color));
		bodyGuard.setItemStackToSlot(EntityEquipmentSlot.LEGS, colorArmor(new ItemStack(Items.LEATHER_LEGGINGS, 1), color));
		bodyGuard.setItemStackToSlot(EntityEquipmentSlot.FEET, colorArmor(new ItemStack(Items.LEATHER_BOOTS, 1), color));
	}

	protected static ItemStack colorArmor(ItemStack stack, int color) {
		ItemArmor armor = (ItemArmor) stack.getItem();
		armor.setColor(stack, color);
		return stack;
	}

	private static Nemesis getNemesisForSpawn(EntityEvent event) {

		if (!(event.getEntity() instanceof EntityLiving)) {
			return null;
		}

		EntityLiving entity = (EntityLiving) event.getEntity();
		World world = entity.world;
		Random rand = entity.getRNG();

		if (!playerInRange(entity, world)) {
			return null;
		}

		if (otherNemesisNearby(entity, world)) {
			return null;
		}

		List<Nemesis> nemeses = NemesisRegistryProvider.get(event.getEntity().world).list();

		nemeses.removeIf(Nemesis::isLoaded);
		nemeses.removeIf(Nemesis::isDead);

		// TODO only spawn once a day?

		// TODO add a spawn chance, unless nemesis has not been spawned in a long time

		// TODO figure out how to handle nemeses that cannot spawn in their location (Husk not in the desert)

		// TODO increase Nemesis level every time they spawn but are not killed
		
		if (nemeses == null || nemeses.size() < 1) {
			return null;
		}

		String entityType = NemesisUtil.getEntityType(event.getEntity());

		nemeses.removeIf(nemesis -> {

			if (!nemesis.getMob().equals(entityType)) {
				return true;
			}

			if (entity.getDistanceSq(nemesis.getX(), entity.posY, nemesis.getZ()) > nemesis.getRangeSq()) {
				return true;
			}

			return false;
		});

		if (nemeses.size() < 1) {
			return null;
		}

		return nemeses.get(event.getEntity().world.rand.nextInt(nemeses.size()));
	}

	private static boolean otherNemesisNearby(EntityLiving entity, World world) {
		int distance = 100;
		List<EntityLiving> entities = world
				.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(entity.getPosition()).grow(distance, distance, distance));
		for (EntityLiving e : entities) {
			if (e.getTags().contains(NemesisSystem.TAG_NEMESIS)) {
				return true;
			}
		}
		return false;
	}

	private static boolean playerInRange(EntityLiving entity, World world) {
		int distance = 100;
		List<EntityPlayer> players = world
				.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(entity.getPosition()).grow(distance, distance, distance));
		for (EntityPlayer player : players) {
			if (entity.getEntitySenses().canSee(player)) {
				return true;
			}
		}
		return false;
	}

}

package net.torocraft.nemesissystem.handlers;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.torocraft.nemesissystem.NemesisSystem;
import net.torocraft.nemesissystem.registry.NemesisEntry;
import net.torocraft.nemesissystem.util.NemesisActions;
import net.torocraft.nemesissystem.util.NemesisUtil;

public class DeathHandler {

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new DeathHandler());
	}

	public static final String TAG_RONIN = "nemesissystem_ronin";

	@SubscribeEvent
	public void onDrops(LivingDropsEvent event) {
		World world = event.getEntity().getEntityWorld();

		if (world.isRemote || !(event.getEntity() instanceof EntityCreature)) {
			return;
		}

		if (event.getEntity().getTags().contains(NemesisSystem.TAG_NEMESIS)) {
			handleNemesisDrops(event.getDrops(), (EntityCreature) event.getEntity());
		}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {

		World world = event.getEntity().getEntityWorld();

		if (world.isRemote) {
			return;
		}

		Entity slayer = event.getSource().getTrueSource();

		if (event.getEntity() instanceof EntityPlayer && slayer instanceof EntityCreature) {
			handlePlayerDeath((EntityPlayer) event.getEntity(), (EntityCreature) slayer);
			return;
		}

		if (!(event.getEntity() instanceof EntityCreature)) {
			return;
		}

		if (event.getEntity().getTags().contains(NemesisSystem.TAG_NEMESIS)) {
			handleNemesisDeath((EntityCreature) event.getEntity(), slayer);
		}
	}

	@SubscribeEvent
	public void dropExperience(LivingExperienceDropEvent event) {
		if (!event.getEntity().getTags().contains(NemesisSystem.TAG_NEMESIS)) {
			return;
		}

		NemesisEntry nemesis = NemesisUtil.loadNemesisFromEntity(event.getEntity());
		if (nemesis == null) {
			return;
		}
		//TODO determine some kind of formula for scaling the amount of experience received
		event.setDroppedExperience(event.getOriginalExperience() * (nemesis.getLevel() + 1));
	}

	private void handlePlayerDeath(EntityPlayer player, EntityCreature slayer) {
		if (slayer == null) {
			return;
		}
		NemesisEntry nemesis = NemesisUtil.loadNemesisFromEntity(slayer);

		if (nemesis == null) {
			if (NemesisUtil.isNemesisClassEntity(slayer)) {
				NemesisEntry newNemesis = NemesisActions.createAndRegisterNemesis(slayer, slayer.getPosition());
				nemesisDuelIfCrowed(slayer.world, newNemesis);
			}
		} else {
			NemesisActions.promote(player.world, nemesis);
		}
	}

	private void nemesisDuelIfCrowed(World world, NemesisEntry exclude) {
		NemesisActions.duelIfCrowded(world, exclude, true);
	}

	private void handleNemesisDrops(List<EntityItem> drops, EntityCreature nemesisEntity) {
		NemesisEntry nemesis = NemesisUtil.loadNemesisFromEntity(nemesisEntity);
		Random rand = nemesisEntity.getRNG();

		if (nemesis == null) {
			return;
		}

		drops.add(drop(nemesisEntity, new ItemStack(Items.DIAMOND, rand.nextInt(1 + nemesis.getLevel()))));

		for (ItemStack stack : nemesis.getArmorInventory()) {
			drops.add(damageAndDrop(nemesisEntity, stack));
		}

		for (ItemStack stack : nemesis.getHandInventory()) {
			drops.add(damageAndDrop(nemesisEntity, stack));
		}
	}

	private static EntityItem damageAndDrop(EntityCreature entity, ItemStack stack) {
		if (stack.isItemStackDamageable()) {
			stack.setItemDamage(stack.getMaxDamage() - entity.getRNG().nextInt(1 + entity.getRNG().nextInt(Math.max(stack.getMaxDamage() - 3, 1))));
		}
		return drop(entity, stack);
	}

	private static EntityItem drop(EntityCreature entity, ItemStack stack) {
		return new EntityItem(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, stack);
	}

	private static void handleNemesisDeath(EntityCreature nemesisEntity, Entity attacker) {
		NemesisEntry nemesis = NemesisUtil.loadNemesisFromEntity(nemesisEntity);

		if (nemesis == null) {
			return;
		}

		if (attacker == null || !(attacker instanceof EntityLivingBase)) {
			return;
		}

		NemesisActions.demote(nemesisEntity.world, nemesis, attacker.getName());

		NemesisUtil.findNemesisBodyGuards(nemesisEntity.world, nemesis.getId(), nemesisEntity.getPosition())
				.forEach((EntityCreature guard) -> {
					guard.setAttackTarget(null);
					guard.getTags().add(TAG_RONIN);
				});
	}

}

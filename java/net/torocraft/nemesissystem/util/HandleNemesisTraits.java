package net.torocraft.nemesissystem.util;

import java.util.List;
import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.torocraft.nemesissystem.registry.Nemesis;
import net.torocraft.nemesissystem.registry.Nemesis.Trait;

public class HandleNemesisTraits {

	public static void handleTraits(Nemesis nemesis, EntityLiving nemesisEntity) {
		for (Trait trait : nemesis.getTraits()) {
			handleTraitUpdate(nemesisEntity, nemesis, trait);
		}
	}

	private static void handleTraitUpdate(EntityLiving entity, Nemesis nemesis, Trait trait) {
		switch (trait) {
		case DOUBLE_MELEE:
			return;
		case ARROW:
			handleArrowTraitUpdate(entity, nemesis, trait);
			return;
		case SUMMON:
			handleSummonTraitUpdate(entity, nemesis, trait);
			return;
		case REFLECT:
			return;
		case HEAT:
			handleHeatTraitUpdate(entity, nemesis, trait);
			return;
		case POTION:
			handlePotionTraitUpdate(entity, nemesis, trait);
			return;
		case TELEPORT:
			handleTeleportTraitUpdate(entity, nemesis, trait);
		}
	}

	private static void handleTeleportTraitUpdate(EntityLiving entity, Nemesis nemesis, Trait trait) {
		World world = entity.world;
		Random rand = entity.getRNG();

		//TODO teleport away when hurt (back to body guard?)

		if (world.getTotalWorldTime() % 40 != 0) {
			return;
		}

		EntityLivingBase target = entity.getAttackTarget();

		if (target == null) {
			return;
		}

		if (!entity.getEntitySenses().canSee(target)) {
			return;
		}

		int charge = 2 + rand.nextInt(5);

		EntityEnderPearl pearl = new EntityEnderPearl(world, entity);

		double dX = target.posX - entity.posX;
		double dY = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - pearl.posY;
		double dZ = target.posZ - entity.posZ;

		double distanceSq = dX * dX + dY * dY + dZ * dZ;

		if (distanceSq < 20) {
			return;
		}

		double levelDistance = MathHelper.sqrt(dX * dX + dZ * dZ);

		pearl.setThrowableHeading(dX, dY + levelDistance * 0.20000000298023224D, dZ, 1.6F,
				(float) (14 - world.getDifficulty().getDifficultyId() * 4));

		int power = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, entity);
		int punch = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, entity);

		entity.playSound(SoundEvents.ENTITY_ENDERPEARL_THROW, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F));

		world.spawnEntity(pearl);
	}

	private static void handleSummonTraitUpdate(EntityLiving entity, Nemesis nemesis, Trait trait) {
		World world = entity.world;
		Random rand = entity.getRNG();

		EntityLivingBase target = entity.getAttackTarget();

		if (target == null) {
			return;
		}

		if (!entity.getEntitySenses().canSee(target)) {
			return;
		}

		if (rand.nextInt(5) != 0) {
			return;
		}

		// TODO check total number of near by mobs before spawning new ones

		int roll = rand.nextInt(100);

		EntityMob mob;

		if (roll < 45) {
			mob = new EntitySkeleton(world);
		} else if (roll < 90) {
			mob = new EntityZombie(world);
		} else {
			mob = new EntityWitch(world);
		}

		mob.setPosition(entity.posX, entity.posY, entity.posZ);
		world.spawnEntity(mob);
	}

	private static void handleHeatTraitUpdate(EntityLiving entity, Nemesis nemesis, Trait trait) {
		World world = entity.world;
		Random rand = entity.getRNG();
		int heatDistance = 8;

		List<EntityPlayer> playersToCook = world
				.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(entity.getPosition()).grow(heatDistance, heatDistance, heatDistance));
		for (EntityPlayer player : playersToCook) {
			if (entity.getEntitySenses().canSee(player)) {
				player.setFire(10);
			}
		}
	}

	private static void handlePotionTraitUpdate(EntityLiving entity, Nemesis nemesis, Trait trait) {
		World world = entity.world;
		Random rand = entity.getRNG();

		EntityLivingBase target = entity.getAttackTarget();

		if (target == null) {
			return;
		}

		if (!entity.getEntitySenses().canSee(target)) {
			return;
		}

		double targetY = target.posY + (double) target.getEyeHeight() - 1.100000023841858D;
		double targetX = target.posX + target.motionX - entity.posX;
		double d2 = targetY - entity.posY;
		double targetZ = target.posZ + target.motionZ - entity.posZ;

		float f = MathHelper.sqrt(targetX * targetX + targetZ * targetZ);
		PotionType potiontype = PotionTypes.HARMING;

		if (f >= 8.0F && !target.isPotionActive(MobEffects.SLOWNESS)) {
			potiontype = PotionTypes.SLOWNESS;
		} else if (target.getHealth() >= 8.0F && !target.isPotionActive(MobEffects.POISON)) {
			potiontype = PotionTypes.POISON;
		} else if (f <= 3.0F && !target.isPotionActive(MobEffects.WEAKNESS) && rand.nextFloat() < 0.25F) {
			potiontype = PotionTypes.WEAKNESS;
		}

		EntityPotion entitypotion = new EntityPotion(world, entity,
				PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potiontype));
		entitypotion.rotationPitch -= -20.0F;
		entitypotion.setThrowableHeading(targetX, d2 + (double) (f * 0.2F), targetZ, 0.75F, 8.0F);

		world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_WITCH_THROW, entity.getSoundCategory(), 1.0F,
				0.8F + rand.nextFloat() * 0.4F);
		world.spawnEntity(entitypotion);
	}

	private static void handleArrowTraitUpdate(EntityLiving entity, Nemesis nemesis, Trait trait) {

		World world = entity.world;
		Random rand = entity.getRNG();

		EntityLivingBase target = entity.getAttackTarget();

		if (target == null) {
			return;
		}

		if (!entity.getEntitySenses().canSee(target)) {
			return;
		}

		int charge = 2 + rand.nextInt(10);

		EntityArrow arrow = new EntityTippedArrow(world, entity);

		double dX = target.posX - entity.posX;
		double dY = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - arrow.posY;
		double dZ = target.posZ - entity.posZ;

		double levelDistance = (double) MathHelper.sqrt(dX * dX + dZ * dZ);

		arrow.setThrowableHeading(dX, dY + levelDistance * 0.20000000298023224D, dZ, 1.6F,
				(float) (14 - world.getDifficulty().getDifficultyId() * 4));

		int power = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, entity);
		int punch = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, entity);

		arrow.setDamage((double) (charge * 2.0F) + rand.nextGaussian() * 0.25D
				+ (double) ((float) world.getDifficulty().getDifficultyId() * 0.11F));

		if (power > 0) {
			arrow.setDamage(arrow.getDamage() + (double) power * 0.5D + 0.5D);
		}

		if (punch > 0) {
			arrow.setKnockbackStrength(punch);
		}

		// TODO bow enchants

		entity.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F));

		world.spawnEntity(arrow);
	}
}
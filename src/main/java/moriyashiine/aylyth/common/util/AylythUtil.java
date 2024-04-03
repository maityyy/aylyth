package moriyashiine.aylyth.common.util;

import moriyashiine.aylyth.api.interfaces.ExtraPlayerData;
import moriyashiine.aylyth.common.Aylyth;
import moriyashiine.aylyth.common.network.packets.SpawnParticlesAroundPacketS2C;
import moriyashiine.aylyth.common.registry.ModItems;
import moriyashiine.aylyth.common.registry.ModParticles;
import moriyashiine.aylyth.common.registry.ModSoundEvents;
import moriyashiine.aylyth.common.registry.ModStatusEffects;
import moriyashiine.aylyth.common.registry.tag.ModBlockTags;
import moriyashiine.aylyth.common.registry.tag.ModDamageTypeTags;
import moriyashiine.aylyth.common.registry.tag.ModItemTags;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AylythUtil {
	public static final int MAX_TRIES = 8;
	public static final TrackedData<Optional<UUID>> HIND_UUID = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

	public static Identifier id(String string){
		return new Identifier(Aylyth.MOD_ID, string);
	}

	public static void decreaseStack(ItemStack stack, @Nullable LivingEntity living) {
		if (living instanceof PlayerEntity player && player.getAbilities().creativeMode) {
			return;
		}
		stack.decrement(1);
	}

	public static BlockPos getSafePosition(World world, BlockPos.Mutable pos, int tries) {
		if (tries >= MAX_TRIES) {
			return world.getSpawnPos();
		}
		pos.setY(world.getTopY() - 1);
		while (world.isInBuildLimit(pos) && !world.getBlockState(pos).shouldSuffocate(world, pos)) {
			pos.setY(pos.getY() - 1);
		}
		while (world.isInBuildLimit(pos) && world.getBlockState(pos).shouldSuffocate(world, pos)) {
			pos.setY(pos.getY() + 2);
		}
		if (world.getBlockState(pos).isReplaceable() && world.getFluidState(pos).getFluid() == Fluids.EMPTY) {
			return pos.toImmutable();
		}
		return getSafePosition(world, pos.set(MathHelper.nextInt(world.random, pos.getX() - 32, pos.getX() + 32) + 0.5, world.getTopY() - 1, MathHelper.nextInt(world.random, pos.getZ() - 32, pos.getZ() + 32) + 0.5), ++tries);
	}

	public static void teleportTo(LivingEntity living, ServerWorld newWorld, int tries) {
		living.getWorld().playSoundFromEntity(living instanceof PlayerEntity player ? player : null, living, ModSoundEvents.ENTITY_GENERIC_SHUCKED.value(), SoundCategory.PLAYERS, 1, living.getSoundPitch());
		FabricDimensions.teleport(living, newWorld, new TeleportTarget(Vec3d.of(AylythUtil.getSafePosition(newWorld, living.getBlockPos().mutableCopy(), tries)), Vec3d.ZERO, living.headYaw, living.getPitch()));
		newWorld.playSoundFromEntity(null, living, ModSoundEvents.ENTITY_GENERIC_SHUCKED.value(), SoundCategory.PLAYERS, 1, living.getSoundPitch());
	}

	public static void teleportTo(RegistryKey<World> world, LivingEntity living, int tries) {
		living.getWorld().playSoundFromEntity(living instanceof PlayerEntity player ? player : null, living, ModSoundEvents.ENTITY_GENERIC_SHUCKED.value(), SoundCategory.PLAYERS, 1, living.getSoundPitch());
		ServerWorld toWorld = living.getWorld().getServer().getWorld(world);
		FabricDimensions.teleport(living, toWorld, new TeleportTarget(Vec3d.of(AylythUtil.getSafePosition(toWorld, living.getBlockPos().mutableCopy(), tries)), Vec3d.ZERO, living.headYaw, living.getPitch()));
		toWorld.playSoundFromEntity(null, living, ModSoundEvents.ENTITY_GENERIC_SHUCKED.value(), SoundCategory.PLAYERS, 1, living.getSoundPitch());
	}

	public static boolean shouldUndeadAttack(LivingEntity target, LivingEntity attacker) {
		return attacker.getAttacker() != target && target.hasStatusEffect(ModStatusEffects.CIMMERIAN) && attacker.getGroup() == EntityGroup.UNDEAD;
	}

	public static boolean isNearSeep(LivingEntity livingEntity, int radius) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					if (livingEntity.getWorld().getBlockState(mutable.set(livingEntity.getX() + x, livingEntity.getY() + y, livingEntity.getZ() + z)).isIn(ModBlockTags.SEEPS)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static double distanceIfNearSeep(LivingEntity livingEntity, int radius) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					mutable.set(livingEntity.getX() + x, livingEntity.getY() + y, livingEntity.getZ() + z);
					if (livingEntity.getWorld().getBlockState(mutable).isIn(ModBlockTags.SEEPS)) {
						return Math.sqrt(mutable.getSquaredDistance(livingEntity.getBlockPos().getX(), livingEntity.getBlockPos().getY(), livingEntity.getBlockPos().getZ()));
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Check if the DamageSource is to be considered all forms of ympe
	 * @param source source of damage
	 * @return true if the source is some for of ympe
	 */
	public static boolean isSourceYmpe(DamageSource source) {
		if (source.getSource() instanceof LivingEntity livingEntity && livingEntity.getMainHandStack().isIn(ModItemTags.YMPE_WEAPONS)) {
			return true;
		}
		return source.isIn(ModDamageTypeTags.IS_YMPE);
	}

	/**
	 * Copy content from list transferFrom to transferTo and then clear list transferFrom
	 * @param transferTo list of items to transfer to
	 * @param transferFrom list of items to transfer from
	 */
	public static void transferList(DefaultedList<ItemStack> transferTo, DefaultedList<ItemStack> transferFrom) {
		for (int i = 0; i < transferFrom.size(); i++) {
			transferTo.set(i, transferFrom.get(i).copy());
		}
		transferFrom.clear();
	}

	/**
	 * A get/create for {@link ExtraPlayerData}
	 * @param player from whom
	 * @return the extra data of the player
	 */
	public static NbtCompound getPlayerData(PlayerEntity player) {
		if (!((ExtraPlayerData) player).getExtraPlayerData().contains("PersistedPlayer")) {
			((ExtraPlayerData) player).getExtraPlayerData().put("PersistedPlayer", new NbtCompound());
		}
		return ((ExtraPlayerData) player).getExtraPlayerData().getCompound("PersistedPlayer");
	}

	/**
	 * Modified version of {@link PlayerInventory#readNbt}
	 * @param nbt from nbt
	 * @param playerInventory to inventory
	 */
	public static void loadInv(NbtList nbt, PlayerInventory playerInventory) {
		List<ItemStack> blockedItems = new ArrayList<>();
		for (int i = 0; i < nbt.size(); ++i) {
			NbtCompound nbtCompound = nbt.getCompound(i);
			int j = nbtCompound.getByte("Slot") & 255;
			ItemStack itemstack = ItemStack.fromNbt(nbtCompound);
			if (!itemstack.isEmpty()) {
				if (j < playerInventory.main.size()) {
					if (playerInventory.main.get(j).isEmpty()) {
						playerInventory.main.set(j, itemstack);
					} else {
						blockedItems.add(itemstack);
					}
				} else if (j >= 100 && j < playerInventory.armor.size() + 100) {
					if (playerInventory.armor.get(j - 100).isEmpty()) {
						playerInventory.armor.set(j - 100, itemstack);
					} else {
						blockedItems.add(itemstack);
					}
				} else if (j >= 150 && j < playerInventory.offHand.size() + 150) {
					if (playerInventory.offHand.get(j - 150).isEmpty()) {
						playerInventory.offHand.set(j - 150, itemstack);
					} else {
						blockedItems.add(itemstack);
					}
				}
			}
		}
		if(!blockedItems.isEmpty()) {
			blockedItems.forEach(playerInventory::insertStack);
		}
	}

	public static float getVampiricWeaponEffect(LivingEntity attacker, LivingEntity target, ItemStack stack, float originalValue) {
		boolean isSword = stack.isOf(ModItems.VAMPIRIC_SWORD);
		boolean isPickaxe = stack.isOf(ModItems.VAMPIRIC_PICKAXE);
		boolean isHoe = stack.isOf(ModItems.VAMPIRIC_HOE);

        if (attacker.getRandom().nextFloat() >= 0.8) {
            attacker.heal(originalValue * 0.5f);

			PlayerLookup.tracking(attacker).forEach(trackingPlayer -> {
				ServerPlayNetworking.send(trackingPlayer, new SpawnParticlesAroundPacketS2C(attacker.getId(), 32, List.of(ModParticles.VAMPIRIC_DRIP)));
			});

			if (attacker instanceof ServerPlayerEntity player) {
				ServerPlayNetworking.send(player, new SpawnParticlesAroundPacketS2C(player.getId(), 32, List.of(ModParticles.VAMPIRIC_DRIP)));
			}

            if (isSword && target.getAbsorptionAmount() > 0) {
				target.setAbsorptionAmount(target.getAbsorptionAmount() <= 1 ? target.getAbsorptionAmount() / 2f : 0);
            }

            if (isHoe) {
				target.addStatusEffect(new StatusEffectInstance(ModStatusEffects.CRIMSON_CURSE, 20 * 10, 0));
            }

            return originalValue * (isPickaxe && target.getArmor() > 10f ? 1.2f : 1f);
        }

		return originalValue;
    }

	public static float getBlightedWeaponEffect(LivingEntity attacker, LivingEntity target, ItemStack stack, float originalValue) {
		boolean isSword = stack.isOf(ModItems.BLIGHTED_SWORD);
		boolean isPickaxe = stack.isOf(ModItems.BLIGHTED_PICKAXE);
		boolean isHoe = stack.isOf(ModItems.BLIGHTED_HOE);

		if (attacker.getRandom().nextFloat() >= 0.75) {
			int amplifier = attacker.getRandom().nextFloat() <= 0.85 && target.hasStatusEffect(ModStatusEffects.BLIGHT) ? 1 : 0;
			target.addStatusEffect(new StatusEffectInstance(ModStatusEffects.BLIGHT, 20 * 4, amplifier));

			PlayerLookup.tracking(target).forEach(trackingPlayer -> {
				ServerPlayNetworking.send(trackingPlayer, new SpawnParticlesAroundPacketS2C(target.getId(), 32, List.of(ModParticles.BLIGHT_DRIP)));
			});

			if (isSword && target.getAbsorptionAmount() > 0) {
				target.setAbsorptionAmount(target.getAbsorptionAmount() <= 1 ? target.getAbsorptionAmount() / 2f : 0);
			}

			if (isHoe) {
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 2, 0));
			}

			return originalValue * (isPickaxe && target.getArmor() > 10f ? 1.2f : 1f);
		}

		return originalValue;
	}
}

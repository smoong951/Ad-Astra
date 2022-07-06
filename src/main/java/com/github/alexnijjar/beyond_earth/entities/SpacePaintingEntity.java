package com.github.alexnijjar.beyond_earth.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.alexnijjar.beyond_earth.BeyondEarth;
import com.github.alexnijjar.beyond_earth.registry.ModEntityTypes;
import com.github.alexnijjar.beyond_earth.registry.ModItems;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class SpacePaintingEntity extends PaintingEntity {

	public SpacePaintingEntity(EntityType<? extends PaintingEntity> entityType, World world) {
		super(entityType, world);
	}

	protected SpacePaintingEntity(World world, BlockPos pos) {
		this(ModEntityTypes.SPACE_PAINTING, world);
		this.attachmentPos = pos;
	}

	public static Optional<SpacePaintingEntity> placeSpacePainting(World world, BlockPos pos, Direction facing) {
		SpacePaintingEntity paintingEntity = new SpacePaintingEntity(world, pos);

		List<RegistryEntry<PaintingMotive>> spacePaintings = getSpacePaintings();

		if (spacePaintings.isEmpty()) {
			return Optional.empty();
		}

		paintingEntity.setFacing(facing);
		spacePaintings.removeIf(variant -> {
			paintingEntity.motive = variant.value();
			return !paintingEntity.canStayAttached();
		});

		if (spacePaintings.isEmpty()) {
			return Optional.empty();
		}

		int i = spacePaintings.stream().mapToInt(SpacePaintingEntity::getSize).max().orElse(0);
		spacePaintings.removeIf(variant -> SpacePaintingEntity.getSize(variant) < i);
		Optional<RegistryEntry<PaintingMotive>> optional = Util.getRandomOrEmpty(spacePaintings, paintingEntity.random);

		if (optional.isEmpty()) {
			return Optional.empty();
		}

		paintingEntity.motive = optional.get().value();
		paintingEntity.setFacing(facing);
		return Optional.of(paintingEntity);
	}

	protected static int getSize(RegistryEntry<PaintingMotive> variant) {
		return variant.value().getWidth() * variant.value().getHeight();
	}

	public static List<RegistryEntry<PaintingMotive>> getSpacePaintings() {
		List<RegistryEntry<PaintingMotive>> paintings = new ArrayList<>();
		Registry.PAINTING_MOTIVE.forEach(painting -> {
			if (Registry.PAINTING_MOTIVE.getId(painting).getNamespace().equals(BeyondEarth.MOD_ID)) {
				paintings.add(RegistryEntry.of(painting));
			}
		});

		return paintings;
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {

		super.readCustomDataFromNbt(nbt);
		RegistryKey<PaintingMotive> registryKey = RegistryKey.of(Registry.MOTIVE_KEY, new Identifier(nbt.getString("Motive")));
		this.motive = Registry.PAINTING_MOTIVE.getEntry(registryKey).get().value();
		this.facing = Direction.fromHorizontal(nbt.getByte("Facing"));
		this.setFacing(this.facing);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putString("Motive", Registry.PAINTING_MOTIVE.getId(this.motive).toString());
		nbt.putByte("Facing", (byte) this.facing.getHorizontal());
	}

	@Override
	public ItemEntity dropItem(ItemConvertible item) {
		return super.dropItem(ModItems.SPACE_PAINTING);
	}

	@Override
	public ItemStack getPickBlockStack() {
		return ModItems.SPACE_PAINTING.getDefaultStack();
	}
}
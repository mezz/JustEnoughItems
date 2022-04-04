package mezz.jei.forge.plugins.forge.ingredients.fluid;

import com.google.common.base.MoreObjects;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.util.TagUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public class FluidStackHelper implements IIngredientHelper<FluidStack> {
	private final ISubtypeManager subtypeManager;
	private final IColorHelper colorHelper;

	public FluidStackHelper(ISubtypeManager subtypeManager, IColorHelper colorHelper) {
		this.subtypeManager = subtypeManager;
		this.colorHelper = colorHelper;
	}

	@Override
	public IIngredientType<FluidStack> getIngredientType() {
		return ForgeTypes.FLUID_STACK;
	}

	@Override
	public String getDisplayName(FluidStack ingredient) {
		Component displayName = ingredient.getDisplayName();
		return displayName.getString();
	}

	@Override
	public String getUniqueId(FluidStack ingredient, UidContext context) {
		Fluid fluid = ingredient.getFluid();
		ResourceLocation registryName = fluid.getRegistryName();
		StringBuilder result = new StringBuilder()
			.append("fluid:")
			.append(registryName);

		String subtypeInfo = subtypeManager.getSubtypeInfo(ForgeTypes.FLUID_STACK, ingredient, context);
		if (!subtypeInfo.isEmpty()) {
			result.append(":");
			result.append(subtypeInfo);
		}

		return result.toString();
	}

	@Override
	public String getWildcardId(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		ResourceLocation registryName = fluid.getRegistryName();
		return "fluid:" + registryName;
	}

	@SuppressWarnings("removal")
	@Override
	public String getModId(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		ResourceLocation registryName = fluid.getRegistryName();
		if (registryName == null) {
			String ingredientInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("fluid.getRegistryName() returned null for: " + ingredientInfo);
		}

		return registryName.getNamespace();
	}

	@Override
	public Iterable<Integer> getColors(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		FluidAttributes attributes = fluid.getAttributes();
		ResourceLocation fluidStill = attributes.getStillTexture(ingredient);
		if (fluidStill != null) {
			Minecraft minecraft = Minecraft.getInstance();
			TextureAtlasSprite fluidStillSprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
			int renderColor = attributes.getColor(ingredient);
			return colorHelper.getColors(fluidStillSprite, renderColor, 1);
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("removal")
	@Override
	public String getResourceId(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		ResourceLocation registryName = fluid.getRegistryName();
		if (registryName == null) {
			String ingredientInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("fluid.getRegistryName() returned null for: " + ingredientInfo);
		}

		return registryName.getPath();
	}

	@Override
	public ResourceLocation getResourceLocation(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		ResourceLocation registryName = fluid.getRegistryName();
		if (registryName == null) {
			String ingredientInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("fluid.getRegistryName() returned null for: " + ingredientInfo);
		}

		return registryName;
	}

	@Override
	public ItemStack getCheatItemStack(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		Item filledBucket = fluid.getBucket();
		return new ItemStack(filledBucket);
	}

	@Override
	public FluidStack copyIngredient(FluidStack ingredient) {
		return ingredient.copy();
	}

	@Override
	public FluidStack normalizeIngredient(FluidStack ingredient) {
		FluidStack copy = this.copyIngredient(ingredient);
		copy.setAmount(FluidAttributes.BUCKET_VOLUME);
		return copy;
	}

	@Override
	public Collection<ResourceLocation> getTags(FluidStack ingredient) {
		Stream<TagKey<Fluid>> tagKeyStream = Registry.FLUID.getResourceKey(ingredient.getFluid())
			.flatMap(Registry.FLUID::getHolder)
			.map(Holder::tags)
			.orElse(Stream.of());

		return TagUtil.getTags(tagKeyStream);
	}

	@Override
	public String getErrorInfo(@Nullable FluidStack ingredient) {
		if (ingredient == null) {
			return "null";
		}
		MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(FluidStack.class);
		Fluid fluid = ingredient.getFluid();
		if (fluid != null) {
			Component displayName = ingredient.getDisplayName();
			toStringHelper.add("Fluid", displayName.getString());
		} else {
			toStringHelper.add("Fluid", "null");
		}

		toStringHelper.add("Amount", ingredient.getAmount());

		CompoundTag tag = ingredient.getTag();
		if (tag != null) {
			toStringHelper.add("Tag", tag);
		}

		return toStringHelper.toString();
	}

	@Override
	public Optional<ResourceLocation> getTagEquivalent(Collection<FluidStack> ingredients) {
		return TagUtil.getTagEquivalent(ingredients, FluidStack::getFluid, Registry.FLUID::getTags);
	}
}

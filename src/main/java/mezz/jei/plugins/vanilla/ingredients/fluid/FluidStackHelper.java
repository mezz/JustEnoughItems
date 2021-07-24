package mezz.jei.plugins.vanilla.ingredients.fluid;

import java.util.Collection;
import javax.annotation.Nullable;
import java.util.Collections;

import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import com.google.common.base.MoreObjects;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;

public class FluidStackHelper implements IIngredientHelper<FluidStack> {
	private final ISubtypeManager subtypeManager;
	private final IColorHelper colorHelper;

	public FluidStackHelper(ISubtypeManager subtypeManager, IColorHelper colorHelper) {
		this.subtypeManager = subtypeManager;
		this.colorHelper = colorHelper;
	}


	@Override
	@Nullable
	public FluidStack getMatch(Iterable<FluidStack> ingredients, FluidStack toMatch) {
		for (FluidStack fluidStack : ingredients) {
			if (toMatch.getFluid() == fluidStack.getFluid()) {
				return fluidStack;
			}
		}
		return null;
	}

	@Override
	public String getDisplayName(FluidStack ingredient) {
		ITextComponent displayName = ingredient.getDisplayName();
		return displayName.getString();
	}

	@Override
	public String getUniqueId(FluidStack ingredient) {
		return getUniqueId(ingredient, UidContext.Ingredient);
	}

	@Override
	public String getUniqueId(FluidStack ingredient, UidContext context) {
		Fluid fluid = ingredient.getFluid();
		ResourceLocation registryName = fluid.getRegistryName();
		StringBuilder result = new StringBuilder()
			.append("fluid:")
			.append(registryName);
		String subtypeInfo = subtypeManager.getSubtypeInfo(ingredient, context);
		if (subtypeInfo != null && !subtypeInfo.isEmpty()) {
			result.append(":");
			result.append(subtypeInfo);
		}
		return result.toString();
	}

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
			TextureAtlasSprite fluidStillSprite = minecraft.getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(fluidStill);
			int renderColor = attributes.getColor(ingredient);
			return colorHelper.getColors(fluidStillSprite, renderColor, 1);
		}
		return Collections.emptyList();
	}

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
		return ingredient.getFluid().getTags();
	}

	@Override
	public String getErrorInfo(@Nullable FluidStack ingredient) {
		if (ingredient == null) {
			return "null";
		}
		MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(FluidStack.class);
		Fluid fluid = ingredient.getFluid();
		if (fluid != null) {
			ITextComponent displayName = ingredient.getDisplayName();
			toStringHelper.add("Fluid", displayName.getString());
		} else {
			toStringHelper.add("Fluid", "null");
		}

		toStringHelper.add("Amount", ingredient.getAmount());

		CompoundNBT tag = ingredient.getTag();
		if (tag != null) {
			toStringHelper.add("Tag", tag);
		}

		return toStringHelper.toString();
	}
}

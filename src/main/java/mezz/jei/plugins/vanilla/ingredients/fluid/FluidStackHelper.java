package mezz.jei.plugins.vanilla.ingredients.fluid;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collections;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.MoreObjects;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.color.ColorGetter;

public class FluidStackHelper implements IIngredientHelper<FluidStack> {
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
		return ingredient.getLocalizedName();
	}

	@Override
	public String getUniqueId(FluidStack ingredient) {
		if (ingredient.tag != null) {
			return "fluid:" + ingredient.getFluid().getName() + ":" + ingredient.tag;
		}
		return "fluid:" + ingredient.getFluid().getName();
	}

	@Override
	public String getWildcardId(FluidStack ingredient) {
		return getUniqueId(ingredient);
	}

	@Override
	public String getModId(FluidStack ingredient) {
		String defaultFluidName = FluidRegistry.getDefaultFluidName(ingredient.getFluid());
		if (defaultFluidName == null) {
			return "";
		}
		ResourceLocation fluidResourceName = new ResourceLocation(defaultFluidName);
		return fluidResourceName.getNamespace();
	}

	@Override
	public Iterable<Color> getColors(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
		ResourceLocation fluidStill = fluid.getStill();
		if (fluidStill != null) {
			TextureAtlasSprite fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
			if (fluidStillSprite != null) {
				int renderColor = ingredient.getFluid().getColor(ingredient);
				return ColorGetter.getColors(fluidStillSprite, renderColor, 1);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public String getResourceId(FluidStack ingredient) {
		String defaultFluidName = FluidRegistry.getDefaultFluidName(ingredient.getFluid());
		if (defaultFluidName == null) {
			return "";
		}
		ResourceLocation fluidResourceName = new ResourceLocation(defaultFluidName);
		return fluidResourceName.getPath();
	}

	@Override
	public ItemStack getCheatItemStack(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		if (fluid == FluidRegistry.WATER) {
			return new ItemStack(Items.WATER_BUCKET);
		} else if (fluid == FluidRegistry.LAVA) {
			return new ItemStack(Items.LAVA_BUCKET);
		} else if (fluid.getName().equals("milk")) {
			return new ItemStack(Items.MILK_BUCKET);
		} else if (FluidRegistry.isUniversalBucketEnabled()) {
			ItemStack filledBucket = FluidUtil.getFilledBucket(ingredient);
			FluidStack fluidContained = FluidUtil.getFluidContained(filledBucket);
			if (fluidContained != null && fluidContained.isFluidEqual(ingredient)) {
				return filledBucket;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public FluidStack copyIngredient(FluidStack ingredient) {
		return ingredient.copy();
	}

	@Override
	public String getErrorInfo(@Nullable FluidStack ingredient) {
		if (ingredient == null) {
			return "null";
		}
		MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(FluidStack.class);

		Fluid fluid = ingredient.getFluid();
		if (fluid != null) {
			toStringHelper.add("Fluid", fluid.getLocalizedName(ingredient));
		} else {
			toStringHelper.add("Fluid", "null");
		}

		toStringHelper.add("Amount", ingredient.amount);

		if (ingredient.tag != null) {
			toStringHelper.add("Tag", ingredient.tag);
		}

		return toStringHelper.toString();
	}
}

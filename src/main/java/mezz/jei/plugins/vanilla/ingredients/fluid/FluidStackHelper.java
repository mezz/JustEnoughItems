package mezz.jei.plugins.vanilla.ingredients.fluid;

import javax.annotation.Nullable;
import java.util.Collections;

import net.minecraft.fluid.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
		return ingredient.getDisplayName().getFormattedText();
	}

	@Override
	public String getUniqueId(FluidStack ingredient) {
		if (ingredient.hasTag()) {
			return "fluid:" + ingredient.getFluid().getRegistryName() + ":" + ingredient.getTag();
		}
		return "fluid:" + ingredient.getFluid().getRegistryName();
	}

	@Override
	public String getWildcardId(FluidStack ingredient) {
		return getUniqueId(ingredient);
	}

	@Override
	public String getModId(FluidStack ingredient) {
		return ingredient.getFluid().getRegistryName().getNamespace();
	}

	@Override
	public Iterable<Integer> getColors(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		AtlasTexture textureMapBlocks = Minecraft.getInstance().getTextureMap();
		ResourceLocation fluidStill = fluid.getAttributes().getStill(ingredient);
		if (fluidStill != null) {
			TextureAtlasSprite fluidStillSprite = textureMapBlocks.getSprite(fluidStill);
			if (fluidStillSprite != null) {
				int renderColor = ingredient.getFluid().getAttributes().getColor(ingredient);
				return ColorGetter.getColors(fluidStillSprite, renderColor, 1);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public String getResourceId(FluidStack ingredient) {
		return ingredient.getFluid().getRegistryName().toString();
	}

	@Override
	public ItemStack getCheatItemStack(FluidStack ingredient) {
		return new ItemStack(ingredient.getFluid().getFilledBucket());
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
	public String getErrorInfo(FluidStack ingredient) {
		if (ingredient.isEmpty()) {
			return "EMPTY";
		}
		MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(FluidStack.class);

		Fluid fluid = ingredient.getFluid();
		if (fluid != null) {
			toStringHelper.add("Fluid", ingredient.getDisplayName().getFormattedText());
		} else {
			toStringHelper.add("Fluid", "null");
		}

		toStringHelper.add("Amount", ingredient.getAmount());

		if (ingredient.hasTag()) {
			toStringHelper.add("Tag", ingredient.getTag());
		}

		return toStringHelper.toString();
	}
}

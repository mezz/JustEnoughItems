package mezz.jei.api.constants;

import net.minecraftforge.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.ingredients.IIngredientType;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 */
public final class VanillaTypes {
	public static final IIngredientType<ItemStack> ITEM = () -> ItemStack.class;
	public static final IIngredientType<FluidStack> FLUID = () -> FluidStack.class;

	private VanillaTypes() {

	}
}

package mezz.jei.api.constants;

import mezz.jei.api.forge.ForgeTypes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.ingredients.IIngredientType;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 */
public final class VanillaTypes {
	public static final IIngredientType<ItemStack> ITEM = () -> ItemStack.class;
	/**
	 * @deprecated use {@link ForgeTypes#FLUID}
	 */
	@Deprecated(forRemoval = true, since = "9.6.0")
	public static final IIngredientType<FluidStack> FLUID = ForgeTypes.FLUID;

	private VanillaTypes() {

	}
}

package mezz.jei.api.ingredients;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.item.ItemStack;

import mezz.jei.api.recipe.IIngredientType;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 *
 * @since JEI 4.12.0
 */
public final class VanillaTypes {
	public static final IIngredientType<ItemStack> ITEM = () -> ItemStack.class;
	public static final IIngredientType<FluidStack> FLUID = () -> FluidStack.class;

	/**
	 * @deprecated to be removed in 1.13, this is a hack to work around issues with enchanted books.
	 */
	@Deprecated
	public static final IIngredientType<EnchantmentData> ENCHANT = () -> EnchantmentData.class;

	private VanillaTypes() {

	}
}

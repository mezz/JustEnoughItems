package mezz.jei.api.ingredients;

import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 * @since JEI 4.12.0
 */
public final class VanillaTypes {
	public static final IIngredientType<ItemStack> ITEM = () -> ItemStack.class;
	public static final IIngredientType<EnchantmentData> ENCHANT = () -> EnchantmentData.class;
	public static final IIngredientType<FluidStack> FLUID = () -> FluidStack.class;

	private VanillaTypes() {

	}
}

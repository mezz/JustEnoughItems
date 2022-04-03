package mezz.jei.api.ingredients.subtypes;

import mezz.jei.api.ingredients.IIngredientType;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraftforge.fluids.FluidStack;

/**
 * Gets subtype information from ingredients that have subtype interpreters.
 *
 * Add subtypes for your ingredients with {@link ISubtypeRegistration#registerSubtypeInterpreter(Item, IIngredientSubtypeInterpreter)}.
 */
public interface ISubtypeManager {
	/**
	 * Get the data from an ingredient that is relevant to comparing and telling subtypes apart.
	 * Returns null if the ingredient has no information used for subtypes.
	 *
	 * @since 9.6.0
	 */
	@Nullable
	<T> String getSubtypeInfo(IIngredientType<T> ingredientType, T ingredient, UidContext context);

	/**
	 * Get the data from an itemStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the itemStack has no information used for subtypes.
	 *
	 * @since 7.3.0
	 * @deprecated use {@link #getSubtypeInfo(IIngredientType, Object, UidContext)}
	 */
	@Deprecated(forRemoval = true, since = "9.6.0")
	@Nullable
	String getSubtypeInfo(ItemStack itemStack, UidContext context);

	/**
	 * Get the data from a fluidStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the fluidStack has no information used for subtypes.
	 *
	 * @since 7.6.2
	 * @deprecated use {@link #getSubtypeInfo(IIngredientType, Object, UidContext)}
	 */
	@Deprecated(forRemoval = true, since = "9.6.0")
	@Nullable
	String getSubtypeInfo(FluidStack fluidStack, UidContext context);
}

package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

/**
 * Allows adding extra ingredients (including ItemStack and FluidStack) for any registered ingredient type.
 *
 * This is intended to be used to add ingredients to another mod's type.
 * If you want to add ingredients to your own custom type,
 * pass them to {@link IModIngredientRegistration#register} instead.
 *
 * This is given to your {@link IModPlugin#registerExtraIngredients(IExtraIngredientRegistration)}.
 *
 * @since 19.18.0
 */
public interface IExtraIngredientRegistration {
	/**
	 * Add extra ItemStacks that are not already in the creative menu.
	 *
	 * @param extraItemStacks A collection of extra ItemStacks to be displayed in the ingredient list.
	 *
	 * @since 19.18.0
	 */
	default void addExtraItemStacks(Collection<ItemStack> extraItemStacks) {
		addExtraIngredients(VanillaTypes.ITEM_STACK, extraItemStacks);
	}

	/**
	 * Add extra ingredients to an existing ingredient type.
	 *
	 * @param ingredientType     The type of the ingredient.
	 *                           This must already be registered with {@link IModIngredientRegistration#register} by another mod.
	 * @param extraIngredients   A collection of extra ingredients to be displayed in the ingredient list.
	 *
	 * @since 19.18.0
	 */
	<V> void addExtraIngredients(
		IIngredientType<V> ingredientType,
		Collection<V> extraIngredients
	);
}

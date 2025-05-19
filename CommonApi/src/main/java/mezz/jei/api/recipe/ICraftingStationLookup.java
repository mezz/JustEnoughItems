package mezz.jei.api.recipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

/**
 * This is a helper class for looking up crafting stations.
 * Create one with {@link IRecipeManager#createCraftingStationLookup(IRecipeType)},
 * then set its properties and call {@link #get()} to get the results.
 *
 * @since 20.0.0
 */
public interface ICraftingStationLookup {
	/**
	 * By default, hidden results are not returned.
	 * Calling this will make this lookup include hidden crafting stations.
	 *
	 * @since 20.0.0
	 */
	ICraftingStationLookup includeHidden();

	/**
	 * Get the crafting station results for this lookup.
	 *
	 * @since 20.0.0
	 */
	Stream<ITypedIngredient<?>> get();

	/**
	 * Get the crafting station results of the given type for this lookup.
	 *
	 * @since 20.0.0
	 */
	<S> Stream<S> get(IIngredientType<S> ingredientType);

	/**
	 * Get the ItemStack crafting station results for this lookup.
	 *
	 * @since 20.0.0
	 */
	default Stream<ItemStack> getItemStack() {
		return get(VanillaTypes.ITEM_STACK);
	}
}

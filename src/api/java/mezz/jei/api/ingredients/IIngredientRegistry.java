package mezz.jei.api.ingredients;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;

/**
 * The IIngredientRegistry is provided by JEI and has some useful functions related to recipe ingredients.
 * Get the instance from {@link IModRegistry#getIngredientRegistry()}.
 *
 * @since JEI 3.11.0
 */
public interface IIngredientRegistry {
	/**
	 * Returns an unmodifiable list of all the ingredients known to JEI, of the specified class.
	 */
	<V> List<V> getIngredients(Class<V> ingredientClass);

	/**
	 * Returns the appropriate ingredient helper for this ingredient.
	 */
	<V> IIngredientHelper<V> getIngredientHelper(V ingredient);

	/**
	 * Returns the appropriate ingredient helper for this ingredient class
	 */
	<V> IIngredientHelper<V> getIngredientHelper(Class<V> ingredientClass);

	/**
	 * Returns the ingredient renderer for this ingredient.
	 */
	<V> IIngredientRenderer<V> getIngredientRenderer(V ingredient);

	/**
	 * Returns the ingredient renderer for this ingredient class.
	 */
	<V> IIngredientRenderer<V> getIngredientRenderer(Class<V> ingredientClass);

	/**
	 * Returns an unmodifiable collection of all registered ingredient classes.
	 * Without addons, there is ItemStack.class and FluidStack.class.
	 */
	Collection<Class> getRegisteredIngredientClasses();

	/**
	 * Returns an unmodifiable list of all the ItemStacks that can be used as fuel in a vanilla furnace.
	 */
	List<ItemStack> getFuels();

	/**
	 * Returns an unmodifiable list of all the ItemStacks that return true to isPotionIngredient.
	 */
	List<ItemStack> getPotionIngredients();

	/**
	 * Add new ingredients to JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 * Using this method will reload the ingredient list, do not call it unless necessary.
	 *
	 * @since JEI 4.0.2
	 */
	<V> void addIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients);
}

package mezz.jei.api.ingredients;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * The IIngredientRegistry is provided by JEI and has some useful functions related to recipe ingredients.
 * Get the instance from {@link IModRegistry#getIngredientRegistry()}.
 *
 * @since JEI 3.11.0
 */
public interface IIngredientRegistry {
	/**
	 * Returns a list of all the ingredients known to JEI, of the specified class.
	 * Calling this with ItemStack.class is equivalent to {@link IItemRegistry#getItemList()}.
	 */
	<V> ImmutableList<V> getIngredients(Class<V> ingredientClass);

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
	 * Returns a list of all registered ingredient classes.
	 * Without addons, there is ItemStack.class and FluidStack.class.
	 */
	ImmutableCollection<Class> getRegisteredIngredientClasses();

	/**
	 * Returns a list of all the ItemStacks that can be used as fuel in a vanilla furnace.
	 */
	ImmutableList<ItemStack> getFuels();

	/**
	 * Returns a list of all the ItemStacks that return true to isPotionIngredient.
	 */
	ImmutableList<ItemStack> getPotionIngredients();

	/**
	 * Add new ingredients to JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 * Using this method will reload the ingredient list, do not call it unless necessary.
	 *
	 * @since JEI 3.14.4
	 */
	<V> void addIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients);
}

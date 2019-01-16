package mezz.jei.api.ingredients;

import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IIngredientType;

/**
 * The IIngredientRegistry is provided by JEI and has some useful functions related to recipe ingredients.
 * Get the instance from {@link IModRegistry#getIngredientRegistry()}.
 *
 * @since JEI 3.11.0
 */
public interface IIngredientRegistry {
	/**
	 * Returns an unmodifiable collection of all the ingredients known to JEI, of the specified type.
	 *
	 * @since JEI 4.12.0
	 */
	<V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType);

	/**
	 * Returns the appropriate ingredient helper for this ingredient.
	 */
	<V> IIngredientHelper<V> getIngredientHelper(V ingredient);

	/**
	 * Returns the appropriate ingredient helper for this ingredient type.
	 *
	 * @since JEI 4.12.0
	 */
	<V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType);

	/**
	 * Returns the ingredient renderer for this ingredient.
	 */
	<V> IIngredientRenderer<V> getIngredientRenderer(V ingredient);

	/**
	 * Returns the ingredient renderer for this ingredient class.
	 *
	 * @since JEI 4.12.0
	 */
	<V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType);

	/**
	 * Returns an unmodifiable collection of all registered ingredient types.
	 * Without addons, there are {@link VanillaTypes#ITEM} and {@link VanillaTypes#FLUID}.
	 *
	 * @since JEI 4.12.0
	 */
	Collection<IIngredientType> getRegisteredIngredientTypes();

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
	 *
	 * @since JEI 4.12.0
	 */
	<V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

	/**
	 * Remove ingredients from JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 *
	 * @since JEI 4.12.0
	 */
	<V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

	/**
	 * Helper method to get ingredient type for an ingredient.
	 *
	 * @since JEI 4.12.0
	 */
	<V> IIngredientType<V> getIngredientType(V ingredient);

	/**
	 * Helper method to get ingredient type from a legacy ingredient class.
	 *
	 * @since JEI 4.12.0
	 */
	<V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass);

	/**
	 * Returns an unmodifiable collection of all the ingredients known to JEI, of the specified class.
	 *
	 * @since JEI 4.7.3
	 * @deprecated since JEI 4.12.0. Use {@link #getAllIngredients(IIngredientType)}
	 */
	@Deprecated
	<V> Collection<V> getAllIngredients(Class<V> ingredientClass);

	/**
	 * Returns the appropriate ingredient helper for this ingredient class.
	 *
	 * @deprecated since JEI 4.12.0. Use {@link #getIngredientHelper(IIngredientType)}
	 */
	@Deprecated
	<V> IIngredientHelper<V> getIngredientHelper(Class<? extends V> ingredientClass);

	/**
	 * Returns the ingredient renderer for this ingredient class.
	 *
	 * @deprecated since JEI 4.12.0. Use {@link #getIngredientRenderer(IIngredientType)}
	 */
	@Deprecated
	<V> IIngredientRenderer<V> getIngredientRenderer(Class<? extends V> ingredientClass);

	/**
	 * Returns an unmodifiable collection of all registered ingredient classes.
	 * Without addons, there is ItemStack.class and FluidStack.class.
	 *
	 * @deprecated since JEI 4.12.0. Use {@link #getRegisteredIngredientTypes()}
	 */
	@Deprecated
	Collection<Class> getRegisteredIngredientClasses();

	/**
	 * Add new ingredients to JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 *
	 * @since JEI 4.8.2
	 * @deprecated since JEi 4.12.0. Use {@link #addIngredientsAtRuntime(IIngredientType, Collection)}
	 */
	@Deprecated
	<V> void addIngredientsAtRuntime(Class<V> ingredientClass, Collection<V> ingredients);

	/**
	 * Remove ingredients from JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 *
	 * @since JEI 4.8.2
	 * @deprecated since JEI 4.12.0. Use {@link #removeIngredientsAtRuntime(IIngredientType, Collection)}
	 */
	@Deprecated
	<V> void removeIngredientsAtRuntime(Class<V> ingredientClass, Collection<V> ingredients);

	/**
	 * Add new ingredients to JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 *
	 * @since JEI 4.0.2
	 * @deprecated since JEI 4.7.3. Use {@link #addIngredientsAtRuntime(IIngredientType, Collection)}
	 */
	@Deprecated
	<V> void addIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients);

	/**
	 * Remove ingredients from JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 *
	 * @since JEI 4.3.5
	 * @deprecated since JEI 4.7.3. Use {@link #removeIngredientsAtRuntime(IIngredientType, Collection)}
	 */
	@Deprecated
	<V> void removeIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients);

	/**
	 * Returns an unmodifiable list of all the ingredients known to JEI, of the specified class.
	 *
	 * @deprecated since JEI 4.7.3. Use {@link #getAllIngredients(IIngredientType)}
	 */
	@Deprecated
	<V> List<V> getIngredients(Class<V> ingredientClass);
}

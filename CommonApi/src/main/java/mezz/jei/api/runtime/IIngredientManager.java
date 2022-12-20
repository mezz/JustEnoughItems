package mezz.jei.api.runtime;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

/**
 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
 * An instance is passed to your plugin in {@link IModPlugin#registerRecipes} and it is accessible from
 * {@link IJeiHelpers#getIngredientManager()} and {@link IJeiRuntime#getIngredientManager()}.
 */
public interface IIngredientManager {
	/**
	 * Returns an unmodifiable collection of all the ItemStacks known to JEI.
	 *
	 * @see #getAllIngredients(IIngredientType) to get other ingredient types besides ItemStack.
	 *
	 * @since 11.1.1
	 */
	@Unmodifiable
	default Collection<ItemStack> getAllItemStacks() {
		return getAllIngredients(VanillaTypes.ITEM_STACK);
	}

	/**
	 * Returns an unmodifiable collection of all the ingredients known to JEI, of the specified type.
	 */
	@Unmodifiable
	<V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType);

	/**
	 * Returns the appropriate ingredient helper for this ingredient.
	 */
	<V> IIngredientHelper<V> getIngredientHelper(V ingredient);

	/**
	 * Returns the appropriate ingredient helper for this ingredient type.
	 */
	<V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType);

	/**
	 * Returns the ingredient renderer for this ingredient.
	 */
	<V> IIngredientRenderer<V> getIngredientRenderer(V ingredient);

	/**
	 * Returns the ingredient renderer for this ingredient class.
	 */
	<V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType);

	/**
	 * Returns an unmodifiable collection of all registered ingredient types.
	 * Without addons, there is {@link VanillaTypes#ITEM_STACK}.
	 */
	@Unmodifiable
	Collection<IIngredientType<?>> getRegisteredIngredientTypes();

	/**
	 * Add new ingredients to JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 */
	<V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

	/**
	 * Remove ingredients from JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 */
	<V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

	/**
	 * Helper method to get ingredient type for an ingredient.
	 * Returns {@link Optional#empty()} if there is no known type for the given ingredient.
	 *
	 * @since 11.5.0
	 */
	<V> Optional<IIngredientType<V>> getIngredientTypeChecked(V ingredient);

	/**
	 * Helper method to get ingredient type for an ingredient.
	 * Returns {@link Optional#empty()} if there is no known type for the given ingredient.
	 *
	 * @since 11.5.0
	 */
	<V> Optional<IIngredientType<V>> getIngredientTypeChecked(Class<? extends V> ingredientClass);

	/**
	 * Helper method to get ingredient type for an ingredient.
	 * @deprecated use {@link #getIngredientTypeChecked(Object)}
	 */
	@Deprecated(since = "11.5.0", forRemoval = true)
	<V> IIngredientType<V> getIngredientType(V ingredient);

	/**
	 * Helper method to get ingredient type from a legacy ingredient class.
	 *
	 * @deprecated use {@link #getIngredientTypeChecked(Class)}
	 */
	@Deprecated(since = "11.5.0", forRemoval = true)
	<V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass);

	/**
	 * Create a typed ingredient, if the given ingredient is valid.
	 *
	 * Invalid ingredients (according to {@link IIngredientHelper#isValidIngredient}
	 * cannot be created into {@link ITypedIngredient} and will instead be {@link Optional#empty()}.
	 * This helps turn all special cases like {@link ItemStack#EMPTY} into {@link Optional#empty()} instead.
	 *
	 * @since 11.5.0
	 */
	<V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient);

	/**
	 * Get an ingredient by the given unique id.
	 * This uses the uids from {@link IIngredientHelper#getUniqueId(Object, UidContext)}
	 *
	 * @since 11.5.0
	 */
	<V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String ingredientUuid);

	/**
	 * Add a listener to receive updates when ingredients are added or removed from the ingredient manager.
	 *
	 * @since 11.5.0
	 */
	void registerIngredientListener(IIngredientListener listener);

	/**
	 * A listener that receives updates when ingredients are added or removed from the ingredient manager.
	 *
	 * @since 11.5.0
	 */
	interface IIngredientListener {
		/**
		 * Called when ingredients are added to the ingredient manager.
		 * @since 11.5.0
		 */
		<V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients);

		/**
		 * Called when ingredients are removed from the ingredient manager.
		 * @since 11.5.0
		 */
		<V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients);
	}
}

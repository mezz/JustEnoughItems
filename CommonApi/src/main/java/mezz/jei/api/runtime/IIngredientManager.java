package mezz.jei.api.runtime;

import com.mojang.serialization.Codec;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
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
	 * Returns an unmodifiable collection of all the ingredients known to JEI, of the specified type.
	 *
	 * @since 24.1.0
	 */
	@Unmodifiable
	<V> Collection<ITypedIngredient<V>> getAllTypedIngredients(IIngredientType<V> ingredientType);

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
	 * Returns an appropriate ingredient serializer codec for this ingredient type.
	 *
	 * @since 19.9.0
	 */
	<V> Codec<V> getIngredientCodec(IIngredientType<V> ingredientType);

	/**
	 * Returns an unmodifiable collection of all registered ingredient types.
	 * Without addons, there is {@link VanillaTypes#ITEM_STACK}.
	 */
	@Unmodifiable
	Collection<IIngredientType<?>> getRegisteredIngredientTypes();

	/**
	 * @return the ingredient type that has the given uid.
	 * @see IIngredientType#getUid()
	 * @since 19.1.0
	 */
	Optional<IIngredientType<?>> getIngredientTypeForUid(String ingredientTypeUid);

	/**
	 * Add new ingredients to JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 *
	 * If you just want to add ingredients to an existing type
	 * (like adding more ItemStacks or FluidStacks, not at runtime),
	 * use {@link IExtraIngredientRegistration#addExtraIngredients} instead.
	 */
	<V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

	/**
	 * Remove ingredients from JEI at runtime.
	 * Used by mods that have items created while the game is running, or use the server to define items.
	 */
	<V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

	/**
	 * Helper method to get ingredient type for an ingredient.
	 * Returns null if there is no known type for the given ingredient.
	 *
	 * @since 19.19.5
	 */
	@Nullable
	<V> IIngredientType<V> getIngredientType(V ingredient);

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
	 * @since 19.5.6
	 */
	<B, I> Optional<IIngredientTypeWithSubtypes<B, I>> getIngredientTypeWithSubtypesFromBase(B baseIngredient);

	/**
	 * Helper method to get ingredient type for an ingredient.
	 * Returns {@link Optional#empty()} if there is no known type for the given ingredient.
	 *
	 * @since 11.5.0
	 */
	<V> Optional<IIngredientType<V>> getIngredientTypeChecked(Class<? extends V> ingredientClass);

	/**
	 * Create a typed ingredient, if the given ingredient is valid.
	 *
	 * Invalid ingredients (according to {@link IIngredientHelper#isValidIngredient})
	 * cannot be used in {@link ITypedIngredient} and will instead be {@link Optional#empty()}.
	 * This helps turn all special cases like {@link ItemStack#EMPTY} into {@link Optional#empty()} instead.
	 *
	 * @param ingredientType the type of the ingredient
	 * @param ingredient the ingredient
	 * @param normalize set true to normalize the ingredient (see {@link IIngredientHelper#normalizeIngredient}
	 *
	 * @since 21.2.0
	 */
	<V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient, boolean normalize);

	/**
	 * Create a typed ingredient, if the given ingredient is valid and has a known type.
	 *
	 * Invalid ingredients (according to {@link IIngredientHelper#isValidIngredient}
	 * cannot be created into {@link ITypedIngredient} and will instead be {@link Optional#empty()}.
	 * This helps turn all special cases like {@link ItemStack#EMPTY} into {@link Optional#empty()} instead.
	 *
	 * @param ingredient the ingredient
	 * @param normalize set true to normalize the ingredient (see {@link IIngredientHelper#normalizeIngredient}
	 *
	 * @return {@link Optional#empty()} if there is no known type for the given ingredient or the ingredient is invalid.
	 *
	 * @since 21.2.0
	 */
	default <T> Optional<ITypedIngredient<T>> createTypedIngredient(T ingredient, boolean normalize) {
		return getIngredientTypeChecked(ingredient)
			.flatMap(ingredientType -> createTypedIngredient(ingredientType, ingredient, normalize));
	}

	/**
	 * Create a typed ingredient, if the given ingredient is valid.
	 *
	 * Invalid ingredients (according to {@link IIngredientHelper#isValidIngredient})
	 * cannot be used in {@link ITypedIngredient} and will instead be {@link Optional#empty()}.
	 * This helps turn all special cases like {@link ItemStack#EMPTY} into {@link Optional#empty()} instead.
	 *
	 * @since 11.5.0
	 * @deprecated use {@link #createTypedIngredient(IIngredientType, Object, boolean)}
	 */
	@Deprecated(forRemoval = true, since = "21.2.0")
	default <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient) {
		return createTypedIngredient(ingredientType, ingredient, false);
	}

	/**
	 * Create a typed ingredient, if the given ingredient is valid and has a known type.
	 *
	 * Invalid ingredients (according to {@link IIngredientHelper#isValidIngredient}
	 * cannot be created into {@link ITypedIngredient} and will instead be {@link Optional#empty()}.
	 * This helps turn all special cases like {@link ItemStack#EMPTY} into {@link Optional#empty()} instead.
	 *
	 * @return {@link Optional#empty()} if there is no known type for the given ingredient or the ingredient is invalid.
	 *
	 * @since 15.2.0
	 * @deprecated use {@link #createTypedIngredient(Object, boolean)}
	 */
	@Deprecated(forRemoval = true, since = "21.2.0")
	default <V> Optional<ITypedIngredient<V>> createTypedIngredient(V ingredient) {
		return createTypedIngredient(ingredient, false);
	}

	/**
	 * Normalize a typed ingredient.
	 *
	 * @see IIngredientHelper#normalizeIngredient
	 *
	 * @since 19.1.0
	 */
	<V> ITypedIngredient<V> normalizeTypedIngredient(ITypedIngredient<V> typedIngredient);

	/**
	 * Get the factory for creating clickable ingredients.
	 *
	 * @see IClickableIngredient
	 *
	 * @since 21.2.0
	 */
	IClickableIngredientFactory getClickableIngredientFactory();

	/**
	 * Create a clickable ingredient.
	 *
	 * @see IClickableIngredient
	 *
	 * @param ingredientType the type of the ingredient being clicked
	 * @param ingredient the ingredient being clicked
	 * @param area the area that this clickable ingredient is drawn in, in absolute screen coordinates.
	 * @param normalize set true to normalize the ingredient (see {@link IIngredientHelper#normalizeIngredient}
	 *
	 * @return a clickable ingredient, or {@link Optional#empty()} if the ingredient is invalid (see {@link IIngredientHelper#isValidIngredient}
	 *
	 * @since 19.18.5
	 *
	 * @deprecated use {@link #getClickableIngredientFactory()}
	 */
	@Deprecated(forRemoval = true, since = "21.2.0")
	<V> Optional<IClickableIngredient<V>> createClickableIngredient(IIngredientType<V> ingredientType, V ingredient, Rect2i area, boolean normalize);

	/**
	 * Create a clickable ingredient.
	 *
	 * @see IClickableIngredient
	 *
	 * @param ingredient the ingredient being clicked
	 * @param area the area that this clickable ingredient is drawn in, in absolute screen coordinates.
	 * @param normalize set true to normalize the ingredient (see {@link IIngredientHelper#normalizeIngredient}
	 *
	 * @return a clickable ingredient, or {@link Optional#empty()} if the ingredient is invalid (see {@link IIngredientHelper#isValidIngredient}
	 *
	 * @since 19.18.6
	 *
	 * @deprecated use {@link #getClickableIngredientFactory()}
	 */
	@Deprecated(forRemoval = true, since = "21.2.0")
	default <V> Optional<IClickableIngredient<V>> createClickableIngredient(V ingredient, Rect2i area, boolean normalize) {
		return getIngredientTypeChecked(ingredient)
			.flatMap(type -> createClickableIngredient(type, ingredient, area, normalize));
	}

	/**
	 * Get localized search aliases for ingredients.
	 * Registered by mods with {@link IIngredientAliasRegistration#addAlias}.
	 *
	 * If search aliases are disabled by the player in the configs, this will return an empty collection.
	 *
	 * @since 19.10.0
	 */
	Collection<String> getIngredientAliases(ITypedIngredient<?> ingredient);

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

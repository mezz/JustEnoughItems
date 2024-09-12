package mezz.jei.api.runtime;

import com.mojang.serialization.Codec;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
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
	 * @since 11.5.0
	 */
	<V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient);

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
	 */
	default <V> Optional<ITypedIngredient<V>> createTypedIngredient(V ingredient) {
		return getIngredientTypeChecked(ingredient)
			.flatMap(ingredientType -> createTypedIngredient(ingredientType, ingredient));
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
	 * Get an ingredient by the given unique id.
	 * This uses the uids from {@link IIngredientHelper#getUniqueId(Object, UidContext)}
	 *
	 * @since 11.5.0
	 * @deprecated Use ingredient serialization from {@link ICodecHelper#getTypedIngredientCodec()} instead of this method.
	 */
	@SuppressWarnings("removal")
	@Deprecated(since = "19.1.0", forRemoval = true)
	<V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String ingredientUuid);

	/**
	 * Get an ingredient by the given type and unique id.
	 * This uses the uids from {@link IIngredientHelper#getUniqueId(Object, UidContext)}
	 *
	 * @since 19.1.0
	 * @deprecated use ingredient serialization from {@link ICodecHelper#getTypedIngredientCodec()} instead of this method.
	 */
	@SuppressWarnings("removal")
	@Deprecated(since = "19.9.0", forRemoval = true)
	<V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(IIngredientType<V> ingredientType, String ingredientUuid);

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

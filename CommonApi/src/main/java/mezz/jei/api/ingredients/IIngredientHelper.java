package mezz.jei.api.ingredients;

import mezz.jei.api.constants.Tags;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An ingredient helper allows JEI to get information about ingredients for searching and other purposes.
 * An ingredient is anything used in a recipe, like ItemStacks and FluidStacks.
 *
 * If you have a new type of ingredient to add to JEI, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 */
public interface IIngredientHelper<V> {
	/**
	 * @return The ingredient type for this {@link IIngredientHelper}.
	 */
	IIngredientType<V> getIngredientType();

	/**
	 * Change one focus into a different focus.
	 * This can be used to treat lookups of one focus as if it were something else.
	 *
	 * @deprecated There isn't a good use for this anymore.
	 */
	@Deprecated(forRemoval = true, since = "9.2.0")
	default IFocus<?> translateFocus(IFocus<V> focus, IFocusFactory focusFactory) {
		return focus;
	}

	/**
	 * Find a matching ingredient from a group of them.
	 * Used for finding a specific focused ingredient in a recipe.
	 * Return null if there is no match.
	 * @since 7.3.0
	 *
	 * @deprecated use {@link #getUniqueId(Object, UidContext)} and compare those instead.
	 */
	@Nullable
	@Deprecated(forRemoval = true, since = "9.4.1")
	default V getMatch(Iterable<V> ingredients, V ingredientToMatch, UidContext context) {
		String uid = getUniqueId(ingredientToMatch, context);
		return StreamSupport.stream(ingredients.spliterator(), false)
			.filter(i -> getUniqueId(i, context).equals(uid))
			.findFirst()
			.orElse(null);
	}

	/**
	 * Display name used for searching. Normally this is the first line of the tooltip.
	 */
	String getDisplayName(V ingredient);

	/**
	 * Unique ID for use in comparing, blacklisting, and looking up ingredients.
	 * @since 7.3.0
	 */
	String getUniqueId(V ingredient, UidContext context);

	/**
	 * Unique ID for use in comparing and looking up ingredients.
	 *
	 * Returns an {@link Object} so that UID creation can be optimized.
	 * Make sure the returned value implements {@link Object#equals(Object)} and {@link Object#hashCode()}.
	 *
	 * @since 10.71.0
	 */
	default Object getUid(V ingredient, UidContext context) {
		return getUniqueId(ingredient, context);
	}

	/**
	 * Unique ID for use in comparing, blacklisting, and looking up ingredients.
	 *
	 * @since 10.57.0
	 */
	default String getUniqueId(ITypedIngredient<V> typedIngredient, UidContext context) {
		return getUniqueId(typedIngredient.getIngredient(), context);
	}

	/**
	 * Unique ID for use in comparing and looking up ingredients.
	 *
	 * Returns an {@link Object} so that UID creation can be optimized.
	 * Make sure the returned value implements {@link Object#equals(Object)} and {@link Object#hashCode()}.
	 *
	 * @since 10.71.0
	 */
	default Object getUid(ITypedIngredient<V> typedIngredient, UidContext context) {
		return getUid(typedIngredient.getIngredient(), context);
	}

	/**
	 * Return true if the given ingredient can have subtypes.
	 * For example in the vanilla game an enchanted book may have subtypes, but an apple does not.
	 * <p>
	 * This is used as an optimization to skip some processing for ingredients that never have subtypes.
	 *
	 * @since 10.5.0
	 */
	default boolean hasSubtypes(V ingredient) {
		return getIngredientType() instanceof IIngredientTypeWithSubtypes<?, ?>;
	}

	/**
	 * Wildcard ID for use in comparing, blacklisting, and looking up ingredients.
	 * For an example, ItemStack's wildcardId does not include NBT.
	 * For ingredients which do not have a wildcardId, just return the uniqueId here.
	 */
	default String getWildcardId(V ingredient) {
		return getUniqueId(ingredient, UidContext.Ingredient);
	}

	/**
	 * Return the modId of the mod that created this ingredient.
	 * @deprecated Use {@link #getResourceLocation(Object)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.2.2")
	String getModId(V ingredient);

	/**
	 * Unique ID for use in grouping ingredients together.
	 * <p>
	 * Ingredients with the same grouping UID are variants of the same base ingredient. JEI uses this for operations
	 * that apply to a whole group, including hiding ingredients together and matching recipe displays that accept
	 * all subtypes.
	 *
	 * @since 10.57.0
	 */
	default String getGroupingUid(V ingredient) {
		return getWildcardId(ingredient);
	}

	/**
	 * Unique ID for use in grouping ingredients together.
	 * <p>
	 * Ingredients with the same grouping UID are variants of the same base ingredient. JEI uses this for operations
	 * that apply to a whole group, including hiding ingredients together and matching recipe displays that accept
	 * all subtypes.
	 *
	 * @since 10.57.0
	 */
	default String getGroupingUid(ITypedIngredient<V> typedIngredient) {
		return getGroupingUid(typedIngredient.getIngredient());
	}

	/**
	 * Return the modId of the mod that should be displayed.
	 * This mod id can be different from the one in the resource location.
	 */
	default String getDisplayModId(V ingredient) {
		return getResourceLocation(ingredient).getNamespace();
	}

	/**
	 * Get the amount of an ingredient.
	 * For example, an ItemStack's amount is its count.
	 *
	 * Returns -1 if this type of ingredient can't be counted.
	 *
	 * @since 10.28.0
	 */
	default long getAmount(V ingredient) {
		return -1;
	}

	/**
	 * Creates an ingredient with the given amount.
	 * For example, an ItemStack's amount is its count.
	 *
	 * Does not mutate the given ingredient.
	 * If this ingredient can't store an amount, this just returns a copy.
	 *
	 * @since 10.28.0
	 */
	default V copyWithAmount(V ingredient, long amount) {
		return copyIngredient(ingredient);
	}

	/**
	 * Get the main colors of this ingredient. Used for the color search.
	 * If this is too difficult to implement for your ingredient, just return an empty collection.
	 * @see mezz.jei.api.helpers.IColorHelper
	 */
	default Iterable<Integer> getColors(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Return the resource id of the given ingredient.
	 * @deprecated Use {@link #getResourceLocation(Object)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.2.2")
	String getResourceId(V ingredient);

	/**
	 * Return the registry name of the given ingredient.
	 * @since 9.2.2
	 */
	default ResourceLocation getResourceLocation(V ingredient) {
		return new ResourceLocation(getModId(ingredient), getResourceId(ingredient));
	}

	/**
	 * Called when a player is in cheat mode and clicks an ingredient in the list.
	 *
	 * @param ingredient The ingredient to cheat in. Do not edit this ingredient.
	 * @return an ItemStack for JEI to give the player, or an empty stack if there is nothing that can be given.
	 */
	default ItemStack getCheatItemStack(V ingredient) {
		return ItemStack.EMPTY;
	}

	/**
	 * Makes a copy of the given ingredient.
	 * Used by JEI to protect against mutation of ingredients.
	 *
	 * @param ingredient the ingredient to copy
	 * @return a copy of the ingredient
	 */
	V copyIngredient(V ingredient);

	/**
	 * Makes a normalized version of the given ingredient.
	 * Used by JEI for bookmarks.
	 *
	 * @param ingredient the ingredient to normalize
	 * @return a normalized version of the ingredient, or the same ingredient if it is already normalized.
	 */
	default V normalizeIngredient(V ingredient) {
		return ingredient;
	}

	/**
	 * Checks if the given ingredient is valid for lookups and recipes.
	 *
	 * @param ingredient the ingredient to check
	 * @return whether the ingredient is valid for lookups and recipes.
	 */
	default boolean isValidIngredient(V ingredient) {
		return true;
	}

	/**
	 * This is called when connecting to a server, to hide ingredients that are missing on the server.
	 * This call must be fast, the client should already know the answer without making any network calls.
	 * If in doubt, just leave this with the default implementation and return true.
	 *
	 * @param ingredient the ingredient to check
	 * @return true if the ingredient is on the server as well as the client
	 */
	default boolean isIngredientOnServer(V ingredient) {
		return true;
	}

	/**
	 * Get a list of tags that include this ingredient.
	 * Used for searching by tags.
	 *
	 * @deprecated use {@link #getTagStream} instead
	 */
	@Deprecated(since = "10.3.1")
	default Collection<ResourceLocation> getTags(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Get a stream of tags that include this ingredient.
	 * Used for searching by tags.
	 *
	 * @since 10.3.1
	 */
	default Stream<ResourceLocation> getTagStream(V ingredient) {
		return getTags(ingredient).stream();
	}

	/**
	 * Get a list of creative tab names that include this ingredient.
	 * Used for searching by creative tab name.
	 */
	default Collection<String> getCreativeTabNames(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Return true if the given ingredient is hidden from recipe viewers by its tags.
	 *
	 * @see Tags#HIDDEN_FROM_RECIPE_VIEWERS
	 *
	 * @since 10.27.0
	 */
	default boolean isHiddenFromRecipeViewersByTags(V ingredient) {
		return getTagStream(ingredient)
			.anyMatch(Tags.HIDDEN_FROM_RECIPE_VIEWERS::equals);
	}

	/**
	 * Return true if the given ingredient is hidden from recipe viewers by its tags.
	 *
	 * @see Tags#HIDDEN_FROM_RECIPE_VIEWERS
	 *
	 * @since 10.57.0
	 */
	default boolean isHiddenFromRecipeViewersByTags(ITypedIngredient<V> ingredient) {
		return isHiddenFromRecipeViewersByTags(ingredient.getIngredient());
	}

	/**
	 * Get information for error messages involving this ingredient.
	 * Be extremely careful not to crash here, get as much useful info as possible.
	 */
	String getErrorInfo(@Nullable V ingredient);

	/**
	 * If these ingredients represent everything from a single tag,
	 * returns that tag's resource location.
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #getTagKeyEquivalent}
	 */
	@Deprecated(since = "10.5.0", forRemoval = true)
	default Optional<ResourceLocation> getTagEquivalent(Collection<V> ingredients) {
		return getTagKeyEquivalent(ingredients)
			.map(TagKey::location);
	}

	/**
	 * If these ingredients represent everything from a single tag, returns that tag.
	 *
	 * @since 10.5.0
	 */
	default Optional<TagKey<?>> getTagKeyEquivalent(Collection<V> ingredients) {
		return Optional.empty();
	}
}

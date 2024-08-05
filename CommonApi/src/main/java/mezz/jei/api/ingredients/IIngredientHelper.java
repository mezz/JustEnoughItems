package mezz.jei.api.ingredients;

import mezz.jei.api.constants.Tags;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

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
	 * Display name used for searching. Normally this is the first line of the tooltip.
	 */
	String getDisplayName(V ingredient);

	/**
	 * Unique ID for use in comparing, blacklisting, and looking up ingredients.
	 * @since 7.3.0
	 */
	String getUniqueId(V ingredient, UidContext context);

	/**
	 * Return true if the given ingredient can have subtypes.
	 * For example in the vanilla game an enchanted book may have subtypes, but an apple does not.
	 * <p>
	 * This is used as an optimization to skip some processing for ingredients that never have subtypes.
	 *
	 * @since 19.3.0
	 */
	default boolean hasSubtypes(V ingredient) {
		return getIngredientType() instanceof IIngredientTypeWithSubtypes<?,?>;
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
	 * @since 19.4.0
	 */
	default long getAmount(V ingredient){
		return -1;
	}

	/**
	 * Creates an ingredient with the given amount.
	 * For example, an ItemStack's amount is its count.
	 *
	 * Does not mutate the given ingredient.
	 * If this ingredient can't store an amount, this just returns a copy.
	 *
	 * @since 19.4.0
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
	 * Return the registry name of the given ingredient.
	 * @since 9.2.2
	 */
	ResourceLocation getResourceLocation(V ingredient);

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
	 * Get a stream of tags that include this ingredient.
	 * Used for searching by tags.
	 *
	 * @since 12.0.1
	 */
	default Stream<ResourceLocation> getTagStream(V ingredient) {
		return Stream.empty();
	}

	/**
	 * Return true if the given ingredient is hidden from recipe viewers by its tags.
	 *
	 * @see Tags#HIDDEN_FROM_RECIPE_VIEWERS
	 *
	 * @since 19.3.0
	 */
	default boolean isHiddenFromRecipeViewersByTags(V ingredient) {
		return getTagStream(ingredient)
			.anyMatch(Tags.HIDDEN_FROM_RECIPE_VIEWERS::equals);
	}

	/**
	 * Get information for error messages involving this ingredient.
	 * Be extremely careful not to crash here, get as much useful info as possible.
	 */
	String getErrorInfo(@Nullable V ingredient);

	/**
	 * If these ingredients represent everything from a single tag, returns that tag.
	 *
	 * @since 19.5.4
	 */
	default Optional<TagKey<?>> getTagKeyEquivalent(Collection<V> ingredients) {
		return Optional.empty();
	}

	/**
	 * If these ingredients represent everything from a single tag,
	 * returns that tag's resource location.
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #getTagKeyEquivalent}
	 */
	@Deprecated(since = "19.5.5", forRemoval = true)
	default Optional<ResourceLocation> getTagEquivalent(Collection<V> ingredients) {
		return getTagKeyEquivalent(ingredients)
			.map(TagKey::location);
	}
}

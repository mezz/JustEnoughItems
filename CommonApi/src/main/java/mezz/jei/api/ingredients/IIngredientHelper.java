package mezz.jei.api.ingredients;

import mezz.jei.api.constants.Tags;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An ingredient helper allows JEI to get information about ingredients for searching and other purposes.
 * An ingredient is anything used in a recipe, like ItemStacks and FluidStacks.
 *
 * If you have a new type of ingredient to add to JEI, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register}
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
	 * Unique ID for use in comparing and looking up ingredients.
	 *
	 * Returns an {@link Object} so that UID creation can be optimized.
	 * Make sure the returned value implements {@link Object#equals} and {@link Object#hashCode}.
	 *
	 * @since 19.9.0
	 */
	Object getUid(V ingredient, UidContext context);

	/**
	 * Unique ID for use in comparing and looking up ingredients.
	 *
	 * Returns an {@link Object} so that UID creation can be optimized.
	 * Make sure the returned value implements {@link Object#equals} and {@link Object#hashCode}.
	 *
	 * @since 19.19.4
	 */
	default Object getUid(ITypedIngredient<V> typedIngredient, UidContext context) {
		return getUid(typedIngredient.getIngredient(), context);
	}

	/**
	 * Unique ID for use in grouping ingredients together.
	 * This is used for hiding groups of ingredients together at once.
	 *
	 * @since 19.13.0
	 */
	default Object getGroupingUid(V ingredient) {
		return getUid(ingredient, UidContext.Ingredient);
	}

	/**
	 * Unique ID for use in grouping ingredients together.
	 * This is used for hiding groups of ingredients together at once.
	 *
	 * @since 19.19.5
	 */
	default Object getGroupingUid(ITypedIngredient<V> typedIngredient) {
		return getGroupingUid(typedIngredient.getIngredient());
	}

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
	 * Return the modId of the mod that should be displayed.
	 * This mod id can be different from the one in the resource location.
	 */
	default String getDisplayModId(V ingredient) {
		return getIdentifier(ingredient).getNamespace();
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
	 * Return the registry identifier for the given ingredient.
	 * @since 27.0.0
	 */
	Identifier getIdentifier(V ingredient);

	/**
	 * Return the registry identifier for the given ingredient.
	 * @since 9.2.2
	 * @deprecated use {@link #getIdentifier(Object)}
	 */
	@Deprecated(since = "27.0.0", forRemoval = true)
	default Identifier getResourceLocation(V ingredient) {
		return getIdentifier(ingredient);
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
	 * Get a stream of tags that include this ingredient.
	 * Used for searching by tags.
	 *
	 * @since 12.0.1
	 */
	default Stream<Identifier> getTagStream(V ingredient) {
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
	 * Return true if the given ingredient is hidden from recipe viewers by its tags.
	 *
	 * @see Tags#HIDDEN_FROM_RECIPE_VIEWERS
	 *
	 * @since 19.19.5
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
	 * If these ingredients represent everything from a single tag, returns that tag.
	 *
	 * @since 19.5.4
	 */
	default Optional<TagKey<?>> getTagKeyEquivalent(Collection<V> ingredients) {
		return Optional.empty();
	}

	/**
	 * Optionally provides a {@link DisplayContentsFactory} that JEI can use when resolving Minecraft
	 * {@link SlotDisplay} instances into displayable contents.
	 * <p>
	 * Minecraft resolves a {@code SlotDisplay} by calling
	 * {@link SlotDisplay#resolve(ContextMap, DisplayContentsFactory)}.
	 * If this ingredient type can be represented as stack-like contents, it may provide a
	 * {@link DisplayContentsFactory.ForStacks} implementation here.
	 * <p>
	 * JEI will use this factory when it needs to expand a {@code SlotDisplay} into concrete stacks,
	 * via {@link SlotDisplay#resolve(ContextMap, DisplayContentsFactory)}.
	 *
	 * @return an {@link Optional} containing a {@link DisplayContentsFactory.ForStacks} for this ingredient
	 * type, or {@link Optional#empty()} if not supported
	 *
	 * @since 28.1.0
	 */
	default Optional<DisplayContentsFactory<V>> getDisplayContentsFactory() {
		return Optional.empty();
	}
}

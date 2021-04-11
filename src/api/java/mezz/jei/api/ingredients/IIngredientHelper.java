package mezz.jei.api.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.registration.IModIngredientRegistration;

/**
 * An ingredient helper allows JEI to get information about ingredients for searching and other purposes.
 * An ingredient is anything used in a recipe, like ItemStacks and FluidStacks.
 *
 * If you have a new type of ingredient to add to JEI, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 */
public interface IIngredientHelper<V> {
	/**
	 * Change one focus into a different focus.
	 * This can be used to treat lookups of one focus as if it were something else.
	 *
	 * On example is looking up fluid blocks, which get translated here into looking up the fluid itself.
	 */
	default IFocus<?> translateFocus(IFocus<V> focus, IFocusFactory focusFactory) {
		return focus;
	}

	/**
	 * Find a matching ingredient from a group of them.
	 * Used for finding a specific focused ingredient in a recipe.
	 * Return null if there is no match.
	 *
	 * @deprecated since JEI 7.3.0. Use {@link #getMatch(Iterable, Object, UidContext)}
	 */
	@Deprecated
	@Nullable
	V getMatch(Iterable<V> ingredients, V ingredientToMatch);

	/**
	 * Find a matching ingredient from a group of them.
	 * Used for finding a specific focused ingredient in a recipe.
	 * Return null if there is no match.
	 * @since JEI 7.3.0
	 */
	@Nullable
	default V getMatch(Iterable<V> ingredients, V ingredientToMatch, UidContext context) {
		return getMatch(ingredients, ingredientToMatch);
	}

	/**
	 * Display name used for searching. Normally this is the first line of the tooltip.
	 */
	String getDisplayName(V ingredient);

	/**
	 * Unique ID for use in comparing, blacklisting, and looking up ingredients.
	 *
	 * @deprecated since JEI 7.3.0. Use {@link #getUniqueId(Object, UidContext)}
	 */
	@Deprecated
	String getUniqueId(V ingredient);

	/**
	 * Unique ID for use in comparing, blacklisting, and looking up ingredients.
	 * @since JEI 7.3.0
	 */
	default String getUniqueId(V ingredient, UidContext context) {
		return getUniqueId(ingredient);
	}

	/**
	 * Wildcard ID for use in comparing, blacklisting, and looking up ingredients.
	 * For an example, ItemStack's wildcardId does not include NBT.
	 * For ingredients like FluidStacks which do not have a wildcardId, just return the uniqueId here.
	 */
	default String getWildcardId(V ingredient) {
		return getUniqueId(ingredient);
	}

	/**
	 * Return the modId of the mod that created this ingredient.
	 */
	String getModId(V ingredient);

	/**
	 * Return the modId of the mod that should be displayed.
	 */
	default String getDisplayModId(V ingredient) {
		return getModId(ingredient);
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
	 */
	String getResourceId(V ingredient);

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
	 * Makes a normalized copy of the given ingredient.
	 * Used by JEI for bookmarks.
	 *
	 * @param ingredient the ingredient to copy and normalize
	 * @return a normalized copy of the ingredient
	 */
	default V normalizeIngredient(V ingredient) {
		return copyIngredient(ingredient);
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
	 */
	default Collection<ResourceLocation> getTags(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Get a list of creative tab names that include this ingredient.
	 * Used for searching by creative tab name.
	 */
	default Collection<String> getCreativeTabNames(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Get information for error messages involving this ingredient.
	 * Be extremely careful not to crash here, get as much useful info as possible.
	 */
	String getErrorInfo(@Nullable V ingredient);

}

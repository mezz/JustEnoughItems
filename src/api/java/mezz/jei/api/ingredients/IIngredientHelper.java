package mezz.jei.api.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IIngredientType;

/**
 * An ingredient helper allows JEI to get information about ingredients for searching and other purposes.
 * An ingredient is anything used in a recipe, like ItemStacks and FluidStacks.
 * <p>
 * If you have a new type of ingredient to add to JEI, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @since JEI 3.11.0
 */
public interface IIngredientHelper<V> {
	/**
	 * Expands any wildcard ingredients into all its subtypes.
	 * Ingredients like FluidStack that have no wildcard ingredients should simply return the collection without editing it.
	 *
	 * @since JEI 3.11.0
	 * Has a default implementation since JEI 4.14.2
	 */
	default List<V> expandSubtypes(List<V> ingredients) {
		return ingredients;
	}

	/**
	 * Change one focus into a different focus.
	 * This can be used to treat lookups of one focus as if it were something else.
	 *
	 * On example is looking up fluid blocks, which get translated here into looking up the fluid itself.
	 *
	 * @since JEI 4.14.2
	 */
	default IFocus<?> translateFocus(IFocus<V> focus, IFocusFactory focusFactory) {
		return focus;
	}

	/**
	 * Find a matching ingredient from a group of them.
	 * Used for finding a specific focused ingredient in a recipe.
	 * Return null if there is no match.
	 *
	 * @since JEI 3.11.0
	 */
	@Nullable
	V getMatch(Iterable<V> ingredients, V ingredientToMatch);

	/**
	 * Display name used for searching. Normally this is the first line of the tooltip.
	 *
	 * @since JEI 3.11.0
	 */
	String getDisplayName(V ingredient);

	/**
	 * Unique ID for use in comparing, blacklisting, and looking up ingredients.
	 *
	 * @since JEI 3.11.0
	 */
	String getUniqueId(V ingredient);

	/**
	 * Wildcard ID for use in comparing, blacklisting, and looking up ingredients.
	 * For an example, ItemStack's wildcardId does not include NBT or meta.
	 * For ingredients like FluidStacks which do not have a wildcardId, just return the uniqueId here.
	 *
	 * @since JEI 3.11.0
	 */
	String getWildcardId(V ingredient);

	/**
	 * Return the modId of the mod that created this ingredient.
	 *
	 * @since JEI 3.11.0
	 */
	String getModId(V ingredient);

	/**
	 * Return the modId of the mod that should be displayed.
	 *
	 * @since JEI 4.8.0
	 */
	default String getDisplayModId(V ingredient) {
		return getModId(ingredient);
	}

	/**
	 * Get the main colors of this ingredient. Used for the color search.
	 * If this is too difficult to implement for your ingredient, just return an empty collection.
	 *
	 * @since JEI 3.11.0
	 * Has a default implementation since JEI 4.14.2
	 */
	default Iterable<Color> getColors(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Return the resource id of the given ingredient.
	 *
	 * @since JEI 4.3.2
	 */
	String getResourceId(V ingredient);

	/**
	 * Called when a player is in cheat mode and clicks an ingredient in the list.
	 *
	 * @param ingredient The ingredient to cheat in. Do not edit this ingredient.
	 * @return an ItemStack for JEI to give the player, or an empty stack if there is nothing that can be given.
	 * @since JEI 4.8.3
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
	 * @since JEI 4.2.10
	 */
	V copyIngredient(V ingredient);

	/**
	 * Checks if the given ingredient is valid for lookups and recipes.
	 *
	 * @param ingredient the ingredient to check
	 * @return whether the ingredient is valid for lookups and recipes.
	 * @since JEI 4.7.2
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
	 * @since JEI 4.8.5
	 */
	default boolean isIngredientOnServer(V ingredient) {
		return true;
	}

	/**
	 * Get a list of ore dictionary names that include this ingredient.
	 * Used for searching by ore dictionary name.
	 *
	 * @since JEI 4.14.2
	 */
	default Collection<String> getOreDictNames(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Get a list of creative tab names that include this ingredient.
	 * Used for searching by creative tab name.
	 *
	 * @since JEI 4.14.2
	 */
	default Collection<String> getCreativeTabNames(V ingredient) {
		return Collections.emptyList();
	}

	/**
	 * Get information for error messages involving this ingredient.
	 * Be extremely careful not to crash here, get as much useful info as possible.
	 *
	 * @since JEI 3.11.0
	 */
	String getErrorInfo(@Nullable V ingredient);

	/**
	 * An action for when a player is in cheat mode and clicks an ingredient in the list.
	 * <p>
	 * This method can either:
	 * return an ItemStack for JEI to give the player,
	 * or
	 * return an empty ItemStack and handle the action manually.
	 *
	 * @param ingredient The ingredient to cheat in. Do not edit this ingredient.
	 * @param fullStack  Only used for manual handling, true if a full stack should be cheated in instead of a single ingredient.
	 * @return an ItemStack for JEI to give the player, or an empty stack if this method handles it manually.
	 * @since JEI 4.2.9
	 * @deprecated since JEI 4.8.3, use {@link #getCheatItemStack(Object)}
	 */
	@Deprecated
	default ItemStack cheatIngredient(V ingredient, boolean fullStack) {
		return ItemStack.EMPTY;
	}

	/**
	 * @since JEI 4.14.2
	 */
	interface IFocusFactory {
		/**
		 * Returns a new focus.
		 */
		<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);
	}
}

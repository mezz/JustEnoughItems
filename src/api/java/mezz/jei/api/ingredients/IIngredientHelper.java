package mezz.jei.api.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * An ingredient helper allows JEI to get information about ingredients for searching and other purposes.
 * An ingredient is anything used in a recipe, like ItemStacks and FluidStacks.
 * <p>
 * If you have a new type of ingredient to add to JEI, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register(Class, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @since JEI 3.11.0
 */
public interface IIngredientHelper<V> {
	/**
	 * Expands any wildcard ingredients into all its subtypes.
	 * Ingredients like FluidStack that have no wildcard ingredients should simply return the collection without editing it.
	 */
	List<V> expandSubtypes(List<V> ingredients);

	/**
	 * Find a matching ingredient from a group of them.
	 * Used for finding a specific focused ingredient in a recipe.
	 * Return null if there is no match.
	 */
	@Nullable
	V getMatch(Iterable<V> ingredients, V ingredientToMatch);

	/**
	 * Display name used for searching. Normally this is the first line of the tooltip.
	 */
	String getDisplayName(V ingredient);

	/**
	 * Unique ID for use in comparing, blacklisting, and looking up ingredients.
	 */
	String getUniqueId(V ingredient);

	/**
	 * Wildcard ID for use in comparing, blacklisting, and looking up ingredients.
	 * For an example, ItemStack's wildcardId does not include NBT or meta.
	 * For ingredients like FluidStacks which do not have a wildcardId, just return the uniqueId here.
	 */
	String getWildcardId(V ingredient);

	/**
	 * Return the modId of the mod that created this ingredient.
	 */
	String getModId(V ingredient);

	/**
	 * Get the main colors of this ingredient. Used for the color search.
	 * If this is too difficult to implement for your ingredient, just return an empty collection.
	 */
	Iterable<Color> getColors(V ingredient);

	/**
	 * An action for when a player is in cheat mode and clicks an ingredient in the list.
	 * <p>
	 * This method can either:
	 * return an ItemStack for JEI to give the player,
	 * or
	 * return an empty ItemStack and handle the action manually.
	 *
	 * @param ingredient The ingredient to cheat in. Do not edit this ingredient.
	 * @param fullStack Only used for manual handling, true if a full stack should be cheated in instead of a single ingredient.
	 * @return an ItemStack for JEI to give the player, or an empty stack if this method handles it manually.
	 * @since JEI 4.2.9
	 */
	ItemStack cheatIngredient(V ingredient, boolean fullStack);

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
	 * Get information for error messages involving this ingredient.
	 * Be extremely careful not to crash here, get as much useful info as possible.
	 */
	String getErrorInfo(V ingredient);
}

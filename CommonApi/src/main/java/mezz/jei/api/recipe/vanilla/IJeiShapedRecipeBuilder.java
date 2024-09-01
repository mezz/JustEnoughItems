package mezz.jei.api.recipe.vanilla;

import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

/**
 * Builds a serializable ShapedRecipe that isn't registered with the vanilla game.
 * Useful for generating crafting recipes from {@link IRecipeManagerPlugin}.
 *
 * @since 11.47.0
 */
@ApiStatus.NonExtendable
public interface IJeiShapedRecipeBuilder {

	/**
	 * Indicate which ingredient should be used for the given character in the recipe pattern.
	 *
	 * @see ShapedRecipeBuilder#define
	 * @since 11.47.0
	 */
	IJeiShapedRecipeBuilder define(Character character, Ingredient ingredient);

	/**
	 * Set a row of the pattern for this recipe.
	 *
	 * @see ShapedRecipeBuilder#pattern
	 * @since 11.47.0
	 */
	IJeiShapedRecipeBuilder pattern(String patternRow);

	/**
	 * Optionally set the group name of this recipe.
	 *
	 * @see ShapedRecipeBuilder#group(String)
	 * @since 11.47.0
	 */
	IJeiShapedRecipeBuilder group(String group);

	/**
	 * Create an unregistered shaped recipe based on the ingredients and pattern.
	 *
	 * @since 11.47.0
	 */
	CraftingRecipe build();
}

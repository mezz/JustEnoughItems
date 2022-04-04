package mezz.jei.api.registration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IRecipeCatalystRegistration {
	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 * @since 9.5.5
	 */
	IIngredientManager getIngredientManager();

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param ingredient  the {@link ItemStack} that can craft recipes (like a furnace or crafting table)
	 * @param recipeTypes the types of recipe that the ingredient is a catalyst for
	 * @see #addRecipeCatalyst(IIngredientType, Object, RecipeType...) to add non-{@link ItemStack} catalysts.
	 *
	 * @since 9.5.0
	 */
	default void addRecipeCatalyst(ItemStack ingredient, RecipeType<?>... recipeTypes) {
		addRecipeCatalyst(VanillaTypes.ITEM_STACK, ingredient, recipeTypes);
	}

	/**
	 * Add an association between an ingredient and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param ingredientType the type of the ingredient
	 * @param ingredient     the ingredient that can craft recipes (like a furnace or crafting table)
	 * @param recipeTypes    the types of recipe that the ingredient is a catalyst for
	 * @since 9.5.0
	 */
	<T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T ingredient, RecipeType<?>... recipeTypes);

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param catalystIngredient the {@link ItemStack} that can craft recipes (like a furnace or crafting table)
	 * @param recipeCategoryUids the recipe categories handled by the catalyst
	 * @see #addRecipeCatalyst(IIngredientType, Object, ResourceLocation...) to add non-{@link ItemStack} catalysts.
	 *
	 * @deprecated use {@link #addRecipeCatalyst(ItemStack, RecipeType...)}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	default void addRecipeCatalyst(ItemStack catalystIngredient, ResourceLocation... recipeCategoryUids) {
		addRecipeCatalyst(VanillaTypes.ITEM_STACK, catalystIngredient, recipeCategoryUids);
	}

	/**
	 * Add an association between an ingredient and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param catalystIngredient the ingredient that can craft recipes (like a furnace or crafting table)
	 * @param recipeCategoryUids the recipe categories handled by the ingredient
	 *
	 * @deprecated use {@link #addRecipeCatalyst(IIngredientType, Object, RecipeType...)}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T catalystIngredient, ResourceLocation... recipeCategoryUids);
}

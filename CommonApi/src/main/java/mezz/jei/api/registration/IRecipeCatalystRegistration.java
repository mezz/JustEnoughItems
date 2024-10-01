package mezz.jei.api.registration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public interface IRecipeCatalystRegistration {
	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 * @since 9.5.5
	 */
	IIngredientManager getIngredientManager();

	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 * @since 11.4.0
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Add an association between {@link ItemLike}s and what it can craft.
	 * (i.e. Furnace Item can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param recipeType the types of recipe that the ingredient is a catalyst for
	 * @param ingredients the {@link ItemLike}s that can craft recipes (like a furnace or crafting table)
	 *
	 * @see #addRecipeCatalysts(RecipeType, ItemStack...) to add {@link ItemStack} catalysts.
	 * @see #addRecipeCatalysts(RecipeType, IIngredientType, List) to add non-{@link ItemLike} catalysts.
	 *
	 * @since 15.20.0
	 */
	void addRecipeCatalysts(RecipeType<?> recipeType, ItemLike... ingredients);

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param ingredients the {@link ItemStack}s that can craft recipes (like a furnace or crafting table)
	 * @param recipeType  the type of recipe that the ingredients are a catalyst for
	 *
	 * @see #addRecipeCatalysts(RecipeType, IIngredientType, List) to add non-{@link ItemStack} catalysts.
	 *
	 * @since 15.20.0
	 */
	default void addRecipeCatalysts(RecipeType<?> recipeType, ItemStack... ingredients) {
		addRecipeCatalysts(recipeType, VanillaTypes.ITEM_STACK, List.of(ingredients));
	}

	/**
	 * Add an association between ingredients and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredients they need to craft in order to make recipes from a recipe category.
	 *
	 * @param recipeType     the type of recipe that the ingredients are a catalyst for
	 * @param ingredientType the type of the ingredient
	 * @param ingredients    the ingredients that can craft recipes (like a furnace or crafting table)
	 * @since 15.20.0
	 */
	<T> void addRecipeCatalysts(RecipeType<?> recipeType, IIngredientType<T> ingredientType, List<T> ingredients);

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param itemLike    the {@link ItemLike} that can craft recipes (like a furnace or crafting table)
	 * @param recipeTypes the types of recipe that the ingredient is a catalyst for
	 *
	 * @see #addRecipeCatalyst(ItemStack, RecipeType...) to add {@link ItemStack} catalysts.
	 * @see #addRecipeCatalyst(IIngredientType, Object, RecipeType...) to add non-{@link ItemLike} catalysts.
	 *
	 * @since 15.19.4
	 */
	default void addRecipeCatalyst(ItemLike itemLike, RecipeType<?>... recipeTypes) {
		addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemLike.asItem().getDefaultInstance(), recipeTypes);
	}

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
}

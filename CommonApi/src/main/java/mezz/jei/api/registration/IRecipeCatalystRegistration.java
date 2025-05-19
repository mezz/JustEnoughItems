package mezz.jei.api.registration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.types.IRecipeType;
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
	 * @see #addCraftingStation(IRecipeType, ItemStack...) to add {@link ItemStack} catalysts.
	 * @see #addCraftingStations(IRecipeType, IIngredientType, List) to add non-{@link ItemLike} catalysts.
	 *
	 * @since 20.0.0
	 */
	void addCraftingStation(IRecipeType<?> recipeType, ItemLike... ingredients);

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param ingredients the {@link ItemStack}s that can craft recipes (like a furnace or crafting table)
	 * @param recipeType  the type of recipe that the ingredients are a catalyst for
	 *
	 * @see #addRecipeCatalysts(IRecipeType, IIngredientType, List) to add non-{@link ItemStack} catalysts.
	 *
	 * @since 20.0.0
	 */
	default void addCraftingStation(IRecipeType<?> recipeType, ItemStack... ingredients) {
		addCraftingStations(recipeType, VanillaTypes.ITEM_STACK, List.of(ingredients));
	}

	/**
	 * Add an association between ingredients and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredients they need to craft in order to make recipes from a recipe category.
	 *
	 * @param recipeType     the type of recipe that the ingredients are a catalyst for
	 * @param ingredientType the type of the ingredient
	 * @param ingredient     the ingredient that can craft recipes (like a furnace or crafting table)
	 * @since 20.0.0
	 */
	<T> void addCraftingStation(IRecipeType<?> recipeType, IIngredientType<T> ingredientType, T ingredient);

	/**
	 * Add an association between ingredients and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredients they need to craft in order to make recipes from a recipe category.
	 *
	 * @param recipeType     the type of recipe that the ingredients are a catalyst for
	 * @param ingredientType the type of the ingredient
	 * @param ingredients    the ingredients that can craft recipes (like a furnace or crafting table)
	 * @since 20.0.0
	 */
	<T> void addCraftingStations(IRecipeType<?> recipeType, IIngredientType<T> ingredientType, List<T> ingredients);

	/**
	 * Add an association between {@link ItemLike}s and what it can craft.
	 * (i.e. Furnace Item can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param recipeType the types of recipe that the ingredient is a catalyst for
	 * @param ingredients the {@link ItemLike}s that can craft recipes (like a furnace or crafting table)
	 *
	 * @see #addRecipeCatalysts(IRecipeType, ItemStack...) to add {@link ItemStack} catalysts.
	 * @see #addRecipeCatalysts(IRecipeType, IIngredientType, List) to add non-{@link ItemLike} catalysts.
	 *
	 * @since 19.19.2
	 * @deprecated use {@link #addCraftingStation(IRecipeType, ItemLike...)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default void addRecipeCatalysts(IRecipeType<?> recipeType, ItemLike... ingredients) {
		addCraftingStation(recipeType, ingredients);
	}

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param ingredients the {@link ItemStack}s that can craft recipes (like a furnace or crafting table)
	 * @param recipeType  the type of recipe that the ingredients are a catalyst for
	 *
	 * @see #addRecipeCatalysts(IRecipeType, IIngredientType, List) to add non-{@link ItemStack} catalysts.
	 *
	 * @since 19.19.2
	 * @deprecated use {@link #addCraftingStation(IRecipeType, ItemStack...)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default void addRecipeCatalysts(IRecipeType<?> recipeType, ItemStack... ingredients) {
		addCraftingStation(recipeType, ingredients);
	}

	/**
	 * Add an association between ingredients and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredients they need to craft in order to make recipes from a recipe category.
	 *
	 * @param recipeType     the type of recipe that the ingredients are a catalyst for
	 * @param ingredientType the type of the ingredient
	 * @param ingredients    the ingredients that can craft recipes (like a furnace or crafting table)
	 * @since 19.19.2
	 * @deprecated use {@link #addCraftingStations(IRecipeType, IIngredientType, List)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default <T> void addRecipeCatalysts(IRecipeType<?> recipeType, IIngredientType<T> ingredientType, List<T> ingredients) {
		addCraftingStations(recipeType, ingredientType, ingredients);
	}

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param itemLike    the {@link ItemLike} that can craft recipes (like a furnace or crafting table)
	 * @param recipeTypes the types of recipe that the ingredient is a catalyst for
	 *
	 * @see #addRecipeCatalyst(ItemStack, IRecipeType...) to add {@link ItemStack} catalysts.
	 * @see #addRecipeCatalyst(IIngredientType, Object, IRecipeType...) to add non-{@link ItemLike} catalysts.
	 *
	 * @since 19.18.2
	 * @deprecated use {@link #addCraftingStation(IRecipeType, ItemLike...)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default void addRecipeCatalyst(ItemLike itemLike, IRecipeType<?>... recipeTypes) {
		addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemLike.asItem().getDefaultInstance(), recipeTypes);
	}

	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param ingredient  the {@link ItemStack} that can craft recipes (like a furnace or crafting table)
	 * @param recipeTypes the types of recipe that the ingredient is a catalyst for
	 * @see #addRecipeCatalyst(IIngredientType, Object, IRecipeType...) to add non-{@link ItemStack} catalysts.
	 *
	 * @since 9.5.0
	 * @deprecated use {@link #addCraftingStation(IRecipeType, ItemStack...)}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(forRemoval = true, since = "20.0.0")
	default void addRecipeCatalyst(ItemStack ingredient, IRecipeType<?>... recipeTypes) {
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
	 * @deprecated use {@link #addCraftingStation(IRecipeType, IIngredientType, T)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	<T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T ingredient, IRecipeType<?>... recipeTypes);
}

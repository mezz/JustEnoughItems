package mezz.jei.load.registration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.plugins.jei.info.IngredientInfoRecipe;
import mezz.jei.util.ErrorUtil;

public class RecipeRegistration implements IRecipeRegistration {
	private final ListMultiMap<ResourceLocation, Object> recipes = new ListMultiMap<>();
	private final ImmutableMap<ResourceLocation, IRecipeCategory<?>> recipeCategoriesByUid;
	private final IJeiHelpers jeiHelpers;
	private final IIngredientManager ingredientManager;
	private final IVanillaRecipeFactory vanillaRecipeFactory;

	public RecipeRegistration(ImmutableMap<ResourceLocation, IRecipeCategory<?>> recipeCategoriesByUid, IJeiHelpers jeiHelpers, IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory) {
		this.recipeCategoriesByUid = recipeCategoriesByUid;
		this.jeiHelpers = jeiHelpers;
		this.ingredientManager = ingredientManager;
		this.vanillaRecipeFactory = vanillaRecipeFactory;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IVanillaRecipeFactory getVanillaRecipeFactory() {
		return vanillaRecipeFactory;
	}

	@Override
	public void addRecipes(Collection<?> recipes, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipes, "recipes");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

		IRecipeCategory<?> recipeCategory = this.recipeCategoriesByUid.get(recipeCategoryUid);
		if (recipeCategory == null) {
			throw new NullPointerException("No recipe category has been registered for recipeCategoryUid " + recipeCategoryUid);
		}
		Class<?> recipeClass = recipeCategory.getRecipeClass();

		for (Object recipe : recipes) {
			ErrorUtil.checkNotNull(recipe, "recipe");
			if (!recipeClass.isInstance(recipe)) {
				throw new IllegalArgumentException(recipeCategory.getUid() + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
			}
			this.recipes.put(recipeCategoryUid, recipe);
		}
	}

	@Override
	public <T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, String... descriptionKeys) {
		ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		addIngredientInfo(Collections.singletonList(ingredient), ingredientType, descriptionKeys);
	}

	@Override
	public <T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, String... descriptionKeys) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		for (Object ingredient : ingredients) {
			ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
		}
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		List<IngredientInfoRecipe<T>> recipes = IngredientInfoRecipe.create(ingredients, ingredientType, descriptionKeys);
		addRecipes(recipes, VanillaRecipeCategoryUid.INFORMATION);
	}

	public ImmutableListMultimap<ResourceLocation, Object> getRecipes() {
		return recipes.toImmutable();
	}
}

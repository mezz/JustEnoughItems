package mezz.jei.recipes;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeCatalystLookup;
import mezz.jei.api.recipe.IRecipeCategoriesLookup;
import mezz.jei.api.recipe.IRecipeLookup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeManager implements IRecipeManager {
	private final RecipeManagerInternal internal;
	private final IModIdHelper modIdHelper;
	private final RegisteredIngredients registeredIngredients;

	public RecipeManager(RecipeManagerInternal internal, IModIdHelper modIdHelper, RegisteredIngredients registeredIngredients) {
		this.internal = internal;
		this.modIdHelper = modIdHelper;
		this.registeredIngredients = registeredIngredients;
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		ErrorUtil.checkNotNull(mode, "mode");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		RecipeIngredientRole role = mode.toRole();
		IIngredientType<V> ingredientType = registeredIngredients.getIngredientType(ingredient);
		return Focus.createFromApi(registeredIngredients, role, ingredientType, ingredient);
	}

	@Override
	public <R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		return new RecipeLookup<>(recipeType, internal);
	}

	@Override
	public IRecipeCategoriesLookup createRecipeCategoryLookup() {
		return new RecipeCategoriesLookup(internal);
	}

	@Override
	public IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType) {
		return new RecipeCatalystLookup(recipeType, internal);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		internal.addRecipes(recipeCategoryUid, List.of(recipe));
	}

	@Override
	public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(recipes, "recipes");
		ErrorUtil.assertMainThread();

		internal.addRecipes(recipeType, recipes);
	}

	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.5.0")
	@Override
	@Nullable
	public IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return internal.getRecipeCategoriesStream(List.of(recipeCategoryUid), FocusGroup.EMPTY, includeHidden)
			.findFirst()
			.orElse(null);
	}

	@Deprecated
	public List<RecipeType<?>> getRecipeTypes(Collection<ResourceLocation> recipeCategoryUids) {
		return recipeCategoryUids.stream()
			.<RecipeType<?>>map(internal::getTypeForRecipeCategoryUid)
			.toList();
	}

	@Deprecated
	public RecipeType<?> getRecipeType(ResourceLocation recipeCategoryUid) {
		return internal.getTypeForRecipeCategoryUid(recipeCategoryUid);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");
		IFocusGroup internalFocus = FocusGroup.createFromNullable(focus);
		return internal.getRecipeCategoriesStream(recipeCategoryUids, internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden) {
		IFocusGroup internalFocus = FocusGroup.createFromNullable(focus);
		return internal.getRecipeCategoriesStream(List.of(), internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public List<IRecipeCategory<?>> getRecipeCategories(Collection<? extends IFocus<?>> focus, boolean includeHidden) {
		IFocusGroup internalFocus = FocusGroup.create(focus);
		return internal.getRecipeCategoriesStream(List.of(), internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		IFocusGroup internalFocus = FocusGroup.createFromNullable(focus);
		return internal.getRecipesStream(recipeCategory.getRecipeType(), internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory, List<? extends IFocus<?>> focuses, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		IFocusGroup internalFocus = FocusGroup.create(focuses);
		return internal.getRecipesStream(recipeCategory.getRecipeType(), internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		return internal.getRecipeCatalystStream(recipeType, includeHidden)
			.map(ITypedIngredient::getIngredient)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public List<ITypedIngredient<?>> getRecipeCatalystsTyped(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		return internal.getRecipeCatalystStream(recipeType, includeHidden)
			.toList();
	}

	@Override
	public <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, @Nullable IFocus<?> focus) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		IFocusGroup focusGroup = FocusGroup.createFromNullable(focus);
		RecipeLayout<T> recipeLayout = RecipeLayout.create(-1, recipeCategory, recipe, focusGroup, registeredIngredients, modIdHelper, 0, 0);
		Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
		return recipeLayout.getLegacyAdapter();
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.hideRecipe(recipeCategoryUid, recipe);
	}

	@Override
	public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		ErrorUtil.checkNotNull(recipes, "recipe");
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.hideRecipes(recipeType, recipes);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.unhideRecipe(recipe, recipeCategoryUid);
	}

	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		ErrorUtil.checkNotNull(recipes, "recipe");
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.unhideRecipes(recipeType, recipes);
	}

	@Override
	public void hideRecipeCategory(RecipeType<?> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.hideRecipeCategory(recipeType);
	}

	@Override
	public void unhideRecipeCategory(RecipeType<?> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.unhideRecipeCategory(recipeType);
	}

	@SuppressWarnings("removal")
	@Deprecated
	@Override
	public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.hideRecipeCategory(recipeCategoryUid);
	}

	@SuppressWarnings("removal")
	@Deprecated
	@Override
	public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.unhideRecipeCategory(recipeCategoryUid);
	}
}

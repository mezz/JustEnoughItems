package mezz.jei.library.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.library.focus.Focus;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecipeManager implements IRecipeManager {
	private final RecipeManagerInternal internal;
	private final IModIdHelper modIdHelper;
	private final IIngredientManager ingredientManager;
	private final Textures textures;
	private final IIngredientVisibility ingredientVisibility;

	public RecipeManager(RecipeManagerInternal internal, IModIdHelper modIdHelper, IIngredientManager ingredientManager, Textures textures, IIngredientVisibility ingredientVisibility) {
		this.internal = internal;
		this.modIdHelper = modIdHelper;
		this.ingredientManager = ingredientManager;
		this.textures = textures;
		this.ingredientVisibility = ingredientVisibility;
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		ErrorUtil.checkNotNull(mode, "mode");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		RecipeIngredientRole role = mode.toRole();
		IIngredientType<V> ingredientType = ingredientManager.getIngredientType(ingredient);
		return Focus.createFromApi(ingredientManager, role, ingredientType, ingredient);
	}

	@Override
	public <R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		return new RecipeLookup<>(recipeType, internal, ingredientManager);
	}

	@Override
	public IRecipeCategoriesLookup createRecipeCategoryLookup() {
		return new RecipeCategoriesLookup(internal, ingredientManager);
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
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	@Nullable
	public IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return internal.getRecipeCategoriesStream(List.of(recipeCategoryUid), FocusGroup.EMPTY, includeHidden)
			.findFirst()
			.orElse(null);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");
		IFocusGroup internalFocus = FocusGroup.createFromNullable(focus, ingredientManager);
		return internal.getRecipeCategoriesStream(recipeCategoryUids, internalFocus, includeHidden)
			.toList();
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden) {
		IFocusGroup internalFocus = FocusGroup.createFromNullable(focus, ingredientManager);
		return internal.getRecipeCategoriesStream(List.of(), internalFocus, includeHidden)
			.toList();
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public List<IRecipeCategory<?>> getRecipeCategories(Collection<? extends IFocus<?>> focus, boolean includeHidden) {
		IFocusGroup internalFocus = FocusGroup.create(focus, ingredientManager);
		return internal.getRecipeCategoriesStream(List.of(), internalFocus, includeHidden)
			.toList();
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		IFocusGroup internalFocus = FocusGroup.createFromNullable(focus, ingredientManager);
		return internal.getRecipesStream(recipeCategory.getRecipeType(), internalFocus, includeHidden)
			.toList();
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory, List<? extends IFocus<?>> focuses, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		IFocusGroup internalFocus = FocusGroup.create(focuses, ingredientManager);
		return internal.getRecipesStream(recipeCategory.getRecipeType(), internalFocus, includeHidden)
			.toList();
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		return internal.getRecipeCatalystStream(recipeType, includeHidden)
			.<Object>map(ITypedIngredient::getIngredient)
			.toList();
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

	@SuppressWarnings("removal")
	@Override
	public <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, @Nullable IFocus<?> focus) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		IFocusGroup focusGroup = FocusGroup.createFromNullable(focus, ingredientManager);
		return RecipeLayout.create(
			recipeCategory,
			recipe,
			focusGroup,
			ingredientManager,
			ingredientVisibility,
			modIdHelper,
			textures
		).orElseThrow(() -> new NullPointerException("Recipe layout crashed during creation, see log."));
	}

	@Override
	public <T> Optional<IRecipeLayoutDrawable> createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focusGroup) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");
		return RecipeLayout.create(
			recipeCategory,
			recipe,
			focusGroup,
			ingredientManager,
			ingredientVisibility,
			modIdHelper,
			textures
		);
	}

	@Override
	public IRecipeSlotDrawable createRecipeSlotDrawable(RecipeIngredientRole role, List<Optional<ITypedIngredient<?>>> ingredients, Set<Integer> focusedIngredients, int xPos, int yPos, int ingredientCycleOffset) {
		RecipeSlot recipeSlot = new RecipeSlot(ingredientManager, role, xPos, yPos, ingredientCycleOffset);
		recipeSlot.set(ingredients, focusedIngredients, ingredientVisibility);
		return recipeSlot;
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
	public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.hideRecipe(recipeCategoryUid, recipe);
	}

	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		ErrorUtil.checkNotNull(recipes, "recipe");
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.unhideRecipes(recipeType, recipes);
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
	public void hideRecipeCategory(RecipeType<?> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.hideRecipeCategory(recipeType);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.hideRecipeCategory(recipeCategoryUid);
	}

	@Override
	public void unhideRecipeCategory(RecipeType<?> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.unhideRecipeCategory(recipeType);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.unhideRecipeCategory(recipeCategoryUid);
	}

	@Override
	public Optional<RecipeType<?>> getRecipeType(ResourceLocation recipeUid) {
		return internal.getRecipeType(recipeUid);
	}
}

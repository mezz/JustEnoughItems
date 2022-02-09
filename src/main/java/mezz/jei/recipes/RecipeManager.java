package mezz.jei.recipes;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeManager implements IRecipeManager {
	private final RecipeManagerInternal internal;
	private final IModIdHelper modIdHelper;
	private final IIngredientManager ingredientManager;

	public RecipeManager(RecipeManagerInternal internal, IModIdHelper modIdHelper, IIngredientManager ingredientManager) {
		this.internal = internal;
		this.modIdHelper = modIdHelper;
		this.ingredientManager = ingredientManager;
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		RecipeIngredientRole role = mode.toRole();
		IIngredientType<V> ingredientType = ingredientManager.getIngredientType(ingredient);
		return Focus.createFromApi(ingredientManager, role, ingredientType, ingredient);
	}

	@Override
	@Deprecated
	public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		internal.addRecipes(List.of(recipe), recipeCategoryUid);
	}

	@Override
	@Nullable
	public IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return internal.getRecipeCategoriesStream(List.of(recipeCategoryUid), List.of(), includeHidden)
			.findFirst()
			.orElse(null);
	}

	@Override
	public <V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");
		List<Focus<?>> internalFocus = Focus.checkNullable(focus);
		return internal.getRecipeCategoriesStream(recipeCategoryUids, internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@Override
	public <V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden) {
		List<Focus<?>> internalFocus = Focus.checkNullable(focus);
		return internal.getRecipeCategoriesStream(List.of(), internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@Override
	public List<IRecipeCategory<?>> getRecipeCategories(Collection<? extends IFocus<?>> focus, boolean includeHidden) {
		List<Focus<?>> internalFocus = Focus.check(focus);
		return internal.getRecipeCategoriesStream(List.of(), internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		List<Focus<?>> internalFocus = Focus.checkNullable(focus);
		return internal.getRecipesStream(recipeCategory, internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory, List<? extends IFocus<?>> focuses, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		List<Focus<?>> internalFocus = Focus.check(focuses);
		return internal.getRecipesStream(recipeCategory, internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("removal")
	@Override
	public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		return internal.getRecipeCatalystStream(recipeCategory, includeHidden)
			.map(ITypedIngredient::getIngredient)
			.collect(Collectors.toList());
	}

	@Override
	public List<ITypedIngredient<?>> getRecipeCatalystsTyped(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		return internal.getRecipeCatalystStream(recipeCategory, includeHidden)
			.toList();
	}

	@Override
	public <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus) {
		List<Focus<?>> checkedFocus = Focus.check(focus);
		RecipeLayout<T> recipeLayout = RecipeLayout.create(-1, recipeCategory, recipe, checkedFocus, ingredientManager, modIdHelper, 0, 0);
		Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
		return recipeLayout.getLegacyAdapter();
	}

	@Override
	public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.hideRecipe(recipe, recipeCategoryUid);
	}

	@Override
	public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.unhideRecipe(recipe, recipeCategoryUid);
	}

	@Override
	public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.hideRecipeCategory(recipeCategoryUid);
	}

	@Override
	public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		internal.unhideRecipeCategory(recipeCategoryUid);
	}
}

package mezz.jei.recipes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
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

	public RecipeManager(RecipeManagerInternal internal, IModIdHelper modIdHelper) {
		this.internal = internal;
		this.modIdHelper = modIdHelper;
	}

	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		return new Focus<>(mode, ingredient);
	}

	@Override
	@Deprecated
	public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		internal.addRecipe(recipe, recipeCategoryUid);
	}

	@Override
	@Nullable
	public IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return internal.getRecipeCategoriesStream(ImmutableSet.of(recipeCategoryUid), null, includeHidden)
			.findFirst()
			.orElse(null);
	}

	@Override
	public <V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");
		Focus<V> internalFocus = Focus.checkNullable(focus);
		return internal.getRecipeCategoriesStream(recipeCategoryUids, internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@Override
	public <V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden) {
		Focus<V> internalFocus = Focus.checkNullable(focus);
		return internal.getRecipeCategoriesStream(null, internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		Focus<V> internalFocus = Focus.checkNullable(focus);
		return internal.getRecipesStream(recipeCategory, internalFocus, includeHidden)
			.collect(Collectors.toList());
	}

	@Override
	public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		return internal.getRecipeCatalysts(recipeCategory, includeHidden);
	}

	@Override
	public <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus) {
		Focus<?> checkedFocus = Focus.check(focus);
		RecipeLayout<?> recipeLayout = RecipeLayout.create(-1, recipeCategory, recipe, checkedFocus, modIdHelper, 0, 0);
		Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
		return recipeLayout;
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

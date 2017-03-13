package mezz.jei.gui.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.Focus;
import mezz.jei.util.MathUtil;
import net.minecraft.item.ItemStack;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeRegistry recipeRegistry;
	private final IRecipeLogicStateListener stateListener;

	private boolean initialState = true;
	private IngredientLookupState state;
	private final Stack<IngredientLookupState> history = new Stack<IngredientLookupState>();

	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private List<IRecipeWrapper> recipes = Collections.emptyList();

	public RecipeGuiLogic(IRecipeRegistry recipeRegistry, IRecipeLogicStateListener stateListener) {
		this.recipeRegistry = recipeRegistry;
		this.stateListener = stateListener;
		List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories();
		this.state = new IngredientLookupState(null, recipeCategories, 0, 0);
	}

	@Override
	public <V> boolean setFocus(IFocus<V> focus) {
		focus = Focus.check(focus);
		final List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories(focus);
		if (recipeCategories.isEmpty()) {
			return false;
		}

		if (!initialState) {
			history.push(this.state);
		}

		final IngredientLookupState state = Internal.getIngredientLookupMemory().getState(focus, recipeCategories);
		setState(state);

		return true;
	}

	@Override
	public boolean back() {
		if (history.empty()) {
			return false;
		}
		final IngredientLookupState state = history.pop();
		setState(state);
		return true;
	}

	@Override
	public void clearHistory() {
		while (!history.empty()) {
			history.pop();
		}
	}

	private void setState(IngredientLookupState state) {
		this.state = state;
		this.initialState = false;
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public boolean setCategoryFocus() {
		IRecipeCategory recipeCategory = getSelectedRecipeCategory();

		if (!initialState) {
			history.push(this.state);
		}

		final List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories();
		final int recipeCategoryIndex = recipeCategories.indexOf(recipeCategory);
		final IngredientLookupState state = new IngredientLookupState(null, recipeCategories, recipeCategoryIndex, 0);
		setState(state);

		return true;
	}

	@Override
	public boolean setCategoryFocus(List<String> recipeCategoryUids) {
		List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories(recipeCategoryUids);
		if (recipeCategories.isEmpty()) {
			return false;
		}

		if (!initialState) {
			history.push(this.state);
		}

		final IngredientLookupState state = new IngredientLookupState(null, recipeCategories, 0, 0);
		setState(state);

		return true;
	}

	@Override
	public List<ItemStack> getRecipeCategoryCraftingItems() {
		IRecipeCategory category = getSelectedRecipeCategory();
		return getRecipeCategoryCraftingItems(category);
	}

	@Override
	public List<ItemStack> getRecipeCategoryCraftingItems(IRecipeCategory recipeCategory) {
		return recipeRegistry.getCraftingItems(recipeCategory);
	}

	@Override
	public void setRecipesPerPage(int recipesPerPage) {
		if (state.getRecipesPerPage() != recipesPerPage) {
			state.setRecipesPerPage(recipesPerPage);
			updateRecipes();
		}
	}

	private void updateRecipes() {
		final IRecipeCategory recipeCategory = getSelectedRecipeCategory();
		IFocus<?> focus = state.getFocus();
		if (focus != null) {
			//noinspection unchecked
			this.recipes = recipeRegistry.getRecipeWrappers(recipeCategory, focus);
		} else {
			//noinspection unchecked
			this.recipes = recipeRegistry.getRecipeWrappers(recipeCategory);
		}
	}

	@Override
	public IRecipeCategory getSelectedRecipeCategory() {
		return state.getRecipeCategories().get(state.getRecipeCategoryIndex());
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		return state.getRecipeCategories();
	}

	@Override
	public List<RecipeLayout> getRecipeLayouts(final int posX, final int posY, final int spacingY) {
		List<RecipeLayout> recipeLayouts = new ArrayList<RecipeLayout>();

		IRecipeCategory recipeCategory = getSelectedRecipeCategory();

		int recipeWidgetIndex = 0;
		int recipePosY = posY;
		final int firstRecipeIndex = state.getRecipeIndex() - (state.getRecipeIndex() % state.getRecipesPerPage());
		for (int recipeIndex = firstRecipeIndex; recipeIndex < recipes.size() && recipeLayouts.size() < state.getRecipesPerPage(); recipeIndex++) {
			IRecipeWrapper recipeWrapper = recipes.get(recipeIndex);
			if (recipeWrapper == null) {
				continue;
			}

			RecipeLayout recipeLayout = new RecipeLayout(recipeWidgetIndex++, recipeCategory, recipeWrapper, state.getFocus(), posX, recipePosY);
			recipeLayouts.add(recipeLayout);

			recipePosY += spacingY;
		}

		return recipeLayouts;
	}

	@Override
	public void nextRecipeCategory() {
		final int recipesTypesCount = state.getRecipeCategories().size();
		state.setRecipeCategoryIndex((state.getRecipeCategoryIndex() + 1) % recipesTypesCount);
		state.setRecipeIndex(0);
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public void setRecipeCategory(IRecipeCategory category) {
		int index = state.getRecipeCategories().indexOf(category);
		if (index < 0) {
			return;
		}

		state.setRecipeCategoryIndex(index);
		state.setRecipeIndex(0);
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public boolean hasMultiplePages() {
		return recipes.size() > state.getRecipesPerPage();
	}

	@Override
	public void previousRecipeCategory() {
		final int recipesTypesCount = state.getRecipeCategories().size();
		state.setRecipeCategoryIndex((recipesTypesCount + state.getRecipeCategoryIndex() - 1) % recipesTypesCount);
		state.setRecipeIndex(0);
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public void nextPage() {
		state.setRecipeIndex(state.getRecipeIndex() + state.getRecipesPerPage());
		if (state.getRecipeIndex() >= recipes.size()) {
			state.setRecipeIndex(0);
		}
		stateListener.onStateChange();
	}

	@Override
	public void previousPage() {
		state.setRecipeIndex(state.getRecipeIndex() - state.getRecipesPerPage());
		if (state.getRecipeIndex() < 0) {
			final int pageCount = pageCount(state.getRecipesPerPage());
			state.setRecipeIndex((pageCount - 1) * state.getRecipesPerPage());
		}
		stateListener.onStateChange();
	}

	private int pageCount(int recipesPerPage) {
		if (recipes.size() <= 1) {
			return 1;
		}

		return MathUtil.divideCeil(recipes.size(), recipesPerPage);
	}

	@Override
	public String getPageString() {
		int pageIndex = MathUtil.divideCeil(state.getRecipeIndex() + 1, state.getRecipesPerPage());
		return pageIndex + "/" + pageCount(state.getRecipesPerPage());
	}

	@Override
	public boolean hasMultipleCategories() {
		return state.getRecipeCategories().size() > 1;
	}

	@Override
	public boolean hasAllCategories() {
		return state.getRecipeCategories().size() == recipeRegistry.getRecipeCategories().size();
	}

}

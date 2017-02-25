package mezz.jei.gui.recipes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private static class State {
		@Nonnull
		public final IFocus<?> focus;
		@Nonnull
		public final ImmutableList<IRecipeCategory> recipeCategories;

		public int recipeCategoryIndex;
		public int pageIndex;
		public int recipesPerPage;

		public State(IFocus<?> focus, List<IRecipeCategory> recipeCategories, int recipeCategoryIndex, int pageIndex) {
			Preconditions.checkArgument(!recipeCategories.isEmpty(), "Recipe categories cannot be empty.");
			Preconditions.checkArgument(recipeCategoryIndex >= 0, "Recipe category index cannot be negative.");
			Preconditions.checkArgument(pageIndex >= 0, "Page index cannot be negative.");
			this.focus = focus;
			this.recipeCategories = ImmutableList.copyOf(recipeCategories);
			this.recipeCategoryIndex = recipeCategoryIndex;
			this.pageIndex = pageIndex;
		}
	}

	@Nonnull
	private final IRecipeRegistry recipeRegistry;
	@Nonnull
	private final IRecipeLogicStateListener stateListener;

	private boolean initialState = true;
	@Nonnull
	private State state;
	@Nonnull
	private final Stack<State> history = new Stack<State>();

	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private List<IRecipeWrapper> recipes = Collections.emptyList();

	public RecipeGuiLogic(IRecipeRegistry recipeRegistry, IRecipeLogicStateListener stateListener) {
		this.recipeRegistry = recipeRegistry;
		this.stateListener = stateListener;
		IFocus focus = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories();
		this.state = new State(focus, recipeCategories, 0, 0);
	}

	@Override
	public <V> boolean setFocus(IFocus<V> focus) {
		final List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories(focus);
		if (recipeCategories.isEmpty()) {
			return false;
		}

		final int recipeCategoryIndex = getRecipeCategoryIndex(recipeCategories);

		if (!initialState) {
			history.push(this.state);
		}

		final State state = new State(focus, recipeCategories, recipeCategoryIndex, 0);
		setState(state);

		return true;
	}

	@Override
	public boolean back() {
		if (history.empty()) {
			return false;
		}
		final State state = history.pop();
		setState(state);
		return true;
	}

	@Override
	public void clearHistory() {
		while (!history.empty()) {
			history.pop();
		}
	}

	private void setState(State state) {
		this.state = state;
		this.initialState = false;
		updateRecipes();
		stateListener.onStateChange();
	}

	private int getRecipeCategoryIndex(List<IRecipeCategory> recipeCategories) {
		final Container container = Minecraft.getMinecraft().thePlayer.openContainer;
		if (container == null) {
			return 0;
		}

		for (int i = 0; i < recipeCategories.size(); i++) {
			IRecipeCategory recipeCategory = recipeCategories.get(i);
			if (recipeRegistry.getRecipeTransferHandler(container, recipeCategory) != null) {
				return i;
			}
		}

		return 0;
	}

	@Override
	public boolean setCategoryFocus() {
		IRecipeCategory recipeCategory = getSelectedRecipeCategory();

		if (!initialState) {
			history.push(this.state);
		}

		final List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories();
		final int recipeCategoryIndex = recipeCategories.indexOf(recipeCategory);
		IFocus<Object> focus = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		final State state = new State(focus, recipeCategories, recipeCategoryIndex, 0);
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

		IFocus<Object> focus = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		final State state = new State(focus, recipeCategories, 0, 0);
		setState(state);

		return true;
	}

	@Override
	public IFocus getFocus() {
		return state.focus;
	}

	@Override
	public List<ItemStack> getRecipeCategoryCraftingItems() {
		IRecipeCategory category = getSelectedRecipeCategory();
		IFocus focusNone = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		return recipeRegistry.getCraftingItems(category, focusNone);
	}

	@Override
	public List<ItemStack> getRecipeCategoryCraftingItems(IRecipeCategory recipeCategory) {
		IFocus focusNone = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		return recipeRegistry.getCraftingItems(recipeCategory, focusNone);
	}

	@Override
	public void setRecipesPerPage(int recipesPerPage) {
		if (state.recipesPerPage != recipesPerPage) {
			int recipeIndex = state.pageIndex * state.recipesPerPage;
			state.pageIndex = recipeIndex / recipesPerPage;

			state.recipesPerPage = recipesPerPage;
			updateRecipes();
		}
	}

	private void updateRecipes() {
		final IRecipeCategory recipeCategory = getSelectedRecipeCategory();
		IFocus<?> focus = state.focus;
		//noinspection unchecked
		this.recipes = recipeRegistry.getRecipeWrappers(recipeCategory, focus);
	}

	@Override
	public IRecipeCategory getSelectedRecipeCategory() {
		return state.recipeCategories.get(state.recipeCategoryIndex);
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		return state.recipeCategories;
	}

	@Override
	public List<RecipeLayout> getRecipeLayouts(int posX, int posY, int spacingY) {
		List<RecipeLayout> recipeLayouts = new ArrayList<RecipeLayout>();

		IRecipeCategory recipeCategory = getSelectedRecipeCategory();

		int recipeWidgetIndex = 0;
		for (int recipeIndex = state.pageIndex * state.recipesPerPage; recipeIndex < recipes.size() && recipeLayouts.size() < state.recipesPerPage; recipeIndex++) {
			IRecipeWrapper recipeWrapper = recipes.get(recipeIndex);
			if (recipeWrapper == null) {
				continue;
			}

			RecipeLayout recipeLayout = new RecipeLayout(recipeWidgetIndex++, recipeCategory, recipeWrapper, state.focus, posX, posY);
			recipeLayouts.add(recipeLayout);

			posY += spacingY;
		}

		return recipeLayouts;
	}

	@Override
	public void nextRecipeCategory() {
		final int recipesTypesCount = state.recipeCategories.size();
		state.recipeCategoryIndex = (state.recipeCategoryIndex + 1) % recipesTypesCount;
		state.pageIndex = 0;
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public void setRecipeCategory(IRecipeCategory category) {
		int index = state.recipeCategories.indexOf(category);
		if (index < 0) {
			return;
		}

		state.recipeCategoryIndex = index;
		state.pageIndex = 0;
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public boolean hasMultiplePages() {
		return recipes.size() > state.recipesPerPage;
	}

	@Override
	public void previousRecipeCategory() {
		final int recipesTypesCount = state.recipeCategories.size();
		state.recipeCategoryIndex = (recipesTypesCount + state.recipeCategoryIndex - 1) % recipesTypesCount;
		state.pageIndex = 0;
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public void nextPage() {
		int pageCount = pageCount(state.recipesPerPage);
		state.pageIndex = (state.pageIndex + 1) % pageCount;
		stateListener.onStateChange();
	}

	@Override
	public void previousPage() {
		int pageCount = pageCount(state.recipesPerPage);
		state.pageIndex = (pageCount + state.pageIndex - 1) % pageCount;
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
		return (state.pageIndex + 1) + "/" + pageCount(state.recipesPerPage);
	}

	@Override
	public boolean hasMultipleCategories() {
		return state.recipeCategories.size() > 1;
	}

	@Override
	public boolean hasAllCategories() {
		return state.recipeCategories.size() == recipeRegistry.getRecipeCategories().size();
	}
}

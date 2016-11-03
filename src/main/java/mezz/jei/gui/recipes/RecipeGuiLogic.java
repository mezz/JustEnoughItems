package mezz.jei.gui.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.ImmutableList;
import mezz.jei.RecipeRegistry;
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
		public final IFocus focus;
		@Nonnull
		public final ImmutableList<IRecipeCategory> recipeCategories;

		public int recipeCategoryIndex;
		public int pageIndex;
		public int recipesPerPage;

		public State(IFocus focus, List<IRecipeCategory> recipeCategories, int recipeCategoryIndex, int pageIndex) {
			this.focus = focus;
			this.recipeCategories = ImmutableList.copyOf(recipeCategories);
			this.recipeCategoryIndex = recipeCategoryIndex;
			this.pageIndex = pageIndex;
		}
	}

	private final RecipeRegistry recipeRegistry;
	private final IRecipeLogicStateListener stateListener;

	@Nullable
	private State state = null;
	private final Stack<State> history = new Stack<State>();

	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private List<IRecipeWrapper> recipes = Collections.emptyList();

	public RecipeGuiLogic(RecipeRegistry recipeRegistry, IRecipeLogicStateListener stateListener) {
		this.recipeRegistry = recipeRegistry;
		this.stateListener = stateListener;
	}

	@Override
	public boolean setFocus(IFocus focus) {
		return setFocus(focus, true);
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

	private <V> boolean setFocus(IFocus<V> focus, boolean saveHistory) {
		final List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories(focus);
		if (recipeCategories.isEmpty()) {
			return false;
		}

		final int recipeCategoryIndex = getRecipeCategoryIndex(recipeCategories);

		if (this.state != null && saveHistory) {
			history.push(this.state);
		}

		final State state = new State(focus, recipeCategories, recipeCategoryIndex, 0);
		setState(state);

		return true;
	}

	private void setState(State state) {
		this.state = state;
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
		if (recipeCategory == null) {
			return false;
		}

		if (this.state != null) {
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

		if (this.state != null) {
			history.push(this.state);
		}

		IFocus<Object> focus = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		final State state = new State(focus, recipeCategories, 0, 0);
		setState(state);

		return true;
	}

	@Override
	public IFocus getFocus() {
		if (state == null) {
			return null;
		}
		return state.focus;
	}

	@Override
	public List<ItemStack> getRecipeCategoryCraftingItems() {
		IRecipeCategory category = getSelectedRecipeCategory();
		if (category == null) {
			return Collections.emptyList();
		}
		return recipeRegistry.getCraftingItems(category, getFocus());
	}

	@Override
	public List<ItemStack> getRecipeCategoryCraftingItems(IRecipeCategory recipeCategory) {
		return recipeRegistry.getCraftingItems(recipeCategory, getFocus());
	}

	@Override
	public void setRecipesPerPage(int recipesPerPage) {
		if (state == null) {
			return;
		}
		if (state.recipesPerPage != recipesPerPage) {
			int recipeIndex = state.pageIndex * state.recipesPerPage;
			state.pageIndex = recipeIndex / recipesPerPage;

			state.recipesPerPage = recipesPerPage;
			updateRecipes();
		}
	}

	private void updateRecipes() {
		if (state == null) {
			return;
		}

		final IRecipeCategory recipeCategory = getSelectedRecipeCategory();
		if (recipeCategory == null) {
			recipes = Collections.emptyList();
		} else {
			IFocus<?> focus = state.focus;
			//noinspection unchecked
			this.recipes = recipeRegistry.getRecipeWrappers(recipeCategory, focus);
		}
	}

	@Override
	@Nullable
	public IRecipeCategory getSelectedRecipeCategory() {
		if (state == null || state.recipeCategories.size() == 0) {
			return null;
		}
		return state.recipeCategories.get(state.recipeCategoryIndex);
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		if (state == null) {
			return ImmutableList.of();
		}
		return state.recipeCategories;
	}

	@Override
	public List<RecipeLayout> getRecipeWidgets(int posX, int posY, int spacingY) {
		if (state == null) {
			return Collections.emptyList();
		}

		List<RecipeLayout> recipeWidgets = new ArrayList<RecipeLayout>();

		IRecipeCategory recipeCategory = getSelectedRecipeCategory();
		if (recipeCategory == null) {
			return recipeWidgets;
		}

		int recipeWidgetIndex = 0;
		for (int recipeIndex = state.pageIndex * state.recipesPerPage; recipeIndex < recipes.size() && recipeWidgets.size() < state.recipesPerPage; recipeIndex++) {
			IRecipeWrapper recipeWrapper = recipes.get(recipeIndex);
			if (recipeWrapper == null) {
				continue;
			}

			RecipeLayout recipeWidget = new RecipeLayout(recipeWidgetIndex++, posX, posY, recipeCategory, recipeWrapper, state.focus);
			recipeWidgets.add(recipeWidget);

			posY += spacingY;
		}

		return recipeWidgets;
	}

	@Override
	public void nextRecipeCategory() {
		if (state == null) {
			return;
		}
		final int recipesTypesCount = state.recipeCategories.size();
		state.recipeCategoryIndex = (state.recipeCategoryIndex + 1) % recipesTypesCount;
		state.pageIndex = 0;
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public void setRecipeCategory(IRecipeCategory category) {
		if (state == null) {
			return;
		}

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
		return state != null && recipes.size() > state.recipesPerPage;
	}

	@Override
	public void previousRecipeCategory() {
		if (state == null) {
			return;
		}
		final int recipesTypesCount = state.recipeCategories.size();
		state.recipeCategoryIndex = (recipesTypesCount + state.recipeCategoryIndex - 1) % recipesTypesCount;
		state.pageIndex = 0;
		updateRecipes();
		stateListener.onStateChange();
	}

	@Override
	public void nextPage() {
		if (state == null) {
			return;
		}
		int pageCount = pageCount(state.recipesPerPage);
		state.pageIndex = (state.pageIndex + 1) % pageCount;
		stateListener.onStateChange();
	}

	@Override
	public void previousPage() {
		if (state == null) {
			return;
		}
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
		if (state == null) {
			return "1/1";
		}
		return (state.pageIndex + 1) + "/" + pageCount(state.recipesPerPage);
	}

	@Override
	public boolean hasMultipleCategories() {
		return state != null && state.recipeCategories.size() > 1;
	}

	@Override
	public boolean hasAllCategories() {
		return state != null && state.recipeCategories.size() == recipeRegistry.getRecipeCategoryCount();
	}
}

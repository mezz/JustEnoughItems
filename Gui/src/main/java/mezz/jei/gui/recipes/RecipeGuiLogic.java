package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.ingredients.IngredientLookupState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeManager recipeManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IRecipeLogicStateListener stateListener;

	private boolean initialState = true;
	private IngredientLookupState state;
	private final Stack<IngredientLookupState> history = new Stack<>();
	private final IFocusFactory focusFactory;

	public RecipeGuiLogic(
		IRecipeManager recipeManager,
		IRecipeTransferManager recipeTransferManager,
		IRecipeLogicStateListener stateListener,
		IFocusFactory focusFactory
	) {
		this.recipeManager = recipeManager;
		this.recipeTransferManager = recipeTransferManager;
		this.stateListener = stateListener;
		this.state = IngredientLookupState.createWithFocus(recipeManager, focusFactory.getEmptyFocusGroup());
		this.focusFactory = focusFactory;
	}

	@Override
	public boolean setFocus(IFocusGroup focuses) {
		IngredientLookupState state = IngredientLookupState.createWithFocus(recipeManager, focuses);
		List<IRecipeCategory<?>> recipeCategories = state.getRecipeCategories();
		if (recipeCategories.isEmpty()) {
			return false;
		}

		int recipeCategoryIndex = getRecipeCategoryIndexToShowFirst(recipeCategories, recipeTransferManager);
		state.setRecipeCategoryIndex(recipeCategoryIndex);

		setState(state, true);

		return true;
	}

	@Nonnegative
	private static int getRecipeCategoryIndexToShowFirst(List<IRecipeCategory<?>> recipeCategories, IRecipeTransferManager recipeTransferManager) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player != null) {
			AbstractContainerMenu openContainer = player.containerMenu;
			//noinspection ConstantConditions
			if (openContainer != null) {
				for (int i = 0; i < recipeCategories.size(); i++) {
					IRecipeCategory<?> recipeCategory = recipeCategories.get(i);
					var recipeTransferHandler = recipeTransferManager.getRecipeTransferHandler(openContainer, recipeCategory);
					if (recipeTransferHandler.isPresent()) {
						return i;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public boolean back() {
		if (history.empty()) {
			return false;
		}
		final IngredientLookupState state = history.pop();
		setState(state, false);
		return true;
	}

	@Override
	public void clearHistory() {
		while (!history.empty()) {
			history.pop();
		}
	}

	private void setState(IngredientLookupState state, boolean saveHistory) {
		if (saveHistory && !initialState) {
			history.push(this.state);
		}
		this.state = state;
		this.initialState = false;
		stateListener.onStateChange();
	}

	@Override
	public boolean setCategoryFocus() {
		IRecipeCategory<?> recipeCategory = getSelectedRecipeCategory();

		final IngredientLookupState state = IngredientLookupState.createWithFocus(recipeManager, focusFactory.getEmptyFocusGroup());
		state.setRecipeCategory(recipeCategory);
		setState(state, true);

		return true;
	}

	@Override
	public boolean setCategoryFocus(List<RecipeType<?>> recipeTypes) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitTypes(recipeTypes)
			.get()
			.toList();

		final IngredientLookupState state = IngredientLookupState.createWithCategories(recipeManager, focusFactory, recipeCategories);
		if (state.getRecipeCategories().isEmpty()) {
			return false;
		}

		setState(state, true);

		return true;
	}

	@Override
	public Stream<ITypedIngredient<?>> getRecipeCatalysts() {
		IRecipeCategory<?> category = getSelectedRecipeCategory();
		return getRecipeCatalysts(category);
	}

	@Override
	public Stream<ITypedIngredient<?>> getRecipeCatalysts(IRecipeCategory<?> recipeCategory) {
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		return recipeManager.createRecipeCatalystLookup(recipeType)
			.get();
	}

	@Override
	public void setRecipesPerPage(int recipesPerPage) {
		if (state.getRecipesPerPage() != recipesPerPage) {
			state.setRecipesPerPage(recipesPerPage);
		}
	}

	@Override
	public IRecipeCategory<?> getSelectedRecipeCategory() {
		return state.getFocusedRecipes().getRecipeCategory();
	}

	@Override
	@Unmodifiable
	public List<IRecipeCategory<?>> getRecipeCategories() {
		return state.getRecipeCategories();
	}

	@Override
	public List<IRecipeLayoutDrawable<?>> getRecipeLayouts() {
		return getRecipeLayouts(this.state.getFocusedRecipes());
	}

	private <T> List<IRecipeLayoutDrawable<?>> getRecipeLayouts(FocusedRecipes<T> selectedRecipes) {
		List<IRecipeLayoutDrawable<?>> recipeLayouts = new ArrayList<>();

		IRecipeCategory<T> recipeCategory = selectedRecipes.getRecipeCategory();
		List<T> recipes = selectedRecipes.getRecipes();
		List<T> brokenRecipes = new ArrayList<>();

		final int firstRecipeIndex = state.getRecipeIndex() - (state.getRecipeIndex() % state.getRecipesPerPage());
		for (int recipeIndex = firstRecipeIndex; recipeIndex < recipes.size() && recipeLayouts.size() < state.getRecipesPerPage(); recipeIndex++) {
			T recipe = recipes.get(recipeIndex);
			recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, state.getFocuses())
				.ifPresentOrElse(recipeLayouts::add, () -> brokenRecipes.add(recipe));
		}

		if (!brokenRecipes.isEmpty()) {
			RecipeType<T> recipeType = recipeCategory.getRecipeType();
			recipeManager.hideRecipes(recipeType, brokenRecipes);
		}

		return recipeLayouts;
	}

	@Override
	public void nextRecipeCategory() {
		state.nextRecipeCategory();
		stateListener.onStateChange();
	}

	@Override
	public void setRecipeCategory(IRecipeCategory<?> category) {
		if (state.setRecipeCategory(category)) {
			stateListener.onStateChange();
		}
	}

	@Override
	public boolean hasMultiplePages() {
		List<?> recipes = state.getFocusedRecipes().getRecipes();
		return recipes.size() > state.getRecipesPerPage();
	}

	@Override
	public void previousRecipeCategory() {
		state.previousRecipeCategory();
		stateListener.onStateChange();
	}

	@Override
	public void nextPage() {
		int recipeCount = recipeCount();
		state.setRecipeIndex(state.getRecipeIndex() + state.getRecipesPerPage());
		if (state.getRecipeIndex() >= recipeCount) {
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
		int recipeCount = recipeCount();
		if (recipeCount <= 1) {
			return 1;
		}

		return MathUtil.divideCeil(recipeCount, recipesPerPage);
	}

	private int recipeCount() {
		List<?> recipes = state.getFocusedRecipes().getRecipes();
		return recipes.size();
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
		long categoryCount = recipeManager.createRecipeCategoryLookup()
			.get()
			.count();

		return state.getRecipeCategories().size() == categoryCount;
	}

}

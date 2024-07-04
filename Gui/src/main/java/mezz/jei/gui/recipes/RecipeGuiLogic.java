package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import mezz.jei.gui.recipes.lookups.ILookupState;
import mezz.jei.gui.recipes.lookups.IngredientLookupState;
import mezz.jei.gui.recipes.lookups.SingleCategoryLookupState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeManager recipeManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IRecipeLogicStateListener stateListener;

	private boolean initialState = true;
	private ILookupState state;
	private final Stack<ILookupState> history = new Stack<>();
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
	public boolean showFocus(IFocusGroup focuses) {
		ILookupState state = IngredientLookupState.createWithFocus(recipeManager, focuses);
		return showState(state);
	}

	@Override
	public boolean showRecipes(IFocusedRecipes<?> recipes, IFocusGroup focuses) {
		ILookupState state = new SingleCategoryLookupState(recipes, focuses);
		return showState(state);
	}

	private boolean showState(ILookupState state) {
		List<IRecipeCategory<?>> recipeCategories = state.getRecipeCategories();
		if (recipeCategories.isEmpty()) {
			return false;
		}

		int recipeCategoryIndex = getRecipeCategoryIndexToShowFirst(recipeCategories, recipeTransferManager);
		state.moveToRecipeCategoryIndex(recipeCategoryIndex);

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
		final ILookupState state = history.pop();
		setState(state, false);
		return true;
	}

	@Override
	public void clearHistory() {
		while (!history.empty()) {
			history.pop();
		}
	}

	private void setState(ILookupState state, boolean saveHistory) {
		if (saveHistory && !initialState) {
			history.push(this.state);
		}
		this.state = state;
		this.initialState = false;
		stateListener.onStateChange();
	}

	@Override
	public boolean showAllRecipes() {
		IRecipeCategory<?> recipeCategory = getSelectedRecipeCategory();

		final ILookupState state = IngredientLookupState.createWithFocus(recipeManager, focusFactory.getEmptyFocusGroup());
		state.moveToRecipeCategory(recipeCategory);
		setState(state, true);

		return true;
	}

	@Override
	public boolean showCategories(List<RecipeType<?>> recipeTypes) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitTypes(recipeTypes)
			.get()
			.toList();

		final ILookupState state = IngredientLookupState.createWithCategories(recipeManager, focusFactory, recipeCategories);
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

	@Override
	public Optional<ImmutableRect2i> getRecipeLayoutSizeWithBorder() {
		return getRecipeLayoutSizeWithBorder(this.state.getFocusedRecipes());
	}

	private <T> Optional<ImmutableRect2i> getRecipeLayoutSizeWithBorder(IFocusedRecipes<T> selectedRecipes) {
		IRecipeCategory<T> recipeCategory = selectedRecipes.getRecipeCategory();

		return selectedRecipes.getRecipes()
			.stream()
			.map(recipe -> recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, state.getFocuses()))
			.flatMap(Optional::stream)
			.map(IRecipeLayoutDrawable::getRectWithBorder)
			.map(ImmutableRect2i::new)
			.findFirst();
	}

	private <T> List<IRecipeLayoutDrawable<?>> getRecipeLayouts(IFocusedRecipes<T> selectedRecipes) {
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
		if (state.moveToRecipeCategory(category)) {
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
		state.nextPage();
		stateListener.onStateChange();
	}

	@Override
	public void previousPage() {
		state.previousPage();
		stateListener.onStateChange();
	}

	@Override
	public String getPageString() {
		int pageIndex = MathUtil.divideCeil(state.getRecipeIndex() + 1, state.getRecipesPerPage());
		return pageIndex + "/" + state.pageCount();
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

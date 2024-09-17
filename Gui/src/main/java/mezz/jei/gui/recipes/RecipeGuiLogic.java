package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.recipes.layouts.IRecipeLayoutList;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import mezz.jei.gui.recipes.lookups.ILookupState;
import mezz.jei.gui.recipes.lookups.IngredientLookupState;
import mezz.jei.gui.recipes.lookups.SingleCategoryLookupState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeManager recipeManager;
	private final IRecipeLogicStateListener stateListener;

	private boolean initialState = true;
	private ILookupState state;
	private final Stack<ILookupState> history = new Stack<>();
	private final IFocusFactory focusFactory;
	private final IRecipeLayoutWithButtonsFactory recipeLayoutFactory;
	private @Nullable IRecipeCategory<?> cachedRecipeCategory;
	private @Nullable IRecipeLayoutList cachedRecipeLayoutsWithButtons;

	public RecipeGuiLogic(
		IRecipeManager recipeManager,
		IRecipeLogicStateListener stateListener,
		IFocusFactory focusFactory,
		IRecipeLayoutWithButtonsFactory recipeLayoutFactory
	) {
		this.recipeManager = recipeManager;
		this.stateListener = stateListener;
		this.recipeLayoutFactory = recipeLayoutFactory;
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.get()
			.toList();
		this.state = IngredientLookupState.create(
			recipeManager,
			focusFactory.getEmptyFocusGroup(),
			recipeCategories
		);
		this.focusFactory = focusFactory;
	}

	@Override
	public void tick() {
		if (cachedRecipeLayoutsWithButtons != null) {
			cachedRecipeLayoutsWithButtons.tick();
		}
	}

	@Override
	public boolean showFocus(IFocusGroup focuses) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitFocus(focuses.getAllFocuses())
			.get()
			.toList();
		ILookupState state = IngredientLookupState.create(
			recipeManager,
			focuses,
			recipeCategories
		);
		return setState(state, true);
	}

	@Override
	public boolean showRecipes(IFocusedRecipes<?> recipes, IFocusGroup focuses) {
		ILookupState state = new SingleCategoryLookupState(recipes, focuses);
		return setState(state, true);
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

	private boolean setState(ILookupState state, boolean saveHistory) {
		List<IRecipeCategory<?>> recipeCategories = state.getRecipeCategories();
		if (recipeCategories.isEmpty()) {
			return false;
		}

		if (saveHistory && !initialState) {
			history.push(this.state);
		}
		this.state = state;
		this.initialState = false;
		this.cachedRecipeCategory = null;
		this.cachedRecipeLayoutsWithButtons = null;
		stateListener.onStateChange();
		return true;
	}

	@Override
	public boolean showAllRecipes() {
		IRecipeCategory<?> recipeCategory = getSelectedRecipeCategory();

		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.get()
			.toList();
		final ILookupState state = IngredientLookupState.create(
			recipeManager,
			focusFactory.getEmptyFocusGroup(),
			recipeCategories
		);
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

		final ILookupState state = IngredientLookupState.create(
			recipeManager,
			focusFactory.getEmptyFocusGroup(),
			recipeCategories
		);
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
	public IRecipeCategory<?> getSelectedRecipeCategory() {
		return state.getFocusedRecipes().getRecipeCategory();
	}

	@Override
	@Unmodifiable
	public List<IRecipeCategory<?>> getRecipeCategories() {
		return state.getRecipeCategories();
	}

	@Override
	public List<RecipeLayoutWithButtons<?>> getVisibleRecipeLayoutsWithButtons(
		int availableHeight,
		int minRecipePadding,
		@Nullable AbstractContainerMenu container
	) {
		Player player = Minecraft.getInstance().player;

		IRecipeCategory<?> recipeCategory = getSelectedRecipeCategory();

		if (this.cachedRecipeLayoutsWithButtons == null ||
			this.cachedRecipeCategory != recipeCategory
		) {
			IFocusedRecipes<?> focusedRecipes = this.state.getFocusedRecipes();

			this.cachedRecipeLayoutsWithButtons = createRecipeLayoutsWithButtons(focusedRecipes, container, player);
			this.cachedRecipeCategory = recipeCategory;
		}

		final int recipeHeight =
			this.cachedRecipeLayoutsWithButtons.findFirst()
				.map(RecipeLayoutWithButtons::recipeLayout)
				.map(IRecipeLayoutDrawable::getRectWithBorder)
				.map(Rect2i::getHeight)
				.orElseGet(recipeCategory::getHeight);

		final int recipesPerPage = Math.max(1, 1 + ((availableHeight - recipeHeight) / (recipeHeight + minRecipePadding)));
		this.state.setRecipesPerPage(recipesPerPage);

		return this.state.getVisible(this.cachedRecipeLayoutsWithButtons);
	}

	@Override
	public int getRecipesPerPage() {
		return this.state.getRecipesPerPage();
	}

	@Unmodifiable
	private <T> IRecipeLayoutList createRecipeLayoutsWithButtons(
		IFocusedRecipes<T> selectedRecipes,
		@Nullable AbstractContainerMenu container,
		@Nullable Player player
	) {
		IRecipeCategory<T> recipeCategory = selectedRecipes.getRecipeCategory();
		List<T> recipes = selectedRecipes.getRecipes();
		List<T> brokenRecipes = new ArrayList<>();

		List<RecipeLayoutWithButtons<T>> results = recipes.stream()
			.<IRecipeLayoutDrawable<T>>mapMulti((recipe, acceptor) ->
				recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, state.getFocuses())
					.ifPresentOrElse(acceptor, () -> brokenRecipes.add(recipe))
			)
			.map(recipeLayoutFactory::create)
			.toList();

		if (!brokenRecipes.isEmpty()) {
			RecipeType<T> recipeType = recipeCategory.getRecipeType();
			recipeManager.hideRecipes(recipeType, brokenRecipes);
		}

		return IRecipeLayoutList.create(container, player, results);
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
	public void goToFirstPage() {
		state.goToFirstPage();
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

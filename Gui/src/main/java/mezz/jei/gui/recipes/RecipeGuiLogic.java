package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistory;
import mezz.jei.gui.recipes.layouts.IRecipeLayoutList;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import mezz.jei.gui.recipes.lookups.ILookupState;
import mezz.jei.gui.recipes.lookups.IngredientLookupState;
import mezz.jei.gui.recipes.lookups.SingleCategoryLookupState;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final RecipeTransferService recipeTransferService;
	private final IRecipeLogicStateListener stateListener;

	private boolean initialState = true;
	private ILookupState state;
	private final NavigationHistory<ILookupState> stateHistory = new NavigationHistory<>();
	private final LookupHistory lookupHistory;
	private final IFocusFactory focusFactory;
	private final BookmarkList bookmarks;
	private final IRecipeLayoutWithButtonsFactory recipeLayoutFactory;
	private @Nullable IRecipeCategory<?> cachedRecipeCategory;
	private @Nullable IRecipeLayoutList cachedRecipeLayoutsWithButtons;
	private int cachedContainerId = -1;
	private Set<RecipeSorterStage> cachedSorterStages = Set.of();

	public RecipeGuiLogic(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		LookupHistory lookupHistory,
		RecipeTransferService recipeTransferService,
		IRecipeLogicStateListener stateListener,
		IFocusFactory focusFactory,
		BookmarkList bookmarks,
		IRecipeLayoutWithButtonsFactory recipeLayoutFactory
	) {
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.lookupHistory = lookupHistory;
		this.recipeTransferService = recipeTransferService;
		this.stateListener = stateListener;
		this.recipeLayoutFactory = recipeLayoutFactory;
		this.bookmarks = bookmarks;
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.get()
			.toList();
		this.state = IngredientLookupState.create(
			recipeManager,
			focusFactory.getEmptyFocusGroup(),
			recipeCategories,
			recipeTransferService
		);
		this.focusFactory = focusFactory;
	}

	@Override
	public void tick(@Nullable AbstractContainerMenu container) {
		if (cachedRecipeLayoutsWithButtons != null) {
			cachedRecipeLayoutsWithButtons.tick(container);
		}
	}

	@Override
	public boolean showFocus(IFocusGroup focuses) {
		List<IFocus<?>> allFocuses = focuses.getAllFocuses();
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitFocus(allFocuses)
			.get()
			.toList();
		ILookupState state = IngredientLookupState.create(
			recipeManager,
			focuses,
			recipeCategories,
			recipeTransferService
		);

		for (IFocus<?> focus : allFocuses) {
			IngredientBookmark<?> ingredientBookmark = IngredientBookmark.create(focus.getTypedValue(), ingredientManager);
			this.lookupHistory.add(ingredientBookmark);
		}

		return setState(state, true);
	}

	public boolean showRecipes(IFocusedRecipes<?> focusedRecipes, IFocusGroup focuses) {
		var recipeBookmark = createRecipeBookmark(recipeManager, ingredientManager, recipeTransferService, focusedRecipes, focuses);
		if (recipeBookmark != null) {
			this.lookupHistory.add(recipeBookmark);
		} else {
			for (IFocus<?> focus : focuses.getAllFocuses()) {
				IngredientBookmark<?> ingredientBookmark = IngredientBookmark.create(focus.getTypedValue(), ingredientManager);
				this.lookupHistory.add(ingredientBookmark);
			}
		}
		ILookupState state = new SingleCategoryLookupState(focusedRecipes, focuses);
		return setState(state, true);
	}

	private static <T> @Nullable RecipeBookmark<T, ?> createRecipeBookmark(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService,
		IFocusedRecipes<T> focusedRecipes,
		IFocusGroup focusGroup
	) {
		IRecipeCategory<T> recipeCategory = focusedRecipes.getRecipeCategory();
		List<T> recipes = focusedRecipes.getRecipes();
		if (recipes.size() != 1) {
			return null;
		}
		T recipe = recipes.get(0);
		return recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, focusGroup)
			.map(drawable -> RecipeBookmark.create(drawable, ingredientManager, recipeTransferService))
			.orElse(null);
	}

	@Override
	public boolean back() {
		return stateHistory.goBack(state)
			.map(previousState -> setState(previousState, false))
			.orElse(false);
	}

	@Override
	public boolean forward() {
		return stateHistory.goForward(state)
			.map(nextState -> setState(nextState, false))
			.orElse(false);
	}

	@Override
	public void clearHistory() {
		stateHistory.clear();
	}

	private boolean setState(ILookupState state, boolean saveHistory) {
		List<IRecipeCategory<?>> recipeCategories = state.getRecipeCategories();
		if (recipeCategories.isEmpty()) {
			return false;
		}

		if (saveHistory && !initialState) {
			stateHistory.record(this.state);
		}
		this.state = state;
		this.initialState = false;
		this.cachedRecipeCategory = null;
		this.cachedRecipeLayoutsWithButtons = null;
		this.cachedContainerId = -1;
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
			recipeCategories,
			recipeTransferService
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
			recipeCategories,
			recipeTransferService
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
	public List<IRecipeLayoutWithButtons<?>> getVisibleRecipeLayoutsWithButtons(
		int availableHeight,
		int minRecipePadding,
		@Nullable AbstractContainerMenu container
	) {
		IRecipeCategory<?> recipeCategory = getSelectedRecipeCategory();

		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		Set<RecipeSorterStage> recipeSorterStages = clientConfig.getRecipeSorterStages();

		int containerId = container == null ? -1 : container.containerId;
		if (!recipeSorterStages.equals(cachedSorterStages) ||
			this.cachedRecipeLayoutsWithButtons == null ||
			this.cachedRecipeCategory != recipeCategory ||
			this.cachedContainerId != containerId
		) {
			IFocusedRecipes<?> focusedRecipes = this.state.getFocusedRecipes();

			this.cachedRecipeLayoutsWithButtons = IRecipeLayoutList.create(
				recipeSorterStages,
				focusedRecipes,
				state.getFocuses(),
				bookmarks,
				recipeManager,
				recipeLayoutFactory
			);
			this.cachedRecipeCategory = recipeCategory;
			this.cachedSorterStages = Set.copyOf(recipeSorterStages);
			this.cachedContainerId = containerId;
		}

		final int recipeHeight =
			this.cachedRecipeLayoutsWithButtons.findFirst(container)
				.map(IRecipeLayoutWithButtons::getRecipeLayout)
				.map(IRecipeLayoutDrawable::getRectWithBorder)
				.map(Rect2i::getHeight)
				.orElseGet(recipeCategory::getHeight);

		final int recipesPerPage = Math.max(1, 1 + ((availableHeight - recipeHeight) / (recipeHeight + minRecipePadding)));
		this.state.setRecipesPerPage(recipesPerPage);

		return this.state.getVisible(this.cachedRecipeLayoutsWithButtons, container);
	}

	@Override
	public int getRecipesPerPage() {
		return this.state.getRecipesPerPage();
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

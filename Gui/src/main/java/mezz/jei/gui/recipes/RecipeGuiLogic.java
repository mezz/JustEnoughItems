package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.config.RecipeSorterStage;
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
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IRecipeLogicStateListener stateListener;

	private boolean initialState = true;
	private ILookupState state;
	private final Stack<ILookupState> stateHistory = new Stack<>();
	private final LookupHistory lookupHistory;
	private final IFocusFactory focusFactory;
	private @Nullable IRecipeCategory<?> cachedRecipeCategory;
	private @Nullable IRecipeLayoutList cachedRecipeLayoutsWithButtons;
	private int cachedContainerId = -1;
	private Set<RecipeSorterStage> cachedSorterStages = Set.of();

	public RecipeGuiLogic(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		LookupHistory lookupHistory,
		IRecipeTransferManager recipeTransferManager,
		IRecipeLogicStateListener stateListener,
		IFocusFactory focusFactory
	) {
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.lookupHistory = lookupHistory;
		this.recipeTransferManager = recipeTransferManager;
		this.stateListener = stateListener;
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.get()
			.toList();
		this.state = IngredientLookupState.create(
			recipeManager,
			focusFactory.getEmptyFocusGroup(),
			recipeCategories,
			recipeTransferManager
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
		List<IFocus<?>> allFocuses = focuses.getAllFocuses();
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitFocus(allFocuses)
			.get()
			.toList();
		ILookupState state = IngredientLookupState.create(
			recipeManager,
			focuses,
			recipeCategories,
			recipeTransferManager
		);

		for (IFocus<?> focus : allFocuses) {
			IngredientBookmark<?> ingredientBookmark = IngredientBookmark.create(focus.getTypedValue(), ingredientManager);
			this.lookupHistory.add(ingredientBookmark);
		}

		return setState(state, true);
	}

	@Override
	public boolean showRecipes(IFocusedRecipes<?> focusedRecipes, IFocusGroup focuses) {
		var recipeBookmark = createRecipeBookmark(recipeManager, ingredientManager, focusedRecipes, focuses);
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
		IFocusedRecipes<T> focusedRecipes,
		IFocusGroup focusGroup
	) {
		IRecipeCategory<T> recipeCategory = focusedRecipes.getRecipeCategory();
		List<T> recipes = focusedRecipes.getRecipes();
		if (recipes.size() != 1) {
			return null;
		}
		T recipe = recipes.getFirst();
		return recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, focusGroup)
			.map(drawable -> RecipeBookmark.create(drawable, ingredientManager))
			.orElse(null);
	}

	@Override
	public boolean back() {
		if (stateHistory.empty()) {
			return false;
		}
		final ILookupState state = stateHistory.pop();
		setState(state, false);
		return true;
	}

	@Override
	public void clearHistory() {
		while (!stateHistory.empty()) {
			stateHistory.pop();
		}
	}

	private boolean setState(ILookupState state, boolean saveHistory) {
		List<IRecipeCategory<?>> recipeCategories = state.getRecipeCategories();
		if (recipeCategories.isEmpty()) {
			return false;
		}

		if (saveHistory && !initialState) {
			stateHistory.push(this.state);
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
			recipeTransferManager
		);
		state.moveToRecipeCategory(recipeCategory);
		setState(state, true);

		return true;
	}

	@Override
	public boolean showCategories(List<IRecipeType<?>> recipeTypes) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitTypes(recipeTypes)
			.get()
			.toList();

		final ILookupState state = IngredientLookupState.create(
			recipeManager,
			focusFactory.getEmptyFocusGroup(),
			recipeCategories,
			recipeTransferManager
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
		IRecipeType<?> recipeType = recipeCategory.getRecipeType();
		return recipeManager.createCraftingStationLookup(recipeType)
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
		@Nullable AbstractContainerMenu container,
		BookmarkList bookmarkList,
		RecipesGui recipesGui
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
				container,
				focusedRecipes,
				state.getFocuses(),
				bookmarkList,
				recipeManager,
				recipesGui
			);
			this.cachedRecipeCategory = recipeCategory;
			this.cachedSorterStages = Set.copyOf(recipeSorterStages);
			this.cachedContainerId = containerId;
		}

		final int recipeHeight =
			this.cachedRecipeLayoutsWithButtons.findFirst()
				.map(IRecipeLayoutWithButtons::getRecipeLayout)
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

	@Override
	public boolean nextRecipeCategory() {
		if (state.nextRecipeCategory()) {
			stateListener.onStateChange();
			return true;
		}
		return false;
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
	public boolean previousRecipeCategory() {
		if (state.previousRecipeCategory()) {
			stateListener.onStateChange();
			return true;
		}
		return false;
	}

	@Override
	public void goToFirstPage() {
		state.goToFirstPage();
		stateListener.onStateChange();
	}

	@Override
	public boolean nextPage() {
		if (state.nextPage()) {
			stateListener.onStateChange();
			return true;
		}
		return false;
	}

	@Override
	public boolean previousPage() {
		if (state.previousPage()) {
			stateListener.onStateChange();
			return true;
		}
		return false;
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

package mezz.jei.gui.recipes;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.IngredientLookupState;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeManager recipeManager;
	private final RecipeTransferManager recipeTransferManager;
	private final IRecipeLogicStateListener stateListener;
	private final IIngredientManager ingredientManager;
	private final IModIdHelper modIdHelper;

	private boolean initialState = true;
	private IngredientLookupState state;
	private final Stack<IngredientLookupState> history = new Stack<>();

	public RecipeGuiLogic(
		IRecipeManager recipeManager,
		RecipeTransferManager recipeTransferManager,
		IRecipeLogicStateListener stateListener,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper
	) {
		this.recipeManager = recipeManager;
		this.recipeTransferManager = recipeTransferManager;
		this.stateListener = stateListener;
		this.ingredientManager = ingredientManager;
		this.modIdHelper = modIdHelper;
		this.state = IngredientLookupState.createWithFocus(recipeManager, List.of());
	}

	@Override
	public boolean setFocus(List<Focus<?>> focuses) {
		IngredientLookupState state = IngredientLookupState.createWithFocus(recipeManager, focuses);
		ImmutableList<IRecipeCategory<?>> recipeCategories = state.getRecipeCategories();
		if (recipeCategories.isEmpty()) {
			return false;
		}

		int recipeCategoryIndex = getRecipeCategoryIndexToShowFirst(recipeCategories, recipeTransferManager);
		state.setRecipeCategoryIndex(recipeCategoryIndex);

		setState(state, true);

		return true;
	}

	@Nonnegative
	private static int getRecipeCategoryIndexToShowFirst(List<IRecipeCategory<?>> recipeCategories, RecipeTransferManager recipeTransferManager) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player != null) {
			AbstractContainerMenu openContainer = player.containerMenu;
			//noinspection ConstantConditions
			if (openContainer != null) {
				for (int i = 0; i < recipeCategories.size(); i++) {
					IRecipeCategory<?> recipeCategory = recipeCategories.get(i);
					IRecipeTransferHandler<?, ?> recipeTransferHandler = recipeTransferManager.getRecipeTransferHandler(openContainer, recipeCategory);
					if (recipeTransferHandler != null) {
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

		final IngredientLookupState state = IngredientLookupState.createWithFocus(recipeManager, List.of());
		state.setRecipeCategory(recipeCategory);
		setState(state, true);

		return true;
	}

	@Override
	public boolean setCategoryFocus(List<ResourceLocation> recipeCategoryUids) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.getRecipeCategories(recipeCategoryUids, null, false);
		final IngredientLookupState state = IngredientLookupState.createWithCategories(recipeManager, recipeCategories);
		if (state.getRecipeCategories().isEmpty()) {
			return false;
		}

		setState(state, true);

		return true;
	}

	@Override
	public List<ITypedIngredient<?>> getRecipeCatalysts() {
		IRecipeCategory<?> category = getSelectedRecipeCategory();
		return getRecipeCatalysts(category);
	}

	@Override
	public List<ITypedIngredient<?>> getRecipeCatalysts(IRecipeCategory<?> recipeCategory) {
		return recipeManager.getRecipeCatalystsTyped(recipeCategory, false);
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
	public ImmutableList<IRecipeCategory<?>> getRecipeCategories() {
		return state.getRecipeCategories();
	}

	@Override
	public List<RecipeLayout<?>> getRecipeLayouts(final int posX, final int posY, final int spacingY) {
		return getRecipeLayouts(this.state.getFocusedRecipes(), posX, posY, spacingY);
	}

	private <T> List<RecipeLayout<?>> getRecipeLayouts(FocusedRecipes<T> selectedRecipes, final int posX, final int posY, final int spacingY) {
		List<RecipeLayout<?>> recipeLayouts = new ArrayList<>();

		IRecipeCategory<T> recipeCategory = selectedRecipes.getRecipeCategory();
		List<T> recipes = selectedRecipes.getRecipes();

		int recipeWidgetIndex = 0;
		int recipePosY = posY;
		final int firstRecipeIndex = state.getRecipeIndex() - (state.getRecipeIndex() % state.getRecipesPerPage());
		for (int recipeIndex = firstRecipeIndex; recipeIndex < recipes.size() && recipeLayouts.size() < state.getRecipesPerPage(); recipeIndex++) {
			T recipe = recipes.get(recipeIndex);
			int index = recipeWidgetIndex++;
			RecipeLayout<T> recipeLayout = RecipeLayout.create(index, recipeCategory, recipe, state.getFocuses(), ingredientManager, modIdHelper, posX, recipePosY);
			if (recipeLayout == null) {
				recipes.remove(recipeIndex);
				recipeManager.hideRecipe(recipe, recipeCategory.getUid());
				recipeIndex--;
			} else {
				recipeLayouts.add(recipeLayout);
				recipePosY += spacingY;
			}
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
		return state.getRecipeCategories().size() == recipeManager.getRecipeCategories(List.of(), false).size();
	}

}

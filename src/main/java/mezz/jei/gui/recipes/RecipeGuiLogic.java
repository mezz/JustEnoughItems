package mezz.jei.gui.recipes;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.IngredientLookupState;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecipeGuiLogic implements IRecipeGuiLogic {
	private final IRecipeManager recipeManager;
	private final RecipeTransferManager recipeTransferManager;
	private final IRecipeLogicStateListener stateListener;
	private final IngredientManager ingredientManager;
	private final IModIdHelper modIdHelper;

	private boolean initialState = true;
	private IngredientLookupState state;
	private final Stack<IngredientLookupState> history = new Stack<>();

	public RecipeGuiLogic(IRecipeManager recipeManager, RecipeTransferManager recipeTransferManager, IRecipeLogicStateListener stateListener, IngredientManager ingredientManager, IModIdHelper modIdHelper) {
		this.recipeManager = recipeManager;
		this.recipeTransferManager = recipeTransferManager;
		this.stateListener = stateListener;
		this.ingredientManager = ingredientManager;
		this.modIdHelper = modIdHelper;
		List<IRecipeCategory<?>> recipeCategories = recipeManager.getRecipeCategories();
		this.state = new IngredientLookupState(recipeManager, null, recipeCategories, 0, 0);
	}

	@Override
	public <V> boolean setFocus(Focus<V> focus) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(focus.getValue());
		IFocus<?> translatedFocus = ingredientHelper.translateFocus(focus, Focus::new);
		Focus<?> checkedTranslatedFocus = Focus.check(translatedFocus);

		final List<IRecipeCategory<?>> recipeCategories = recipeManager.getRecipeCategories(translatedFocus);
		if (recipeCategories.isEmpty()) {
			return false;
		}

		if (!initialState) {
			history.push(this.state);
		}

		int recipeCategoryIndex = getRecipeCategoryIndexToShowFirst(recipeCategories);
		IngredientLookupState state = new IngredientLookupState(recipeManager, checkedTranslatedFocus, recipeCategories, recipeCategoryIndex, 0);
		setState(state);

		return true;
	}

	@Nonnegative
	private int getRecipeCategoryIndexToShowFirst(List<IRecipeCategory<?>> recipeCategories) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPlayerEntity player = minecraft.player;
		if (player != null) {
			Container openContainer = player.openContainer;
			if (openContainer != null) {
				for (int i = 0; i < recipeCategories.size(); i++) {
					IRecipeCategory<?> recipeCategory = recipeCategories.get(i);
					IRecipeTransferHandler<?> recipeTransferHandler = recipeTransferManager.getRecipeTransferHandler(openContainer, recipeCategory);
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
		stateListener.onStateChange();
	}

	@Override
	public boolean setCategoryFocus() {
		IRecipeCategory<?> recipeCategory = getSelectedRecipeCategory();

		if (!initialState) {
			history.push(this.state);
		}

		final List<IRecipeCategory<?>> recipeCategories = recipeManager.getRecipeCategories();
		final int recipeCategoryIndex = recipeCategories.indexOf(recipeCategory);
		final IngredientLookupState state = new IngredientLookupState(recipeManager, null, recipeCategories, recipeCategoryIndex, 0);
		setState(state);

		return true;
	}

	@Override
	public boolean setCategoryFocus(List<ResourceLocation> recipeCategoryUids) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.getRecipeCategories(recipeCategoryUids);
		if (recipeCategories.isEmpty()) {
			return false;
		}

		if (!initialState) {
			history.push(this.state);
		}

		final IngredientLookupState state = new IngredientLookupState(recipeManager, null, recipeCategories, 0, 0);
		setState(state);

		return true;
	}

	@Override
	public List<Object> getRecipeCatalysts() {
		IRecipeCategory<?> category = getSelectedRecipeCategory();
		return getRecipeCatalysts(category);
	}

	@Override
	public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory) {
		return recipeManager.getRecipeCatalysts(recipeCategory);
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
			RecipeLayout<T> recipeLayout = RecipeLayout.create(recipeWidgetIndex++, recipeCategory, recipe, state.getFocus(), modIdHelper, posX, recipePosY);
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
		final int recipesTypesCount = state.getRecipeCategories().size();
		state.setRecipeCategoryIndex((state.getRecipeCategoryIndex() + 1) % recipesTypesCount);
		stateListener.onStateChange();
	}

	@Override
	public void setRecipeCategory(IRecipeCategory<?> category) {
		int index = state.getRecipeCategories().indexOf(category);
		if (index < 0) {
			return;
		}

		state.setRecipeCategoryIndex(index);
		stateListener.onStateChange();
	}

	@Override
	public boolean hasMultiplePages() {
		List<?> recipes = state.getFocusedRecipes().getRecipes();
		return recipes.size() > state.getRecipesPerPage();
	}

	@Override
	public void previousRecipeCategory() {
		final int recipesTypesCount = state.getRecipeCategories().size();
		state.setRecipeCategoryIndex((recipesTypesCount + state.getRecipeCategoryIndex() - 1) % recipesTypesCount);
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
		return state.getRecipeCategories().size() == recipeManager.getRecipeCategories().size();
	}

}

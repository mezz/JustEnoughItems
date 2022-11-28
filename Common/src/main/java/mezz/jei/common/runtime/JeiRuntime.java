package mezz.jei.common.runtime;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IJeiKeyMappings;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.gui.overlay.IngredientListOverlayDummy;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlayDummy;
import mezz.jei.common.gui.recipes.RecipesGuiDummy;
import mezz.jei.common.ingredients.IngredientFilterApiDummy;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.util.ErrorUtil;

import java.util.Optional;

public class JeiRuntime implements IJeiRuntime {
	private final IRecipeManager recipeManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IRegisteredIngredients registeredIngredients;
	private final IIngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	private final IJeiKeyMappings keyMappings;
	private final IJeiHelpers jeiHelpers;
	private final IScreenHelper screenHelper;
	private IIngredientListOverlay ingredientListOverlay;
	private IBookmarkOverlay bookmarkOverlay;
	private IRecipesGui recipesGui;
	private IIngredientFilter ingredientFilter;

	public JeiRuntime(
		IRecipeManager recipeManager,
		IRegisteredIngredients registeredIngredients,
		IIngredientManager ingredientManager,
		IIngredientVisibility ingredientVisibility,
		IJeiKeyMappings keyMappings,
		IJeiHelpers jeiHelpers,
		IScreenHelper screenHelper,
		IRecipeTransferManager recipeTransferManager
	) {
		this.recipeManager = recipeManager;
		this.recipeTransferManager = recipeTransferManager;
		this.ingredientListOverlay = IngredientListOverlayDummy.INSTANCE;
		this.ingredientVisibility = ingredientVisibility;
		this.bookmarkOverlay = BookmarkOverlayDummy.INSTANCE;
		this.recipesGui = RecipesGuiDummy.INSTANCE;
		this.ingredientFilter = IngredientFilterApiDummy.INSTANCE;
		this.registeredIngredients = registeredIngredients;
		this.ingredientManager = ingredientManager;
		this.keyMappings = keyMappings;
		this.jeiHelpers = jeiHelpers;
		this.screenHelper = screenHelper;
	}

	@Override
	public <T> ITypedIngredient<T> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient) {
		Optional<ITypedIngredient<T>> result = TypedIngredient.createTyped(registeredIngredients, ingredientType, ingredient);
		if (result.isEmpty()) {
			String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, ingredientType, registeredIngredients);
			throw new IllegalArgumentException("Invalid ingredient: " + ingredientInfo);
		}
		return result.get();
	}

	@Override
	public IRecipeManager getRecipeManager() {
		return recipeManager;
	}

	@Override
	public IIngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}

	@Override
	public IIngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IBookmarkOverlay getBookmarkOverlay() {
		return bookmarkOverlay;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}

	@Override
	public IIngredientVisibility getIngredientVisibility() {
		return ingredientVisibility;
	}

	@Override
	public IJeiKeyMappings getKeyMappings() {
		return keyMappings;
	}

	@Override
	public IScreenHelper getScreenHelper() {
		return screenHelper;
	}

	@Override
	public IRecipeTransferManager getRecipeTransferManager() {
		return recipeTransferManager;
	}

	@Override
	public IRegisteredIngredients getRegisteredIngredients() {
		return registeredIngredients;
	}

	public void setIngredientListOverlay(IIngredientListOverlay ingredientListOverlay) {
		this.ingredientListOverlay = ingredientListOverlay;
	}

	public void setBookmarkOverlay(IBookmarkOverlay bookmarkOverlay) {
		this.bookmarkOverlay = bookmarkOverlay;
	}

	public void setRecipesGui(IRecipesGui recipesGui) {
		this.recipesGui = recipesGui;
	}

	public void setIngredientFilter(IIngredientFilter ingredientFilter) {
		this.ingredientFilter = ingredientFilter;
	}
}

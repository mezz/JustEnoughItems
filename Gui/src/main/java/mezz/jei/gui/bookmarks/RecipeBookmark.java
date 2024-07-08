package mezz.jei.gui.bookmarks;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.recipes.RecipeCategoryIconUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RecipeBookmark<T, R> implements IBookmark {
	private final IElement<R> element;
	private final IRecipeCategory<T> recipeCategory;
	private final T recipe;
	private final ResourceLocation recipeUid;
	private final ITypedIngredient<R> recipeOutput;
	private final IRecipeManager recipeManager;
	private final IFocusFactory focusFactory;

	private IRecipeLayoutDrawable<T> recipeLayoutDrawable = null;

	public static <T> Optional<RecipeBookmark<T, ?>> create(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		IIngredientManager ingredientManager,
		IRecipeTransferManager recipeTransferManager,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper,
		IFocusFactory focusFactory
	) {
		T recipe = recipeLayoutDrawable.getRecipe();
		IRecipeCategory<T> recipeCategory = recipeLayoutDrawable.getRecipeCategory();
		ResourceLocation recipeUid = recipeCategory.getRegistryName(recipe);
		if (recipeUid == null) {
			return Optional.empty();
		}

		IRecipeSlotsView recipeSlotsView = recipeLayoutDrawable.getRecipeSlotsView();
		return recipeSlotsView.getSlotViews(RecipeIngredientRole.OUTPUT)
			.stream()
			.flatMap(IRecipeSlotView::getAllIngredients)
			.findFirst()
			.map(ingredientManager::normalizeTypedIngredient)
			.map(output -> {
				IDrawable icon = RecipeCategoryIconUtil.create(
					recipeCategory,
					recipeManager,
					guiHelper
				);
				return new RecipeBookmark<>(recipeCategory, recipe, recipeUid, output, icon, recipeManager, focusFactory, ingredientManager, recipeTransferManager);
			});
	}

	public RecipeBookmark(
		IRecipeCategory<T> recipeCategory,
		T recipe,
		ResourceLocation recipeUid,
		ITypedIngredient<R> recipeOutput,
		IDrawable icon,
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		IRecipeTransferManager recipeTransferManager
	) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.recipeUid = recipeUid;
		this.recipeOutput = recipeOutput;
		this.element = new RecipeBookmarkElement<>(this, icon, ingredientManager, recipeTransferManager);
		this.recipeManager = recipeManager;
		this.focusFactory = focusFactory;
	}

	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	public ResourceLocation getRecipeUid() {
		return recipeUid;
	}

	public T getRecipe() {
		return recipe;
	}

	public ITypedIngredient<R> getRecipeOutput() {
		return recipeOutput;
	}

	public IRecipeLayoutDrawable<T> getRecipeLayoutDrawable() {
		if (recipeLayoutDrawable == null) {
			return recipeLayoutDrawable = recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, focusFactory.getEmptyFocusGroup(), true).orElseThrow();
		}
		return recipeLayoutDrawable;
	}

	@Override
	public IElement<?> getElement() {
		return element;
	}

	@Override
	public int hashCode() {
		return recipeUid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecipeBookmark<?, ?> recipeBookmark) {
			return recipeBookmark.recipeUid.equals(recipeUid);
		}
		return false;
	}
}

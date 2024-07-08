package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.config.BookmarkFeature;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.overlay.bookmarks.IBookmarkTooltip;
import mezz.jei.gui.overlay.bookmarks.IngredientsTooltipComponent;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public class RecipeBookmarkElement<T, R> implements IElement<R> {
	private final RecipeBookmark<T, R> recipeBookmark;
	private final IDrawable icon;
	private final IIngredientManager ingredientManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final EnumMap<BookmarkFeature, ClientTooltipComponent> longTermCache = new EnumMap<>(BookmarkFeature.class);

	public RecipeBookmarkElement(RecipeBookmark<T, R> recipeBookmark, IDrawable icon, IIngredientManager ingredientManager, IRecipeTransferManager recipeTransferManager) {
		this.recipeBookmark = recipeBookmark;
		this.icon = icon;
		this.ingredientManager = ingredientManager;
        this.recipeTransferManager = recipeTransferManager;
    }

	@Override
	public ITypedIngredient<R> getTypedIngredient() {
		return recipeBookmark.getRecipeOutput();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.of(recipeBookmark);
	}

	@Override
	public void renderExtras(GuiGraphics guiGraphics) {
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			// this z level seems to be the sweet spot so that
			// 2D icons draw above the items, and
			// 3D icons draw still draw under tooltips.
			poseStack.translate(8, 8, 200);
			poseStack.scale(0.5f, 0.5f, 0.5f);
			icon.draw(guiGraphics);
		}
		poseStack.popPose();
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		// ignore roles, always display the bookmarked recipe if it's clicked

		IRecipeCategory<T> recipeCategory = recipeBookmark.getRecipeCategory();
		T recipe = recipeBookmark.getRecipe();
		ITypedIngredient<?> ingredient = getTypedIngredient();
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, List.of(RecipeIngredientRole.OUTPUT));
		recipesGui.showRecipes(recipeCategory, List.of(recipe), focuses);
	}

	@Override
	public List<Component> getTooltip(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<R> ingredientRenderer, IIngredientHelper<R> ingredientHelper) {
		ITypedIngredient<R> ingredient = recipeBookmark.getRecipeOutput();
		return tooltipHelper.getRecipeTooltip(
			recipeBookmark.getRecipeCategory(),
			recipeBookmark.getRecipe(),
			ingredient,
			ingredientRenderer,
			ingredientHelper
		);
	}

	@Override
	public List<ClientTooltipComponent> getTooltipComponents(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<R> ingredientRenderer, IIngredientHelper<R> ingredientHelper) {
		List<ClientTooltipComponent> components = IElement.super.getTooltipComponents(tooltipHelper, ingredientRenderer, ingredientHelper);
		for (BookmarkFeature feature : BookmarkFeature.values()) {
			ClientTooltipComponent component = longTermCache.get(feature);
			if (component == null || (component instanceof IBookmarkTooltip tooltip && !tooltip.longTerm())){
				longTermCache.put(feature, component = createComponent(feature));
			}
			components.add(component);
		}
		return components;
	}

	private ClientTooltipComponent createComponent(BookmarkFeature feature){
		return switch (feature) {
			case PREVIEW -> new PreviewTooltipComponent(this.recipeBookmark.getRecipeLayoutDrawable(), recipeTransferManager);
			case INGREDIENTS -> new IngredientsTooltipComponent(this.recipeBookmark.getRecipeLayoutDrawable(), ingredientManager);
		};
	}

}

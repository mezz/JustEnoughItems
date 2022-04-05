package mezz.jei.common.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.common.Internal;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.ingredients.RecipeSlot;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.input.ClickedIngredient;
import mezz.jei.common.input.IClickedIngredient;
import mezz.jei.common.input.IRecipeFocusSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The area drawn on left side of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class RecipeCatalysts implements IRecipeFocusSource {
	private static final int ingredientSize = 16;
	private static final int ingredientBorderSize = 1;
	private static final int borderSize = 5;
	private static final int overlapSize = 6;

	private final DrawableNineSliceTexture backgroundTab;

	private final List<RecipeSlot> recipeSlots;
	private final DrawableNineSliceTexture slotBackground;
	private final IIngredientVisibility ingredientVisibility;
	private int left = 0;
	private int top = 0;
	private int width = 0;
	private int height = 0;

	public RecipeCatalysts(Textures textures, IIngredientVisibility ingredientVisibility) {
		this.ingredientVisibility = ingredientVisibility;
		recipeSlots = new ArrayList<>();
		backgroundTab = textures.getCatalystTab();
		slotBackground = textures.getRecipeCatalystSlotBackground();
	}

	public boolean isEmpty() {
		return this.recipeSlots.isEmpty();
	}

	public int getWidth() {
		return width - overlapSize;
	}

	public void updateLayout(List<ITypedIngredient<?>> ingredients, RecipesGui recipesGui) {
		this.recipeSlots.clear();

		if (!ingredients.isEmpty()) {
			ImmutableRect2i recipeArea = recipesGui.getArea();
			int availableHeight = recipeArea.getHeight() - 8;
			int borderHeight = (2 * borderSize) + (2 * ingredientBorderSize);
			int maxIngredientsPerColumn = (availableHeight - borderHeight) / ingredientSize;
			int columnCount = MathUtil.divideCeil(ingredients.size(), maxIngredientsPerColumn);
			maxIngredientsPerColumn = MathUtil.divideCeil(ingredients.size(), columnCount);

			width = (2 * ingredientBorderSize) + (borderSize * 2) + (columnCount * ingredientSize);
			height = (2 * ingredientBorderSize) + (borderSize * 2) + (maxIngredientsPerColumn * ingredientSize);
			top = recipeArea.getY();
			left = recipeArea.getX() - width + overlapSize; // overlaps the recipe gui slightly

			for (int i = 0; i < ingredients.size(); i++) {
				ITypedIngredient<?> ingredientForSlot = ingredients.get(i);
				RecipeSlot recipeSlot = createSlot(ingredientForSlot, i, maxIngredientsPerColumn);
				this.recipeSlots.add(recipeSlot);
			}
		}
	}

	private <T> RecipeSlot createSlot(ITypedIngredient<T> typedIngredient, int index, int maxIngredientsPerColumn) {
		RegisteredIngredients registeredIngredients = Internal.getRegisteredIngredients();
		int column = index / maxIngredientsPerColumn;
		int row = index % maxIngredientsPerColumn;
		int xPos = left + borderSize + (column * ingredientSize) + ingredientBorderSize;
		int yPos = top + borderSize + (row * ingredientSize) + ingredientBorderSize;
		RecipeSlot recipeSlot = new RecipeSlot(
			registeredIngredients,
			RecipeIngredientRole.CATALYST,
			xPos,
			yPos,
			0,
			0
		);
		recipeSlot.set(List.of(Optional.of(typedIngredient)), IntSet.of(0), ingredientVisibility);
		return recipeSlot;
	}

	@Nullable
	public RecipeSlot draw(PoseStack poseStack, int mouseX, int mouseY) {
		int ingredientCount = recipeSlots.size();
		if (ingredientCount > 0) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			RenderSystem.disableDepthTest();
			{
				int slotWidth = width - (2 * borderSize);
				int slotHeight = height - (2 * borderSize);
				backgroundTab.draw(poseStack, this.left, this.top, width, height);
				slotBackground.draw(poseStack, this.left + borderSize, this.top + borderSize, slotWidth, slotHeight);
			}
			RenderSystem.enableDepthTest();

			RecipeSlot hovered = null;
			for (RecipeSlot recipeSlot : this.recipeSlots) {
				if (recipeSlot.isMouseOver(mouseX, mouseY)) {
					hovered = recipeSlot;
				}
				recipeSlot.draw(poseStack);
			}
			return hovered;
		}
		return null;
	}

	private Stream<RecipeSlot> getHovered(double mouseX, double mouseY) {
		return this.recipeSlots.stream()
			.filter(recipeSlot -> recipeSlot.isMouseOver(mouseX, mouseY));
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return getHovered(mouseX, mouseY)
			.map(recipeSlot ->
				recipeSlot.getDisplayedIngredient()
					.map(i -> new ClickedIngredient<>(i, recipeSlot.getRect(), false, true)))
			.flatMap(Optional::stream);
	}
}

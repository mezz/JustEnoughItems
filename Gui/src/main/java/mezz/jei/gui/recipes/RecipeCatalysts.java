package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.ClickableIngredientInternal;
import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.input.IRecipeFocusSource;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nonnegative;
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

	private final List<IRecipeSlotDrawable> recipeSlots;
	private final DrawableNineSliceTexture slotBackground;
	private final IRecipeManager recipeManager;
	private int left = 0;
	private int top = 0;
	private int width = 0;
	private int height = 0;

	public RecipeCatalysts(Textures textures, IRecipeManager recipeManager) {
		this.recipeManager = recipeManager;
		recipeSlots = new ArrayList<>();
		backgroundTab = textures.getCatalystTab();
		slotBackground = textures.getRecipeCatalystSlotBackground();
	}

	public boolean isEmpty() {
		return this.recipeSlots.isEmpty();
	}

	@Nonnegative
	public int getWidth() {
		return Math.max(0, width - overlapSize);
	}

	public void updateLayout(List<ITypedIngredient<?>> ingredients, ImmutableRect2i recipeArea) {
		this.recipeSlots.clear();

		if (!ingredients.isEmpty()) {
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
				IRecipeSlotDrawable recipeSlot = createSlot(ingredientForSlot, i, maxIngredientsPerColumn);
				this.recipeSlots.add(recipeSlot);
			}
		}
	}

	private <T> IRecipeSlotDrawable createSlot(ITypedIngredient<T> typedIngredient, int index, int maxIngredientsPerColumn) {
		int column = index / maxIngredientsPerColumn;
		int row = index % maxIngredientsPerColumn;
		int xPos = left + borderSize + (column * ingredientSize) + ingredientBorderSize;
		int yPos = top + borderSize + (row * ingredientSize) + ingredientBorderSize;
		return recipeManager.createRecipeSlotDrawable(
			RecipeIngredientRole.CATALYST,
			List.of(Optional.of(typedIngredient)),
			IntSet.of(0),
			xPos,
			yPos,
			0
		);
	}

	public Optional<IRecipeSlotDrawable> draw(PoseStack poseStack, int mouseX, int mouseY) {
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

			IRecipeSlotDrawable hovered = null;
			for (IRecipeSlotDrawable recipeSlot : this.recipeSlots) {
				Rect2i rect = recipeSlot.getRect();
				if (MathUtil.contains(rect, mouseX, mouseY)) {
					hovered = recipeSlot;
				}
				recipeSlot.draw(poseStack);
			}
			return Optional.ofNullable(hovered);
		}
		return Optional.empty();
	}

	private Stream<IRecipeSlotDrawable> getHovered(double mouseX, double mouseY) {
		return this.recipeSlots.stream()
			.filter(recipeSlot -> {
				Rect2i rect = recipeSlot.getRect();
				return MathUtil.contains(rect, mouseX, mouseY);
			});
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return getHovered(mouseX, mouseY)
			.map(recipeSlot ->
				recipeSlot.getDisplayedIngredient()
					.map(i -> {
						Rect2i rect = recipeSlot.getRect();
						return new ClickableIngredientInternal<>(i, new ImmutableRect2i(rect), false, true);
					}))
			.flatMap(Optional::stream);
	}
}

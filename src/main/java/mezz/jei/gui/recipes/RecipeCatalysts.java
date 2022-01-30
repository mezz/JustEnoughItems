package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.ingredients.RecipeSlot;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.Internal;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.util.MathUtil;

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
	private int left = 0;
	private int top = 0;
	private int width = 0;
	private int height = 0;

	public RecipeCatalysts() {
		recipeSlots = new ArrayList<>();

		Textures textures = Internal.getTextures();
		backgroundTab = textures.getCatalystTab();
		slotBackground = textures.getNineSliceSlot();
	}

	public boolean isEmpty() {
		return this.recipeSlots.isEmpty();
	}

	public int getWidth() {
		return width - overlapSize;
	}

	public void updateLayout(List<Object> ingredients, RecipesGui recipesGui) {
		this.recipeSlots.clear();

		if (!ingredients.isEmpty()) {
			Rect2i recipeArea = recipesGui.getArea();
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
				Object ingredientForSlot = ingredients.get(i);
				RecipeSlot recipeSlot = createGuiIngredient(ingredientForSlot, i, maxIngredientsPerColumn);
				this.recipeSlots.add(recipeSlot);
			}
		}
	}

	private <T> RecipeSlot createGuiIngredient(T ingredient, int index, int maxIngredientsPerColumn) {
		IngredientManager ingredientManager = Internal.getIngredientManager();
		int column = index / maxIngredientsPerColumn;
		int row = index % maxIngredientsPerColumn;
		Rect2i rect = new Rect2i(
			left + borderSize + (column * ingredientSize) + ingredientBorderSize,
			top + borderSize + (row * ingredientSize) + ingredientBorderSize,
			ingredientSize,
			ingredientSize
		);
		RecipeSlot recipeSlot = new RecipeSlot(
			ingredientManager,
			RecipeIngredientRole.CATALYST,
			index,
			rect,
			0,
			0,
			0
		);
		recipeSlot.set(List.of(ingredient), null);
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
				recipeSlot.draw(poseStack, 0, 0);
			}
			return hovered;
		}
		return null;
	}

	private Optional<RecipeSlot> getHovered(double mouseX, double mouseY) {
		return this.recipeSlots.stream()
			.filter(recipeSlot -> recipeSlot.isMouseOver(mouseX, mouseY))
			.findFirst();
	}

	@Override
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return getHovered(mouseX, mouseY)
			.flatMap(hovered -> ClickedIngredient.create(hovered, false, true));
	}
}

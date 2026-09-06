package mezz.jei.gui.recipes;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The area drawn on left side of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class CraftingStations implements IRecipeFocusSource {
	private static final int ingredientSize = 16;
	private static final int ingredientBorderSize = 1;
	private static final int borderSize = 5;
	private static final int overlapSize = 6;

	private final ScalableDrawable backgroundTab;

	private final List<IRecipeSlotDrawable> recipeSlots;
	private final ScalableDrawable slotBackground;
	private final IGuiHelper guiHelper;
	private int left = 0;
	private int top = 0;
	private int width = 0;
	private int height = 0;

	public CraftingStations(IGuiHelper guiHelper) {
		this.guiHelper = guiHelper;
		recipeSlots = new ArrayList<>();
		Textures textures = Internal.getTextures();
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

	public void updateLayout(
		List<Consumer<IIngredientAcceptor<?>>> craftingStations,
		ImmutableRect2i recipeArea,
		ImmutableRect2i optionButtonsArea
	) {
		this.recipeSlots.clear();
		Layout layout = calculateLayout(craftingStations.size(), recipeArea, optionButtonsArea);
		left = layout.left();
		top = layout.top();
		width = layout.width();
		height = layout.height();

		if (layout.hasSlots()) {
			for (Consumer<IIngredientAcceptor<?>> craftingStation : craftingStations) {
				int index = this.recipeSlots.size();
				IRecipeSlotDrawable recipeSlot = createSlot(craftingStation, index, layout.maxIngredientsPerColumn());
				this.recipeSlots.add(recipeSlot);
			}
		}
	}

	static Layout calculateLayout(int ingredientCount, ImmutableRect2i recipeArea, ImmutableRect2i optionButtonsArea) {
		if (ingredientCount <= 0) {
			return Layout.EMPTY;
		}

		int availableHeight = recipeArea.getHeight() - optionButtonsArea.getHeight() - 8;
		int borderHeight = (2 * borderSize) + (2 * ingredientBorderSize);
		int maxIngredientsPerColumn = Math.max(1, (availableHeight - borderHeight) / ingredientSize);
		int columnCount = MathUtil.divideCeil(ingredientCount, maxIngredientsPerColumn);
		maxIngredientsPerColumn = MathUtil.divideCeil(ingredientCount, columnCount);

		int width = (2 * ingredientBorderSize) + (borderSize * 2) + (columnCount * ingredientSize);
		int height = (2 * ingredientBorderSize) + (borderSize * 2) + (maxIngredientsPerColumn * ingredientSize);
		int top = recipeArea.getY();
		int left = recipeArea.getX() - width + overlapSize; // overlaps the recipe gui slightly
		return new Layout(left, top, width, height, maxIngredientsPerColumn);
	}

	record Layout(int left, int top, int width, int height, int maxIngredientsPerColumn) {
		private static final Layout EMPTY = new Layout(0, 0, 0, 0, 0);

		private boolean hasSlots() {
			return maxIngredientsPerColumn > 0;
		}
	}

	private IRecipeSlotDrawable createSlot(
		Consumer<IIngredientAcceptor<?>> craftingStation,
		int index,
		int maxIngredientsPerColumn
	) {
		IRecipeSlotDrawable recipeSlotDrawable = guiHelper.createRecipeSlotDrawable(
			RecipeIngredientRole.CRAFTING_STATION,
			craftingStation,
			Set.of(),
			0
		);
		setPosition(recipeSlotDrawable, index, maxIngredientsPerColumn);
		return recipeSlotDrawable;
	}

	private void setPosition(IRecipeSlotDrawable recipeSlotDrawable, int index, int maxIngredientsPerColumn) {
		int column = index / maxIngredientsPerColumn;
		int row = index % maxIngredientsPerColumn;
		recipeSlotDrawable.setPosition(
			left + borderSize + (column * ingredientSize) + ingredientBorderSize,
			top + borderSize + (row * ingredientSize) + ingredientBorderSize
		);
	}

	public Optional<IRecipeSlotDrawable> draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		int ingredientCount = recipeSlots.size();
		if (ingredientCount > 0) {
			int slotWidth = width - (2 * borderSize);
			int slotHeight = height - (2 * borderSize);
			backgroundTab.draw(guiGraphics, this.left, this.top, width, height);
			slotBackground.draw(guiGraphics, this.left + borderSize, this.top + borderSize, slotWidth, slotHeight);

			IRecipeSlotDrawable hovered = null;
			for (IRecipeSlotDrawable recipeSlot : this.recipeSlots) {
				if (recipeSlot.isMouseOver(mouseX, mouseY)) {
					hovered = recipeSlot;
				}
				recipeSlot.draw(guiGraphics, recipeSlot.isMouseOver(mouseX, mouseY));
			}
			return Optional.ofNullable(hovered);
		}
		return Optional.empty();
	}

	private Stream<IRecipeSlotDrawable> getHovered(double mouseX, double mouseY) {
		return this.recipeSlots.stream()
			.filter(recipeSlot -> recipeSlot.isMouseOver(mouseX, mouseY));
	}

	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		return getHovered(mouseX, mouseY)
			.findFirst()
			.map(recipeSlot -> new RecipeSlotUnderMouse(recipeSlot, 0, 0));
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return getHovered(mouseX, mouseY)
			.map(recipeSlot -> recipeSlot.getDisplayedIngredient()
				.map(i -> {
					IElement<?> element = new IngredientElement<>(i);
					return new ClickableIngredientInternal<>(element, recipeSlot::isMouseOver, false, true);
				}))
			.flatMap(Optional::stream);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}
}

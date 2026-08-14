package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.IngredientGridTooltipComponent;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.input.IGuiInputLayer;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Stream;

final class InteractiveIngredientTooltip implements IGuiInputLayer {
	private static final int NAVIGATION_BACKGROUND_PADDING = 2;
	private static final InputConstants.Key LEFT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
	private static final InputConstants.Key RIGHT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);

	private final InteractiveIngredientTooltipController controller;
	private final RecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IIngredientManager ingredientManager;
	private final RecipeSlotClickTargetFactory clickTargetFactory;
	private final IScalableDrawable background;
	private final RecipeSlotUnderMouse sourceSlot;
	private final RecipeSlotTooltipPositioner positioner;
	private final InteractiveIngredientGridTooltipComponent ingredientGrid;
	private final int anchorX;
	private final int anchorY;

	static Optional<InteractiveIngredientTooltip> create(
		InteractiveIngredientTooltipController controller,
		RecipesGui recipesGui,
		FocusUtil focusUtil,
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RecipeSlotClickTargetFactory clickTargetFactory,
		RecipeSlotUnderMouse sourceSlot,
		double mouseX,
		double mouseY
	) {
		List<ITypedIngredient<?>> displayedIngredients = sourceSlot.slot()
			.getDisplayedIngredients()
			.toList();
		if (displayedIngredients.size() <= 1) {
			return Optional.empty();
		}
		return Optional.of(new InteractiveIngredientTooltip(
			controller,
			recipesGui,
			focusUtil,
			ingredientManager,
			clickTargetFactory,
			sourceSlot,
			new InteractiveIngredientGridTooltipComponent(recipeManager, displayedIngredients),
			(int) mouseX,
			(int) mouseY
		));
	}

	private InteractiveIngredientTooltip(
		InteractiveIngredientTooltipController controller,
		RecipesGui recipesGui,
		FocusUtil focusUtil,
		IIngredientManager ingredientManager,
		RecipeSlotClickTargetFactory clickTargetFactory,
		RecipeSlotUnderMouse sourceSlot,
		InteractiveIngredientGridTooltipComponent ingredientGrid,
		int anchorX,
		int anchorY
	) {
		this.controller = controller;
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.ingredientManager = ingredientManager;
		this.clickTargetFactory = clickTargetFactory;
		this.background = Internal.getTextures().getInteractiveIngredientTooltipBackground();
		this.sourceSlot = sourceSlot;
		this.positioner = new RecipeSlotTooltipPositioner();
		this.ingredientGrid = ingredientGrid;
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!this.recipesGui.isOpen()) {
			return false;
		}
		return getNavigationArea().contains(mouseX, mouseY);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Optional<IClickableIngredientInternal<?>> ingredient = this.ingredientGrid
			.getIngredientUnderMouse(mouseX, mouseY)
			.findFirst();
		if (ingredient.isPresent()) {
			return ingredient.stream();
		}
		if (this.sourceSlot.isMouseOver(mouseX, mouseY)) {
			return this.clickTargetFactory.create(this.sourceSlot)
				.stream();
		}
		return Stream.empty();
	}

	private boolean startScrollbarDrag(double mouseX, double mouseY) {
		return this.ingredientGrid.startScrollbarDrag(mouseX, mouseY);
	}

	private boolean dragScrollbar(double mouseY) {
		return this.ingredientGrid.mouseDragged(mouseY);
	}

	private boolean isDraggingScrollbar() {
		return this.ingredientGrid.isDraggingScrollbar();
	}

	private void stopScrollbarDrag() {
		this.ingredientGrid.stopScrollbarDrag();
	}

	@SuppressWarnings("removal")
	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (!this.recipesGui.isOpen()) {
			return;
		}
		JeiTooltip tooltip = new JeiTooltip();
		this.sourceSlot.slot().getTooltip(tooltip);
		replaceIngredientGrid(tooltip, this.ingredientGrid);
		this.ingredientGrid.setMousePosition(mouseX, mouseY);

		ImmutableRect2i navigationArea = getNavigationArea();
		guiGraphics.nextStratum();
		guiGraphics.fill(
			0,
			0,
			guiGraphics.guiWidth(),
			guiGraphics.guiHeight(),
			JeiGuiColors.getColor(GuiColor.INTERACTIVE_INGREDIENT_TOOLTIP_SCREEN_DIM)
		);
		guiGraphics.nextStratum();
		if (!navigationArea.isEmpty()) {
			this.background.draw(
				guiGraphics,
				navigationArea.x(),
				navigationArea.y(),
				navigationArea.width(),
				navigationArea.height()
			);
		}
		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, this.anchorX, this.anchorY, this.positioner);
		this.ingredientGrid.getTypedIngredientUnderMouse(mouseX, mouseY)
			.ifPresent(ingredient -> drawIngredientTooltip(guiGraphics, mouseX, mouseY, ingredient));

		if (getNavigationArea().contains(mouseX, mouseY)) {
			if (this.ingredientGrid.isDraggingScrollbar()) {
				guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
			} else if (this.ingredientGrid.getTypedIngredientUnderMouse(mouseX, mouseY).isPresent() ||
				this.ingredientGrid.isMouseOverScrollbar(mouseX, mouseY)
			) {
				guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
			} else {
				guiGraphics.requestCursor(CursorTypes.ARROW);
			}
		}
	}

	private <T> void drawIngredientTooltip(
		GuiGraphics guiGraphics,
		int mouseX,
		int mouseY,
		ITypedIngredient<T> ingredient
	) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientRenderer<T> ingredientRenderer = this.ingredientManager.getIngredientRenderer(ingredientType);
		JeiTooltip tooltip = new JeiTooltip();
		SafeIngredientUtil.getRichTooltip(tooltip, this.ingredientManager, ingredientRenderer, ingredient);
		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, mouseX, mouseY, ingredient, ingredientRenderer, this.ingredientManager);
	}

	private ImmutableRect2i getNavigationArea() {
		ImmutableRect2i tooltipArea = this.positioner.getTooltipArea();
		if (tooltipArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		return tooltipArea.expandBy(NAVIGATION_BACKGROUND_PADDING);
	}

	private static void replaceIngredientGrid(ITooltipBuilder tooltip, TooltipComponent candidateComponent) {
		List<Either<FormattedText, TooltipComponent>> lines = tooltip.getLines();
		Either<FormattedText, TooltipComponent> replacement = Either.right(candidateComponent);
		boolean replaced = false;
		ListIterator<Either<FormattedText, TooltipComponent>> iterator = lines.listIterator();
		while (iterator.hasNext()) {
			Either<FormattedText, TooltipComponent> line = iterator.next();
			boolean isIngredientGrid = line.right()
				.filter(IngredientGridTooltipComponent.class::isInstance)
				.isPresent();
			if (!isIngredientGrid) {
				continue;
			}
			if (replaced) {
				iterator.remove();
			} else {
				iterator.set(replacement);
				replaced = true;
			}
		}
		if (!replaced) {
			lines.add(replacement);
		}
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
		Screen screen,
		IGuiProperties guiProperties,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		if (!this.controller.isActive(this) || !this.recipesGui.isOpen()) {
			return Optional.empty();
		}

		if (input.is(keyBindings.getCloseRecipeGui())) {
			if (!input.isSimulate()) {
				this.controller.hide(this);
			}
			return Optional.of(this);
		}

		boolean leftClick = input.getKey().equals(LEFT_MOUSE_BUTTON);
		boolean rightClick = input.getKey().equals(RIGHT_MOUSE_BUTTON);
		if (!leftClick && !rightClick) {
			return Optional.empty();
		}

		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();
		if (leftClick && isDraggingScrollbar()) {
			if (!input.isSimulate()) {
				stopScrollbarDrag();
			}
			return Optional.of(this);
		}
		if (leftClick && input.isSimulate() && startScrollbarDrag(mouseX, mouseY)) {
			return Optional.of(this);
		}

		Optional<IClickableIngredientInternal<?>> clicked = getIngredientUnderMouse(mouseX, mouseY)
			.findFirst();
		if (clicked.isEmpty()) {
			return Optional.empty();
		}
		IClickableIngredientInternal<?> ingredient = clicked.get();
		if (!input.isSimulate()) {
			List<RecipeIngredientRole> roles;
			if (leftClick) {
				roles = List.of(RecipeIngredientRole.OUTPUT);
			} else {
				roles = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CRAFTING_STATION);
			}
			ingredient.show(this.recipesGui, this.focusUtil, roles);
		}
		return Optional.of(new SameElementInputHandler(this, ingredient::isMouseOver));
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(
		double mouseX,
		double mouseY,
		double scrollDeltaX,
		double scrollDeltaY
	) {
		if (this.controller.isActive(this) && this.recipesGui.isOpen() && this.ingredientGrid.isMouseOver(mouseX, mouseY)) {
			double scrollDelta = scrollDeltaY;
			if (Math.abs(scrollDeltaX) > Math.abs(scrollDeltaY)) {
				scrollDelta = scrollDeltaX;
			}
			this.ingredientGrid.mouseScrolled(scrollDelta);
			return Optional.of(this);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(
		double mouseX,
		double mouseY,
		InputConstants.Key mouseKey,
		double dragX,
		double dragY
	) {
		if (this.controller.isActive(this) && this.recipesGui.isOpen() && mouseKey.equals(LEFT_MOUSE_BUTTON) && dragScrollbar(mouseY)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	@Override
	public void unfocus() {
		stopScrollbarDrag();
	}

}

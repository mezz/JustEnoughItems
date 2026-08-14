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
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.RecipeSlotCandidatesTooltipComponent;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.input.IGuiInputLayer;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Stream;

final class InteractiveIngredientTooltip implements IGuiInputLayer {
	private static final int SCREEN_DIM_COLOR = 0x40000000;
	private static final int NAVIGATION_BACKGROUND_PADDING = 2;
	private static final InputConstants.Key LEFT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
	private static final InputConstants.Key RIGHT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);

	private final RecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final RecipeSlotClickTargetFactory clickTargetFactory;
	private final IScalableDrawable navigationBackground;

	private boolean visible;
	private @Nullable RecipeSlotUnderMouse sourceSlot;
	private @Nullable RecipeSlotTooltipPositioner positioner;
	private @Nullable InteractiveIngredientGridTooltipComponent candidateComponent;
	private int anchorX;
	private int anchorY;

	InteractiveIngredientTooltip(
		RecipesGui recipesGui,
		FocusUtil focusUtil,
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RecipeSlotClickTargetFactory clickTargetFactory
	) {
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.clickTargetFactory = clickTargetFactory;
		this.navigationBackground = Internal.getTextures().getButtonPressedHighlight();
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void hide() {
		this.visible = false;
		this.sourceSlot = null;
		this.positioner = null;
		this.candidateComponent = null;
	}

	public boolean show(RecipeSlotUnderMouse slotUnderMouse, double mouseX, double mouseY) {
		List<ITypedIngredient<?>> displayedIngredients = slotUnderMouse.slot()
			.getDisplayedIngredients()
			.toList();
		if (displayedIngredients.size() <= 1) {
			return false;
		}

		this.sourceSlot = slotUnderMouse;
		this.positioner = new RecipeSlotTooltipPositioner();
		this.candidateComponent = new InteractiveIngredientGridTooltipComponent(this.recipeManager, displayedIngredients);
		this.anchorX = (int) mouseX;
		this.anchorY = (int) mouseY;
		this.visible = true;
		return true;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!this.recipesGui.isOpen() || !this.visible) {
			return false;
		}
		RecipeSlotTooltipPositioner positioner = this.positioner;
		return positioner != null && getNavigationArea(positioner).contains(mouseX, mouseY);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (!this.visible) {
			return Stream.empty();
		}
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		if (candidateComponent != null) {
			Optional<IClickableIngredientInternal<?>> candidate = candidateComponent
				.getIngredientUnderMouse(mouseX, mouseY)
				.findFirst();
			if (candidate.isPresent()) {
				return candidate.stream();
			}
		}
		RecipeSlotUnderMouse sourceSlot = this.sourceSlot;
		if (sourceSlot != null && sourceSlot.isMouseOver(mouseX, mouseY)) {
			return this.clickTargetFactory.create(sourceSlot)
				.stream();
		}
		return Stream.empty();
	}

	private boolean scrollCandidates(double scrollDeltaY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.mouseScrolled(scrollDeltaY);
	}

	private boolean isMouseOverGrid(double mouseX, double mouseY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.isMouseOver(mouseX, mouseY);
	}

	private boolean startScrollbarDrag(double mouseX, double mouseY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.startScrollbarDrag(mouseX, mouseY);
	}

	private boolean dragScrollbar(double mouseY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.mouseDragged(mouseY);
	}

	private boolean isDraggingScrollbar() {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.isDraggingScrollbar();
	}

	private void stopScrollbarDrag() {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		if (candidateComponent != null) {
			candidateComponent.stopScrollbarDrag();
		}
	}

	@SuppressWarnings("removal")
	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!this.recipesGui.isOpen()) {
			return;
		}
		RecipeSlotUnderMouse sourceSlot = this.sourceSlot;
		RecipeSlotTooltipPositioner positioner = this.positioner;
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		if (!this.visible || sourceSlot == null || positioner == null || candidateComponent == null) {
			return;
		}

		JeiTooltip tooltip = new JeiTooltip();
		sourceSlot.slot().getTooltip(tooltip);
		showCandidateInstruction(tooltip);
		replaceIngredientGrid(tooltip, candidateComponent);
		candidateComponent.setMousePosition(mouseX, mouseY);

		ImmutableRect2i navigationArea = getNavigationArea(positioner);
		guiGraphics.nextStratum();
		guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), SCREEN_DIM_COLOR);
		guiGraphics.nextStratum();
		if (!navigationArea.isEmpty()) {
			this.navigationBackground.draw(
				guiGraphics,
				navigationArea.x(),
				navigationArea.y(),
				navigationArea.width(),
				navigationArea.height()
			);
		}
		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, this.anchorX, this.anchorY, positioner);
		candidateComponent.getTypedIngredientUnderMouse(mouseX, mouseY)
			.ifPresent(ingredient -> drawIngredientTooltip(guiGraphics, mouseX, mouseY, ingredient));

		if (getNavigationArea(positioner).contains(mouseX, mouseY)) {
			if (candidateComponent.isDraggingScrollbar()) {
				guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
			} else if (candidateComponent.getTypedIngredientUnderMouse(mouseX, mouseY).isPresent() ||
				candidateComponent.isMouseOverScrollbar(mouseX, mouseY)
			) {
				guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
			} else {
				guiGraphics.requestCursor(CursorTypes.ARROW);
			}
		}
	}

	private <T> void drawIngredientTooltip(
		GuiGraphicsExtractor guiGraphics,
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

	private static ImmutableRect2i getNavigationArea(RecipeSlotTooltipPositioner positioner) {
		ImmutableRect2i tooltipArea = positioner.getTooltipArea();
		if (tooltipArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		return tooltipArea.expandBy(NAVIGATION_BACKGROUND_PADDING);
	}

	private static void showCandidateInstruction(ITooltipBuilder tooltip) {
		tooltip.getLines().stream()
			.map(line -> line.right())
			.flatMap(Optional::stream)
			.filter(RecipeSlotCandidatesTooltipComponent.class::isInstance)
			.map(RecipeSlotCandidatesTooltipComponent.class::cast)
			.forEach(RecipeSlotCandidatesTooltipComponent::forceVisible);
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
		if (!this.recipesGui.isOpen() || !this.visible) {
			return Optional.empty();
		}

		if (input.is(keyBindings.getCloseRecipeGui())) {
			if (!input.isSimulate()) {
				hide();
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
		if (this.recipesGui.isOpen() && isMouseOverGrid(mouseX, mouseY)) {
			double scrollDelta = scrollDeltaY;
			if (Math.abs(scrollDeltaX) > Math.abs(scrollDeltaY)) {
				scrollDelta = scrollDeltaX;
			}
			scrollCandidates(scrollDelta);
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
		if (this.recipesGui.isOpen() && mouseKey.equals(LEFT_MOUSE_BUTTON) && dragScrollbar(mouseY)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	@Override
	public void unfocus() {
		stopScrollbarDrag();
	}

}

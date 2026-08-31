package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IMouseOverable;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.recipes.RecipeSlotClickTargetFactory;
import mezz.jei.gui.recipes.RecipeSlotTooltipPositioner;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

final class BookmarkPreviewTooltip implements IUserInputHandler, IMouseOverable {
	private static final int BACKGROUND_PADDING = 2;
	private static final InputConstants.Key LEFT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
	private static final InputConstants.Key RIGHT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);

	private final BookmarkPreviewTooltipController controller;
	private final RecipeBookmarkElement<?, ?> element;
	private final BooleanSupplier sourceVisible;
	private final PreviewTooltipComponent<?> component;
	private final IRecipeLayoutDrawable<?> drawable;
	private final RecipeSlotClickTargetFactory clickTargetFactory;
	private final RecipeSlotTooltipPositioner positioner;
	private final IScalableDrawable background;
	private final int anchorX;
	private final int anchorY;

	BookmarkPreviewTooltip(
		BookmarkPreviewTooltipController controller,
		RecipeBookmarkElement<?, ?> element,
		BooleanSupplier sourceVisible,
		PreviewTooltipComponent<?> component,
		int anchorX,
		int anchorY
	) {
		this.controller = controller;
		this.element = element;
		this.sourceVisible = sourceVisible;
		this.component = component;
		this.drawable = component.getRecipeLayout();
		IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
		this.clickTargetFactory = new RecipeSlotClickTargetFactory(
			jeiRuntime.getRecipeManager(),
			Internal.getKeyMappings().getPauseRecipeCycling()::isDown
		);
		this.positioner = new RecipeSlotTooltipPositioner();
		this.background = Internal.getTextures().getInteractiveIngredientTooltipBackground();
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}

	public boolean isSourceVisible() {
		return sourceVisible.getAsBoolean();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return getTooltipArea().contains(mouseX, mouseY);
	}

	private ImmutableRect2i getTooltipArea() {
		ImmutableRect2i tooltipArea = this.positioner.getTooltipArea();
		if (tooltipArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		return tooltipArea.expandBy(BACKGROUND_PADDING);
	}

	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		this.element.getPinnedTooltip(tooltip);
		this.component.setInteractive(mouseX, mouseY);

		ImmutableRect2i tooltipArea = getTooltipArea();
		guiGraphics.nextStratum();
		guiGraphics.fill(
			0,
			0,
			guiGraphics.guiWidth(),
			guiGraphics.guiHeight(),
			JeiGuiColors.getColor(GuiColor.INTERACTIVE_INGREDIENT_TOOLTIP_SCREEN_DIM)
		);
		guiGraphics.nextStratum();
		if (!tooltipArea.isEmpty()) {
			this.background.draw(
				guiGraphics,
				tooltipArea.x(),
				tooltipArea.y(),
				tooltipArea.width(),
				tooltipArea.height()
			);
		}
		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, this.anchorX, this.anchorY, this.positioner);
		this.drawable.drawOverlays(guiGraphics, mouseX, mouseY);

		if (isMouseOver(mouseX, mouseY)) {
			if (this.drawable.getSlotUnderMouse(mouseX, mouseY).isPresent()) {
				guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
			} else {
				guiGraphics.requestCursor(CursorTypes.ARROW);
			}
		}
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
		Screen screen,
		IGuiProperties guiProperties,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		if (!this.controller.isActive(this)) {
			return Optional.empty();
		}

		boolean leftClick = input.getKey().equals(LEFT_MOUSE_BUTTON);
		boolean rightClick = input.getKey().equals(RIGHT_MOUSE_BUTTON);
		if (!leftClick && !rightClick) {
			return Optional.empty();
		}

		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();
		if (!isMouseOver(mouseX, mouseY)) {
			return Optional.empty();
		}

		RecipeSlotUnderMouse slotUnderMouse = this.drawable.getSlotUnderMouse(mouseX, mouseY)
			.orElse(null);
		if (slotUnderMouse == null) {
			// keep clicks on the tooltip itself from reaching the screen behind it
			return Optional.of(this);
		}

		IClickableIngredientInternal<?> ingredient = this.clickTargetFactory.create(
				slotUnderMouse,
				RecipeSlotClickTargetFactory.createMouseOverable(this.drawable, slotUnderMouse)
			)
			.orElse(null);
		if (ingredient == null) {
			return Optional.of(this);
		}
		if (!input.isSimulate()) {
			List<RecipeIngredientRole> roles;
			if (leftClick) {
				roles = List.of(RecipeIngredientRole.OUTPUT);
			} else {
				roles = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CRAFTING_STATION);
			}
			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			FocusUtil focusUtil = new FocusUtil(
				jeiRuntime.getJeiHelpers().getFocusFactory(),
				Internal.getJeiClientConfigs().getClientConfig(),
				jeiRuntime.getIngredientManager()
			);
			ingredient.show(jeiRuntime.getRecipesGui(), focusUtil, roles);
			this.controller.hide();
		}
		return Optional.of(new SameElementInputHandler(this, ingredient::isMouseOver));
	}
}

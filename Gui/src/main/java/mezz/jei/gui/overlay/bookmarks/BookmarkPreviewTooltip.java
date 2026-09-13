package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IMouseOverable;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.recipes.PinnedTooltipRenderer;
import mezz.jei.gui.recipes.RecipeSlotClickTargetFactory;
import mezz.jei.gui.recipes.RecipeTransferButtonController;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

final class BookmarkPreviewTooltip implements IUserInputHandler, IMouseOverable {
	private static final InputConstants.Key LEFT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
	private static final InputConstants.Key RIGHT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);

	private final BookmarkPreviewTooltipController controller;
	private final RecipeBookmarkElement<?, ?> element;
	private final BooleanSupplier sourceVisible;
	private final PreviewTooltipComponent<?> component;
	private final IRecipeLayoutDrawable<?> drawable;
	private final RecipeSlotClickTargetFactory clickTargetFactory;
	private final PinnedTooltipRenderer tooltipRenderer;
	private final IconButton transferButton;
	private final IUserInputHandler transferButtonInputHandler;

	BookmarkPreviewTooltip(
		BookmarkPreviewTooltipController controller,
		RecipeBookmarkElement<?, ?> element,
		BooleanSupplier sourceVisible,
		PreviewTooltipComponent<?> component,
		RecipeTransferService recipeTransferService,
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
		this.tooltipRenderer = new PinnedTooltipRenderer(anchorX, anchorY);
		this.transferButton = new IconButton(RecipeTransferButtonController.createForPinnedRecipe(this.drawable, recipeTransferService));
		this.transferButtonInputHandler = this.transferButton.createInputHandler();
	}

	public boolean isSourceVisible() {
		return sourceVisible.getAsBoolean();
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (!this.controller.isActive(this)) {
			return Stream.empty();
		}
		return getClickableIngredientUnderMouse(mouseX, mouseY).stream();
	}

	private Optional<IClickableIngredientInternal<?>> getClickableIngredientUnderMouse(double mouseX, double mouseY) {
		return this.clickTargetFactory.create(this.drawable, mouseX, mouseY);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.tooltipRenderer.isMouseOver(mouseX, mouseY) || this.transferButton.isMouseOver(mouseX, mouseY);
	}

	public void update() {
		this.transferButton.tick();
	}

	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		this.element.getPinnedTooltip(tooltip);
		this.component.setInteractive(mouseX, mouseY, getInteractiveWidth());

		this.tooltipRenderer.draw(guiGraphics, tooltip);
		updateTransferButtonBounds();
		this.transferButton.draw(guiGraphics, mouseX, mouseY, 0.0f);
		this.drawable.drawOverlays(guiGraphics, mouseX, mouseY);
		this.transferButton.drawTooltips(guiGraphics, mouseX, mouseY);

		if (isMouseOver(mouseX, mouseY)) {
			if (this.transferButton.isMouseOver(mouseX, mouseY) ||
				this.drawable.getSlotUnderMouse(mouseX, mouseY).isPresent()
			) {
				guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
			} else {
				guiGraphics.requestCursor(CursorTypes.ARROW);
			}
		}
	}

	private int getInteractiveWidth() {
		if (!this.transferButton.isVisible()) {
			return 0;
		}
		Rect2i buttonArea = this.drawable.getSideButtonArea(0);
		return 2 + buttonArea.getX() + buttonArea.getWidth();
	}

	private void updateTransferButtonBounds() {
		if (!this.transferButton.isVisible()) {
			this.transferButton.updateBounds(ImmutableRect2i.EMPTY);
			return;
		}
		Rect2i recipeArea = this.drawable.getRect();
		Rect2i buttonArea = this.drawable.getSideButtonArea(0);
		this.transferButton.updateBounds(new ImmutableRect2i(
			recipeArea.getX() + buttonArea.getX(),
			recipeArea.getY() + buttonArea.getY(),
			buttonArea.getWidth(),
			buttonArea.getHeight()
		));
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

		Optional<IUserInputHandler> transferButtonHandler = transferButtonInputHandler.handleUserInput(
			screen,
			guiProperties,
			input,
			keyBindings
		);
		if (transferButtonHandler.isPresent()) {
			return transferButtonHandler;
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

		if (this.drawable.getSlotUnderMouse(mouseX, mouseY).isEmpty()) {
			// keep clicks on the tooltip itself from reaching the screen behind it
			return Optional.of(this);
		}

		IClickableIngredientInternal<?> ingredient = getClickableIngredientUnderMouse(mouseX, mouseY)
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

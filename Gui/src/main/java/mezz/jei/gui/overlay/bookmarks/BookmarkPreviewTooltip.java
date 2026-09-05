package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IMouseOverable;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

final class BookmarkPreviewTooltip implements IUserInputHandler, IMouseOverable {
	private static final InputConstants.Key LEFT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
	private static final InputConstants.Key RIGHT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);
	private static final int BACKGROUND_PADDING = 2;
	// Vanilla adds another 400 Z for each tooltip. Keep these additions within 1.19's 1000-deep GUI projection.
	private static final int TOOLTIP_BACKGROUND_Z = 300;
	private static final int NESTED_TOOLTIP_FOREGROUND_Z = 300;

	private final BookmarkPreviewTooltipController controller;
	private final RecipeBookmarkElement<?, ?> element;
	private final BooleanSupplier sourceVisible;
	private final PreviewTooltipComponent<?> component;
	private final IRecipeLayoutDrawable<?> drawable;
	private final IScalableDrawable background;
	private final RecipeTransferButton transferButton;
	private final IUserInputHandler transferButtonInputHandler;
	private final int anchorX;
	private final int anchorY;

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
		this.background = Internal.getTextures().getInteractiveIngredientTooltipBackground();
		this.transferButton = RecipeTransferButton.createForPinnedRecipe(this.drawable, recipeTransferService);
		this.transferButtonInputHandler = this.transferButton.createInputHandler();
		this.anchorX = anchorX;
		this.anchorY = anchorY;
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
		return this.drawable.getSlotUnderMouse(mouseX, mouseY)
			.flatMap(slotUnderMouse -> slotUnderMouse.slot().getDisplayedIngredient()
				.<IClickableIngredientInternal<?>>map(displayedIngredient -> {
					IngredientElement<?> ingredientElement = new IngredientElement<>(displayedIngredient);
					return new ClickableIngredientInternal<>(ingredientElement, slotUnderMouse::isMouseOver, false, true);
				})
			);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return getTooltipArea().contains(mouseX, mouseY) || this.transferButton.isMouseOver(mouseX, mouseY);
	}

	private ImmutableRect2i getTooltipArea() {
		ImmutableRect2i tooltipArea = this.component.getTooltipArea();
		if (tooltipArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		return tooltipArea.expandBy(BACKGROUND_PADDING);
	}

	public void update() {
		this.transferButton.tick();
	}

	public void draw(PoseStack poseStack, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		this.element.getPinnedTooltip(tooltip);
		this.component.setInteractive(mouseX, mouseY, getInteractiveWidth());

		ImmutableRect2i tooltipArea = getTooltipArea();
		Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
		if (screen != null) {
			GuiComponent.fill(
				poseStack,
				0,
				0,
				screen.width,
				screen.height,
				JeiGuiColors.getColor(GuiColor.INTERACTIVE_INGREDIENT_TOOLTIP_SCREEN_DIM)
			);
		}
		poseStack.pushPose();
		{
			poseStack.translate(0, 0, TOOLTIP_BACKGROUND_Z);
			if (!tooltipArea.isEmpty()) {
				this.background.draw(
					poseStack,
					tooltipArea.x(),
					tooltipArea.y(),
					tooltipArea.width(),
					tooltipArea.height()
				);
			}
		}
		poseStack.popPose();
		tooltip.draw(poseStack, this.anchorX, this.anchorY);
		poseStack.pushPose();
		{
			poseStack.translate(0, 0, NESTED_TOOLTIP_FOREGROUND_Z);
			updateTransferButtonBounds();
			this.transferButton.draw(poseStack, mouseX, mouseY, 0.0f);
			this.drawable.drawOverlays(poseStack, mouseX, mouseY);
			this.transferButton.drawTooltips(poseStack, mouseX, mouseY);
		}
		poseStack.popPose();
	}

	private int getInteractiveWidth() {
		if (!this.transferButton.isVisible()) {
			return 0;
		}
		Rect2i buttonArea = this.drawable.getRecipeTransferButtonArea();
		return 2 + buttonArea.getX() + buttonArea.getWidth();
	}

	private void updateTransferButtonBounds() {
		if (!this.transferButton.isVisible()) {
			this.transferButton.updateBounds(ImmutableRect2i.EMPTY);
			return;
		}
		Rect2i recipeArea = this.drawable.getRect();
		Rect2i buttonArea = this.drawable.getRecipeTransferButtonArea();
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
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		if (!this.controller.isActive(this)) {
			return Optional.empty();
		}

		Optional<IUserInputHandler> transferButtonHandler = transferButtonInputHandler.handleUserInput(
			screen,
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
				roles = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST);
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

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
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ApiInputAdapter;
import mezz.jei.common.input.interaction.ConsumedInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.UserInput;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.recipes.RecipeSlotClickTargetFactory;
import mezz.jei.gui.recipes.RecipeTransferButtonController;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

final class BookmarkPreviewTooltip implements IInputTarget, IMouseOverable {
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
	private final RecipeSlotClickTargetFactory clickTargetFactory;
	private final IScalableDrawable background;
	private final IconButton transferButton;
	private final IInputTarget transferButtonInputHandler;
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
		IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
		this.clickTargetFactory = new RecipeSlotClickTargetFactory(
			jeiRuntime.getRecipeManager(),
			Internal.getKeyMappings().getPauseRecipeCycling()::isDown
		);
		this.transferButton = new IconButton(RecipeTransferButtonController.createForPinnedRecipe(this.drawable, recipeTransferService));
		this.background = Internal.getTextures().getInteractiveIngredientTooltipBackground();
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
		return clickTargetFactory.create(drawable, mouseX, mouseY);
	}

	@Override
	public boolean containsMouse(double mouseX, double mouseY) {
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
	public boolean scroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return this.controller.isActive(this) &&
			ApiInputAdapter.handleMouseScrolled(this.drawable.getInputHandler(), mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	@Override
	public Optional<IInputInteraction> beginInput(
		Screen screen,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		if (!this.controller.isActive(this)) {
			return Optional.empty();
		}

		Optional<IInputInteraction> transferButtonHandler = transferButtonInputHandler.beginInput(
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
		if (!containsMouse(mouseX, mouseY)) {
			return Optional.empty();
		}

		if (this.drawable.getSlotUnderMouse(mouseX, mouseY).isEmpty()) {
			// keep clicks on the tooltip itself from reaching the screen behind it
			return Optional.of(ConsumedInput.INSTANCE);
		}

		IClickableIngredientInternal<?> ingredient = getClickableIngredientUnderMouse(mouseX, mouseY)
			.orElse(null);
		if (ingredient == null) {
			return Optional.of(ConsumedInput.INSTANCE);
		}
		return Optional.of(new InputAction(release -> {
				List<RecipeIngredientRole> roles;
				if (leftClick) {
					roles = List.of(RecipeIngredientRole.OUTPUT);
				} else {
					roles = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST);
				}
				IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
				FocusUtil focusUtil = new FocusUtil(
					jeiRuntime.getJeiHelpers().getFocusFactory(),
					Internal.getClientConfigs().getClientConfig(),
					jeiRuntime.getIngredientManager()
				);
				ingredient.show(jeiRuntime.getRecipesGui(), focusUtil, roles);
				this.controller.hide();
			})
			.within(ingredient::isMouseOver));
	}
}

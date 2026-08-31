package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IMouseOverable;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.recipes.PinnedTooltipRenderer;
import mezz.jei.gui.recipes.RecipeSlotClickTargetFactory;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

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
		this.tooltipRenderer = new PinnedTooltipRenderer(anchorX, anchorY);
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
		return this.tooltipRenderer.isMouseOver(mouseX, mouseY);
	}

	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		this.element.getPinnedTooltip(tooltip);
		this.component.setInteractive(mouseX, mouseY);

		this.tooltipRenderer.draw(guiGraphics, tooltip);
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(0, 0, PinnedTooltipRenderer.NESTED_TOOLTIP_FOREGROUND_Z);
			this.drawable.drawOverlays(guiGraphics, mouseX, mouseY);
		}
		poseStack.popPose();
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

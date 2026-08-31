package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IMouseOverable;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

final class BookmarkPreviewTooltip implements IUserInputHandler, IMouseOverable {
	private static final InputConstants.Key LEFT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
	private static final InputConstants.Key RIGHT_MOUSE_BUTTON = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);
	private static final int SCREEN_DIM_COLOR = 0x40000000;

	private final BookmarkPreviewTooltipController controller;
	private final RecipeBookmarkElement<?, ?> element;
	private final PreviewTooltipComponent<?> component;
	private final IRecipeLayoutDrawable<?> drawable;
	private final int anchorX;
	private final int anchorY;

	BookmarkPreviewTooltip(
		BookmarkPreviewTooltipController controller,
		RecipeBookmarkElement<?, ?> element,
		PreviewTooltipComponent<?> component,
		int anchorX,
		int anchorY
	) {
		this.controller = controller;
		this.element = element;
		this.component = component;
		this.drawable = component.getRecipeLayout();
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}

	public boolean isSourceVisible() {
		return this.element.isVisible();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return getTooltipArea().contains(mouseX, mouseY);
	}

	private ImmutableRect2i getTooltipArea() {
		return this.component.getTooltipArea();
	}

	public void draw(PoseStack poseStack, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		this.element.getPinnedTooltip(tooltip);
		this.component.setInteractive(mouseX, mouseY);

		Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
		if (screen != null) {
			GuiComponent.fill(poseStack, 0, 0, screen.width, screen.height, SCREEN_DIM_COLOR);
		}
		tooltip.draw(poseStack, this.anchorX, this.anchorY);
		this.drawable.drawOverlays(poseStack, mouseX, mouseY);
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

		RecipeSlotUnderMouse slotUnderMouse = this.drawable.getSlotUnderMouse(mouseX, mouseY)
			.orElse(null);
		if (slotUnderMouse == null) {
			return Optional.of(this);
		}

		IClickableIngredientInternal<?> ingredient = slotUnderMouse.slot().getDisplayedIngredient()
			.<IClickableIngredientInternal<?>>map(displayedIngredient -> {
				IngredientElement<?> ingredientElement = new IngredientElement<>(displayedIngredient);
				return new ClickableIngredientInternal<>(ingredientElement, slotUnderMouse::isMouseOver, false, true);
			})
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

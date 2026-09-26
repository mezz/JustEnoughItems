package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.handlers.SameElementInputHandler;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RecipeGuiLayouts implements IUserInputHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RecipeSlotClickTargetFactory clickTargetFactory;
	private final List<IRecipeLayoutWithButtons<?>> recipeLayoutsWithButtons = new ArrayList<>();
	@Nullable
	private IUserInputHandler cachedInputHandler;
	private @Nullable ImmutableRect2i viewport;

	RecipeGuiLayouts(RecipeSlotClickTargetFactory clickTargetFactory) {
		this.clickTargetFactory = clickTargetFactory;
	}

	public void updateLayout(ImmutableRect2i recipeLayoutsArea, RecipeGuiGrid grid, @Nullable RecipeGuiScrollState scrollState) {
		this.viewport = null;
		if (scrollState != null) {
			this.viewport = recipeLayoutsArea;
		}
		if (this.recipeLayoutsWithButtons.isEmpty()) {
			return;
		}
		IRecipeLayoutWithButtons<?> firstLayout = this.recipeLayoutsWithButtons.get(0);
		ImmutableRect2i layoutAreaWithBorder = new ImmutableRect2i(firstLayout.getRecipeLayout().getRectWithBorder());
		for (int i = 0; i < recipeLayoutsWithButtons.size(); i++) {
			IRecipeLayoutWithButtons<?> recipeLayoutWithButtons = recipeLayoutsWithButtons.get(i);
			ImmutableRect2i recipeArea = grid.getRecipeArea(i, recipeLayoutsArea, layoutAreaWithBorder.getSize(), recipeLayoutWithButtons.totalWidth());
			int y = recipeArea.y();
			if (scrollState != null) {
				y = recipeLayoutsArea.y() + scrollState.getRecipeY(i);
			}
			recipeLayoutWithButtons.updateBounds(recipeArea.x(), y);
		}
	}

	private IUserInputHandler getRecipeInputHandler() {
		if (cachedInputHandler == null) {
			List<IUserInputHandler> handlers = this.recipeLayoutsWithButtons.stream()
				.map(IRecipeLayoutWithButtons::createUserInputHandler)
				.toList();
			cachedInputHandler = new CombinedInputHandler("RecipeGuiLayouts", handlers);
		}
		return cachedInputHandler;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (!isInsideViewport(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		return getRecipeInputHandler().handleUserInput(screen, input, keyBindings)
			.map(handler -> new SameElementInputHandler(handler, this::isInsideViewport));
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollY) {
		if (!isInsideViewport(mouseX, mouseY)) {
			return Optional.empty();
		}
		return getRecipeInputHandler().handleMouseScrolled(mouseX, mouseY, scrollY);
	}

	@Override
	public void unfocus() {
		getRecipeInputHandler().unfocus();
	}

	public boolean isInsideViewport(double mouseX, double mouseY) {
		return viewport == null || viewport.contains(mouseX, mouseY);
	}

	public void tick(@Nullable AbstractContainerMenu parentContainer) {
		Player player = Minecraft.getInstance().player;
		safeCallOnRecipeLayouts(r -> r.tick(parentContainer, player));
	}

	public void setRecipeLayoutsWithButtons(List<IRecipeLayoutWithButtons<?>> recipeLayoutsWithButtons) {
		if (cachedInputHandler != null) {
			cachedInputHandler.unfocus();
		}
		this.recipeLayoutsWithButtons.clear();
		this.recipeLayoutsWithButtons.addAll(recipeLayoutsWithButtons);
		this.cachedInputHandler = null;
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (!isInsideViewport(mouseX, mouseY)) {
			return Stream.empty();
		}
		return this.recipeLayoutsWithButtons.stream()
			.map(IRecipeLayoutWithButtons::getRecipeLayout)
			.flatMap(recipeLayout -> clickTargetFactory.create(recipeLayout, mouseX, mouseY).stream());
	}

	public Optional<IRecipeLayoutWithButtons<?>> getRecipeLayoutUnderMouse(double mouseX, double mouseY) {
		if (!isInsideViewport(mouseX, mouseY)) {
			return Optional.empty();
		}
		for (IRecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				return Optional.of(recipeLayoutWithButtons);
			}
		}
		return Optional.empty();
	}

	public boolean mouseDragged(double mouseX, double mouseY, InputConstants.Key input, double dragX, double dragY) {
		if (!isInsideViewport(mouseX, mouseY)) {
			return false;
		}
		for (IRecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			if (mouseDragged(recipeLayout, mouseX, mouseY, input, dragX, dragY)) {
				return true;
			}
		}
		return false;
	}

	private <R> boolean mouseDragged(IRecipeLayoutDrawable<R> recipeLayout, double mouseX, double mouseY, InputConstants.Key input, double dragX, double dragY) {
		if (recipeLayout.isMouseOver(mouseX, mouseY)) {
			IJeiInputHandler inputHandler = recipeLayout.getInputHandler();
			return inputHandler.handleMouseDragged(mouseX, mouseY, input, dragX, dragY);
		}
		return false;
	}

	public void mouseMoved(double mouseX, double mouseY) {
		if (!isInsideViewport(mouseX, mouseY)) {
			return;
		}
		for (IRecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				IJeiInputHandler inputHandler = recipeLayout.getInputHandler();
				inputHandler.handleMouseMoved(mouseX, mouseY);
			}
		}
	}

	public Optional<IRecipeLayoutDrawable<?>> draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (viewport == null) {
			return drawContents(guiGraphics, mouseX, mouseY);
		}
		guiGraphics.enableScissor(viewport.x(), viewport.y(), viewport.x() + viewport.width(), viewport.y() + viewport.height());
		try {
			if (!isInsideViewport(mouseX, mouseY)) {
				return drawContents(guiGraphics, Integer.MIN_VALUE, Integer.MIN_VALUE);
			}
			return drawContents(guiGraphics, mouseX, mouseY);
		} finally {
			guiGraphics.disableScissor();
		}
	}

	private Optional<IRecipeLayoutDrawable<?>> drawContents(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		IRecipeLayoutDrawable<?> hoveredLayout = null;

		Minecraft minecraft = Minecraft.getInstance();
		float partialTicks = minecraft.getDeltaFrameTime();

		safeCallOnRecipeLayouts(r -> r.draw(guiGraphics, mouseX, mouseY, partialTicks));

		for (IRecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
				break;
			}
		}
		RenderSystem.disableBlend();
		return Optional.ofNullable(hoveredLayout);
	}

	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (isInsideViewport(mouseX, mouseY)) {
			safeCallOnRecipeLayouts(r -> r.drawTooltips(guiGraphics, mouseX, mouseY));
		}
	}

	private void safeCallOnRecipeLayouts(Consumer<IRecipeLayoutWithButtons<?>> consumer) {
		for (int i = 0; i < recipeLayoutsWithButtons.size(); i++) {
			IRecipeLayoutWithButtons<?> recipeLayoutWithButtons = recipeLayoutsWithButtons.get(i);
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			try {
				consumer.accept(recipeLayoutWithButtons);
			} catch (RuntimeException e) {
				String recipeInfo = ErrorUtil.getRecipeInfo(recipeLayout);
				LOGGER.error("Recipe crashed:\n{}", recipeInfo, e);
				recipeLayoutsWithButtons.set(i, new RecipeLayoutWithButtonsErrored<>(recipeLayout));
				this.cachedInputHandler = null;
			}
		}
	}

	public int getWidth() {
		if (recipeLayoutsWithButtons.isEmpty()) {
			return 0;
		}
		IRecipeLayoutWithButtons<?> first = this.recipeLayoutsWithButtons.get(0);
		return first.totalWidth();
	}
}

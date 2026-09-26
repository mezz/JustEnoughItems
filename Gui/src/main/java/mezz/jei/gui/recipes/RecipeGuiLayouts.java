package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.handlers.SameElementInputHandler;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

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
		IRecipeLayoutWithButtons<?> firstLayout = this.recipeLayoutsWithButtons.getFirst();
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
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (!isInsideViewport(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		return getRecipeInputHandler().handleUserInput(screen, guiProperties, input, keyBindings)
			.map(handler -> new SameElementInputHandler(handler, this::isInsideViewport));
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!isInsideViewport(mouseX, mouseY)) {
			return Optional.empty();
		}
		return getRecipeInputHandler().handleMouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void unfocus() {
		getRecipeInputHandler().unfocus();
	}

	public boolean isInsideViewport(double mouseX, double mouseY) {
		return viewport == null || viewport.contains(mouseX, mouseY);
	}

	public void tick() {
		safeCallOnRecipeLayouts(IRecipeLayoutWithButtons::tick);
	}

	public void setRecipeLayoutsWithButtons(List<IRecipeLayoutWithButtons<?>> recipeLayoutsWithButtons) {
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

	public Optional<IRecipeLayoutDrawable<?>> draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
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

	private Optional<IRecipeLayoutDrawable<?>> drawContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		IRecipeLayoutDrawable<?> hoveredLayout = null;

		Minecraft minecraft = Minecraft.getInstance();
		DeltaTracker deltaTracker = minecraft.getDeltaTracker();
		float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);

		safeCallOnRecipeLayouts(r -> r.draw(guiGraphics, mouseX, mouseY, partialTicks));

		for (IRecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
				break;
			}
		}
		return Optional.ofNullable(hoveredLayout);
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
			}
		}
	}

	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (isInsideViewport(mouseX, mouseY)) {
			safeCallOnRecipeLayouts(r -> r.drawTooltips(guiGraphics, mouseX, mouseY));
		}
	}

	public int getWidth() {
		if (recipeLayoutsWithButtons.isEmpty()) {
			return 0;
		}
		IRecipeLayoutWithButtons<?> first = this.recipeLayoutsWithButtons.getFirst();
		return first.totalWidth();
	}
}

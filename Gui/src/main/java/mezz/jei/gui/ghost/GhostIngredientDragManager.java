package mezz.jei.gui.ghost;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GhostIngredientDragManager {
	private final IRecipeFocusSource source;
	private final IScreenHelper screenHelper;
	private final IIngredientManager ingredientManager;
	private final IClientToggleState toggleState;
	private final List<GhostIngredientReturning<?>> ghostIngredientsReturning = new ArrayList<>();
	@Nullable
	private GhostIngredientDrag<?> ghostIngredientDrag;
	@Nullable
	private ITypedIngredient<?> hoveredIngredient;
	private List<Rect2i> hoveredTargetAreas = List.of();

	public GhostIngredientDragManager(
		IRecipeFocusSource source,
		IScreenHelper screenHelper,
		IIngredientManager ingredientManager,
		IClientToggleState toggleState
	) {
		this.source = source;
		this.screenHelper = screenHelper;
		this.ingredientManager = ingredientManager;
		this.toggleState = toggleState;
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (!(minecraft.screen instanceof AbstractContainerScreen)) { // guiContainer uses drawOnForeground
			drawGhostIngredientHighlights(guiGraphics, mouseX, mouseY);
		}
		if (ghostIngredientDrag != null) {
			ghostIngredientDrag.drawItem(guiGraphics, mouseX, mouseY);
		}
		ghostIngredientsReturning.forEach(returning -> returning.drawItem(guiGraphics));
		ghostIngredientsReturning.removeIf(GhostIngredientReturning::isComplete);
	}

	public void drawOnForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		drawGhostIngredientHighlights(guiGraphics, mouseX, mouseY);
	}

	private void drawGhostIngredientHighlights(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (this.ghostIngredientDrag != null) {
			this.ghostIngredientDrag.drawTargets(guiGraphics, mouseX, mouseY);
		} else {
			ITypedIngredient<?> hovered = this.source.getIngredientUnderMouse(mouseX, mouseY)
				.map(IClickableIngredientInternal::getTypedIngredient)
				.findFirst()
				.orElse(null);
			if (!equals(hovered, this.hoveredIngredient)) {
				this.hoveredIngredient = hovered;
				this.hoveredTargetAreas = getHoveredTargetAreas(hovered);
			}
			if (!this.hoveredTargetAreas.isEmpty() && !toggleState.isCheatItemsEnabled()) {
				GhostIngredientDrag.drawTargets(guiGraphics, mouseX, mouseY, this.hoveredTargetAreas);
			}
		}
	}

	private List<Rect2i> getHoveredTargetAreas(@Nullable ITypedIngredient<?> hovered) {
		if (hovered == null) {
			return List.of();
		}
		Minecraft minecraft = Minecraft.getInstance();
		Screen currentScreen = minecraft.screen;
		if (currentScreen == null) {
			return List.of();
		}
		List<Rect2i> targetAreas = new ArrayList<>();
		List<IGhostIngredientHandler<Screen>> handlers = screenHelper.getGhostIngredientHandlers(currentScreen);
		for (IGhostIngredientHandler<Screen> handler : handlers) {
			if (!handler.shouldHighlightTargets()) {
				continue;
			}
			List<? extends IGhostIngredientHandler.Target<?>> targets = handler.getTargetsTyped(currentScreen, hovered, false);
			for (IGhostIngredientHandler.Target<?> target : targets) {
				Rect2i area = target.getArea();
				targetAreas.add(area);
			}
		}
		return targetAreas;
	}

	private static boolean equals(@Nullable ITypedIngredient<?> a, @Nullable ITypedIngredient<?> b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.getIngredient() == b.getIngredient();
	}

	public void stopDrag() {
		if (this.ghostIngredientDrag != null) {
			this.ghostIngredientDrag.stop();
			this.ghostIngredientDrag = null;
		}
		this.hoveredIngredient = null;
		this.hoveredTargetAreas = List.of();
	}

	private <T extends Screen, V> boolean handleClickGhostIngredient(T currentScreen, IDraggableIngredientInternal<V> clicked, UserInput input) {
		List<IGhostIngredientHandler<T>> handlers = screenHelper.getGhostIngredientHandlers(currentScreen);

		List<GhostIngredientDrag.HandlerData<V>> handlerDataList = new ArrayList<>();
		for (IGhostIngredientHandler<T> handler : handlers) {
			ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
			List<IGhostIngredientHandler.Target<V>> targets = handler.getTargetsTyped(currentScreen, ingredient, true);
			if (!targets.isEmpty()) {
				handlerDataList.add(new GhostIngredientDrag.HandlerData<>(handler, targets));
			}
		}

		if (handlerDataList.isEmpty()) {
			return false;
		}

		ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
		IIngredientType<V> type = ingredient.getType();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		ImmutableRect2i clickedArea = clicked.getArea();
		this.ghostIngredientDrag = new GhostIngredientDrag<>(handlerDataList, ingredientRenderer, ingredient, input.getMouseX(), input.getMouseY(), clickedArea);
		return true;
	}

	public IDragHandler createDragHandler() {
		return new DragHandler();
	}

	private class DragHandler implements IDragHandler {
		@Override
		public Optional<IDragHandler> handleDragStart(Screen screen, UserInput input) {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player == null) {
				return Optional.empty();
			}

			return source.getDraggableIngredientUnderMouse(input.getMouseX(), input.getMouseY())
				.findFirst()
				.flatMap(clicked -> {
					ItemStack mouseItem = player.containerMenu.getCarried();
					if (mouseItem.isEmpty() &&
						handleClickGhostIngredient(screen, clicked, input)) {
						return Optional.of(this);
					}
					return Optional.empty();
				});
		}

		@Override
		public boolean handleDragComplete(Screen screen, UserInput input) {
			if (ghostIngredientDrag == null) {
				return false;
			}
			boolean success = ghostIngredientDrag.onClick(input);
			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			if (!success && GhostIngredientDrag.canStart(ghostIngredientDrag, mouseX, mouseY)) {
				GhostIngredientReturning.create(ghostIngredientDrag, mouseX, mouseY)
					.ifPresent(ghostIngredientsReturning::add);
			}
			ghostIngredientDrag = null;
			hoveredTargetAreas = List.of();
			return success;
		}

		@Override
		public void handleDragCanceled() {
			stopDrag();
		}
	}
}

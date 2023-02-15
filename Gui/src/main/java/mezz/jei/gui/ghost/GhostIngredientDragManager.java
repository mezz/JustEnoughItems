package mezz.jei.gui.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
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
	private final IWorldConfig worldConfig;
	private final List<GhostIngredientReturning<?>> ghostIngredientsReturning = new ArrayList<>();
	@Nullable
	private GhostIngredientDrag<?> ghostIngredientDrag;
	@Nullable
	private ITypedIngredient<?> hoveredIngredient;
	@Nullable
	private List<Rect2i> hoveredTargetAreas;

	public GhostIngredientDragManager(IRecipeFocusSource source, IScreenHelper screenHelper, IIngredientManager ingredientManager, IWorldConfig worldConfig) {
		this.source = source;
		this.screenHelper = screenHelper;
		this.ingredientManager = ingredientManager;
		this.worldConfig = worldConfig;
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (!(minecraft.screen instanceof AbstractContainerScreen)) { // guiContainer uses drawOnForeground
			drawGhostIngredientHighlights(minecraft, poseStack, mouseX, mouseY);
		}
		if (ghostIngredientDrag != null) {
			ghostIngredientDrag.drawItem(minecraft, poseStack, mouseX, mouseY);
		}
		ghostIngredientsReturning.forEach(returning -> returning.drawItem(minecraft, poseStack));
		ghostIngredientsReturning.removeIf(GhostIngredientReturning::isComplete);
	}

	public void drawOnForeground(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		drawGhostIngredientHighlights(minecraft, poseStack, mouseX, mouseY);
	}

	private void drawGhostIngredientHighlights(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (this.ghostIngredientDrag != null) {
			this.ghostIngredientDrag.drawTargets(poseStack, mouseX, mouseY);
		} else {
			ITypedIngredient<?> hovered = this.source.getIngredientUnderMouse(mouseX, mouseY)
				.map(IClickableIngredientInternal::getTypedIngredient)
				.findFirst()
				.orElse(null);
			if (!equals(hovered, this.hoveredIngredient)) {
				this.hoveredIngredient = hovered;
				this.hoveredTargetAreas = null;
				Screen currentScreen = minecraft.screen;
				if (currentScreen != null && hovered != null) {
					screenHelper.getGhostIngredientHandler(currentScreen)
						.filter(IGhostIngredientHandler::shouldHighlightTargets)
						.ifPresent(handler ->
							this.hoveredTargetAreas = handler.getTargetsTyped(currentScreen, hovered, false)
								.stream()
								.map(IGhostIngredientHandler.Target::getArea)
								.toList()
						);
				}
			}
			if (this.hoveredTargetAreas != null && !worldConfig.isCheatItemsEnabled()) {
				GhostIngredientDrag.drawTargets(poseStack, mouseX, mouseY, this.hoveredTargetAreas);
			}
		}
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
		this.hoveredTargetAreas = null;
	}

	private <T extends Screen, V> boolean handleClickGhostIngredient(T currentScreen, IClickableIngredientInternal<V> clicked, UserInput input) {
		return screenHelper.getGhostIngredientHandler(currentScreen)
			.map(handler -> {
				ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
				IIngredientType<V> type = ingredient.getType();

				List<IGhostIngredientHandler.Target<V>> targets = handler.getTargetsTyped(currentScreen, ingredient, true);
				if (targets.isEmpty()) {
					return false;
				}
				IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
				ImmutableRect2i clickedArea = clicked.getArea();
				this.ghostIngredientDrag = new GhostIngredientDrag<>(handler, targets, ingredientRenderer, ingredient, input.getMouseX(), input.getMouseY(), clickedArea);
				return true;
			})
			.orElse(false);
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

			return source.getIngredientUnderMouse(input.getMouseX(), input.getMouseY())
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
			if (!success && GhostIngredientDrag.farEnoughToDraw(ghostIngredientDrag, mouseX, mouseY)) {
				GhostIngredientReturning.create(ghostIngredientDrag, mouseX, mouseY)
					.ifPresent(ghostIngredientsReturning::add);
			}
			ghostIngredientDrag = null;
			hoveredTargetAreas = null;
			return success;
		}

		@Override
		public void handleDragCanceled() {
			stopDrag();
		}
	}
}

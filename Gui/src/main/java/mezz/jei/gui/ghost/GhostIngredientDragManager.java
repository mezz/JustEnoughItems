package mezz.jei.gui.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.api.runtime.IClickedIngredient;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.UserInput;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GhostIngredientDragManager {
	private final IRecipeFocusSource source;
	private final IScreenHelper screenHelper;
	private final IRegisteredIngredients registeredIngredients;
	private final IWorldConfig worldConfig;
	private final List<GhostIngredientReturning<?>> ghostIngredientsReturning = new ArrayList<>();
	@Nullable
	private GhostIngredientDrag<?> ghostIngredientDrag;
	@Nullable
	private ITypedIngredient<?> hoveredIngredient;
	@Nullable
	private List<IGhostIngredientHandler.Target<Object>> hoveredIngredientTargets;

	public GhostIngredientDragManager(IRecipeFocusSource source, IScreenHelper screenHelper, IRegisteredIngredients registeredIngredients, IWorldConfig worldConfig) {
		this.source = source;
		this.screenHelper = screenHelper;
		this.registeredIngredients = registeredIngredients;
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
				.map(IClickedIngredient::getTypedIngredient)
				.findFirst()
				.orElse(null);
			if (!equals(hovered, this.hoveredIngredient)) {
				this.hoveredIngredient = hovered;
				this.hoveredIngredientTargets = null;
				Screen currentScreen = minecraft.screen;
				if (currentScreen != null && hovered != null) {
					screenHelper.getGhostIngredientHandler(currentScreen)
						.filter(IGhostIngredientHandler::shouldHighlightTargets)
						.ifPresent(handler -> {
							this.hoveredIngredientTargets = handler.getTargets(currentScreen, hovered, false);
						});
				}
			}
			if (this.hoveredIngredientTargets != null && !worldConfig.isCheatItemsEnabled()) {
				GhostIngredientDrag.drawTargets(poseStack, mouseX, mouseY, this.hoveredIngredientTargets);
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
		this.hoveredIngredientTargets = null;
	}

	private <T extends Screen, V> boolean handleClickGhostIngredient(T currentScreen, IClickedIngredient<V> clicked, UserInput input) {
		return screenHelper.getGhostIngredientHandler(currentScreen)
			.map(handler -> {
				ITypedIngredient<V> value = clicked.getTypedIngredient();
				V ingredient = value.getIngredient();
				IIngredientType<V> type = value.getType();

				List<IGhostIngredientHandler.Target<V>> targets = handler.getTargets(currentScreen, ingredient, true);
				if (targets.isEmpty()) {
					return false;
				}
				IIngredientRenderer<V> ingredientRenderer = registeredIngredients.getIngredientRenderer(type);
				IImmutableRect2i clickedArea = clicked.getArea().orElse(null);
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
			hoveredIngredientTargets = null;
			return success;
		}

		@Override
		public void handleDragCanceled() {
			stopDrag();
		}
	}
}

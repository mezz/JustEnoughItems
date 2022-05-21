package mezz.jei.gui.ghost;

import com.mojang.blaze3d.matrix.MatrixStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IMouseDragHandler;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.IClickedIngredient;
import net.minecraft.item.ItemStack;

public class GhostIngredientDragManager {
	private final IGhostIngredientDragSource source;
	private final GuiScreenHelper guiScreenHelper;
	private final IngredientManager ingredientManager;
	private final IWorldConfig worldConfig;
	private final List<GhostIngredientReturning<?>> ghostIngredientsReturning = new ArrayList<>();
	private final IMouseDragHandler mouseDragHandler;
	@Nullable
	private GhostIngredientDrag<?> ghostIngredientDrag;
	@Nullable
	private Object hoveredIngredient;
	@Nullable
	private List<IGhostIngredientHandler.Target<Object>> hoveredIngredientTargets;

	public GhostIngredientDragManager(IGhostIngredientDragSource source, GuiScreenHelper guiScreenHelper, IngredientManager ingredientManager, IWorldConfig worldConfig) {
		this.source = source;
		this.guiScreenHelper = guiScreenHelper;
		this.ingredientManager = ingredientManager;
		this.worldConfig = worldConfig;
		this.mouseDragHandler = new MouseDragHandler();
	}

	public void drawTooltips(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		if (!(minecraft.screen instanceof ContainerScreen)) { // guiContainer uses drawOnForeground
			drawGhostIngredientHighlights(minecraft, matrixStack, mouseX, mouseY);
		}
		if (ghostIngredientDrag != null) {
			ghostIngredientDrag.drawItem(minecraft, matrixStack, mouseX, mouseY);
		}
		ghostIngredientsReturning.forEach(returning -> returning.drawItem(minecraft, matrixStack));
		ghostIngredientsReturning.removeIf(GhostIngredientReturning::isComplete);
	}

	public void drawOnForeground(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		drawGhostIngredientHighlights(minecraft, matrixStack, mouseX, mouseY);
	}

	private void drawGhostIngredientHighlights(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		if (this.ghostIngredientDrag != null) {
			this.ghostIngredientDrag.drawTargets(matrixStack, mouseX, mouseY);
		} else {
			IClickedIngredient<?> elementUnderMouse = this.source.getIngredientUnderMouse(mouseX, mouseY);
			Object hovered = elementUnderMouse == null ? null : elementUnderMouse.getValue();
			if (!Objects.equals(hovered, this.hoveredIngredient)) {
				this.hoveredIngredient = hovered;
				this.hoveredIngredientTargets = null;
				Screen currentScreen = minecraft.screen;
				if (currentScreen != null && hovered != null) {
					IGhostIngredientHandler<Screen> handler = guiScreenHelper.getGhostIngredientHandler(currentScreen);
					if (handler != null && handler.shouldHighlightTargets()) {
						this.hoveredIngredientTargets = handler.getTargets(currentScreen, hovered, false);
					}
				}
			}
			if (this.hoveredIngredientTargets != null && !worldConfig.isCheatItemsEnabled()) {
				GhostIngredientDrag.drawTargets(matrixStack, mouseX, mouseY, this.hoveredIngredientTargets);
			}
		}
	}

	public void stopDrag() {
		if (this.ghostIngredientDrag != null) {
			this.ghostIngredientDrag.stop();
			this.ghostIngredientDrag = null;
		}
		this.hoveredIngredient = null;
		this.hoveredIngredientTargets = null;
	}

	private <T extends Screen, V> boolean handleClickGhostIngredient(T currentScreen, IClickedIngredient<V> clicked, double mouseX, double mouseY) {
		IGhostIngredientHandler<T> handler = guiScreenHelper.getGhostIngredientHandler(currentScreen);
		if (handler == null) {
			return false;
		}
		V ingredient = clicked.getValue();
		List<IGhostIngredientHandler.Target<V>> targets = handler.getTargets(currentScreen, ingredient, true);
		if (targets.isEmpty()) {
			return false;
		}
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient);
		Rectangle2d clickedArea = clicked.getArea();
		this.ghostIngredientDrag = new GhostIngredientDrag<>(handler, targets, ingredientRenderer, ingredient, mouseX, mouseY, clickedArea);
		return true;
	}

	public IMouseDragHandler getMouseDragHandler() {
		return mouseDragHandler;
	}

	private class MouseDragHandler implements IMouseDragHandler {
		@Nullable
		@Override
		public IMouseDragHandler handleDragStart(Screen screen, double mouseX, double mouseY) {
			if (screen instanceof RecipesGui) {
				return null;
			}
			IClickedIngredient<?> clicked = source.getIngredientUnderMouse(mouseX, mouseY);
			if (clicked == null) {
				return null;
			}
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft == null) {
				return null;
			}
			ClientPlayerEntity player = minecraft.player;
			if (player == null) {
				return null;
			}
			ItemStack mouseItem = player.inventory.getCarried();
			if (mouseItem.isEmpty() &&
					handleClickGhostIngredient(screen, clicked, mouseX, mouseY)) {
				return this;
			}
			return null;
		}

		@Nullable
		@Override
		public IMouseDragHandler handleDragComplete(Screen screen, double mouseX, double mouseY) {
			if (screen instanceof RecipesGui) {
				return null;
			}
			if (ghostIngredientDrag == null) {
				return null;
			}
			boolean success = ghostIngredientDrag.onClick(mouseX, mouseY, MouseClickState.EXECUTE);
			if (!success && GhostIngredientDrag.farEnoughToDraw(ghostIngredientDrag, mouseX, mouseY)) {
				GhostIngredientReturning<?> returning = GhostIngredientReturning.create(ghostIngredientDrag, mouseX, mouseY);
				ghostIngredientsReturning.add(returning);
			}
			ghostIngredientDrag = null;
			hoveredIngredientTargets = null;
			if (success) {
				return this;
			}
			return null;
		}

		@Override
		public void handleDragCanceled() {
			stopDrag();
		}
	}
}

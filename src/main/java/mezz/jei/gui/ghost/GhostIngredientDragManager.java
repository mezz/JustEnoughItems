package mezz.jei.gui.ghost;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.IClickedIngredient;

public class GhostIngredientDragManager {
	private final IGhostIngredientDragSource source;
	private final GuiScreenHelper guiScreenHelper;
	private final IngredientManager ingredientManager;
	private final IWorldConfig worldConfig;
	private final List<GhostIngredientReturning<?>> ghostIngredientsReturning = new ArrayList<>();
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
	}

	public void drawTooltips(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		if (!(minecraft.currentScreen instanceof ContainerScreen)) { // guiContainer uses drawOnForeground
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
			IIngredientListElement<?> elementUnderMouse = this.source.getElementUnderMouse();
			Object hovered = elementUnderMouse == null ? null : elementUnderMouse.getIngredient();
			if (!Objects.equals(hovered, this.hoveredIngredient)) {
				this.hoveredIngredient = hovered;
				this.hoveredIngredientTargets = null;
				Screen currentScreen = minecraft.currentScreen;
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

	public boolean handleMouseClicked(double mouseX, double mouseY) {
		if (this.ghostIngredientDrag != null) {
			boolean success = this.ghostIngredientDrag.onClick(mouseX, mouseY);
			if (!success) {
				GhostIngredientReturning<?> returning = GhostIngredientReturning.create(this.ghostIngredientDrag, mouseX, mouseY);
				this.ghostIngredientsReturning.add(returning);
			}
			this.ghostIngredientDrag = null;
			return success;
		}
		return false;
	}

	public void stopDrag() {
		if (this.ghostIngredientDrag != null) {
			this.ghostIngredientDrag.stop();
			this.ghostIngredientDrag = null;
		}
	}

	public <T extends Screen, V> boolean handleClickGhostIngredient(T currentScreen, IClickedIngredient<V> clicked) {
		IGhostIngredientHandler<T> handler = guiScreenHelper.getGhostIngredientHandler(currentScreen);
		if (handler != null) {
			V ingredient = clicked.getValue();
			List<IGhostIngredientHandler.Target<V>> targets = handler.getTargets(currentScreen, ingredient, true);
			if (!targets.isEmpty()) {
				IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient);
				Rectangle2d clickedArea = clicked.getArea();
				this.ghostIngredientDrag = new GhostIngredientDrag<>(handler, targets, ingredientRenderer, ingredient, clickedArea);
				clicked.onClickHandled();
				return true;
			}
		}
		return false;
	}
}

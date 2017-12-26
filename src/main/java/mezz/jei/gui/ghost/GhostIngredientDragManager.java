package mezz.jei.gui.ghost;

import mezz.jei.Internal;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GhostIngredientDragManager {
	private final IngredientGrid contents;
	private final List<GhostIngredientReturning> ghostIngredientsReturning = new ArrayList<>();
	@Nullable
	private GhostIngredientDrag<?> ghostIngredientDrag;
	@Nullable
	private Object hoveredIngredient;
	@Nullable
	private List<IGhostIngredientHandler.Target<Object>> hoveredIngredientTargets;

	public GhostIngredientDragManager(IngredientGrid contents) {
		this.contents = contents;
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (!(minecraft.currentScreen instanceof GuiContainer)) { // guiContainer uses drawOnForeground
			drawGhostIngredientHighlights(minecraft, mouseX, mouseY);
		}
		if (ghostIngredientDrag != null) {
			ghostIngredientDrag.drawItem(minecraft, mouseX, mouseY);
		}
		ghostIngredientsReturning.forEach(returning -> returning.drawItem(minecraft));
		ghostIngredientsReturning.removeIf(GhostIngredientReturning::isComplete);
	}

	public void drawOnForeground(Minecraft minecraft, int mouseX, int mouseY) {
		drawGhostIngredientHighlights(minecraft, mouseX, mouseY);
	}

	private void drawGhostIngredientHighlights(Minecraft minecraft, int mouseX, int mouseY) {
		if (this.ghostIngredientDrag != null) {
			this.ghostIngredientDrag.drawTargets(mouseX, mouseY);
		} else {
			IIngredientListElement elementUnderMouse = this.contents.getElementUnderMouse();
			Object hovered = elementUnderMouse == null ? null : elementUnderMouse.getIngredient();
			if (!Objects.equals(hovered, this.hoveredIngredient)) {
				this.hoveredIngredient = hovered;
				this.hoveredIngredientTargets = null;
				JeiRuntime runtime = Internal.getRuntime();
				GuiScreen currentScreen = minecraft.currentScreen;
				if (runtime != null && currentScreen != null && hovered != null) {
					IGhostIngredientHandler<GuiScreen> handler = runtime.getGhostIngredientHandler(currentScreen);
					if (handler != null && handler.shouldHighlightTargets()) {
						this.hoveredIngredientTargets = handler.getTargets(currentScreen, hovered, false);
					}
				}
			}
			if (this.hoveredIngredientTargets != null && !Config.isCheatItemsEnabled()) {
				GhostIngredientDrag.drawTargets(mouseX, mouseY, this.hoveredIngredientTargets);
			}
		}
	}

	public boolean handleMouseClicked(int mouseX, int mouseY) {
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

	public <T extends GuiScreen, V> boolean handleClickGhostIngredient(T currentScreen, IClickedIngredient<V> clicked) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime != null) {
			IGhostIngredientHandler<T> handler = runtime.getGhostIngredientHandler(currentScreen);
			if (handler != null) {
				V ingredient = clicked.getValue();
				List<IGhostIngredientHandler.Target<V>> targets = handler.getTargets(currentScreen, ingredient, true);
				if (!targets.isEmpty()) {
					IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
					IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
					Rectangle clickedArea = clicked.getArea();
					this.ghostIngredientDrag = new GhostIngredientDrag<>(handler, targets, ingredientRenderer, ingredient, clickedArea);
					clicked.onClickHandled();
					return true;
				}
			}
		}
		return false;
	}
}

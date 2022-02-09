package mezz.jei.gui.ghost;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.IClickedIngredient;
import net.minecraft.world.item.ItemStack;

public class GhostIngredientDragManager {
	private final IRecipeFocusSource source;
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

	public GhostIngredientDragManager(IRecipeFocusSource source, GuiScreenHelper guiScreenHelper, IngredientManager ingredientManager, IWorldConfig worldConfig) {
		this.source = source;
		this.guiScreenHelper = guiScreenHelper;
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
			Optional<IClickedIngredient<?>> elementUnderMouse = this.source.getIngredientUnderMouse(mouseX, mouseY);
			Object hovered = elementUnderMouse.map(IClickedIngredient::getValue).orElse(null);
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
				GhostIngredientDrag.drawTargets(poseStack, mouseX, mouseY, this.hoveredIngredientTargets);
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

	private <T extends Screen, V> boolean handleClickGhostIngredient(T currentScreen, IClickedIngredient<V> clicked, UserInput input) {
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
		Rect2i clickedArea = clicked.getArea();
		this.ghostIngredientDrag = new GhostIngredientDrag<>(handler, targets, ingredientRenderer, ingredient, input.getMouseX(), input.getMouseY(), clickedArea);
		return true;
	}

	public IUserInputHandler createInputHandler() {
		return new UserInputHandler();
	}

	private class UserInputHandler implements IUserInputHandler {
		@Override
		public Optional<IUserInputHandler> handleDragStart(Screen screen, UserInput input) {
			if (screen instanceof RecipesGui) {
				return Optional.empty();
			}
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player == null) {
				return Optional.empty();
			}

			return source.getIngredientUnderMouse(input.getMouseX(), input.getMouseY())
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
		public Optional<IUserInputHandler> handleDragComplete(Screen screen, UserInput input) {
			if (screen instanceof RecipesGui) {
				return Optional.empty();
			}
			if (ghostIngredientDrag == null) {
				return Optional.empty();
			}
			boolean success = ghostIngredientDrag.onClick(input);
			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			if (!success && GhostIngredientDrag.farEnoughToDraw(ghostIngredientDrag, mouseX, mouseY)) {
				GhostIngredientReturning<?> returning = GhostIngredientReturning.create(ghostIngredientDrag, mouseX, mouseY);
				ghostIngredientsReturning.add(returning);
			}
			ghostIngredientDrag = null;
			hoveredIngredientTargets = null;
			if (success) {
				return Optional.of(this);
			}
			return Optional.empty();
		}

		@Override
		public void handleDragCanceled() {
			stopDrag();
		}
	}
}

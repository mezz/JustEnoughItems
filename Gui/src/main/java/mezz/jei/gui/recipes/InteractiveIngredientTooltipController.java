package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IGuiInputLayer;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

final class InteractiveIngredientTooltipController implements IGuiInputLayer {
	private final RecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final RecipeSlotClickTargetFactory clickTargetFactory;

	private @Nullable InteractiveIngredientTooltip activeTooltip;

	public InteractiveIngredientTooltipController(
		RecipesGui recipesGui,
		FocusUtil focusUtil,
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RecipeSlotClickTargetFactory clickTargetFactory
	) {
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.clickTargetFactory = clickTargetFactory;
	}

	public boolean isVisible() {
		return this.activeTooltip != null;
	}

	boolean isActive(InteractiveIngredientTooltip tooltip) {
		return this.activeTooltip == tooltip;
	}

	public void hide() {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			activeTooltip.unfocus();
			this.activeTooltip = null;
		}
	}

	void hide(InteractiveIngredientTooltip tooltip) {
		if (isActive(tooltip)) {
			hide();
		}
	}

	public boolean show(RecipeSlotUnderMouse sourceSlot, double mouseX, double mouseY) {
		Optional<InteractiveIngredientTooltip> tooltip = InteractiveIngredientTooltip.create(
			this,
			this.recipesGui,
			this.focusUtil,
			this.recipeManager,
			this.ingredientManager,
			this.clickTargetFactory,
			sourceSlot,
			mouseX,
			mouseY
		);
		if (tooltip.isEmpty()) {
			return false;
		}
		hide();
		this.activeTooltip = tooltip.get();
		return true;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		return activeTooltip != null && activeTooltip.isMouseOver(mouseX, mouseY);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Stream.empty();
		}
		return activeTooltip.getIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			activeTooltip.draw(guiGraphics, mouseX, mouseY);
		}
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
		Screen screen,
		IGuiProperties guiProperties,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleUserInput(screen, guiProperties, input, keyBindings);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(
		double mouseX,
		double mouseY,
		double scrollDeltaX,
		double scrollDeltaY
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(
		double mouseX,
		double mouseY,
		InputConstants.Key mouseKey,
		double dragX,
		double dragY
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleMouseDragged(mouseX, mouseY, mouseKey, dragX, dragY);
	}

	@Override
	public void unfocus() {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			activeTooltip.unfocus();
		}
	}
}

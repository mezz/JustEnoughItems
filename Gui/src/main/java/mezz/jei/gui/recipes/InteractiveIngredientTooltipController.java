package mezz.jei.gui.recipes;

import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IPinnedTooltipHolder;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.InputArea;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.input.PinnedTooltipManager;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

final class InteractiveIngredientTooltipController implements IGuiInputLayer, IPinnedTooltipHolder, IRecipeFocusSource {
	private final RecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IGuiHelper guiHelper;
	private final IIngredientManager ingredientManager;
	private final RecipeSlotClickTargetFactory clickTargetFactory;

	private @Nullable InteractiveIngredientTooltip activeTooltip;

	public InteractiveIngredientTooltipController(
		RecipesGui recipesGui,
		FocusUtil focusUtil,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RecipeSlotClickTargetFactory clickTargetFactory
	) {
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.guiHelper = guiHelper;
		this.ingredientManager = ingredientManager;
		this.clickTargetFactory = clickTargetFactory;
	}

	InputArea createInputArea() {
		return InputArea.builder("Recipe tooltip", this)
			.blockUnhandledMouseInput()
			.controls(this)
			.build();
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
			activeTooltip.resetInput();
			this.activeTooltip = null;
			PinnedTooltipManager.closed(this);
		}
	}

	void hide(InteractiveIngredientTooltip tooltip) {
		if (isActive(tooltip)) {
			hide();
		}
	}

	public boolean show(
		RecipeSlotUnderMouse sourceSlot,
		IMouseOverable sourceMouseOverable,
		double mouseX,
		double mouseY
	) {
		Optional<InteractiveIngredientTooltip> tooltip = InteractiveIngredientTooltip.create(
			this,
			this.recipesGui,
			this.focusUtil,
			this.guiHelper,
			this.ingredientManager,
			this.clickTargetFactory,
			sourceSlot,
			sourceMouseOverable,
			mouseX,
			mouseY
		);
		if (tooltip.isEmpty()) {
			return false;
		}
		hide();
		this.activeTooltip = tooltip.get();
		PinnedTooltipManager.opened(this);
		return true;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		return activeTooltip != null && activeTooltip.isMouseOver(mouseX, mouseY);
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Stream.empty();
		}
		return activeTooltip.getIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			PinnedTooltipManager.draw(this, () -> activeTooltip.draw(guiGraphics, mouseX, mouseY));
		}
	}

	public void registerInputCommands(InputCommands commands, IInternalKeyMappings keys) {
		commands.add("Close ingredient tooltip", input -> input.is(keys.getCloseRecipeGui()), (screen, input) -> {
			InteractiveIngredientTooltip tooltip = activeTooltip;
			if (tooltip == null) {
				return Optional.empty();
			}
			return Optional.of(InputAction.run(() -> hide(tooltip)));
		});
	}

	@Override
	public Optional<IInputInteraction> beginInput(
		Screen screen,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.beginInput(screen, input, keyBindings);
	}

	@Override
	public boolean scroll(
		double mouseX,
		double mouseY,
		double scrollDeltaX,
		double scrollDeltaY
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return false;
		}
		return activeTooltip.scroll(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	@Override
	public void resetInput() {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			activeTooltip.resetInput();
		}
	}
}

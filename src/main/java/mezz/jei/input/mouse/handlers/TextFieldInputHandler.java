package mezz.jei.input.mouse.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.config.KeyBindings;
import mezz.jei.events.DebugRestartJeiEvent;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.TextHistory;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.MinecraftForge;

import java.util.Optional;

public class TextFieldInputHandler implements IUserInputHandler {
	private final GuiTextFieldFilter textFieldFilter;

	public TextFieldInputHandler(GuiTextFieldFilter textFieldFilter) {
		this.textFieldFilter = textFieldFilter;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (handleUserInputBoolean(input)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	private boolean handleUserInputBoolean(UserInput input) {
		if (input.is(KeyBindings.enterKey) || input.is(KeyBindings.escapeKey)) {
			return handleSetFocused(input, false);
		}

		if (input.is(KeyBindings.focusSearch)) {
			return handleSetFocused(input, true);
		}

		if (input.is(KeyBindings.hoveredClearSearchBar) &&
			textFieldFilter.isMouseOver(input.getMouseX(), input.getMouseY())
		) {
			return handleHoveredClearSearchBar(input);
		}

		if (input.callVanilla(
			textFieldFilter::isMouseOver,
			textFieldFilter::mouseClicked,
			textFieldFilter::keyPressed
		)) {
			return true;
		}

		if (input.is(KeyBindings.previousSearch)) {
			return handleNavigateHistory(input, TextHistory.Direction.PREVIOUS);
		}

		if (input.is(KeyBindings.nextSearch)) {
			return handleNavigateHistory(input, TextHistory.Direction.NEXT);
		}

		if (input.is(KeyBindings.reloadJeiOverTextFilter)) {
			MinecraftForge.EVENT_BUS.post(new DebugRestartJeiEvent());
		}

		// If we can handle this input as a typed char,
		// treat it as handled to prevent other handlers from using it.
		return textFieldFilter.canConsumeInput() && input.isAllowedChatCharacter();
	}

	private boolean handleSetFocused(UserInput input, boolean focused) {
		if (textFieldFilter.isFocused() != focused) {
			if (!input.isSimulate()) {
				textFieldFilter.setFocused(focused);
			}
			return true;
		}
		return false;
	}

	private boolean handleHoveredClearSearchBar(UserInput input) {
		if (!input.isSimulate()) {
			textFieldFilter.setValue("");
			textFieldFilter.setFocused(true);
		}
		return true;
	}

	private boolean handleNavigateHistory(UserInput input, TextHistory.Direction direction) {
		if (textFieldFilter.isFocused()) {
			return textFieldFilter.getHistory(direction)
				.map(newText -> {
					if (!input.isSimulate()) {
						textFieldFilter.setValue(newText);
					}
					return true;
				})
				.orElse(false);
		}

		return false;
	}

	@Override
	public void handleMouseClickedOut(InputConstants.Key input) {
		textFieldFilter.setFocused(false);
	}
}

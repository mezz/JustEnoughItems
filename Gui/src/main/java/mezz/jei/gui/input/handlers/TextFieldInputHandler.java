package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.core.util.TextHistory;
import mezz.jei.gui.input.GuiTextFieldFilter;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class TextFieldInputHandler implements IUserInputHandler {
	private final GuiTextFieldFilter textFieldFilter;

	public TextFieldInputHandler(GuiTextFieldFilter textFieldFilter) {
		this.textFieldFilter = textFieldFilter;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (handleUserInputBoolean(input, keyBindings)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	private boolean handleUserInputBoolean(UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getEnterKey()) || input.is(keyBindings.getEscapeKey())) {
			return handleSetFocused(input, false);
		}

		if (input.is(keyBindings.getFocusSearch())) {
			return handleSetFocused(input, true);
		}

		if (input.is(keyBindings.getHoveredClearSearchBar()) &&
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

		if (input.is(keyBindings.getPreviousSearch())) {
			return handleNavigateHistory(input, TextHistory.Direction.PREVIOUS);
		}

		if (input.is(keyBindings.getNextSearch())) {
			return handleNavigateHistory(input, TextHistory.Direction.NEXT);
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

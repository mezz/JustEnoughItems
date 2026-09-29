package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ConsumedInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.util.TextHistory;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.input.ITextInputHandler;
import mezz.jei.gui.input.InputArea;
import mezz.jei.gui.input.InputCommands;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class SearchInputLayer implements IGuiInputLayer, ITextInputHandler {
	private final GuiTextFieldFilter textFieldFilter;
	private final BooleanSupplier active;

	public SearchInputLayer(GuiTextFieldFilter textFieldFilter, BooleanSupplier active) {
		this.textFieldFilter = textFieldFilter;
		this.active = active;
	}

	@Override
	public void update(double mouseX, double mouseY) {
		if (active.getAsBoolean()) {
			textFieldFilter.updateCompletion((int) mouseX, (int) mouseY);
		} else {
			textFieldFilter.hideCompletion();
			clearFocus();
		}
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (active.getAsBoolean()) {
			textFieldFilter.drawCompletion(guiGraphics);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return active.getAsBoolean() &&
			(textFieldFilter.isMouseOver(mouseX, mouseY) || textFieldFilter.isCompletionMouseOver(mouseX, mouseY));
	}

	/** Creates input handling for the search field and its suggestions. */
	public InputArea createInputArea(IInternalKeyMappings keys) {
		InputCommands commands = new InputCommands();
		commands.add("Focus Search", keys.getFocusSearch(), active, () -> textFieldFilter.setFocused(true));
		return InputArea.builder("Search", this)
			.blockUnhandledMouseInput()
			.controls(this)
			.commands(commands)
			.build();
	}

	@Override
	public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!active.getAsBoolean()) {
			return Optional.empty();
		}
		if (textFieldFilter.isCompletionVisible()) {
			if (input.is(keys.getEnterKey()) || input.is(keys.getTabKey())) {
				return Optional.of(InputAction.run(textFieldFilter::acceptCompletion));
			}
			if (input.is(keys.getPreviousSearch())) {
				return Optional.of(InputAction.run(() -> textFieldFilter.moveCompletion(-1)));
			}
			if (input.is(keys.getNextSearch())) {
				return Optional.of(InputAction.run(() -> textFieldFilter.moveCompletion(1)));
			}
			if (input.is(keys.getEscapeKey())) {
				return Optional.of(InputAction.run(textFieldFilter::closeCompletion));
			}
			if (input.isMouseInput() && textFieldFilter.isCompletionMouseOver(input.getMouseX(), input.getMouseY())) {
				return Optional.of(new InputAction(release -> textFieldFilter.handleCompletionClick(release.getMouseX(), release.getMouseY()))
					.within(textFieldFilter::isCompletionMouseOver));
			}
		}
		if (hasKeyboardFocus() && (input.is(keys.getEnterKey()) || input.is(keys.getEscapeKey()))) {
			return Optional.of(InputAction.run(this::clearFocus));
		}
		if (input.is(keys.getHoveredClearSearchBar()) && textFieldFilter.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.of(InputAction.run(() -> {
					textFieldFilter.setValue("");
					textFieldFilter.setFocused(true);
				})
				.within(textFieldFilter::isMouseOver));
		}
		boolean searchFieldClick = input.ifMouseEvent((event, ignoredDoubleClick) -> textFieldFilter.isActive() &&
			textFieldFilter.isMouseOver(event.x(), event.y()) &&
			event.button() == InputConstants.MOUSE_BUTTON_LEFT
		);
		if (searchFieldClick) {
			return Optional.of(new InputAction(release -> {
					UserInput click = input.withMousePosition(release.getMouseX(), release.getMouseY());
					if (click.ifMouseEvent(textFieldFilter::mouseClicked)) {
						textFieldFilter.setFocused(true);
					}
				})
				.within(textFieldFilter::isMouseOver)
				.when(active));
		}
		if (!hasKeyboardFocus()) {
			return Optional.empty();
		}
		if (input.ifKeyboardEvent(textFieldFilter::keyPressed)) {
			return Optional.of(ConsumedInput.INSTANCE);
		}
		if (input.is(keys.getPreviousSearch())) {
			return prepareSearchHistoryAction(TextHistory.Direction.PREVIOUS);
		}
		if (input.is(keys.getNextSearch())) {
			return prepareSearchHistoryAction(TextHistory.Direction.NEXT);
		}
		if (textFieldFilter.canConsumeInput() && input.isAllowedChatCharacter()) {
			return Optional.of(ConsumedInput.INSTANCE);
		}
		return Optional.empty();
	}

	private Optional<IInputInteraction> prepareSearchHistoryAction(TextHistory.Direction direction) {
		return textFieldFilter.getHistory(direction).map(text -> InputAction.run(() -> textFieldFilter.setValue(text)));
	}

	@Override
	public void clearFocus() {
		textFieldFilter.setFocused(false);
	}

	@Override
	public void resetInput() {
		clearFocus();
	}

	@Override
	public boolean scroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		if (!active.getAsBoolean() || !textFieldFilter.isCompletionMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (scrollDeltaY < 0) {
			textFieldFilter.scrollCompletion(1);
		} else if (scrollDeltaY > 0) {
			textFieldFilter.scrollCompletion(-1);
		}
		return true;
	}

	@Override
	public boolean hasKeyboardFocus() {
		return active.getAsBoolean() && textFieldFilter.isFocused();
	}

	@Override
	public boolean onCharTyped(CharacterEvent event) {
		return textFieldFilter.charTyped(event);
	}
}

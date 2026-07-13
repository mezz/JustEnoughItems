package mezz.jei.gui.config.screen;

import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.Supplier;

final class ConfigValueSelectorInputHandler implements IUserInputHandler {
	private final Supplier<ConfigValueSelector<?>> valueSelectorSupplier;
	private final Runnable valueSelectorCloser;
	private final Runnable layoutUpdater;

	ConfigValueSelectorInputHandler(
		Supplier<ConfigValueSelector<?>> valueSelectorSupplier,
		Runnable valueSelectorCloser,
		Runnable layoutUpdater
	) {
		this.valueSelectorSupplier = valueSelectorSupplier;
		this.valueSelectorCloser = valueSelectorCloser;
		this.layoutUpdater = layoutUpdater;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		ConfigValueSelector<?> valueSelector = valueSelectorSupplier.get();
		if (valueSelector == null || !input.is(keyBindings.getLeftClick())) {
			return Optional.empty();
		}

		if (valueSelector.isMouseOver(input.getMouseX(), input.getMouseY())) {
			if (valueSelector.onMouseClicked(input)) {
				if (!input.isSimulate()) {
					closeValueSelector();
					layoutUpdater.run();
				}
				return Optional.of(this);
			}
			return Optional.empty();
		}

		if (!input.isSimulate()) {
			closeValueSelector();
		}
		return Optional.of(this);
	}

	private void closeValueSelector() {
		valueSelectorCloser.run();
	}
}

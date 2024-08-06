package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.KeyNameUtil;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserInputRouter {
	private static final Logger LOGGER = LogManager.getLogger();

	private final String debugName;
	private final CombinedInputHandler combinedInputHandler;
	private final Map<InputConstants.Key, IUserInputHandler> pending = new HashMap<>();

	public UserInputRouter(String debugName, IUserInputHandler... inputHandlers) {
		this.debugName = debugName;
		this.combinedInputHandler = new CombinedInputHandler(debugName, inputHandlers);
	}

	public boolean handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (DebugConfig.isDebugInputsEnabled()) {
			LOGGER.debug("{} received user input: {}", debugName, input);
		}
		return switch (input.getInputType()) {
			case IMMEDIATE -> handleImmediateClick(screen, input, keyBindings);
			case SIMULATE -> handleSimulateClick(screen, input, keyBindings);
			case EXECUTE -> handleExecuteClick(screen, input, keyBindings);
		};
	}

	/*
	 * A vanilla click or key-down will be handled immediately.
	 * We do not track the mousedDown for it,
	 * the first handler to use it will be the "winner", the rest will get a clicked-out.
	 */
	private boolean handleImmediateClick(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		IUserInputHandler oldClick = this.pending.remove(input.getKey());
		if (oldClick != null) {
			if (DebugConfig.isDebugInputsEnabled()) {
				LOGGER.debug("{} canceled previous user input: {}", debugName, oldClick);
			}
		}

		return this.combinedInputHandler.handleUserInput(screen, input, keyBindings)
			.map(callback -> {
				if (DebugConfig.isDebugInputsEnabled()) {
					LOGGER.debug("{} immediate click handled by: {}\n{}", debugName, callback, input);
				}
				return true;
			})
			.orElse(false);
	}

	/*
	 * For JEI-controlled clicks.
	 * JEI activates clicks when the player clicks down on it and releases the mouse on the same element.
	 *
	 * In the first click pass, it is a "simulate" to check if the handler can handle the click,
	 * and it will be added to mousedDown.
	 * In the second pass, all handlers that were in mousedDown will be sent the real click.
	 */
	private boolean handleSimulateClick(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		IUserInputHandler oldClick = this.pending.remove(input.getKey());
		if (oldClick != null) {
			if (DebugConfig.isDebugInputsEnabled()) {
				LOGGER.debug("{} canceled pending user input: {}", debugName, oldClick);
			}
		}

		return this.combinedInputHandler.handleUserInput(screen, input, keyBindings)
			.map(callback -> {
				this.pending.put(input.getKey(), callback);
				if (DebugConfig.isDebugInputsEnabled()) {
					LOGGER.debug("{} click successfully simulated by: {}\n{}", debugName, callback, input);
				}
				return true;
			})
			.orElse(false);
	}

	private boolean handleExecuteClick(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		return Optional.ofNullable(this.pending.remove(input.getKey()))
			.flatMap(inputHandler -> inputHandler.handleUserInput(screen, input, keyBindings))
			.map(callback -> {
				if (DebugConfig.isDebugInputsEnabled()) {
					LOGGER.debug("{} click successfully executed by: {}\n{}", debugName, callback, input);
				}
				return true;
			})
			.orElse(false);
	}

	public void handleGuiChange() {
		if (DebugConfig.isDebugInputsEnabled()) {
			LOGGER.debug("{}: The GUI has changed, clearing all pending clicks", debugName);
		}
		this.combinedInputHandler.unfocus();
		this.pending.clear();
	}

	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return this.combinedInputHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta)
			.map(callback -> {
				if (DebugConfig.isDebugInputsEnabled()) {
					LOGGER.debug("{} scroll handled by: {}", debugName, callback);
				}
				return true;
			})
			.orElse(false);
	}

	@Override
	public String toString() {
		String pendingString = pending.entrySet().stream()
			.map(e -> KeyNameUtil.getKeyDisplayName(e.getKey()) + ": " + e.getValue())
			.collect(Collectors.joining(", ", "[", "]"));

		return "UserInputRouter{" +
			"debugName='" + debugName + '\'' +
			", combinedInputHandler=" + combinedInputHandler +
			", pending=" + pendingString +
		'}';
	}
}

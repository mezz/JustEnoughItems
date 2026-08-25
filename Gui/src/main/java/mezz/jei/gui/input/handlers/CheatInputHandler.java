package mezz.jei.gui.input.handlers;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.GiveAmount;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class CheatInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IIngredientManager ingredientManager;
	private final IClientToggleState toggleState;
	private final CommandUtil commandUtil;

	public CheatInputHandler(
		CombinedRecipeFocusSource focusSource,
		IClientConfig clientConfig,
		IIngredientManager ingredientManager,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection
	) {
		this.focusSource = focusSource;
		this.ingredientManager = ingredientManager;
		this.toggleState = toggleState;
		this.commandUtil = new CommandUtil(clientConfig, serverConnection);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (!toggleState.isCheatItemsEnabled() ||
			!(screen instanceof AbstractContainerScreen<?>)
		) {
			return Optional.empty();
		}

		if (input.is(keyBindings.getCheatItemStack())) {
			Optional<IUserInputHandler> handler = handleGive(input, keyBindings, GiveAmount.MAX);
			if (handler.isPresent()) {
				return handler;
			}
		}

		if (input.is(keyBindings.getCheatOneItem())) {
			return handleGive(input, keyBindings, GiveAmount.ONE);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleGive(UserInput input, IInternalKeyMappings keyBindings, GiveAmount giveAmount) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.<IUserInputHandler>mapMulti((clicked, consumer) -> {
				ItemStack itemStack = clicked.getCheatItemStack(ingredientManager);
				if (!itemStack.isEmpty()) {
					if (!input.isSimulate()) {
						commandUtil.giveStack(itemStack, giveAmount);
					}
					consumer.accept(new SameElementInputHandler(this, clicked::isMouseOver));
				}
			})
			.findFirst();
	}
}

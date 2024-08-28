package mezz.jei.gui.input.handlers;

import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.FocusUtil;
import mezz.jei.gui.util.GiveAmount;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class FocusInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IRecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IIngredientManager ingredientManager;
	private final IClientToggleState toggleState;
	private final CommandUtil commandUtil;

	public FocusInputHandler(
		CombinedRecipeFocusSource focusSource,
		IRecipesGui recipesGui,
		FocusUtil focusUtil,
		IClientConfig clientConfig,
		IIngredientManager ingredientManager,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection
	) {
		this.focusSource = focusSource;
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.ingredientManager = ingredientManager;
		this.toggleState = toggleState;
		this.commandUtil = new CommandUtil(clientConfig, serverConnection);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		Optional<IUserInputHandler> handledClick = handleClick(input, keyBindings);
		if (handledClick.isPresent()) {
			return handledClick;
		}

		if (toggleState.isCheatItemsEnabled()) {
			if (screen instanceof AbstractContainerScreen) {
				if (input.is(keyBindings.getCheatItemStack())) {
					Optional<IUserInputHandler> handler = handleGive(input, keyBindings, GiveAmount.MAX);
					if (handler.isPresent()) {
						return handler;
					}
				}

				if (input.is(keyBindings.getCheatOneItem())) {
					Optional<IUserInputHandler> handler = handleGive(input, keyBindings, GiveAmount.ONE);
					if (handler.isPresent()) {
						return handler;
					}
				}
			}
		}

		if (input.is(keyBindings.getShowRecipe())) {
			return handleShow(input, List.of(RecipeIngredientRole.OUTPUT), keyBindings);
		}

		if (input.is(keyBindings.getShowUses())) {
			return handleShow(input, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST), keyBindings);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleClick(UserInput input, IInternalKeyMappings keyBindings) {
		List<IClickableIngredientInternal<?>> ingredientUnderMouse = focusSource.getIngredientUnderMouse(input, keyBindings)
			.toList();

		for (IClickableIngredientInternal<?> clicked : ingredientUnderMouse) {
			IElement<?> element = clicked.getElement();
			if (element.handleClick(input, keyBindings)) {
				IUserInputHandler result = new SameElementInputHandler(this, clicked::isMouseOver);
				return Optional.of(result);
			}
		}
		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleShow(UserInput input, List<RecipeIngredientRole> roles, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.filter(clicked -> clicked.getElement().isVisible())
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					IElement<?> element = clicked.getElement();
					element.show(recipesGui, focusUtil, roles);
				}
				return new SameElementInputHandler(this, clicked::isMouseOver);
			});
	}

	private Optional<IUserInputHandler> handleGive(UserInput input, IInternalKeyMappings keyBindings, GiveAmount giveAmount) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.<IUserInputHandler>mapMulti((clicked, consumer) -> {
				ItemStack itemStack = clicked.getCheatItemStack(ingredientManager);
				if (!itemStack.isEmpty()) {
					if (!input.isSimulate()) {
						commandUtil.giveStack(itemStack, giveAmount);
					}
					IUserInputHandler handler = new SameElementInputHandler(this, clicked::isMouseOver);
					consumer.accept(handler);
				}
			})
			.findFirst();
	}
}

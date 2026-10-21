package mezz.jei.gui.input.handlers;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.common.ingredients.TypedIngredientUtil;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IngredientFocusSource;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.input.PinnedTooltipManager;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.FocusUtil;
import mezz.jei.gui.util.GiveAmount;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Shortcuts for showing recipes, hiding ingredients, and giving items. */
public final class IngredientCommands {
	private final IngredientFocusSource focusSource;
	private final IInternalKeyMappings keys;
	private final RecipesGui recipes;
	private final FocusUtil focusUtil;
	private final IIngredientManager ingredients;
	private final IWorldConfig toggles;
	private final IEditModeConfig editMode;
	private final CommandUtil commandUtil;

	public IngredientCommands(
		IngredientFocusSource focusSource,
		IInternalKeyMappings keys,
		RecipesGui recipes,
		FocusUtil focusUtil,
		IIngredientManager ingredients,
		IWorldConfig toggles,
		IEditModeConfig editMode,
		IClientConfig config,
		IConnectionToServer connection
	) {
		this.focusSource = focusSource;
		this.keys = keys;
		this.recipes = recipes;
		this.focusUtil = focusUtil;
		this.ingredients = ingredients;
		this.toggles = toggles;
		this.editMode = editMode;
		this.commandUtil = new CommandUtil(config, connection);
	}

	public void registerInputCommands(InputCommands commands) {
		commands.add(
			"Open focused ingredient",
			input -> input.is(keys.getEnterKey()),
			(screen, input) -> prepareKeyboardRecipeLookupAction(input)
		);
		commands.add(
			"Hide ingredient",
			input -> input.is(keys.getToggleHideIngredient()),
			(screen, input) -> prepareToggleIngredientVisibilityAction(input, IEditModeConfig.HideMode.SINGLE)
		);
		commands.add(
			"Hide ingredient variants",
			input -> input.is(keys.getToggleWildcardHideIngredient()),
			(screen, input) -> prepareToggleIngredientVisibilityAction(input, IEditModeConfig.HideMode.WILDCARD)
		);
		commands.add(
			"Give stack to inventory",
			input -> input.is(keys.getCheatItemStackToInventory()),
			(screen, input) -> prepareGiveIngredientToInventoryAction(input, GiveAmount.MAX)
		);
		commands.add(
			"Give item to inventory",
			input -> input.is(keys.getCheatOneItemToInventory()),
			(screen, input) -> prepareGiveIngredientToInventoryAction(input, GiveAmount.ONE)
		);
		commands.add(
			"Give stack",
			input -> input.is(keys.getCheatItemStack()),
			(screen, input) -> prepareGiveIngredientAction(screen, input, GiveAmount.MAX)
		);
		commands.add(
			"Give item",
			input -> input.is(keys.getCheatOneItem()),
			(screen, input) -> prepareGiveIngredientAction(screen, input, GiveAmount.ONE)
		);
		commands.add(
			"Ingredient action",
			ignored -> true,
			(ignoredScreen, input) -> prepareElementAction(input)
		);
		commands.add(
			"Show recipes",
			input -> PinnedTooltipManager.matchesInput(input.getKey(), keys.getShowRecipe(), keys.getPauseRecipeCycling()),
			(screen, input) -> prepareRecipeLookupAction(input, List.of(RecipeIngredientRole.OUTPUT))
		);
		commands.add(
			"Share ingredient",
			input -> input.is(keys.getShareToChat()),
			(screen, input) -> prepareShareIngredientAction(input)
		);
		commands.add(
			"Show uses",
			input -> PinnedTooltipManager.matchesInput(input.getKey(), keys.getShowUses(), keys.getPauseRecipeCycling()),
			(screen, input) -> prepareRecipeLookupAction(input, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST))
		);
	}

	private Optional<IInputInteraction> prepareElementAction(UserInput input) {
		for (IClickableIngredientInternal<?> clicked : focusSource.getIngredientForInput(input, keys).toList()) {
			Optional<InputAction> action = clicked.getElement().prepareAction(input, keys);
			if (action.isPresent()) {
				return Optional.of(action.get().within(clicked::isMouseOver).when(clicked.getElement()::isVisible));
			}
		}
		return Optional.empty();
	}

	private Optional<IInputInteraction> prepareToggleIngredientVisibilityAction(UserInput input, IEditModeConfig.HideMode mode) {
		if (!toggles.isEditModeEnabled()) {
			return Optional.empty();
		}
		return focusSource.getIngredientForInput(input, keys).findFirst()
			.map(clicked -> createIngredientAction(clicked, () -> toggleIngredientVisibility(clicked.getTypedIngredient(), mode))
				.when(toggles::isEditModeEnabled));
	}

	private void toggleIngredientVisibility(ITypedIngredient<?> ingredient, IEditModeConfig.HideMode mode) {
		if (editMode.getIngredientHiddenUsingConfigFile(ingredient).contains(mode)) {
			editMode.showIngredientUsingConfigFile(ingredient, mode);
		} else {
			editMode.hideIngredientUsingConfigFile(ingredient, mode);
		}
	}

	private Optional<IInputInteraction> prepareGiveIngredientAction(Screen screen, UserInput input, GiveAmount amount) {
		if (!(screen instanceof AbstractContainerScreen<?>)) {
			return Optional.empty();
		}
		return prepareGiveAction(
			focusSource.getIngredientForInput(input, keys).filter(IClickableIngredientInternal::canClickToCheat),
			stack -> commandUtil.giveStack(stack, amount));
	}

	private Optional<IInputInteraction> prepareGiveIngredientToInventoryAction(UserInput input, GiveAmount amount) {
		return prepareGiveAction(
			focusSource.getIngredientForInput(input, keys),
			stack -> commandUtil.giveStack(stack, amount, GiveMode.INVENTORY));
	}

	private Optional<IInputInteraction> prepareGiveAction(
		Stream<IClickableIngredientInternal<?>> clickedIngredients,
		Consumer<ItemStack> giveStack
	) {
		if (!toggles.isCheatItemsEnabled()) {
			return Optional.empty();
		}
		for (IClickableIngredientInternal<?> clicked : clickedIngredients.toList()) {
			ItemStack stack = TypedIngredientUtil.getCheatItemStack(ingredients, clicked.getTypedIngredient());
			if (!stack.isEmpty()) {
				return Optional.of(createIngredientAction(clicked, () -> giveStack.accept(stack)).when(toggles::isCheatItemsEnabled));
			}
		}
		return Optional.empty();
	}

	private Optional<IInputInteraction> prepareKeyboardRecipeLookupAction(UserInput input) {
		if (!input.isKeyboardInput() || !focusSource.hasKeyboardFocus()) {
			return Optional.empty();
		}
		return prepareRecipeLookupAction(input, List.of(RecipeIngredientRole.OUTPUT));
	}

	private Optional<IInputInteraction> prepareRecipeLookupAction(UserInput input, List<RecipeIngredientRole> roles) {
		return focusSource.getIngredientForInput(input, keys)
			.filter(clicked -> clicked.getElement().isVisible())
			.findFirst()
			.map(clicked -> createIngredientAction(clicked, () -> clicked.show(recipes, focusUtil, roles)));
	}

	private Optional<IInputInteraction> prepareShareIngredientAction(UserInput input) {
		return focusSource.getIngredientForInput(input, keys)
			.filter(clicked -> clicked.getElement().isVisible())
			.findFirst()
			.map(clicked -> createIngredientAction(clicked, () -> shareIngredientToChat(clicked.getTypedIngredient())));
	}

	private void shareIngredientToChat(ITypedIngredient<?> ingredient) {
		String text = JeiChatItemLinks.createLinkMarker(ingredient, ingredients);
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.tell(() -> minecraft.setScreen(new ChatScreen(text)));
	}

	private static InputAction createIngredientAction(IClickableIngredientInternal<?> clicked, Runnable action) {
		return InputAction.run(action).within(clicked::isMouseOver).when(clicked.getElement()::isVisible);
	}
}

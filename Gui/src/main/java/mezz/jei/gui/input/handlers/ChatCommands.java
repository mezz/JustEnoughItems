package mezz.jei.gui.input.handlers;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.gui.input.InputArea;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class ChatCommands {
	private final IRecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IScreenHelper screenHelper;
	private final IBookmarkManager bookmarkManager;

	public ChatCommands(
		IRecipesGui recipesGui,
		FocusUtil focusUtil,
		IScreenHelper screenHelper,
		IBookmarkManager bookmarkManager
	) {
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.screenHelper = screenHelper;
		this.bookmarkManager = bookmarkManager;
	}

	/** Creates the shortcuts for ingredients linked in chat. */
	public InputArea createInputArea(IInternalKeyMappings keys) {
		InputCommands commands = new InputCommands();
		registerInputCommands(commands, keys);
		return InputArea.builder("Chat links", (x, y) -> Minecraft.getInstance().gui.screen() instanceof ChatScreen)
			.commands(commands)
			.build();
	}

	private void registerInputCommands(InputCommands commands, IInternalKeyMappings keys) {
		commands.add(
			"Chat recipe",
			input -> input.is(keys.getShowRecipe()),
			(screen, input) -> prepareChatLinkAction(screen, input, Action.SHOW_RECIPE)
		);
		commands.add(
			"Chat uses",
			input -> input.is(keys.getShowUses()),
			(screen, input) -> prepareChatLinkAction(screen, input, Action.SHOW_USES)
		);
		commands.add(
			"Chat bookmark",
			input -> input.is(keys.getBookmark()),
			(screen, input) -> prepareChatLinkAction(screen, input, Action.BOOKMARK)
		);
	}

	private Optional<IInputInteraction> prepareChatLinkAction(Screen screen, UserInput input, Action action) {
		if (!(screen instanceof ChatScreen chat)) {
			return Optional.empty();
		}
		return getHoveredIngredient(chat, input.getMouseX(), input.getMouseY())
			.map(ingredient -> new InputAction(
				release -> executeChatLinkAction(ingredient, action),
				release -> getHoveredIngredient(chat, release.getMouseX(), release.getMouseY()).orElse(null) == ingredient));
	}

	private Optional<ITypedIngredient<?>> getHoveredIngredient(ChatScreen chatScreen, double mouseX, double mouseY) {
		return screenHelper.getClickableIngredientUnderMouse(chatScreen, mouseX, mouseY)
			.<ITypedIngredient<?>>map(ChatCommands::getTypedIngredient)
			.findFirst();
	}

	private static ITypedIngredient<?> getTypedIngredient(IClickableIngredient<?> clickableIngredient) {
		return clickableIngredient.getTypedIngredient();
	}

	private void executeChatLinkAction(ITypedIngredient<?> typedIngredient, Action action) {
		switch (action) {
			case SHOW_RECIPE -> showIngredientRecipes(typedIngredient, List.of(RecipeIngredientRole.OUTPUT));
			case SHOW_USES -> showIngredientRecipes(typedIngredient, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CRAFTING_STATION));
			case BOOKMARK -> bookmarkManager.add(typedIngredient);
		}
	}

	private void showIngredientRecipes(ITypedIngredient<?> typedIngredient, List<RecipeIngredientRole> roles) {
		IngredientElement<?> element = new IngredientElement<>(typedIngredient);
		element.show(recipesGui, focusUtil, roles);
	}

	private enum Action {
		/** Show recipes producing the linked ingredient. */
		SHOW_RECIPE,
		/** Show recipes using the linked ingredient. */
		SHOW_USES,
		/** Add the linked ingredient to bookmarks. */
		BOOKMARK
	}
}

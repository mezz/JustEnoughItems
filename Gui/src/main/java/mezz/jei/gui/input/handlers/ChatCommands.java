package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.chat.JeiChatItemLinkHover;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.util.ImmutableRect2i;
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
	private final IIngredientManager ingredientManager;

	public ChatCommands(
		IRecipesGui recipesGui,
		FocusUtil focusUtil,
		IScreenHelper screenHelper,
		IBookmarkManager bookmarkManager,
		IIngredientManager ingredientManager
	) {
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.screenHelper = screenHelper;
		this.bookmarkManager = bookmarkManager;
		this.ingredientManager = ingredientManager;
	}

	/** Creates the shortcuts for ingredients linked in chat. */
	public InputArea createInputArea(IInternalKeyMappings keys) {
		InputCommands commands = new InputCommands();
		registerInputCommands(commands, keys);
		return InputArea.builder("Chat links", (x, y) -> Minecraft.getInstance().screen instanceof ChatScreen)
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
		// Minecraft handles explicit chat actions, including JEI's own recipe command.
		if (input.ifMouseEvent((event, doubleClick) -> event.button() == InputConstants.MOUSE_BUTTON_LEFT) &&
			JeiChatItemLinkHover.getHoveredStyle(chat, input.getMouseX(), input.getMouseY())
			.filter(style -> style.getClickEvent() != null).isPresent()
		) {
			return Optional.empty();
		}
		return getHoveredIngredient(chat, input.getMouseX(), input.getMouseY())
			.map(ingredient -> createChatLinkAction(chat, ingredient, action));
	}

	private Optional<IClickableIngredient<?>> getHoveredIngredient(ChatScreen chatScreen, double mouseX, double mouseY) {
		return screenHelper.getClickableIngredientUnderMouse(chatScreen, mouseX, mouseY)
			.findFirst();
	}

	private <T> InputAction createChatLinkAction(ChatScreen chat, IClickableIngredient<T> clickableIngredient, Action action) {
		ITypedIngredient<T> ingredient = clickableIngredient.getTypedIngredient();
		var type = ingredient.getType();
		var helper = ingredientManager.getIngredientHelper(type);
		Object uid = helper.getUid(ingredient, UidContext.Ingredient);
		ImmutableRect2i area = new ImmutableRect2i(clickableIngredient.getArea());
		return new InputAction(
			release -> executeChatLinkAction(ingredient, action),
			release -> canExecuteChatLinkAction(chat, release, area, ingredient, uid));
	}

	private <T> boolean canExecuteChatLinkAction(ChatScreen chat, UserInput release, ImmutableRect2i area, ITypedIngredient<T> ingredient, Object uid) {
		if (release.isKeyboardInput()) {
			return true;
		}
		double mouseX = release.getMouseX();
		double mouseY = release.getMouseY();
		if (!area.contains(mouseX, mouseY)) {
			return false;
		}
		IClickableIngredient<?> hoveredIngredient = getHoveredIngredient(chat, mouseX, mouseY).orElse(null);
		if (hoveredIngredient == null) {
			return false;
		}
		return matchesIngredient(ingredient, hoveredIngredient.getTypedIngredient(), uid);
	}

	private <T> boolean matchesIngredient(ITypedIngredient<T> ingredient, ITypedIngredient<?> current, Object uid) {
		var type = ingredient.getType();
		ITypedIngredient<T> currentIngredient = current.cast(type);
		if (currentIngredient == null) {
			return false;
		}
		var helper = ingredientManager.getIngredientHelper(type);
		return uid.equals(helper.getUid(currentIngredient, UidContext.Ingredient));
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

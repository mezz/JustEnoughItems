package mezz.jei.gui.startup;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.input.ClientInputHandler;
import mezz.jei.gui.input.GuiContainerWrapper;
import mezz.jei.gui.input.GuiInputController;
import mezz.jei.gui.input.GuiInputScene;
import mezz.jei.gui.input.IngredientFocusSource;
import mezz.jei.gui.input.InputArea;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.input.handlers.BookmarkCommands;
import mezz.jei.gui.input.handlers.ChatCommands;
import mezz.jei.gui.input.handlers.IngredientCommands;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.util.CheatModeUtil;
import mezz.jei.gui.util.FocusUtil;

/**
 * Input areas and Minecraft event handling for JEI's screens.
 *
 * @param scene the input areas, ordered from front to back
 * @param handler sends Minecraft input events to JEI's input controller
 */
public record JeiInputSetup(GuiInputScene scene, ClientInputHandler handler) {
	public static JeiInputSetup create(
		IngredientListOverlay ingredientListOverlay,
		BookmarkOverlay bookmarkOverlay,
		RecipesGui recipesGui,
		BookmarkList bookmarkList,
		IBookmarkManager bookmarkManager,
		IScreenHelper screenHelper,
		IIngredientManager ingredientManager,
		IFocusFactory focusFactory,
		FocusUtil focusUtil,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IEditModeConfig editModeConfig,
		IConnectionToServer serverConnection,
		IInternalKeyMappings keyMappings
	) {
		var searchInputLayer = ingredientListOverlay.getSearchInputLayer();
		InputArea searchInputArea = searchInputLayer.createInputArea(keyMappings);
		InputArea recipeTooltipInputArea = recipesGui.createTooltipInputArea();
		InputArea bookmarkTooltipInputArea = bookmarkOverlay.createTooltipInputArea();
		InputArea recipeInputArea = recipesGui.createInputArea(keyMappings);
		InputArea ingredientInputArea = ingredientListOverlay.createInputArea(keyMappings);
		InputArea bookmarkInputArea = bookmarkOverlay.createInputArea(keyMappings);
		GuiContainerWrapper containerWrapper = new GuiContainerWrapper(screenHelper);
		InputArea containerInputArea = containerWrapper.createInputArea(recipesGui, focusFactory);
		ChatCommands chatCommands = new ChatCommands(recipesGui, focusUtil, screenHelper, bookmarkManager);
		InputArea chatInputArea = chatCommands.createInputArea(keyMappings);
		GuiInputScene inputScene = new GuiInputScene(
			searchInputArea,
			recipeTooltipInputArea,
			bookmarkTooltipInputArea,
			recipeInputArea,
			ingredientInputArea,
			bookmarkInputArea,
			containerInputArea,
			chatInputArea
		);

		InputCommands commands = new InputCommands(screen -> screenHelper.getGuiProperties(screen).isPresent());
		IngredientFocusSource focusSource = new IngredientFocusSource(inputScene);
		IngredientCommands ingredientCommands = new IngredientCommands(
			focusSource,
			keyMappings,
			recipesGui,
			focusUtil,
			ingredientManager,
			toggleState,
			editModeConfig,
			clientConfig,
			serverConnection
		);
		ingredientCommands.registerInputCommands(commands);
		BookmarkCommands bookmarkCommands = new BookmarkCommands(
			focusSource,
			bookmarkList,
			bookmarkOverlay,
			inputScene,
			clientConfig,
			recipesGui
		);
		bookmarkCommands.registerInputCommands(commands, keyMappings);
		commands.add(
			"Toggle Overlay",
			keyMappings.getToggleOverlay(),
			() -> true,
			toggleState::toggleOverlayEnabled
		);
		commands.add(
			"Toggle Bookmark Overlay",
			keyMappings.getToggleBookmarkOverlay(),
			() -> true,
			toggleState::toggleBookmarkEnabled
		);
		commands.add(
			"Toggle Cheat Mode",
			keyMappings.getToggleCheatMode(),
			() -> true,
			() -> CheatModeUtil.toggleCheatMode(toggleState)
		);
		commands.add(
			"Toggle Edit Mode",
			keyMappings.getToggleEditMode(),
			() -> true,
			toggleState::toggleEditModeEnabled
		);

		GuiInputController controller = new GuiInputController(inputScene, commands, searchInputLayer, keyMappings);
		ClientInputHandler handler = new ClientInputHandler(controller, keyMappings);
		return new JeiInputSetup(inputScene, handler);
	}
}

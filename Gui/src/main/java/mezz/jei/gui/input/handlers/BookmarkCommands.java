package mezz.jei.gui.input.handlers;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.input.GuiInputScene;
import mezz.jei.gui.input.IngredientFocusSource;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.input.PinnedTooltipManager;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipesGui;

import java.util.Optional;

public class BookmarkCommands {
	private final IngredientFocusSource focusSource;
	private final BookmarkList bookmarkList;
	private final BookmarkOverlay bookmarkOverlay;
	private final GuiInputScene inputScene;
	private final IClientConfig clientConfig;
	private final RecipesGui recipesGui;

	public BookmarkCommands(
		IngredientFocusSource focusSource,
		BookmarkList bookmarkList,
		BookmarkOverlay bookmarkOverlay,
		GuiInputScene inputScene,
		IClientConfig clientConfig,
		RecipesGui recipesGui
	) {
		this.focusSource = focusSource;
		this.bookmarkList = bookmarkList;
		this.bookmarkOverlay = bookmarkOverlay;
		this.inputScene = inputScene;
		this.clientConfig = clientConfig;
		this.recipesGui = recipesGui;
	}

	public void registerInputCommands(InputCommands commands, IInternalKeyMappings keys) {
		commands.add(
			"Bookmark",
			input -> PinnedTooltipManager.matchesInput(input.getKey(), keys.getBookmark(), keys.getPauseRecipeCycling()),
			(screen, input) -> prepareBookmarkAction(input, keys)
		);
	}

	private Optional<IInputInteraction> prepareBookmarkAction(UserInput input, IInternalKeyMappings keys) {
		Optional<IInputInteraction> recipe = prepareRecipeBookmarkAction(input);
		if (recipe.isPresent()) {
			return recipe;
		}
		return prepareIngredientBookmarkAction(input, keys);
	}

	private Optional<IInputInteraction> prepareRecipeBookmarkAction(UserInput input) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();
		if (!inputScene.isTarget(recipesGui, mouseX, mouseY)) {
			return Optional.empty();
		}
		Optional<IRecipeLayoutWithButtons<?>> layoutWithButtons = recipesGui.getRecipeLayoutUnderMouse(mouseX, mouseY);
		if (layoutWithButtons.isEmpty()) {
			return Optional.empty();
		}

		IRecipeLayoutWithButtons<?> recipeLayoutWithButtons = layoutWithButtons.get();
		RecipeBookmark<?, ?> recipeBookmark = recipeLayoutWithButtons.getRecipeBookmark();
		if (recipeBookmark == null) {
			return Optional.empty();
		}

		IRecipeLayoutDrawable<?> layout = recipeLayoutWithButtons.getRecipeLayout();
		Optional<RecipeSlotUnderMouse> slotUnderMouse = layout.getSlotUnderMouse(mouseX, mouseY);
		if (!shouldBookmarkRecipe(slotUnderMouse, clientConfig.bookmarkOutputAsRecipe().get())) {
			return Optional.empty();
		}

		return Optional.of(InputAction.run(() -> bookmarkList.toggleBookmark(recipeBookmark)).within(layout::isMouseOver));
	}

	static boolean shouldBookmarkRecipe(Optional<RecipeSlotUnderMouse> slotUnderMouse, boolean bookmarkOutputAsRecipe) {
		return slotUnderMouse
			.map(slot -> shouldBookmarkRecipe(slot.slot().getRole(), bookmarkOutputAsRecipe))
			.orElse(true);
	}

	static boolean shouldBookmarkRecipe(RecipeIngredientRole role, boolean bookmarkOutputAsRecipe) {
		return role == RecipeIngredientRole.OUTPUT && bookmarkOutputAsRecipe;
	}

	private Optional<IInputInteraction> prepareIngredientBookmarkAction(UserInput input, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.map(clicked -> new InputAction(release -> bookmarkList.onElementBookmarked(clicked.getElement(), release, bookmarkOverlay))
				.within(clicked::isMouseOver).when(clicked.getElement()::isVisible));
	}
}

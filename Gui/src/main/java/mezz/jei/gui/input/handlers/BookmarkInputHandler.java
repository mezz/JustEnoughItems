package mezz.jei.gui.input.handlers;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class BookmarkInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final BookmarkList bookmarkList;
	private final BookmarkOverlay bookmarkOverlay;
	private final IClientConfig clientConfig;
	private final RecipesGui recipesGui;

	public BookmarkInputHandler(
		CombinedRecipeFocusSource focusSource,
		BookmarkList bookmarkList,
		BookmarkOverlay bookmarkOverlay,
		IClientConfig clientConfig,
		RecipesGui recipesGui
	) {
		this.focusSource = focusSource;
		this.bookmarkList = bookmarkList;
		this.bookmarkOverlay = bookmarkOverlay;
		this.clientConfig = clientConfig;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getBookmark())) {
			Optional<IUserInputHandler> recipeHandler = handleRecipeBookmark(input);
			if (recipeHandler.isPresent()) {
				return recipeHandler;
			}
			return handleIngredientBookmark(input, keyBindings);
		}
		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleRecipeBookmark(UserInput input) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();
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
		if (!shouldBookmarkRecipe(slotUnderMouse, clientConfig.bookmarkOutputAsRecipeEnabled().getValue())) {
			return Optional.empty();
		}

		if (!input.isSimulate()) {
			bookmarkList.toggleBookmark(recipeBookmark);
		}
		return Optional.of(new SameElementInputHandler(this, layout::isMouseOver));
	}

	static boolean shouldBookmarkRecipe(Optional<RecipeSlotUnderMouse> slotUnderMouse, boolean bookmarkOutputAsRecipeEnabled) {
		return slotUnderMouse
			.map(slot -> shouldBookmarkRecipe(slot.slot().getRole(), bookmarkOutputAsRecipeEnabled))
			.orElse(true);
	}

	static boolean shouldBookmarkRecipe(RecipeIngredientRole role, boolean bookmarkOutputAsRecipeEnabled) {
		return role == RecipeIngredientRole.OUTPUT && bookmarkOutputAsRecipeEnabled;
	}

	private Optional<IUserInputHandler> handleIngredientBookmark(UserInput input, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.flatMap(clicked -> {
				if (input.isSimulate() ||
					bookmarkList.onElementBookmarked(clicked.getElement(), input, bookmarkOverlay)
				) {
					IUserInputHandler handler = new SameElementInputHandler(this, clicked::isMouseOver);
					return Optional.of(handler);
				}
				return Optional.empty();
			});
	}
}

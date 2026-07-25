package mezz.jei.gui.input.handlers;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class BookmarkInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final BookmarkList bookmarkList;
	private final BookmarkOverlay bookmarkOverlay;
	private final IClientConfig clientConfig;
	private final IIngredientManager ingredientManager;
	private final IRecipesGui recipesGui;

	public BookmarkInputHandler(CombinedRecipeFocusSource focusSource, BookmarkList bookmarkList, BookmarkOverlay bookmarkOverlay, IClientConfig clientConfig, IIngredientManager ingredientManager, IRecipesGui recipesGui) {
		this.focusSource = focusSource;
		this.bookmarkList = bookmarkList;
		this.bookmarkOverlay = bookmarkOverlay;
		this.clientConfig = clientConfig;
		this.ingredientManager = ingredientManager;
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
		Optional<IRecipeLayoutDrawable<?>> recipeLayout = recipesGui.getRecipeLayoutUnderMouse(mouseX, mouseY);
		if (recipeLayout.isEmpty()) {
			return Optional.empty();
		}

		IRecipeLayoutDrawable<?> layout = recipeLayout.get();
		Optional<RecipeSlotUnderMouse> slotUnderMouse = layout.getSlotUnderMouse(mouseX, mouseY);
		if (slotUnderMouse.isPresent()) {
			RecipeIngredientRole role = slotUnderMouse.get().slot().getRole();
			if (role != RecipeIngredientRole.OUTPUT || !clientConfig.isBookmarkOutputAsRecipeEnabled()) {
				return Optional.empty();
			}
		}

		RecipeBookmark<?, ?> recipeBookmark = RecipeBookmark.create(layout, ingredientManager);
		if (recipeBookmark == null) {
			return Optional.empty();
		}

		if (!input.isSimulate()) {
			bookmarkList.toggleBookmark(recipeBookmark);
		}
		return Optional.of(new SameElementInputHandler(this, layout::isMouseOver));
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

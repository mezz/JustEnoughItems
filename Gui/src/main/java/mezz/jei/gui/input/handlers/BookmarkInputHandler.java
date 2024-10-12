package mezz.jei.gui.input.handlers;

import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class BookmarkInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final BookmarkList bookmarkList;

	public BookmarkInputHandler(CombinedRecipeFocusSource focusSource, BookmarkList bookmarkList) {
		this.focusSource = focusSource;
		this.bookmarkList = bookmarkList;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getForceBookmark())) {
			return handleBookmark(input, keyBindings, true);
		}
		else if (input.is(keyBindings.getBookmark())) {
			return handleBookmark(input, keyBindings, false);
		}
		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleBookmark(UserInput input, IInternalKeyMappings keyBindings, boolean shouldForce) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.flatMap(clicked -> {
				if (input.isSimulate() ||
					bookmarkList.onElementBookmarked(clicked.getElement(), shouldForce)
				) {
					IUserInputHandler handler = new SameElementInputHandler(this, clicked::isMouseOver);
					return Optional.of(handler);
				}
				return Optional.empty();
			});
	}
}

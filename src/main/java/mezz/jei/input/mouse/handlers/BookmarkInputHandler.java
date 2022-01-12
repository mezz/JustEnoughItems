package mezz.jei.input.mouse.handlers;

import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.KeyBindings;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
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
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (input.is(KeyBindings.bookmark)) {
			return handleBookmark(input);
		}
		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleBookmark(UserInput input) {
		return focusSource.getIngredientUnderMouse(input)
			.flatMap(clicked -> {
				if (input.isSimulate() ||
					bookmarkList.remove(clicked.getValue()) ||
					bookmarkList.add(clicked.getValue())
				) {
					return Optional.of(LimitedAreaInputHandler.create(this, clicked.getArea()));
				}
				return Optional.empty();
			});
	}
}

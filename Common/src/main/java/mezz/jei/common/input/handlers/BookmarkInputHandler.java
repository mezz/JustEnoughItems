package mezz.jei.common.input.handlers;

import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.input.CombinedRecipeFocusSource;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
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
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IKeyBindings keyBindings) {
		if (input.is(keyBindings.getBookmark())) {
			return handleBookmark(input, keyBindings);
		}
		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleBookmark(UserInput input, IKeyBindings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.flatMap(clicked -> {
				if (input.isSimulate() ||
					bookmarkList.remove(clicked.getTypedIngredient()) ||
					bookmarkList.add(clicked.getTypedIngredient())
				) {
					return Optional.of(LimitedAreaInputHandler.create(this, clicked.getArea()));
				}
				return Optional.empty();
			});
	}
}

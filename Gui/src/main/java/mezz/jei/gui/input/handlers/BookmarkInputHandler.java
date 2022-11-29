package mezz.jei.gui.input.handlers;

import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.handlers.LimitedAreaInputHandler;
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
		if (input.is(keyBindings.getBookmark())) {
			return handleBookmark(input, keyBindings);
		}
		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleBookmark(UserInput input, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.flatMap(clicked -> {
				if (input.isSimulate() ||
					bookmarkList.remove(clicked.getTypedIngredient()) ||
					bookmarkList.add(clicked.getTypedIngredient())
				) {
					IImmutableRect2i area = clicked.getArea().orElse(null);
					IUserInputHandler handler = LimitedAreaInputHandler.create(this, area);
					return Optional.of(handler);
				}
				return Optional.empty();
			});
	}
}

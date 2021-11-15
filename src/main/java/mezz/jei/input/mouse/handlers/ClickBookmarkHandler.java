package mezz.jei.input.mouse.handlers;

import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.KeyBindings;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public class ClickBookmarkHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final BookmarkList bookmarkList;

	public ClickBookmarkHandler(CombinedRecipeFocusSource focusSource, BookmarkList bookmarkList) {
		this.focusSource = focusSource;
		this.bookmarkList = bookmarkList;
	}

	@Override
	@Nullable
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		if (input.is(KeyBindings.bookmark)) {
			IClickedIngredient<?> clicked = focusSource.getIngredientUnderMouse(input);
			if (clicked == null) {
				return null;
			}

			if (!input.isSimulate()) {
				if (bookmarkList.remove(clicked.getValue())) {
					return LimitedAreaUserInputHandler.create(this, clicked.getArea());
				}
				if (bookmarkList.add(clicked.getValue())) {
					return LimitedAreaUserInputHandler.create(this, clicked.getArea());
				}
				return null;
			}

			return LimitedAreaUserInputHandler.create(this, clicked.getArea());
		}

		return null;
	}
}

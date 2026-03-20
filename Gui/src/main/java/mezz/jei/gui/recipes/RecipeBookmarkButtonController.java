package mezz.jei.gui.recipes;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class RecipeBookmarkButtonController implements IIconButtonController {
	private final BookmarkList bookmarks;
	private final @Nullable IBookmark recipeBookmark;
	private boolean bookmarked;

	public RecipeBookmarkButtonController(BookmarkList bookmarks, @Nullable IBookmark recipeBookmark) {
		this.bookmarks = bookmarks;
		this.recipeBookmark = recipeBookmark;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		if (recipeBookmark != null) {
			if (bookmarks.contains(recipeBookmark)) {
				tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe.remove"));
			} else {
				tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe.add"));
			}
		}
	}

	@Override
	public void initState(IButtonState state) {
		Textures textures = Internal.getTextures();
		state.setIcon(textures.getRecipeBookmark());
		if (recipeBookmark == null) {
			state.setActive(false);
			state.setVisible(false);
		}
		updateState(state);
	}

	@Override
	public void updateState(IButtonState state) {
		bookmarked = recipeBookmark != null && bookmarks.contains(recipeBookmark);
		state.setForcePressed(bookmarked);
	}

	@Override
	public boolean onPress(IJeiUserInput input) {
		if (recipeBookmark != null) {
			if (!input.isSimulate()) {
				bookmarks.toggleBookmark(recipeBookmark);
			}
			return true;
		}
		return false;
	}

	@Override
	public void drawExtras(GuiGraphicsExtractor guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {
		if (bookmarked) {
			guiGraphics.fill(
				buttonArea.getX(),
				buttonArea.getY(),
				buttonArea.getX() + buttonArea.getWidth(),
				buttonArea.getY() + buttonArea.getHeight(),
				0x1100FF00
			);
		}
	}
}

package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class RecipeBookmarkButton extends GuiIconToggleButton {
	private final BookmarkList bookmarks;
	private final RecipeBookmark<?, ?> recipeBookmark;

	public static Optional<RecipeBookmarkButton> create(
		IRecipeLayoutDrawable<?> recipeLayout,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService,
		BookmarkList bookmarks,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper
	) {
		return RecipeBookmark.create(recipeLayout, ingredientManager, recipeManager, guiHelper, recipeTransferService)
			.map(recipeBookmark -> create(recipeLayout, bookmarks, recipeBookmark));
	}

	public static RecipeBookmarkButton create(
		IRecipeLayoutDrawable<?> recipeLayout,
		BookmarkList bookmarks,
		RecipeBookmark<?, ?> recipeBookmark
	) {
		IDrawable icon = Internal.getTextures().getRecipeBookmark();
		Rect2i area = recipeLayout.getRecipeBookmarkButtonArea();
		Rect2i layoutArea = recipeLayout.getRect();
		area.setX(area.getX() + layoutArea.getX());
		area.setY(area.getY() + layoutArea.getY());

		RecipeBookmarkButton recipeBookmarkButton = new RecipeBookmarkButton(icon, bookmarks, recipeBookmark);
		recipeBookmarkButton.updateBounds(area);
		return recipeBookmarkButton;
	}

	private RecipeBookmarkButton(IDrawable icon, BookmarkList bookmarks, RecipeBookmark<?, ?> recipeBookmark) {
		super(icon, icon);
		this.bookmarks = bookmarks;
		this.recipeBookmark = recipeBookmark;
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
		if (bookmarks.contains(recipeBookmark)) {
			tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe.remove"));
		} else {
			tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe.add"));
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return bookmarks.contains(recipeBookmark);
	}

	public boolean isBookmarked() {
		return bookmarks.contains(recipeBookmark);
	}

	public RecipeBookmark<?, ?> getRecipeBookmark() {
		return recipeBookmark;
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (!input.isSimulate()) {
			bookmarks.toggleBookmark(recipeBookmark);
		}
		return true;
	}
}

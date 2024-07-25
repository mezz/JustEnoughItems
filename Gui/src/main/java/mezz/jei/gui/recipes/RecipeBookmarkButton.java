package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class RecipeBookmarkButton extends GuiIconToggleButton {
	private final BookmarkList bookmarks;
	private final @Nullable RecipeBookmark<?, ?> recipeBookmark;
	private boolean bookmarked;

	public static RecipeBookmarkButton create(
		IRecipeLayoutDrawable<?> recipeLayout,
		IIngredientManager ingredientManager,
		BookmarkList bookmarks,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper
	) {
		RecipeBookmark<?, ?> recipeBookmark = RecipeBookmark.create(recipeLayout, ingredientManager, recipeManager, guiHelper)
			.orElse(null);

		Textures textures = Internal.getTextures();
		IDrawable icon = textures.getRecipeBookmark();
		Rect2i area = recipeLayout.getRecipeBookmarkButtonArea();
		Rect2i layoutArea = recipeLayout.getRect();
		area.setX(area.getX() + layoutArea.getX());
		area.setY(area.getY() + layoutArea.getY());

		RecipeBookmarkButton recipeBookmarkButton = new RecipeBookmarkButton(icon, bookmarks, recipeBookmark);
		recipeBookmarkButton.updateBounds(area);
		return recipeBookmarkButton;
	}

	private RecipeBookmarkButton(IDrawable icon, BookmarkList bookmarks, @Nullable RecipeBookmark<?, ?> recipeBookmark) {
		super(icon, icon);

		this.bookmarks = bookmarks;
		this.recipeBookmark = recipeBookmark;

		if (recipeBookmark == null) {
			button.active = false;
			button.visible = false;
		}

		tick();
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
		if (recipeBookmark != null) {
			if (bookmarks.contains(recipeBookmark)) {
				tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe.remove"));
			} else {
				tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe.add"));
			}
		}
	}

	@Override
	public void tick() {
		bookmarked = recipeBookmark != null && bookmarks.contains(recipeBookmark);
	}

	@Override
	protected boolean isIconToggledOn() {
		return bookmarked;
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (recipeBookmark != null) {
			if (!input.isSimulate()) {
				bookmarks.toggleBookmark(recipeBookmark);
			}
			return true;
		}
		return false;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.draw(guiGraphics, mouseX, mouseY, partialTicks);
		if (bookmarked) {
			guiGraphics.fill(
				RenderType.gui(),
				button.getX(),
				button.getY(),
				button.getX() + button.getWidth(),
				button.getY() + button.getHeight(),
				0x1100FF00
			);
		}
	}

	public boolean isBookmarked() {
		return bookmarked;
	}
}

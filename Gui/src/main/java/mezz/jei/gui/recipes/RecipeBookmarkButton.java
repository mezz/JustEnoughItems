package mezz.jei.gui.recipes;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.elements.IIconButtonDescriptor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class RecipeBookmarkButton implements IIconButtonDescriptor {
	private final BookmarkList bookmarks;
	private final @Nullable IBookmark recipeBookmark;
	private final IDrawable icon;
	private boolean bookmarked;
	private boolean active = true;
	private boolean visible = true;

	public RecipeBookmarkButton(BookmarkList bookmarks, @Nullable IBookmark recipeBookmark) {
		Textures textures = Internal.getTextures();
		this.icon = textures.getRecipeBookmark();
		this.bookmarks = bookmarks;
		this.recipeBookmark = recipeBookmark;

		if (recipeBookmark == null) {
			this.active = false;
			this.visible = false;
		}

		tick();
	}

	@Override
	public IDrawable getIcon() {
		return icon;
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
	public void tick() {
		bookmarked = recipeBookmark != null && bookmarks.contains(recipeBookmark);
	}

	@Override
	public boolean isForcePressed() {
		return bookmarked;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isVisible() {
		return visible;
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
	public void drawExtras(GuiGraphics guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {
		if (bookmarked) {
			guiGraphics.fill(
				RenderType.gui(),
				buttonArea.getX(),
				buttonArea.getY(),
				buttonArea.getX() + buttonArea.getWidth(),
				buttonArea.getY() + buttonArea.getHeight(),
				0x1100FF00
			);
		}
	}

	public boolean isBookmarked() {
		return bookmarked;
	}
}

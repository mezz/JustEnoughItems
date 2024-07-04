package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class RecipeBookmarkButton extends GuiIconButtonSmall {
	private final BookmarkList bookmarks;
	private final RecipeBookmark<?, ?> recipeBookmark;
	private final IOnClickHandler onClickHandler;

	public static Optional<RecipeBookmarkButton> create(
		IRecipeLayoutDrawable<?> recipeLayout,
		IIngredientManager ingredientManager,
		BookmarkList bookmarks,
		Textures textures,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper
	) {
		return RecipeBookmark.create(recipeLayout, ingredientManager, recipeManager, guiHelper)
			.map(recipeBookmark -> {
				IDrawable icon = textures.getRecipeBookmark();
				Rect2i area = recipeLayout.getRecipeBookmarkButtonArea();

				return new RecipeBookmarkButton(icon, bookmarks, recipeBookmark, textures, area);
			});
	}

	private RecipeBookmarkButton(IDrawable icon, BookmarkList bookmarks, RecipeBookmark<?, ?> recipeBookmark, Textures textures, Rect2i area) {
		super(area.getX(), area.getY(), area.getWidth(), area.getHeight(), icon, b -> {}, textures);
		this.bookmarks = bookmarks;
		this.recipeBookmark = recipeBookmark;

		this.onClickHandler = (mouseX, mouseY) -> {
			bookmarks.toggleBookmark(recipeBookmark);
		};
	}

	public void drawToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			Component tooltip;
			if (bookmarks.contains(recipeBookmark)) {
				tooltip = Component.translatable("jei.tooltip.bookmarks.recipe.remove");
			} else {
				tooltip = Component.translatable("jei.tooltip.bookmarks.recipe.add");
			}
			TooltipRenderer.drawHoveringText(guiGraphics, List.of(tooltip), mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= this.getX() &&
			mouseY >= this.getY() &&
			mouseX < this.getX() + this.getWidth() &&
			mouseY < this.getY() + this.getHeight();
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return;
		}
		if (onClickHandler != null) {
			onClickHandler.onClick(mouseX, mouseY);
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

		if (bookmarks.contains(recipeBookmark)) {
			guiGraphics.fill(
				RenderType.gui(),
				this.getX(),
				this.getY(),
				this.getX() + this.getWidth(),
				this.getY() + this.getHeight(),
				0x2200FF00
			);
		}
	}
}

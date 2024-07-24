package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class BookmarkDrag<T> {
	private final BookmarkOverlay bookmarkOverlay;
	private final List<IBookmarkDragTarget> targets;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final ITypedIngredient<T> ingredient;
	private final double mouseStartX;
	private final double mouseStartY;
	private final IBookmark bookmark;
	private final ImmutableRect2i origin;

	public BookmarkDrag(
		BookmarkOverlay bookmarkOverlay,
		List<IBookmarkDragTarget> targets,
		IIngredientRenderer<T> ingredientRenderer,
		ITypedIngredient<T> ingredient,
		IBookmark bookmark,
		double mouseX,
		double mouseY,
		ImmutableRect2i origin
	) {
		this.bookmarkOverlay = bookmarkOverlay;
		this.targets = targets;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.bookmark = bookmark;
		this.origin = origin;
		this.mouseStartX = mouseX;
		this.mouseStartY = mouseY;
	}

	public static boolean farEnoughToDraw(BookmarkDrag<?> drag, double mouseX, double mouseY) {
		ImmutableRect2i origin = drag.origin;
		final Vec2 center;
		if (origin.isEmpty()) {
			center = new Vec2((float) drag.mouseStartX, (float) drag.mouseStartY);
		} else {
			if (origin.contains(mouseX, mouseY)) {
				return false;
			}
			center = new Vec2(
				origin.getX() + (origin.getWidth() / 2.0f),
				origin.getY() + (origin.getHeight() / 2.0f)
			);
		}

		double mouseXDist = center.x - mouseX;
		double mouseYDist = center.y - mouseY;
		double mouseDistSq = mouseXDist * mouseXDist + mouseYDist * mouseYDist;
		return mouseDistSq > 64.0;
	}

	public void update(int mouseX, int mouseY) {
		if (bookmark.isVisible() && !farEnoughToDraw(this, mouseX, mouseY)) {
			return;
		}

		bookmark.setVisible(false);
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateMouseExclusionArea(new ImmutablePoint2i(mouseX, mouseY))
			.update();
	}

	public boolean drawItem(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (bookmark.isVisible()) {
			return false;
		}

		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(mouseX - 8, mouseY - 8, 0);
			SafeIngredientUtil.render(guiGraphics, ingredientRenderer, ingredient);
		}
		poseStack.popPose();
		return true;
	}

	public boolean onClick(UserInput input) {
		for (IBookmarkDragTarget target : targets) {
			ImmutableRect2i area = target.getArea();
			if (MathUtil.contains(area, input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					target.accept(bookmark);
					stop();
					return true;
				}
			}
		}
		if (!input.isSimulate()) {
			stop();
		}
		return false;
	}

	public void stop() {
		bookmark.setVisible(true);
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateMouseExclusionArea(null)
			.update();
	}
}

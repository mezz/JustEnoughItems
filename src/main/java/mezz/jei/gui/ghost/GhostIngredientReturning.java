package mezz.jei.gui.ghost;

import javax.annotation.Nullable;
import java.awt.Point;
import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;

import mezz.jei.api.ingredients.IIngredientRenderer;

/**
 * Renders an item returning to the ingredient list after a failed ghost drag.
 */
public class GhostIngredientReturning<T> {
	private static final long DURATION_PER_SCREEN_WIDTH = 500; // milliseconds to move across a full screen
	private final IIngredientRenderer<T> ingredientRenderer;
	private final T ingredient;
	private final Point start;
	private final Point end;
	private final long startTime;
	private final long duration;

	@Nullable
	public static <T> GhostIngredientReturning<T> create(GhostIngredientDrag<T> ghostIngredientDrag, int mouseX, int mouseY) {
		Rectangle origin = ghostIngredientDrag.getOrigin();
		if (origin != null) {
			IIngredientRenderer<T> ingredientRenderer = ghostIngredientDrag.getIngredientRenderer();
			T ingredient = ghostIngredientDrag.getIngredient();
			Point end = new Point(origin.x, origin.y);
			Point start = new Point(mouseX - 8, mouseY - 8);
			return new GhostIngredientReturning<>(ingredientRenderer, ingredient, start, end);
		}
		return null;
	}

	private GhostIngredientReturning(IIngredientRenderer<T> ingredientRenderer, T ingredient, Point start, Point end) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.start = start;
		this.end = end;
		this.startTime = System.currentTimeMillis();
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen != null) {
			int width = currentScreen.width;
			float durationPerPixel = DURATION_PER_SCREEN_WIDTH / (float) width;
			float distance = (float) start.distance(end);
			this.duration = Math.round(durationPerPixel * distance);
		} else {
			this.duration = Math.round(0.5f * DURATION_PER_SCREEN_WIDTH);
		}
	}

	public void drawItem(Minecraft minecraft) {
		long time = System.currentTimeMillis();
		long elapsed = time - startTime;
		float percent = Math.min(elapsed / (float) this.duration, 1);
		int dx = end.x - start.x;
		int dy = end.y - start.y;
		int x = start.x + Math.round(dx * percent);
		int y = start.y + Math.round(dy * percent);
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.zLevel += 150.0F;
		ingredientRenderer.render(minecraft, x, y, ingredient);
		renderItem.zLevel -= 150.0F;
	}

	public boolean isComplete() {
		long time = System.currentTimeMillis();
		return startTime + this.duration < time;
	}
}

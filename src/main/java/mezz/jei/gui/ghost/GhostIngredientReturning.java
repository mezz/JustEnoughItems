package mezz.jei.gui.ghost;

import javax.annotation.Nullable;

import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.util.math.Vec2f;

/**
 * Renders an item returning to the ingredient list after a failed ghost drag.
 */
public class GhostIngredientReturning<T> {
	private static final long DURATION_PER_SCREEN_WIDTH = 500; // milliseconds to move across a full screen
	private final IIngredientRenderer<T> ingredientRenderer;
	private final T ingredient;
	private final Vec2f start;
	private final Vec2f end;
	private final long startTime;
	private final long duration;

	@Nullable
	public static <T> GhostIngredientReturning<T> create(GhostIngredientDrag<T> ghostIngredientDrag, double mouseX, double mouseY) {
		Rectangle2d origin = ghostIngredientDrag.getOrigin();
		if (origin != null) {
			IIngredientRenderer<T> ingredientRenderer = ghostIngredientDrag.getIngredientRenderer();
			T ingredient = ghostIngredientDrag.getIngredient();
			Vec2f end = new Vec2f(origin.getX(), origin.getY());
			Vec2f start = new Vec2f((float) mouseX - 8, (float) mouseY - 8);
			return new GhostIngredientReturning<>(ingredientRenderer, ingredient, start, end);
		}
		return null;
	}

	private GhostIngredientReturning(IIngredientRenderer<T> ingredientRenderer, T ingredient, Vec2f start, Vec2f end) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.start = start;
		this.end = end;
		this.startTime = System.currentTimeMillis();
		Screen currentScreen = Minecraft.getInstance().currentScreen;
		if (currentScreen != null) {
			int width = currentScreen.width;
			float durationPerPixel = DURATION_PER_SCREEN_WIDTH / (float) width;
			float distance = (float) MathUtil.distance(start, end);
			this.duration = Math.round(durationPerPixel * distance);
		} else {
			this.duration = Math.round(0.5f * DURATION_PER_SCREEN_WIDTH);
		}
	}

	public void drawItem(Minecraft minecraft) {
		long time = System.currentTimeMillis();
		long elapsed = time - startTime;
		double percent = Math.min(elapsed / (double) this.duration, 1);
		double dx = end.x - start.x;
		double dy = end.y - start.y;
		double x = start.x + Math.round(dx * percent);
		double y = start.y + Math.round(dy * percent);
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		itemRenderer.zLevel += 150.0F;
		ingredientRenderer.render((int) x, (int) y, ingredient);
		itemRenderer.zLevel -= 150.0F;
	}

	public boolean isComplete() {
		long time = System.currentTimeMillis();
		return startTime + this.duration < time;
	}

}

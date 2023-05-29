package mezz.jei.gui.ghost;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec2;

import java.util.Optional;

/**
 * Renders an item returning to the ingredient list after a failed ghost drag.
 */
public class GhostIngredientReturning<T> {
	private static final long DURATION_PER_SCREEN_WIDTH = 500; // milliseconds to move across a full screen
	private final IIngredientRenderer<T> ingredientRenderer;
	private final ITypedIngredient<T> ingredient;
	private final Vec2 start;
	private final Vec2 end;
	private final long startTime;
	private final long duration;

	public static <T> Optional<GhostIngredientReturning<T>> create(GhostIngredientDrag<T> ghostIngredientDrag, double mouseX, double mouseY) {
		ImmutableRect2i origin = ghostIngredientDrag.getOrigin();
		if (origin.isEmpty()) {
			return Optional.empty();
		}

		IIngredientRenderer<T> ingredientRenderer = ghostIngredientDrag.getIngredientRenderer();
		ITypedIngredient<T> ingredient = ghostIngredientDrag.getIngredient();
		Vec2 end = new Vec2(origin.getX(), origin.getY());
		Vec2 start = new Vec2((float) mouseX - 8, (float) mouseY - 8);
		GhostIngredientReturning<T> returning = new GhostIngredientReturning<>(ingredientRenderer, ingredient, start, end);
		return Optional.of(returning);
	}

	private GhostIngredientReturning(IIngredientRenderer<T> ingredientRenderer, ITypedIngredient<T> ingredient, Vec2 start, Vec2 end) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.start = start;
		this.end = end;
		this.startTime = System.currentTimeMillis();
		Screen currentScreen = Minecraft.getInstance().screen;
		if (currentScreen != null) {
			int width = currentScreen.width;
			float durationPerPixel = DURATION_PER_SCREEN_WIDTH / (float) width;
			float distance = (float) MathUtil.distance(start, end);
			this.duration = Math.round(durationPerPixel * distance);
		} else {
			this.duration = Math.round(0.5f * DURATION_PER_SCREEN_WIDTH);
		}
	}

	public void drawItem(GuiGraphics guiGraphics) {
		long time = System.currentTimeMillis();
		long elapsed = time - startTime;
		double percent = Math.min(elapsed / (double) this.duration, 1);
		double dx = end.x - start.x;
		double dy = end.y - start.y;
		double x = start.x + Math.round(dx * percent);
		double y = start.y + Math.round(dy * percent);
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(x, y, 0);
			ingredientRenderer.render(guiGraphics, ingredient.getIngredient());
		}
		poseStack.popPose();
	}

	public boolean isComplete() {
		long time = System.currentTimeMillis();
		return startTime + this.duration < time;
	}

}

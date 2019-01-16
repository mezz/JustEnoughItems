package mezz.jei.gui.ghost;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;

import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import mezz.jei.api.ingredients.IIngredientRenderer;
import org.lwjgl.opengl.GL11;

public class GhostIngredientDrag<T> {
	private static final Color targetColor = new Color(19, 201, 10, 64);
	private static final Color hoverColor = new Color(76, 201, 25, 128);

	private final IGhostIngredientHandler<?> handler;
	private final List<Target<T>> targets;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final T ingredient;
	@Nullable
	private final Rectangle origin;

	public GhostIngredientDrag(IGhostIngredientHandler<?> handler, List<Target<T>> targets, IIngredientRenderer<T> ingredientRenderer, T ingredient, @Nullable Rectangle origin) {
		this.handler = handler;
		this.targets = targets;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.origin = origin;
	}

	public void drawTargets(int mouseX, int mouseY) {
		if (handler.shouldHighlightTargets()) {
			@SuppressWarnings("unchecked")
			List<Target<Object>> targets = (List<Target<Object>>) (Object) this.targets;
			drawTargets(mouseX, mouseY, targets);
		}
	}

	public void drawItem(Minecraft minecraft, int mouseX, int mouseY) {
		if (origin != null) {
			int originX = origin.x + (origin.width / 2);
			int originY = origin.y + (origin.height / 2);
			int xDist = originX - mouseX;
			int yDist = originY - mouseY;
			float lineWidth = 2;
			if (minecraft.currentScreen != null) {
				long distanceSq = xDist * xDist + yDist * yDist;
				int screenDim = minecraft.currentScreen.width * minecraft.currentScreen.height;
				float percentOfDim = Math.min(1, distanceSq / (float) screenDim);
				lineWidth = 1 + ((1 - (percentOfDim)) * 3);
			}
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GlStateManager.disableDepth();
			GL11.glLineWidth(lineWidth);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor4f(targetColor.getRed() / 255f, targetColor.getGreen() / 255f, targetColor.getBlue() / 255f, targetColor.getAlpha() / 255f);
			GL11.glVertex3f(mouseX, mouseY, 150);
			GL11.glVertex3f(originX, originY, 150);
			GL11.glEnd();
			GlStateManager.enableDepth();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.zLevel += 150.0F;
		ingredientRenderer.render(minecraft, mouseX - 8, mouseY - 8, ingredient);
		renderItem.zLevel -= 150.0F;
	}

	public static void drawTargets(int mouseX, int mouseY, List<Target<Object>> targets) {
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		for (Target target : targets) {
			Rectangle area = target.getArea();
			Color color;
			if (area.contains(mouseX, mouseY)) {
				color = hoverColor;
			} else {
				color = targetColor;
			}
			Gui.drawRect(area.x, area.y, area.x + area.width, area.y + area.height, color.getRGB());
		}
		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	public boolean onClick(int mouseX, int mouseY) {
		for (Target<T> target : targets) {
			Rectangle area = target.getArea();
			if (area.contains(mouseX, mouseY)) {
				target.accept(ingredient);
				handler.onComplete();
				return true;
			}
		}
		handler.onComplete();
		return false;
	}

	public void stop() {
		handler.onComplete();
	}

	public IIngredientRenderer<T> getIngredientRenderer() {
		return ingredientRenderer;
	}

	public T getIngredient() {
		return ingredient;
	}

	@Nullable
	public Rectangle getOrigin() {
		return origin;
	}
}

package mezz.jei.gui.ghost;

import javax.annotation.Nullable;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.MathUtil;
import org.lwjgl.opengl.GL11;

public class GhostIngredientDrag<T> {
	private static final int targetColor = 0x4013C90A;
	private static final int hoverColor = 0x804CC919;

	private final IGhostIngredientHandler<?> handler;
	private final List<Target<T>> targets;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final T ingredient;
	@Nullable
	private final Rectangle2d origin;

	public GhostIngredientDrag(IGhostIngredientHandler<?> handler, List<Target<T>> targets, IIngredientRenderer<T> ingredientRenderer, T ingredient, @Nullable Rectangle2d origin) {
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
			int originX = origin.getX() + (origin.getWidth() / 2);
			int originY = origin.getY() + (origin.getHeight() / 2);
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
			RenderSystem.disableDepthTest();
			GL11.glLineWidth(lineWidth);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glBegin(GL11.GL_LINES);
			float red = (float)(targetColor >> 24 & 255) / 255.0F;
			float green = (float)(targetColor >> 16 & 255) / 255.0F;
			float blue = (float)(targetColor >> 8 & 255) / 255.0F;
			float alpha = (float)(targetColor & 255) / 255.0F;
			RenderSystem.color4f(red, green, blue, alpha);
			GL11.glVertex3f(mouseX, mouseY, 150);
			GL11.glVertex3f(originX, originY, 150);
			GL11.glEnd();
			RenderSystem.enableDepthTest();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		itemRenderer.zLevel += 150.0F;
		ingredientRenderer.render(mouseX - 8, mouseY - 8, ingredient);
		itemRenderer.zLevel -= 150.0F;
	}

	public static void drawTargets(int mouseX, int mouseY, List<Target<Object>> targets) {
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		for (Target target : targets) {
			Rectangle2d area = target.getArea();
			int color;
			if (MathUtil.contains(area, mouseX, mouseY)) {
				color = hoverColor;
			} else {
				color = targetColor;
			}
			Screen.fill(area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), color);
		}
		RenderSystem.color4f(1f, 1f, 1f, 1f);
	}

	public boolean onClick(double mouseX, double mouseY) {
		for (Target<T> target : targets) {
			Rectangle2d area = target.getArea();
			if (MathUtil.contains(area, mouseX, mouseY)) {
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
	public Rectangle2d getOrigin() {
		return origin;
	}
}

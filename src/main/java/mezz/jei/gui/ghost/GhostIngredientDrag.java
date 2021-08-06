package mezz.jei.gui.ghost;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nullable;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.Rect2i;

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
	private final double mouseStartX;
	private final double mouseStartY;
	@Nullable
	private final Rect2i origin;

	public GhostIngredientDrag(
		IGhostIngredientHandler<?> handler,
		List<Target<T>> targets,
		IIngredientRenderer<T> ingredientRenderer,
		T ingredient,
		double mouseX,
		double mouseY,
		@Nullable Rect2i origin
	) {
		this.handler = handler;
		this.targets = targets;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.origin = origin;
		this.mouseStartX = mouseX;
		this.mouseStartY = mouseY;
	}

	public void drawTargets(PoseStack poseStack, int mouseX, int mouseY) {
		if (handler.shouldHighlightTargets()) {
			drawTargets(poseStack, mouseX, mouseY, targets);
		}
	}

	public void drawItem(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		double mouseXDist = this.mouseStartX - mouseX;
		double mouseYDist = this.mouseStartY - mouseY;
		double mouseDistSq = mouseXDist * mouseXDist + mouseYDist * mouseYDist;
		if (mouseDistSq < 10.0) {
			return;
		}

		if (origin != null) {
			int originX = origin.getX() + (origin.getWidth() / 2);
			int originY = origin.getY() + (origin.getHeight() / 2);
			int xDist = originX - mouseX;
			int yDist = originY - mouseY;
			float lineWidth = 2;
			if (minecraft.screen != null) {
				long distanceSq = (long) xDist * xDist + (long) yDist * yDist;
				int screenDim = minecraft.screen.width * minecraft.screen.height;
				float percentOfDim = Math.min(1, distanceSq / (float) screenDim);
				lineWidth = 1 + ((1 - (percentOfDim)) * 3);
			}
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			RenderSystem.disableDepthTest();
			GL11.glLineWidth(lineWidth);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glBegin(GL11.GL_LINES);
			float red = (targetColor >> 24 & 255) / 255.0F;
			float green = (targetColor >> 16 & 255) / 255.0F;
			float blue = (targetColor >> 8 & 255) / 255.0F;
			float alpha = (targetColor & 255) / 255.0F;
			RenderSystem.setShaderColor(red, green, blue, alpha);
			GL11.glVertex3f(mouseX, mouseY, 150);
			GL11.glVertex3f(originX, originY, 150);
			GL11.glEnd();
			RenderSystem.enableDepthTest();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		itemRenderer.blitOffset += 150.0F;
		ingredientRenderer.render(poseStack, mouseX - 8, mouseY - 8, ingredient);
		itemRenderer.blitOffset -= 150.0F;
	}

	public static <V> void drawTargets(PoseStack poseStack, int mouseX, int mouseY, List<Target<V>> targets) {
		RenderSystem.disableDepthTest();
		for (Target<?> target : targets) {
			Rect2i area = target.getArea();
			int color;
			if (MathUtil.contains(area, mouseX, mouseY)) {
				color = hoverColor;
			} else {
				color = targetColor;
			}
			GuiComponent.fill(poseStack, area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), color);
		}
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	public boolean onClick(double mouseX, double mouseY, MouseClickState clickState) {
		for (Target<T> target : targets) {
			Rect2i area = target.getArea();
			if (MathUtil.contains(area, mouseX, mouseY)) {
				if (!clickState.isSimulate()) {
					target.accept(ingredient);
					handler.onComplete();
				}
				return true;
			}
		}
		if (!clickState.isSimulate()) {
			handler.onComplete();
		}
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
	public Rect2i getOrigin() {
		return origin;
	}
}

package mezz.jei.gui.ghost;

import com.mojang.blaze3d.matrix.MatrixStack;

import javax.annotation.Nullable;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

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
	private final Rectangle2d origin;

	public GhostIngredientDrag(
		IGhostIngredientHandler<?> handler,
		List<Target<T>> targets,
		IIngredientRenderer<T> ingredientRenderer,
		T ingredient,
		double mouseX,
		double mouseY,
		@Nullable Rectangle2d origin
	) {
		this.handler = handler;
		this.targets = targets;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.origin = origin;
		this.mouseStartX = mouseX;
		this.mouseStartY = mouseY;
	}

	public void drawTargets(MatrixStack matrixStack, int mouseX, int mouseY) {
		if (handler.shouldHighlightTargets()) {
			drawTargets(matrixStack, mouseX, mouseY, targets);
		}
	}

	public static boolean farEnoughToDraw(GhostIngredientDrag<?> drag, double mouseX, double mouseY) {
		final double centerX;
		final double centerY;

		Rectangle2d origin = drag.getOrigin();
		if (origin != null) {
			centerX = origin.getX() + (origin.getWidth() / 2.0);
			centerY = origin.getY() + (origin.getHeight() / 2.0);
		} else {
			centerX = drag.mouseStartX;
			centerY = drag.mouseStartY;
		}
		double mouseXDist = centerX - mouseX;
		double mouseYDist = centerY - mouseY;
		double mouseDistSq = mouseXDist * mouseXDist + mouseYDist * mouseYDist;
		return mouseDistSq > 64.0;
	}

	@SuppressWarnings("deprecation")
	public void drawItem(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		if (!farEnoughToDraw(this, mouseX, mouseY)) {
			return;
		}

		if (origin != null) {
			int originX = origin.getX() + (origin.getWidth() / 2);
			int originY = origin.getY() + (origin.getHeight() / 2);

			RenderSystem.disableTexture();
			RenderSystem.disableDepthTest();
			RenderSystem.depthMask(false);

			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tessellator.getBuilder();
			bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			int red = targetColor >> 24 & 255;
			int green = targetColor >> 16 & 255;
			int blue = targetColor >> 8 & 255;
			int alpha = targetColor & 255;
			bufferBuilder.vertex(mouseX, mouseY, 150).color(red, green, blue, alpha).endVertex();
			bufferBuilder.vertex(originX, originY, 150).color(red, green, blue, alpha).endVertex();
			tessellator.end();

			RenderSystem.enableDepthTest();
			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
		}

		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		itemRenderer.blitOffset += 150.0F;
		ingredientRenderer.render(matrixStack, mouseX - 8, mouseY - 8, ingredient);
		itemRenderer.blitOffset -= 150.0F;
	}

	@SuppressWarnings("deprecation")
	public static <V> void drawTargets(MatrixStack matrixStack, int mouseX, int mouseY, List<Target<V>> targets) {
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		for (Target<?> target : targets) {
			Rectangle2d area = target.getArea();
			int color;
			if (MathUtil.contains(area, mouseX, mouseY)) {
				color = hoverColor;
			} else {
				color = targetColor;
			}
			AbstractGui.fill(matrixStack, area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), color);
		}
		RenderSystem.color4f(1f, 1f, 1f, 1f);
	}

	public boolean onClick(double mouseX, double mouseY, MouseClickState clickState) {
		for (Target<T> target : targets) {
			Rectangle2d area = target.getArea();
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
	public Rectangle2d getOrigin() {
		return origin;
	}
}

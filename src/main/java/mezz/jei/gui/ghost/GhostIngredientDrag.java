package mezz.jei.gui.ghost;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.jei.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
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

	public static boolean farEnoughToDraw(GhostIngredientDrag<?> drag, double mouseX, double mouseY) {
		final double centerX;
		final double centerY;

		Rect2i origin = drag.getOrigin();
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

	public void drawItem(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (!farEnoughToDraw(this, mouseX, mouseY)) {
			return;
		}

		if (origin != null) {
			int originX = origin.getX() + (origin.getWidth() / 2);
			int originY = origin.getY() + (origin.getHeight() / 2);

			RenderSystem.disableTexture();
			RenderSystem.disableDepthTest();
			RenderSystem.depthMask(false);

			var oldShader = RenderSystem.getShader();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);

			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

			var tesselator = RenderSystem.renderThreadTesselator();
			var builder = tesselator.getBuilder();
			builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
			float red = (targetColor >> 24 & 255) / 255.0F;
			float green = (targetColor >> 16 & 255) / 255.0F;
			float blue = (targetColor >> 8 & 255) / 255.0F;
			float alpha = (targetColor & 255) / 255.0F;
			builder.vertex(mouseX, mouseY, 150).color(red, green, blue, alpha).endVertex();
			builder.vertex(originX, originY, 150).color(red, green, blue, alpha).endVertex();
			tesselator.end();

			RenderSystem.setShader(() -> oldShader);
			RenderSystem.enableDepthTest();
			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
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

	public boolean onClick(UserInput input) {
		for (Target<T> target : targets) {
			Rect2i area = target.getArea();
			if (MathUtil.contains(area, input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					target.accept(ingredient);
					handler.onComplete();
				}
				return true;
			}
		}
		if (!input.isSimulate()) {
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

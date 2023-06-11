package mezz.jei.gui.ghost;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GhostIngredientDrag<T> {
	private static final int targetColor = 0x4013C90A;
	private static final int hoverColor = 0x804CC919;

	private final IGhostIngredientHandler<?> handler;
	private final List<Target<T>> targets;
	private final List<Rect2i> targetAreas;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final ITypedIngredient<T> ingredient;
	private final double mouseStartX;
	private final double mouseStartY;
	private final ImmutableRect2i origin;

	public GhostIngredientDrag(
		IGhostIngredientHandler<?> handler,
		List<Target<T>> targets,
		IIngredientRenderer<T> ingredientRenderer,
		ITypedIngredient<T> ingredient,
		double mouseX,
		double mouseY,
		ImmutableRect2i origin
	) {
		this.handler = handler;
		this.targets = targets;
		this.targetAreas = targets.stream()
			.map(Target::getArea)
			.toList();
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.origin = origin;
		this.mouseStartX = mouseX;
		this.mouseStartY = mouseY;
	}

	public void drawTargets(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (handler.shouldHighlightTargets()) {
			drawTargets(guiGraphics, mouseX, mouseY, targetAreas);
		}
	}

	public static boolean farEnoughToDraw(GhostIngredientDrag<?> drag, double mouseX, double mouseY) {
		ImmutableRect2i origin = drag.getOrigin();
		final Vec2 center;
		if (origin.isEmpty()) {
			center = new Vec2((float) drag.mouseStartX, (float) drag.mouseStartY);
		} else {
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

	public void drawItem(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (!farEnoughToDraw(this, mouseX, mouseY)) {
			return;
		}

		if (!origin.isEmpty()) {
			int originX = origin.getX() + (origin.getWidth() / 2);
			int originY = origin.getY() + (origin.getHeight() / 2);

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
			RenderSystem.depthMask(true);
		}

		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(mouseX - 8, mouseY - 8, 0);
			ingredientRenderer.render(guiGraphics, ingredient.getIngredient());
		}
		poseStack.popPose();
	}

	public static void drawTargets(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Rect2i> targetAreas) {
		RenderSystem.disableDepthTest();
		for (Rect2i area : targetAreas) {
			int color;
			if (MathUtil.contains(area, mouseX, mouseY)) {
				color = hoverColor;
			} else {
				color = targetColor;
			}
			guiGraphics.fill(
				RenderType.guiOverlay(),
				area.getX(),
				area.getY(),
				area.getX() + area.getWidth(),
				area.getY() + area.getHeight(),
				color
			);
		}
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	public boolean onClick(UserInput input) {
		for (Target<T> target : targets) {
			Rect2i area = target.getArea();
			if (MathUtil.contains(area, input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					target.accept(ingredient.getIngredient());
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

	public ITypedIngredient<T> getIngredient() {
		return ingredient;
	}

	public ImmutableRect2i getOrigin() {
		return origin;
	}
}

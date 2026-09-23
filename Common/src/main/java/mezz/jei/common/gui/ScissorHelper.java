package mezz.jei.common.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

/** Clips GUI rendering in the current pose while preserving any enclosing clip. */
public final class ScissorHelper {
	private static final Deque<State> STATES = new ArrayDeque<>();

	private ScissorHelper() {}

	public static void enableScissor(PoseStack poseStack, int left, int top, int right, int bottom) {
		boolean enabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
		int[] previous = new int[4];
		GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, previous);
		STATES.push(new State(enabled, previous));
		Vector4f start = new Vector4f(left, top, 0, 1);
		Vector4f end = new Vector4f(right, bottom, 0, 1);
		start.transform(poseStack.last().pose());
		end.transform(poseStack.last().pose());
		var window = Minecraft.getInstance().getWindow();
		double scale = window.getGuiScale();
		int x = (int) Math.floor(Math.min(start.x(), end.x()) * scale);
		int y = (int) Math.floor(window.getHeight() - Math.max(start.y(), end.y()) * scale);
		int maxX = (int) Math.ceil(Math.max(start.x(), end.x()) * scale);
		int maxY = (int) Math.ceil(window.getHeight() - Math.min(start.y(), end.y()) * scale);
		if (enabled) {
			x = Math.max(x, previous[0]);
			y = Math.max(y, previous[1]);
			maxX = Math.min(maxX, previous[0] + previous[2]);
			maxY = Math.min(maxY, previous[1] + previous[3]);
		}
		RenderSystem.enableScissor(x, y, Math.max(0, maxX - x), Math.max(0, maxY - y));
	}

	public static void disableScissor() {
		State previous = STATES.pop();
		if (previous.enabled) {
			int[] box = previous.box;
			RenderSystem.enableScissor(box[0], box[1], box[2], box[3]);
		} else {
			RenderSystem.disableScissor();
		}
	}

	private record State(boolean enabled, int[] box) {}
}

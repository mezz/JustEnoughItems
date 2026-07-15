package mezz.jei.debug;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.List;

public class DebugExclusionAreaHandler implements IGlobalGuiHandler {
	private static final int HANDLE_SIZE = 8;
	private static final int MIN_WIDTH = 20;
	private static final int MIN_HEIGHT = 20;
	private static final int INITIAL_X = 200;
	private static final int INITIAL_Y = 30;
	private static final int INITIAL_WIDTH = 80;
	private static final int INITIAL_HEIGHT = 80;

	private int x = INITIAL_X;
	private int y = INITIAL_Y;
	private int width = INITIAL_WIDTH;
	private int height = INITIAL_HEIGHT;

	private boolean dragging = false;
	private boolean resizing = false;
	private double dragOffsetX = 0;
	private double dragOffsetY = 0;

	private boolean wasLeftButtonDown = false;

	@Override
	public Collection<Rect2i> getGuiExtraAreas() {
		pollMouse();
		return List.of(new Rect2i(x, y, width, height));
	}

	private void pollMouse() {
		Minecraft minecraft = Minecraft.getInstance();

        long windowHandle = minecraft.getWindow().handle();
		boolean leftButtonDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

		double mouseX = getScaledMouseX(minecraft);
		double mouseY = getScaledMouseY(minecraft);

		if (!wasLeftButtonDown && leftButtonDown) {
			if (isOnResizeHandle(mouseX, mouseY)) {
				resizing = true;
			} else if (isInside(mouseX, mouseY)) {
				dragging = true;
				dragOffsetX = mouseX - x;
				dragOffsetY = mouseY - y;
			}
		}

		if (dragging && leftButtonDown) {
			int screenWidth = minecraft.getWindow().getGuiScaledWidth();
			int screenHeight = minecraft.getWindow().getGuiScaledHeight();
			x = Mth.clamp((int) (mouseX - dragOffsetX), 0, screenWidth - width);
			y = Mth.clamp((int) (mouseY - dragOffsetY), 0, screenHeight - height);
		}

		if (resizing && leftButtonDown) {
			width = Math.max(MIN_WIDTH, (int) mouseX - x);
			height = Math.max(MIN_HEIGHT, (int) mouseY - y);
		}

		if (!leftButtonDown) {
			dragging = false;
			resizing = false;
		}

		wasLeftButtonDown = leftButtonDown;
	}

	private boolean isInside(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private boolean isOnResizeHandle(double mouseX, double mouseY) {
		return mouseX >= x + width - HANDLE_SIZE && mouseX < x + width &&
			mouseY >= y + height - HANDLE_SIZE && mouseY < y + height;
	}

	private static double getScaledMouseX(Minecraft minecraft) {
		double scale = (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth();
		return minecraft.mouseHandler.xpos() * scale;
	}

	private static double getScaledMouseY(Minecraft minecraft) {
		double scale = (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight();
		return minecraft.mouseHandler.ypos() * scale;
	}
}

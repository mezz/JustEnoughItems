package mezz.jei.debug;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

public class DebugExclusionAreaHandler implements IGlobalGuiHandler {
	private static final String DEBUG_TEXT = "debug";
	private static final int BACKGROUND_COLOR = 0xCC202020;
	private static final int BORDER_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int HANDLE_SIZE = 8;
	private static final int MIN_WIDTH = 20;
	private static final int MIN_HEIGHT = 20;
	private static final int INITIAL_X = 200;
	private static final int INITIAL_Y = 30;
	private static final int INITIAL_WIDTH = 80;
	private static final int INITIAL_HEIGHT = 80;
	private static final Field RENDERABLES_FIELD = findRenderablesField();

	private int x = INITIAL_X;
	private int y = INITIAL_Y;
	private int width = INITIAL_WIDTH;
	private int height = INITIAL_HEIGHT;

	private boolean dragging = false;
	private boolean resizing = false;
	private double dragOffsetX = 0;
	private double dragOffsetY = 0;

	private boolean wasLeftButtonDown = false;
	private final BooleanSupplier screenHasGuiProperties;
	private final Renderable renderScheduler = (guiGraphics, mouseX, mouseY, partialTick) -> {
		if (screenHasGuiProperties()) {
			guiGraphics.setPreeditOverlay(this::draw);
		}
	};

	public DebugExclusionAreaHandler(BooleanSupplier screenHasGuiProperties) {
		this.screenHasGuiProperties = screenHasGuiProperties;
	}

	@Override
	public Collection<Rect2i> getGuiExtraAreas() {
		if (!screenHasGuiProperties()) {
			stopInteraction();
			return List.of();
		}

		pollMouse();
		installRenderer();
		return List.of(getArea());
	}

	private void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (!screenHasGuiProperties()) {
			return;
		}

		Rect2i area = getArea();
		guiGraphics.fill(area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), BACKGROUND_COLOR);
		guiGraphics.outline(area.getX(), area.getY(), area.getWidth(), area.getHeight(), BORDER_COLOR);
		drawLabel(guiGraphics, area);
	}

	private void installRenderer() {
		Screen screen = Minecraft.getInstance().screen;
		if (screen == null || RENDERABLES_FIELD == null) {
			return;
		}
		try {
			@SuppressWarnings("unchecked")
			List<Renderable> renderables = (List<Renderable>) RENDERABLES_FIELD.get(screen);
			if (!renderables.contains(renderScheduler)) {
				renderables.add(renderScheduler);
			}
		} catch (IllegalAccessException ignored) {}
	}

	private static Field findRenderablesField() {
		try {
			Field field = Screen.class.getDeclaredField("renderables");
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException | SecurityException e) {
			return null;
		}
	}

	private boolean screenHasGuiProperties() {
		return screenHasGuiProperties.getAsBoolean();
	}

	private Rect2i getArea() {
		return new Rect2i(x, y, width, height);
	}

	private static void drawLabel(GuiGraphicsExtractor guiGraphics, Rect2i area) {
		Font font = Minecraft.getInstance().font;
		int textX = area.getX() + (area.getWidth() - font.width(DEBUG_TEXT)) / 2;
		int textY = area.getY() + (area.getHeight() - font.lineHeight) / 2;
		guiGraphics.text(font, DEBUG_TEXT, textX, textY, TEXT_COLOR, false);
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

	private void stopInteraction() {
		dragging = false;
		resizing = false;
		wasLeftButtonDown = false;
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

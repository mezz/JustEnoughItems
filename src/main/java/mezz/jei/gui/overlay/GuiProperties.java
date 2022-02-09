package mezz.jei.gui.overlay;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.renderer.Rect2i;

public class GuiProperties implements IGuiProperties {
	private final Class<? extends Screen> screenClass;
	private final int guiLeft;
	private final int guiTop;
	private final int guiXSize;
	private final int guiYSize;
	private final int screenWidth;
	private final int screenHeight;

	@Nullable
	public static GuiProperties create(AbstractContainerScreen<?> containerScreen) {
		if (containerScreen.width == 0 || containerScreen.height == 0) {
			return null;
		}
		return new GuiProperties(
			containerScreen.getClass(),
			containerScreen.getGuiLeft(),
			containerScreen.getGuiTop(),
			containerScreen.getXSize(),
			containerScreen.getYSize(),
			containerScreen.width,
			containerScreen.height
		);
	}

	public static GuiProperties create(RecipesGui recipesGui) {
		int extraWidth = recipesGui.getRecipeCatalystExtraWidth();
		Rect2i recipeArea = recipesGui.getArea();
		return new GuiProperties(
			recipesGui.getClass(),
			recipeArea.getX() - extraWidth,
			recipeArea.getY(),
			recipeArea.getWidth() + extraWidth,
			recipeArea.getHeight(),
			recipesGui.width,
			recipesGui.height
		);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean areEqual(@Nullable IGuiProperties a, @Nullable IGuiProperties b) {
		if (a == b) {
			return true;
		}
		return a != null && b != null &&
			a.getScreenClass().equals(b.getScreenClass()) &&
			a.getGuiLeft() == b.getGuiLeft() &&
			a.getGuiXSize() == b.getGuiXSize() &&
			a.getScreenWidth() == b.getScreenWidth() &&
			a.getScreenHeight() == b.getScreenHeight();
	}

	public static Rect2i getScreenRectangle(IGuiProperties guiProperties) {
		return new Rect2i(0, 0, guiProperties.getScreenWidth(), guiProperties.getScreenHeight());
	}

	public static Rect2i getGuiRectangle(IGuiProperties guiProperties) {
		return new Rect2i(guiProperties.getGuiLeft(), guiProperties.getGuiTop(), guiProperties.getGuiXSize(), guiProperties.getGuiYSize());
	}

	public static int getGuiRight(IGuiProperties guiProperties) {
		return guiProperties.getGuiLeft() + guiProperties.getGuiXSize();
	}

	public static int getGuiBottom(IGuiProperties guiProperties) {
		return guiProperties.getGuiTop() + guiProperties.getGuiYSize();
	}

	private GuiProperties(Class<? extends Screen> screenClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		this.screenClass = screenClass;
		this.guiLeft = guiLeft;
		this.guiTop = guiTop;
		this.guiXSize = guiXSize;
		this.guiYSize = guiYSize;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	@Override
	public Class<? extends Screen> getScreenClass() {
		return screenClass;
	}

	@Override
	public int getGuiLeft() {
		return guiLeft;
	}

	@Override
	public int getGuiTop() {
		return guiTop;
	}

	@Override
	public int getGuiXSize() {
		return guiXSize;
	}

	@Override
	public int getGuiYSize() {
		return guiYSize;
	}

	@Override
	public int getScreenWidth() {
		return screenWidth;
	}

	@Override
	public int getScreenHeight() {
		return screenHeight;
	}
}

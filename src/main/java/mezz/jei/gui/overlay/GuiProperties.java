package mezz.jei.gui.overlay;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.gui.recipes.RecipesGui;

public class GuiProperties implements IGuiProperties {
	private final Class<? extends Screen> screenClass;
	private final int guiLeft;
	private final int guiTop;
	private final int guiXSize;
	private final int guiYSize;
	private final int screenWidth;
	private final int screenHeight;

	@Nullable
	public static GuiProperties create(ContainerScreen containerScreen) {
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
		return new GuiProperties(
			recipesGui.getClass(),
			recipesGui.getGuiLeft() - extraWidth,
			recipesGui.getGuiTop(),
			recipesGui.getXSize() + extraWidth,
			recipesGui.getYSize(),
			recipesGui.width,
			recipesGui.height
		);
	}

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

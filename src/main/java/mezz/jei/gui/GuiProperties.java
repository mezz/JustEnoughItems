package mezz.jei.gui;

import javax.annotation.Nullable;

import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiProperties {
	private final Class guiClass;
	private final int guiLeft;
	private final int guiTop;
	private final int guiXSize;
	private final int guiYSize;
	private final int screenWidth;
	private final int screenHeight;

	@Nullable
	public static GuiProperties create(GuiScreen guiScreen) {
		if (guiScreen instanceof RecipesGui) {
			return create((RecipesGui) guiScreen);
		} else if (guiScreen instanceof GuiContainer) {
			return create((GuiContainer) guiScreen);
		} else {
			return null;
		}
	}

	public static GuiProperties create(GuiContainer guiContainer) {
		return new GuiProperties(
				guiContainer.getClass(),
				guiContainer.getGuiLeft(),
				guiContainer.getGuiTop(),
				guiContainer.getXSize(),
				guiContainer.getYSize(),
				guiContainer.width,
				guiContainer.height
		);
	}

	public static GuiProperties create(RecipesGui recipesGui) {
		return new GuiProperties(
				recipesGui.getClass(),
				recipesGui.getGuiLeft(),
				recipesGui.getGuiTop(),
				recipesGui.getXSize(),
				recipesGui.getYSize(),
				recipesGui.width,
				recipesGui.height
		);
	}

	private GuiProperties(Class guiClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		this.guiClass = guiClass;
		this.guiLeft = guiLeft;
		this.guiTop = guiTop;
		this.guiXSize = guiXSize;
		this.guiYSize = guiYSize;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	public Class getGuiClass() {
		return guiClass;
	}

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getGuiTop() {
		return guiTop;
	}

	public int getGuiXSize() {
		return guiXSize;
	}

	public int getGuiYSize() {
		return guiYSize;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GuiProperties)) {
			return false;
		}
		GuiProperties other = (GuiProperties) obj;
		return guiClass == other.getGuiClass() &&
				guiLeft == other.getGuiLeft() &&
				guiXSize == other.getGuiXSize() &&
				screenWidth == other.getScreenWidth() &&
				screenHeight == other.getScreenHeight();
	}

	@Override
	public int hashCode() {
		int result = guiClass.hashCode();
		result = 31 * result + guiLeft;
		result = 31 * result + guiXSize;
		result = 31 * result + screenWidth;
		result = 31 * result + screenHeight;
		return result;
	}
}

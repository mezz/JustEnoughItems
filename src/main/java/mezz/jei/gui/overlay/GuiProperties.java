package mezz.jei.gui.overlay;

import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;

public class GuiProperties implements IGuiProperties {
	private final Class<? extends GuiScreen> guiClass;
	private final int guiLeft;
	private final int guiTop;
	private final int guiXSize;
	private final int guiYSize;
	private final int screenWidth;
	private final int screenHeight;

	@Nullable
	public static GuiProperties create(GuiContainer guiContainer) {
		if (guiContainer.width == 0 || guiContainer.height == 0) {
			return null;
		}
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

	private GuiProperties(Class<? extends GuiScreen> guiClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		this.guiClass = guiClass;
		this.guiLeft = guiLeft;
		this.guiTop = guiTop;
		this.guiXSize = guiXSize;
		this.guiYSize = guiYSize;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	@Override
	public Class<? extends GuiScreen> getGuiClass() {
		return guiClass;
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

package mezz.jei.gui.overlay;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.util.Log;

public class GuiProperties implements IGuiProperties {
	private static final int MIN_GUI_POSITION = -1_000_000_000;
	private static final int MAX_GUI_DIMENSION = 1_000_000_000;

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
		return create(
			guiContainer.getClass(),
			guiContainer.getGuiLeft(),
			guiContainer.getGuiTop(),
			guiContainer.getXSize(),
			guiContainer.getYSize(),
			guiContainer.width,
			guiContainer.height
		);
	}

	@Nullable
	public static GuiProperties create(RecipesGui recipesGui) {
		int extraWidth = recipesGui.getRecipeCatalystExtraWidth();
		return create(
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
			a.getGuiClass().equals(b.getGuiClass()) &&
			a.getGuiLeft() == b.getGuiLeft() &&
			a.getGuiXSize() == b.getGuiXSize() &&
			a.getScreenWidth() == b.getScreenWidth() &&
			a.getScreenHeight() == b.getScreenHeight();
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

	@Nullable
	private static GuiProperties create(Class<? extends GuiScreen> guiClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		if (!areValid(guiLeft, guiTop, guiXSize, guiYSize, screenWidth, screenHeight)) {
			Log.get().error("Received invalid GUI properties for screen: {}", guiClass);
			return null;
		}
		return new GuiProperties(guiClass, guiLeft, guiTop, guiXSize, guiYSize, screenWidth, screenHeight);
	}

	private static boolean areValid(int guiLeft, int guiTop, int guiWidth, int guiHeight, int screenWidth, int screenHeight) {
		return isValidPosition(guiLeft) &&
			isValidPosition(guiTop) &&
			isValidDimension(guiWidth) &&
			isValidDimension(guiHeight) &&
			isValidDimension(screenWidth) &&
			isValidDimension(screenHeight);
	}

	private static boolean isValidPosition(int value) {
		return value >= MIN_GUI_POSITION && value <= MAX_GUI_DIMENSION;
	}

	private static boolean isValidDimension(int value) {
		return value > 0 && value <= MAX_GUI_DIMENSION;
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

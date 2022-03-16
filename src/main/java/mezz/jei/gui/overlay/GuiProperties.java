package mezz.jei.gui.overlay;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import mezz.jei.plugins.vanilla.RecipeBookGuiHandler;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
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
		if (containerScreen.width <= 0 || containerScreen.height <= 0) {
			return null;
		}
		int x = containerScreen.getGuiLeft();
		int y = containerScreen.getGuiTop();
		int width = containerScreen.getXSize();
		int height = containerScreen.getYSize();
		if (containerScreen instanceof RecipeUpdateListener r) {
			Rect2i bookArea = RecipeBookGuiHandler.getBookArea(r);
			if (bookArea != null) {
				width += (x - bookArea.getX());
				x = bookArea.getX();
			}
		}

		if (x < 0) {
			width -= x;
			x = 0;
		}
		if (y < 0) {
			height -= y;
			y = 0;
		}
		if (width <= 0 || height <= 0) {
			return null;
		}
		return new GuiProperties(
			containerScreen.getClass(),
			x,
			y,
			width,
			height,
			containerScreen.width,
			containerScreen.height
		);
	}

	@Nullable
	public static GuiProperties create(RecipesGui recipesGui) {
		if (recipesGui.width <= 0 || recipesGui.height <= 0) {
			return null;
		}
		int extraWidth = recipesGui.getRecipeCatalystExtraWidth();
		ImmutableRect2i recipeArea = recipesGui.getArea();
		int guiXSize = recipeArea.getWidth() + extraWidth;
		int guiYSize = recipeArea.getHeight();
		if (guiXSize <= 0 || guiYSize <= 0) {
			return null;
		}
		return new GuiProperties(
			recipesGui.getClass(),
			recipeArea.getX() - extraWidth,
			recipeArea.getY(),
			guiXSize,
			guiYSize,
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

	public static ImmutableRect2i getScreenRectangle(IGuiProperties guiProperties) {
		return new ImmutableRect2i(0, 0, guiProperties.getScreenWidth(), guiProperties.getScreenHeight());
	}

	public static ImmutableRect2i getGuiRectangle(IGuiProperties guiProperties) {
		return new ImmutableRect2i(guiProperties.getGuiLeft(), guiProperties.getGuiTop(), guiProperties.getGuiXSize(), guiProperties.getGuiYSize());
	}

	public static int getGuiRight(IGuiProperties guiProperties) {
		return guiProperties.getGuiLeft() + guiProperties.getGuiXSize();
	}

	public static int getGuiBottom(IGuiProperties guiProperties) {
		return guiProperties.getGuiTop() + guiProperties.getGuiYSize();
	}

	private GuiProperties(Class<? extends Screen> screenClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		Preconditions.checkArgument(guiLeft >= 0, "guiLeft must be >= 0");
		Preconditions.checkArgument(guiTop >= 0, "guiTop must be >= 0");
		Preconditions.checkArgument(guiXSize > 0, "guiXSize must be > 0");
		Preconditions.checkArgument(guiYSize > 0, "guiYSize must be > 0");
		Preconditions.checkArgument(screenWidth > 0, "screenWidth must be > 0");
		Preconditions.checkArgument(screenHeight > 0, "screenHeight must be > 0");
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

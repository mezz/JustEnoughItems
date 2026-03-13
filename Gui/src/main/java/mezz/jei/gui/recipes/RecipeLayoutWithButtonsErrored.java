package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.RecipeLayoutDrawableErrored;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

public final class RecipeLayoutWithButtonsErrored<R> implements IRecipeLayoutWithButtons<R> {
	private final RecipeLayoutDrawableErrored<R> errorLayout;

	public RecipeLayoutWithButtonsErrored(IRecipeLayoutDrawable<R> brokenRecipeLayout) {
		IScalableDrawable recipeBackground = Internal.getTextures().getRecipeBackground();
		this.errorLayout = new RecipeLayoutDrawableErrored<>(brokenRecipeLayout.getRecipeCategory(), brokenRecipeLayout.getRecipe(), recipeBackground, 4);
		Rect2i rect = brokenRecipeLayout.getRect();
		this.errorLayout.setPosition(rect.getX(), rect.getY());
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		errorLayout.drawRecipe(guiGraphics, mouseX, mouseY);
	}

	@Override
	public void updateBounds(int recipeXOffset, int recipeYOffset) {
		Rect2i rectWithBorder = errorLayout.getRectWithBorder();
		Rect2i rect = errorLayout.getRect();
		errorLayout.setPosition(
			recipeXOffset - rectWithBorder.getX() + rect.getX(),
			recipeYOffset - rectWithBorder.getY() + rect.getY()
		);
	}

	@Override
	public int totalWidth() {
		Rect2i area = errorLayout.getRect();
		Rect2i areaWithBorder = errorLayout.getRectWithBorder();
		int leftBorderWidth = area.getX() - areaWithBorder.getX();
		int rightAreaWidth = areaWithBorder.getWidth() - leftBorderWidth;
		return leftBorderWidth + rightAreaWidth;
	}

	@Override
	public IUserInputHandler createUserInputHandler() {
		return new CombinedInputHandler(
			"RecipeLayoutWithButtonsErrored",
			new UserInputHandler<>(errorLayout)
		);
	}

	@Override
	public void tick() {
		errorLayout.tick();
	}

	@Override
	public IRecipeLayoutDrawable<R> getRecipeLayout() {
		return errorLayout;
	}

	@Override
	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {

	}

	@Override
	public int getMissingCountHint() {
		return Integer.MAX_VALUE;
	}

	private record UserInputHandler<R>(IRecipeLayoutDrawable<R> recipeLayout) implements IUserInputHandler {
		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
			final double mouseX = input.getMouseX();
			final double mouseY = input.getMouseY();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				if (recipeLayout.getInputHandler().handleInput(mouseX, mouseY, input)) {
					return Optional.of(this);
				}
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			if (recipeLayout.isMouseOver(mouseX, mouseY) &&
				recipeLayout.getInputHandler().handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY)
			) {
				return Optional.of(this);
			}

			return Optional.empty();
		}
	}
}

package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.textures.Textures;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.network.chat.Component;

public abstract class RecipeGuiTab implements IUserInputHandler {
	public static final int TAB_HEIGHT = 24;
	public static final int TAB_WIDTH = 24;

	protected final int x;
	protected final int y;
	private final HoverChecker hoverChecker;

	public RecipeGuiTab(int x, int y) {
		this.x = x;
		this.y = y;
		this.hoverChecker = new HoverChecker();
		this.hoverChecker.updateBounds(y, y + TAB_HEIGHT, x, x + TAB_WIDTH);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public abstract boolean isSelected(IRecipeCategory<?> selectedCategory);

	public void draw(boolean selected, PoseStack poseStack, int mouseX, int mouseY) {
		Textures textures = Internal.getTextures();
		IDrawable tab = selected ? textures.getTabSelected() : textures.getTabUnselected();

		tab.draw(poseStack, x, y);
	}

	public abstract List<Component> getTooltip();
}

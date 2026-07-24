package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class RecipeGuiTab implements IUserInputHandler {
	public static final int TAB_HEIGHT = 24;
	public static final int TAB_WIDTH = 24;

	protected final int x;
	protected final int y;
	private final ImmutableRect2i area;

	public RecipeGuiTab(int x, int y) {
		this.x = x;
		this.y = y;
		this.area = new ImmutableRect2i(x, y, TAB_WIDTH, TAB_HEIGHT);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public abstract boolean isSelected(IRecipeCategory<?> selectedCategory);

	public void draw(boolean selected, PoseStack poseStack, int mouseX, int mouseY) {
		Textures textures = Internal.getTextures();
		IDrawable tab = selected ? textures.getTabSelected() : textures.getTabUnselected();

		tab.draw(poseStack, x, y);
	}

	public abstract List<Component> getTooltip();
}

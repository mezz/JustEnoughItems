package mezz.jei.gui.recipes;

import javax.annotation.Nullable;

import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class RecipeArrowTab extends RecipeGuiTab {
	private final RecipeGuiTabs recipeGuiTabs;
	private final boolean next;

	public RecipeArrowTab(RecipeGuiTabs recipeGuiTabs, boolean next, int x, int y) {
		super(x, y);
		this.recipeGuiTabs = recipeGuiTabs;
		this.next = next;
	}

	@Override
	public void draw(Minecraft minecraft, boolean selected, int mouseX, int mouseY) {
		super.draw(minecraft, selected, mouseX, mouseY);
		String arrow = next ? ">" : "<";
		FontRenderer fontRenderer = minecraft.fontRendererObj;
		float textCenterX = x + (TAB_WIDTH / 2f);
		float textCenterY = y + (TAB_HEIGHT / 2f) - 3;
		int color = isMouseOver(mouseX, mouseY) ? 16777120 : 14737632;
		fontRenderer.drawStringWithShadow(arrow, textCenterX - fontRenderer.getStringWidth(arrow) / 2f, textCenterY, color);
		GlStateManager.color(1, 1, 1, 1);
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (next) {
			recipeGuiTabs.nextPage();
		} else {
			recipeGuiTabs.prevPage();
		}
		return true;
	}

	@Override
	public boolean isSelected(IRecipeCategory selectedCategory) {
		return false;
	}

	@Nullable
	@Override
	public String getTooltip() {
		return null;
	}
}

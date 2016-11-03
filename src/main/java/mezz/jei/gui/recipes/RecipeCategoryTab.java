package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.plugins.vanilla.ingredients.ItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class RecipeCategoryTab extends RecipeGuiTab {
	private final IRecipeGuiLogic logic;
	private final IRecipeCategory category;

	public RecipeCategoryTab(IRecipeGuiLogic logic, IRecipeCategory category, int x, int y) {
		super(x, y);
		this.logic = logic;
		this.category = category;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		logic.setRecipeCategory(category);
		return true;
	}

	@Override
	public void draw(Minecraft minecraft, boolean selected, int mouseX, int mouseY) {
		super.draw(minecraft, selected, mouseX, mouseY);

		int iconX = x + 4;
		int iconY = y + 4;

		IDrawable icon = getCategoryIcon(category);
		if (icon != null) {
			iconX += (16 - icon.getWidth()) / 2;
			iconY += (16 - icon.getHeight()) / 2;
			icon.draw(minecraft, iconX, iconY);
		} else {
			List<ItemStack> craftingItems = logic.getRecipeCategoryCraftingItems(category);
			if (!craftingItems.isEmpty()) {
				ItemStackRenderer renderer = new ItemStackRenderer();
				ItemStack ingredient = craftingItems.get(0);
				GlStateManager.enableDepth();
				renderer.render(minecraft, iconX, iconY, ingredient);
				GlStateManager.enableAlpha();
				GlStateManager.disableDepth();
			} else {
				String text = category.getTitle().substring(0, 2);
				FontRenderer fontRenderer = minecraft.fontRendererObj;
				float textCenterX = x + (TAB_WIDTH / 2f);
				float textCenterY = y + (TAB_HEIGHT / 2f) - 3;
				int color = isMouseOver(mouseX, mouseY) ? 16777120 : 14737632;
				fontRenderer.drawStringWithShadow(text, textCenterX - fontRenderer.getStringWidth(text) / 2f, textCenterY, color);
				GlStateManager.color(1, 1, 1, 1);
			}
		}
	}

	@Nullable
	private static IDrawable getCategoryIcon(IRecipeCategory recipeCategory) {
		try {
			return recipeCategory.getIcon();
		} catch (AbstractMethodError ignored) { // old recipe categories do not implement this method
			return null;
		}
	}

	@Override
	public boolean isSelected(IRecipeCategory selectedCategory) {
		return category.getUid().equals(selectedCategory.getUid());
	}

	@Nullable
	@Override
	public String getTooltip() {
		return category.getTitle();
	}
}

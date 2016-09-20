package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * The area drawn on top of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class RecipeCategoryCraftingItemsArea implements IShowsRecipeFocuses {
	private final IDrawable leftDrawable;
	private final IDrawable spacerDrawable;
	private final IDrawable rightDrawable;
	private final IDrawable boxDrawable;

	private GuiItemStackGroup craftingItems = new GuiItemStackGroup(new Focus<ItemStack>(null));
	private int left = 0;
	private int top = 0;

	public RecipeCategoryCraftingItemsArea() {
		ResourceLocation recipeBackgroundResource = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");

		IGuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		leftDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 15, 5, 25);
		spacerDrawable = guiHelper.createDrawable(recipeBackgroundResource, 204, 15, 2, 25);
		rightDrawable = guiHelper.createDrawable(recipeBackgroundResource, 209, 15, 5, 25);
		boxDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 40, 18, 25);
	}

	public void updateLayout(Collection<ItemStack> itemStacks, GuiProperties guiProperties) {
		craftingItems = new GuiItemStackGroup(new Focus<ItemStack>(null));

		if (!itemStacks.isEmpty()) {
			int totalWidth = leftDrawable.getWidth() + boxDrawable.getWidth() + rightDrawable.getWidth();
			int ingredientCount = 1;

			final int extraBoxWidth = boxDrawable.getWidth() + spacerDrawable.getWidth();
			for (int i = 1; i < itemStacks.size(); i++) {
				if (totalWidth + extraBoxWidth <= (guiProperties.getGuiXSize() - 8)) {
					totalWidth += extraBoxWidth;
					ingredientCount++;
				} else {
					break;
				}
			}

			left = guiProperties.getGuiLeft() + (guiProperties.getGuiXSize() - totalWidth) / 2; // center it
			top = guiProperties.getGuiTop() - boxDrawable.getHeight() + 3; // overlaps the recipe gui slightly

			List<ItemStack> itemStacksCopy = new ArrayList<ItemStack>(itemStacks);
			for (int i = 0; i < ingredientCount; i++) {
				craftingItems.init(i, true, left + 5 + (i * 20), top + 5);
				if (i + 1 < ingredientCount) {
					ItemStack itemStack = itemStacksCopy.remove(0);
					craftingItems.set(i, itemStack);
				} else {
					// we're out of space. put all the rest of the items into the last box together so they cycle
					craftingItems.set(i, itemStacksCopy);
					break;
				}
			}
		}
	}

	@Nullable
	public GuiIngredient<ItemStack> draw(Minecraft minecraft, int mouseX, int mouseY) {
		int ingredientCount = craftingItems.getGuiIngredients().keySet().size();
		if (ingredientCount > 0) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.disableDepth();
			GlStateManager.enableAlpha();
			{
				int left = this.left;
				leftDrawable.draw(minecraft, left, top);
				left += leftDrawable.getWidth();

				boxDrawable.draw(minecraft, left, top);
				left += boxDrawable.getWidth();

				while (--ingredientCount > 0) {
					spacerDrawable.draw(minecraft, left, top);
					left += spacerDrawable.getWidth();
					boxDrawable.draw(minecraft, left, top);
					left += boxDrawable.getWidth();
				}

				rightDrawable.draw(minecraft, left, top);
			}
			GlStateManager.disableAlpha();
			GlStateManager.enableDepth();

			RenderHelper.enableGUIStandardItemLighting();
			GuiIngredient<ItemStack> hovered = craftingItems.draw(minecraft, 0, 0, mouseX, mouseY);
			RenderHelper.disableStandardItemLighting();

			return hovered;
		}
		return null;
	}

	@Nullable
	@Override
	public Focus<?> getFocusUnderMouse(int mouseX, int mouseY) {
		return craftingItems.getFocusUnderMouse(0, 0, mouseX, mouseY);
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}
}

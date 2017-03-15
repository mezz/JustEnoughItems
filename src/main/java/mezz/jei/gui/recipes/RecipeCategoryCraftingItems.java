package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import mezz.jei.Internal;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * The area drawn on left side of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class RecipeCategoryCraftingItems implements IShowsRecipeFocuses {
	private final IDrawable topDrawable;
	private final IDrawable middleDrawable;
	private final IDrawable bottomDrawable;

	private GuiItemStackGroup craftingItems;
	private int left = 0;
	private int top = 0;

	public RecipeCategoryCraftingItems() {
		craftingItems = new GuiItemStackGroup(null, 0);

		ResourceLocation recipeBackgroundResource = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);

		IGuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		topDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 65, 26, 6);
		middleDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 71, 26, 16);
		bottomDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 87, 26, 6);
	}

	public void updateLayout(List<ItemStack> itemStacks, GuiProperties guiProperties) {
		craftingItems = new GuiItemStackGroup(null, 0);

		if (!itemStacks.isEmpty()) {
			int totalHeight = topDrawable.getHeight() + middleDrawable.getHeight() + bottomDrawable.getHeight();
			int ingredientCount = 1;

			final int extraBoxHeight = middleDrawable.getHeight();
			for (int i = 1; i < itemStacks.size(); i++) {
				if (totalHeight + extraBoxHeight <= (guiProperties.getGuiYSize() - 8)) {
					totalHeight += extraBoxHeight;
					ingredientCount++;
				} else {
					break;
				}
			}

			top = guiProperties.getGuiTop();
			left = guiProperties.getGuiLeft() - topDrawable.getWidth() + 4; // overlaps the recipe gui slightly

			ListMultimap<Integer, ItemStack> itemStacksForSlots = ArrayListMultimap.create();
			for (int i = 0; i < itemStacks.size(); i++) {
				ItemStack itemStack = itemStacks.get(i);
				if (i < ingredientCount) {
					itemStacksForSlots.put(i, itemStack);
				} else {
					// start from the end and work our way back, do not override the first one
					int index = ingredientCount - (i % ingredientCount);
					itemStacksForSlots.put(index, itemStack);
				}
			}

			for (int i = 0; i < ingredientCount; i++) {
				craftingItems.init(i, true, left + 5, top + 5 + (i * middleDrawable.getHeight()));
				List<ItemStack> itemStacksForSlot = itemStacksForSlots.get(i);
				craftingItems.set(i, itemStacksForSlot);
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
				int top = this.top;
				topDrawable.draw(minecraft, this.left, top);
				top += topDrawable.getHeight();

				while (ingredientCount-- > 0) {
					middleDrawable.draw(minecraft, this.left, top);
					top += middleDrawable.getHeight();
				}

				bottomDrawable.draw(minecraft, this.left, top);
			}
			GlStateManager.disableAlpha();
			GlStateManager.enableDepth();

			return craftingItems.draw(minecraft, 0, 0, mouseX, mouseY);
		}
		return null;
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		ItemStack ingredientUnderMouse = craftingItems.getIngredientUnderMouse(0, 0, mouseX, mouseY);
		if (ingredientUnderMouse != null) {
			return new ClickedIngredient<Object>(ingredientUnderMouse);
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}
}

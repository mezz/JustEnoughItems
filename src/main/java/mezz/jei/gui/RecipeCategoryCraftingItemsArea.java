package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import mezz.jei.Internal;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * The area drawn on top of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class RecipeCategoryCraftingItemsArea implements IShowsRecipeFocuses {
	private final IRecipeRegistry recipeRegistry;
	private final IDrawable leftDrawable;
	private final IDrawable spacerDrawable;
	private final IDrawable rightDrawable;
	private final IDrawable boxDrawable;

	private GuiItemStackGroup craftingItems;
	private int left = 0;
	private int top = 0;

	public RecipeCategoryCraftingItemsArea(IRecipeRegistry recipeRegistry) {
		this.recipeRegistry = recipeRegistry;
		IFocus<ItemStack> focus = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		craftingItems = new GuiItemStackGroup(focus);

		ResourceLocation recipeBackgroundResource = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);

		IGuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		leftDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 15, 5, 25);
		spacerDrawable = guiHelper.createDrawable(recipeBackgroundResource, 204, 15, 2, 25);
		rightDrawable = guiHelper.createDrawable(recipeBackgroundResource, 209, 15, 5, 25);
		boxDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 40, 18, 25);
	}

	public void updateLayout(List<ItemStack> itemStacks, GuiProperties guiProperties) {
		IFocus<ItemStack> focus = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		craftingItems = new GuiItemStackGroup(focus);

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
				craftingItems.init(i, true, left + 5 + (i * 20), top + 5);
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

			return craftingItems.draw(minecraft, 0, 0, mouseX, mouseY);
		}
		return null;
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		return craftingItems.getIngredientUnderMouse(0, 0, mouseX, mouseY);
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}
}

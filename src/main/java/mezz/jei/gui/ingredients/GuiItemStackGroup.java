package mezz.jei.gui.ingredients;

import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IGuiItemStackGroup;

public class GuiItemStackGroup extends GuiIngredientGroup<ItemStack, GuiIngredient<ItemStack>> implements IGuiItemStackGroup {
	private static final int baseWidth = 16;
	private static final int baseHeight = 16;
	private static final ItemStackRenderer renderer = new ItemStackRenderer();
	private static final ItemStackHelper helper = new ItemStackHelper();

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}

	public static GuiIngredient<ItemStack> createGuiItemStack(int xPosition, int yPosition, int padding) {
		return new GuiIngredient<>(renderer, helper, xPosition, yPosition, getWidth(padding), getHeight(padding), padding);
	}

	@Override
	public void init(int index, int xPosition, int yPosition) {
		init(index, xPosition, yPosition, 1);
	}

	public void init(int index, int xPosition, int yPosition, int padding) {
		GuiIngredient<ItemStack> guiIngredient = createGuiItemStack(xPosition, yPosition, padding);
		guiIngredients.put(index, guiIngredient);
	}
}

package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;

public class GuiItemStackGroup extends GuiIngredientGroup<ItemStack> implements IGuiItemStackGroup {
	private static final int baseWidth = 16;
	private static final int baseHeight = 16;
	private static final ItemStackRenderer renderer = new ItemStackRenderer();

	public GuiItemStackGroup(@Nullable IFocus<ItemStack> focus, int cycleOffset) {
		super(VanillaTypes.ITEM, focus, cycleOffset);
	}

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
		init(slotIndex, input, renderer, xPosition, yPosition, getWidth(1), getHeight(1), 1, 1);
	}

}

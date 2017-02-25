package mezz.jei.gui.ingredients;

import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.plugins.vanilla.ingredients.ItemStackRenderer;
import net.minecraft.item.ItemStack;

public class GuiItemStackGroup extends GuiIngredientGroup<ItemStack> implements IGuiItemStackGroup {
	private static final int baseWidth = 16;
	private static final int baseHeight = 16;
	private static final ItemStackRenderer renderer = new ItemStackRenderer();

	public GuiItemStackGroup(IFocus<ItemStack> focus, int cycleOffset) {
		super(ItemStack.class, focus, cycleOffset);
	}

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}

	@Override
	public void setFromRecipe(int slotIndex, List ingredients) {
		set(slotIndex, Internal.getStackHelper().toItemStackList(ingredients));
	}

	@Override
	public void setFromRecipe(int slotIndex, Object ingredients) {
		set(slotIndex, Internal.getStackHelper().toItemStackList(ingredients));
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
		init(slotIndex, input, xPosition, yPosition, 1);
	}

	public void init(int index, boolean input, int xPosition, int yPosition, int padding) {
		init(index, input, renderer, xPosition, yPosition, getWidth(padding), getHeight(padding), padding, padding);
	}
}

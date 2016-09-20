package mezz.jei.gui.ingredients;

import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack;

public class GuiItemStackGroup extends GuiIngredientGroup<ItemStack, GuiIngredient<ItemStack>> implements IGuiItemStackGroup {
	private static final int baseWidth = 16;
	private static final int baseHeight = 16;
	private static final ItemStackHelper helper = new ItemStackHelper();

	public GuiItemStackGroup(IFocus<ItemStack> focus) {
		super(focus);
	}

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}

	private GuiIngredient<ItemStack> createGuiItemStack(int index, boolean input, int xPosition, int yPosition, int padding) {
		ItemStackRenderer renderer = new ItemStackRenderer();
		return new GuiIngredient<ItemStack>(renderer, helper, index, input, xPosition, yPosition, getWidth(padding), getHeight(padding), padding, itemCycleOffset);
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
		GuiIngredient<ItemStack> guiIngredient = createGuiItemStack(index, input, xPosition, yPosition, padding);
		guiIngredients.put(index, guiIngredient);
	}
}

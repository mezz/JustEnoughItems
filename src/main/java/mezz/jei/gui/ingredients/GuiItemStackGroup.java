package mezz.jei.gui.ingredients;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import net.minecraft.world.item.ItemStack;

public class GuiItemStackGroup extends GuiIngredientGroup<ItemStack> implements IGuiItemStackGroup {
	private static final ItemStackRenderer renderer = new ItemStackRenderer();

	public GuiItemStackGroup(IIngredientManager ingredientManager, int cycleOffset) {
		super(ingredientManager, VanillaTypes.ITEM, cycleOffset);
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
		init(slotIndex, input, renderer, xPosition, yPosition, GuiIngredientProperties.getWidth(1), GuiIngredientProperties.getHeight(1), 1, 1);
	}

}

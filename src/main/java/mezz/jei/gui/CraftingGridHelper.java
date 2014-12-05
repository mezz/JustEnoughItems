package mezz.jei.gui;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CraftingGridHelper implements ICraftingGridHelper {

	private final int craftInputSlot1;
	private final int craftOutputSlot;

	public CraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		this.craftInputSlot1 = craftInputSlot1;
		this.craftOutputSlot = craftOutputSlot;
	}

	public void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input) {
		int width, height;
		if (input.size() > 4)
			width = height = 3;
		else if (input.size() > 1)
			width = height = 2;
		else
			width = height = 1;

		setInput(guiItemStacks, input, width, height);
	}

	public void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input, int width, int height) {
		for (int i = 0; i < input.size(); i++) {
			Object recipeItem = input.get(i);
			int index;
			if (width == 1) {
				if (height == 3)
					index = (i * 3) + 1;
				else if (height == 2)
					index = (i * 3) + 4;
				else
					index = 4;
			} else if (height == 1) {
				index = i + 6;
			} else if (width == 2) {
				index = i;
				if (i > 1) {
					index++;
					if (i > 3)
						index++;
				}
			} else if (height == 2) {
				index = i + 3;
			} else {
				index = i;
			}

			if (recipeItem instanceof ItemStack) {
				List<ItemStack> itemStacks = Collections.singletonList((ItemStack) recipeItem);
				setInput(guiItemStacks, index, itemStacks);
			} else if (recipeItem instanceof Iterable) {
				List<ItemStack> itemStacks = StackUtil.toItemStackList((Iterable) recipeItem);
				setInput(guiItemStacks, index, itemStacks);
			}
		}
	}

	public void setOutput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List<ItemStack> output) {
		guiItemStacks.setItemStack(craftOutputSlot, output);
	}

	private void setInput(@Nonnull IGuiItemStacks guiItemStacks, int inputIndex, @Nonnull Iterable<ItemStack> input) {
		guiItemStacks.setItemStack(craftInputSlot1 + inputIndex, input);
	}

}

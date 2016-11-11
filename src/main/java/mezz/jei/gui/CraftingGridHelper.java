package mezz.jei.gui;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import net.minecraft.item.ItemStack;

public class CraftingGridHelper implements ICraftingGridHelper {
	private final int craftInputSlot1;
	private final int craftOutputSlot;

	public CraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		this.craftInputSlot1 = craftInputSlot1;
		this.craftOutputSlot = craftOutputSlot;
	}

	@Override
	public void setInputStacks(IGuiItemStackGroup guiItemStacks, List<List<ItemStack>> input) {
		int width, height;
		if (input.size() > 4) {
			width = height = 3;
		} else if (input.size() > 1) {
			width = height = 2;
		} else {
			width = height = 1;
		}

		setInputStacks(guiItemStacks, input, width, height);
	}

	@Override
	public void setInputStacks(IGuiItemStackGroup guiItemStacks, List<List<ItemStack>> input, int width, int height) {
		for (int i = 0; i < input.size(); i++) {
			List<ItemStack> recipeItem = input.get(i);
			int index = getCraftingIndex(i, width, height);

			setInput(guiItemStacks, index, recipeItem);
		}
	}

	private int getCraftingIndex(int i, int width, int height) {
		int index;
		if (width == 1) {
			if (height == 3) {
				index = (i * 3) + 1;
			} else if (height == 2) {
				index = (i * 3) + 1;
			} else {
				index = 4;
			}
		} else if (height == 1) {
			index = i + 3;
		} else if (width == 2) {
			index = i;
			if (i > 1) {
				index++;
				if (i > 3) {
					index++;
				}
			}
		} else if (height == 2) {
			index = i + 3;
		} else {
			index = i;
		}
		return index;
	}

	@Override
	public void setOutput(IGuiItemStackGroup guiItemStacks, List<ItemStack> output) {
		guiItemStacks.set(craftOutputSlot, output);
	}

	private void setInput(IGuiItemStackGroup guiItemStacks, int inputIndex, List<ItemStack> input) {
		guiItemStacks.set(craftInputSlot1 + inputIndex, input);
	}

}

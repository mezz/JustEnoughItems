package mezz.jei.input;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

public interface IShowsItemStacks {

	@Nullable
	ItemStack getStackUnderMouse(int mouseX, int mouseY);

}

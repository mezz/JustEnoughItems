package mezz.jei.input;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IShowsItemStacks {

	@Nullable
	public ItemStack getStackUnderMouse(int mouseX, int mouseY);

}

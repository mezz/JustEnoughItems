package mezz.jei.input;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

public interface IShowsItemStacks {

	@Nullable
	public ItemStack getStackUnderMouse(int mouseX, int mouseY);

}

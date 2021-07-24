package mezz.jei.input;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

public interface IClickedIngredient<V> {

	V getValue();

	@Nullable
	Rect2i getArea();

	ItemStack getCheatItemStack();
}

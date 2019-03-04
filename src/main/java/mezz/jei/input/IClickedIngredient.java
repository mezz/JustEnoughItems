package mezz.jei.input;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;

public interface IClickedIngredient<V> {

	V getValue();

	@Nullable
	Rectangle2d getArea();

	ItemStack getCheatItemStack();

	void onClickHandled();

	void setOnClickHandler(IOnClickHandler onClickHandler);

	@FunctionalInterface
	interface IOnClickHandler {
		void onClick();
	}
}

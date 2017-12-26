package mezz.jei.input;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.Rectangle;

public interface IClickedIngredient<V> {

	V getValue();

	@Nullable
	Rectangle getArea();

	ItemStack getCheatItemStack();

	void onClickHandled();

	void setOnClickHandler(IOnClickHandler onClickHandler);

	@FunctionalInterface
	interface IOnClickHandler {
		void onClick();
	}
}

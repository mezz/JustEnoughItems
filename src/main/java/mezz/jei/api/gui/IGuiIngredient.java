package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;

public interface IGuiIngredient<T> {
	@Nullable
	IFocus<T> getCurrentlyDisplayed();

	@Nonnull
	List<T> getAllIngredients();

	boolean isInput();

	void drawHighlight(@Nonnull Minecraft minecraft, Color color, int xOffset, int yOffset);
}

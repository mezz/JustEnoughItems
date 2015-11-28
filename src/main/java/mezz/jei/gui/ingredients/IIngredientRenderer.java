package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public interface IIngredientRenderer<T> {
	void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nonnull T value);

	List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull T value);

	FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull T value);
}

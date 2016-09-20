package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public interface IIngredientRenderer<T> {
	void setIngredients(Collection<T> ingredients);

	void draw(Minecraft minecraft, int xPosition, int yPosition, @Nullable T value);

	List<String> getTooltip(Minecraft minecraft, T value);

	FontRenderer getFontRenderer(Minecraft minecraft, T value);
}

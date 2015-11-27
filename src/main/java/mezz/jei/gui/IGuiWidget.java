package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraft.client.Minecraft;

public interface IGuiWidget<T> {
	void set(@Nonnull T contained, @Nonnull Focus focus);

	void set(@Nonnull Collection<T> contained, @Nonnull Focus focus);

	void clear();

	@Nullable
	T get();

	boolean isMouseOver(int mouseX, int mouseY);

	void draw(@Nonnull Minecraft minecraft);

	void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY);
}

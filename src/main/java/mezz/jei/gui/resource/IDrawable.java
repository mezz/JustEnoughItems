package mezz.jei.gui.resource;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public interface IDrawable {

	void draw(@Nonnull Minecraft minecraft, int x, int y);

}

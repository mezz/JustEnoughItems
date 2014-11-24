package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public interface IDrawable {

	int getWidth();
	int getHeight();
	void draw(@Nonnull Minecraft minecraft);

}

package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;

/**
 * An extension of {@link IDrawable} that allows masking parts of the image.
 */
public interface IDrawableStatic extends IDrawable {
	/**
	 * Draw only part of the image, by masking off parts of it
	 */
	void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight);
}

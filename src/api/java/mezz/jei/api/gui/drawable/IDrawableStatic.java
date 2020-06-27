package mezz.jei.api.gui.drawable;

import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * An extension of {@link IDrawable} that allows masking parts of the image.
 */
public interface IDrawableStatic extends IDrawable {
	/**
	 * Draw only part of the image, by masking off parts of it
	 */
	void draw(MatrixStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight);
}

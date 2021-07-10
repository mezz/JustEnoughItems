package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.Set;

import mezz.jei.input.IMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.input.IShowsRecipeFocuses;

public interface ILeftAreaContent extends IShowsRecipeFocuses {

	void drawScreen(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

	void drawTooltips(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY);

	void updateBounds(Rectangle2d area, Set<Rectangle2d> guiExclusionAreas);

	IMouseHandler getMouseHandler();

}

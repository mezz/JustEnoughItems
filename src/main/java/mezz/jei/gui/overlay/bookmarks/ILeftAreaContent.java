package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Set;

import mezz.jei.input.IMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.input.IShowsRecipeFocuses;

public interface ILeftAreaContent extends IShowsRecipeFocuses {

	void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks);

	void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY);

	void updateBounds(Rect2i area, Set<Rect2i> guiExclusionAreas);

	IMouseHandler getMouseHandler();

}

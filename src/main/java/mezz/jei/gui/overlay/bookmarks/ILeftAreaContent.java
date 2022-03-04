package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Set;

import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;

import mezz.jei.input.IRecipeFocusSource;

public interface ILeftAreaContent extends IRecipeFocusSource {

	void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks);

	void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY);

	boolean updateBounds(ImmutableRect2i area, Set<ImmutableRect2i> guiExclusionAreas);

	IUserInputHandler createInputHandler();
}

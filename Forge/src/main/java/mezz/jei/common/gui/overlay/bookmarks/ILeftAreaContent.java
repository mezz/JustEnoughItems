package mezz.jei.common.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Set;

import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;

import mezz.jei.common.input.IRecipeFocusSource;

public interface ILeftAreaContent extends IRecipeFocusSource {

	void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks);

	void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY);

	boolean updateBounds(ImmutableRect2i area, Set<ImmutableRect2i> guiExclusionAreas);

	IUserInputHandler createInputHandler();
}

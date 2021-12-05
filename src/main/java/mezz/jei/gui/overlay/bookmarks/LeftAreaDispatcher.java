package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.NullUserInputHandler;
import mezz.jei.input.mouse.handlers.ProxyUserInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nullable;
import java.util.Set;

public class LeftAreaDispatcher implements IRecipeFocusSource {
	private static final int BORDER_PADDING = 2;

	private final ILeftAreaContent contents;
	private final GuiScreenHelper guiScreenHelper;
	@Nullable
	private IGuiProperties guiProperties;
	private Rect2i displayArea = new Rect2i(0, 0, 0, 0);
	private boolean canShow = false;

	public LeftAreaDispatcher(GuiScreenHelper guiScreenHelper, ILeftAreaContent contents) {
		this.guiScreenHelper = guiScreenHelper;
		this.contents = contents;
	}

	public void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (canShow) {
			contents.drawScreen(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (canShow) {
			contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}
	}

	public void updateScreen(@Nullable Screen guiScreen, boolean forceUpdate) {
		canShow = false;
		IGuiProperties currentGuiProperties = guiScreenHelper.getGuiProperties(guiScreen);
		if (currentGuiProperties == null) {
			guiProperties = null;
		} else {
			if (forceUpdate || !GuiProperties.areEqual(guiProperties, currentGuiProperties)) {
				Set<Rect2i> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
				guiProperties = currentGuiProperties;
				makeDisplayArea(guiProperties);
				contents.updateBounds(displayArea, guiExclusionAreas);
			}
			canShow = true;
		}
	}

	private void makeDisplayArea(IGuiProperties guiProperties) {
		final int x = BORDER_PADDING;
		final int y = BORDER_PADDING;
		int width = guiProperties.getGuiLeft() - x - BORDER_PADDING;
		final int height = guiProperties.getScreenHeight() - y - BORDER_PADDING;
		displayArea = new Rect2i(x, y, width, height);
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (canShow) {
			return contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return null;
	}

	public IUserInputHandler createInputHandler() {
		IUserInputHandler contentsInputHandler = this.contents.createInputHandler();
		return new ProxyUserInputHandler(() -> {
			if (canShow) {
				return contentsInputHandler;
			}
			return NullUserInputHandler.INSTANCE;
		});
	}
}

package mezz.jei.common.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.GuiProperties;
import mezz.jei.common.input.IClickedIngredient;
import mezz.jei.common.input.IRecipeFocusSource;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.handlers.NullInputHandler;
import mezz.jei.common.input.handlers.ProxyInputHandler;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class LeftAreaDispatcher implements IRecipeFocusSource {
	private static final int BORDER_MARGIN = 6;

	private final ILeftAreaContent contents;
	private final GuiScreenHelper guiScreenHelper;
	@Nullable
	private IGuiProperties guiProperties;
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
				Set<ImmutableRect2i> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
				guiProperties = currentGuiProperties;
				ImmutableRect2i displayArea = makeDisplayArea(guiProperties);
				if (displayArea.isEmpty()) {
					canShow = false;
				} else {
					contents.updateBounds(displayArea, guiExclusionAreas);
					canShow = true;
				}
			} else {
				canShow = true;
			}
		}
	}

	private static ImmutableRect2i makeDisplayArea(IGuiProperties guiProperties) {
		int guiLeft = guiProperties.getGuiLeft();
		int screenHeight = guiProperties.getScreenHeight();
		if (guiLeft <= 2 * BORDER_MARGIN || screenHeight < 2 * BORDER_MARGIN) {
			return ImmutableRect2i.EMPTY;
		}
		return new ImmutableRect2i(0, 0, guiLeft, screenHeight)
			.insetBy(BORDER_MARGIN);
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (canShow) {
			return contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	public IUserInputHandler createInputHandler() {
		IUserInputHandler contentsInputHandler = this.contents.createInputHandler();
		return new ProxyInputHandler(() -> {
			if (canShow) {
				return contentsInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
	}
}

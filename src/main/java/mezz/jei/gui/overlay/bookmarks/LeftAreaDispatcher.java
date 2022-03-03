package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.NullInputHandler;
import mezz.jei.input.mouse.handlers.ProxyInputHandler;
import mezz.jei.util.ImmutableRect2i;
import mezz.jei.util.MutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class LeftAreaDispatcher implements IRecipeFocusSource {
	private static final int SCREEN_EDGE_PADDING = 7;

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
				canShow = makeDisplayArea(guiProperties)
					.map(displayArea -> {
						contents.updateBounds(displayArea, guiExclusionAreas);
						return true;
					})
					.orElse(false);
			} else {
				canShow = true;
			}
		}
	}

	private static Optional<ImmutableRect2i> makeDisplayArea(IGuiProperties guiProperties) {
		return new MutableRect2i(0, 0, guiProperties.getGuiLeft(), guiProperties.getScreenHeight())
			.insetByPadding(SCREEN_EDGE_PADDING)
			.toImmutableSafe();
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

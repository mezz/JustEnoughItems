package mezz.jei.input.click;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IMouseHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

import javax.annotation.Nullable;

public class GuiAreaClickHandler implements IMouseHandler {
	private final RecipesGui recipesGui;
	private final IGuiClickableArea clickableArea;
	private final ContainerScreen<?> guiContainer;

	public GuiAreaClickHandler(RecipesGui recipesGui, IGuiClickableArea clickableArea, ContainerScreen<?> guiContainer) {
		this.recipesGui = recipesGui;
		this.clickableArea = clickableArea;
		this.guiContainer = guiContainer;
	}

	@Override
	@Nullable
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player != null && !player.inventory.getCarried().isEmpty()) {
			return null;
		}
		if (!MathUtil.contains(clickableArea.getArea(), mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop())) {
			return null;
		}
		if (!clickState.isSimulate()) {
			clickableArea.onClick(Focus::new, recipesGui);
		}
		return this;
	}
}

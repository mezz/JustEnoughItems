package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.MathUtil;

public class LeftAreaDispatcher implements IShowsRecipeFocuses, IPaged {

	private static final int BORDER_PADDING = 2;
	private static final int NAVIGATION_HEIGHT = 20;

	private final List<ILeftAreaContent> contents = new ArrayList<>();
	private final GuiScreenHelper guiScreenHelper;
	private int current = 0;
	@Nullable
	private IGuiProperties guiProperties;
	private Rectangle2d naviArea = new Rectangle2d(0, 0, 0, 0);
	private Rectangle2d displayArea = new Rectangle2d(0, 0, 0, 0);
	private final PageNavigation navigation;
	private boolean canShow = false;

	public LeftAreaDispatcher(GuiScreenHelper guiScreenHelper) {
		this.guiScreenHelper = guiScreenHelper;
		this.navigation = new PageNavigation(this, false);
	}

	public void addContent(ILeftAreaContent content) {
		this.contents.add(content);
	}

	private boolean hasContent() {
		return current >= 0 && current < contents.size();
	}

	public void drawScreen(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (canShow && hasContent()) {
			contents.get(current).drawScreen(minecraft, matrixStack, mouseX, mouseY, partialTicks);
			if (naviArea.getHeight() > 0) {
				navigation.draw(minecraft, matrixStack, mouseX, mouseY, partialTicks);
			}
		}
	}

	public void drawOnForeground(ContainerScreen<?> gui, int mouseX, int mouseY) {
		if (canShow && hasContent()) {
			contents.get(current).drawOnForeground(gui, mouseX, mouseY);
		}
	}

	public void drawTooltips(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		if (canShow && hasContent()) {
			contents.get(current).drawTooltips(minecraft, matrixStack, mouseX, mouseY);
		}
	}

	public void updateScreen(@Nullable Screen guiScreen, boolean forceUpdate) {
		canShow = false;
		if (hasContent()) {
			IGuiProperties currentGuiProperties = guiScreenHelper.getGuiProperties(guiScreen);
			if (currentGuiProperties == null) {
				guiProperties = null;
			} else {
				ILeftAreaContent content = contents.get(current);
				if (forceUpdate || !GuiProperties.areEqual(guiProperties, currentGuiProperties)) {
					Set<Rectangle2d> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
					guiProperties = currentGuiProperties;
					makeDisplayArea(guiProperties);
					content.updateBounds(displayArea, guiExclusionAreas);
					canShow = true;
				} else {
					canShow = true;
				}
			}
		}
	}

	private void makeDisplayArea(IGuiProperties guiProperties) {
		final int x = BORDER_PADDING;
		final int y = BORDER_PADDING;
		int width = guiProperties.getGuiLeft() - x - BORDER_PADDING;
		final int height = guiProperties.getScreenHeight() - y - BORDER_PADDING;
		displayArea = new Rectangle2d(x, y, width, height);
		if (contents.size() > 1) {
			naviArea = new Rectangle2d(
				displayArea.getX(),
				displayArea.getY(),
				displayArea.getWidth(),
				NAVIGATION_HEIGHT
			);
			displayArea = new Rectangle2d(
				displayArea.getX(),
				displayArea.getY() + NAVIGATION_HEIGHT + BORDER_PADDING,
				displayArea.getWidth(),
				displayArea.getHeight() - NAVIGATION_HEIGHT + BORDER_PADDING
			);
			navigation.updateBounds(naviArea);
		} else {
			naviArea = new Rectangle2d(0, 0, 0, 0);
		}
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (canShow && hasContent()) {
			return contents.get(current).getIngredientUnderMouse(mouseX, mouseY);
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		if (canShow && hasContent()) {
			return contents.get(current).canSetFocusWithMouse();
		}
		return false;
	}

	public boolean handleMouseScrolled(double mouseX, double mouseY, double dWheel) {
		if (canShow && hasContent()) {
			if (MathUtil.contains(displayArea, mouseX, mouseY)) {
				return contents.get(current).handleMouseScrolled(mouseX, mouseY, dWheel);
			} else if (MathUtil.contains(naviArea, mouseX, mouseY)) {
				if (dWheel < 0) {
					nextPage();
				} else {
					previousPage();
				}
				return true;
			}
		}
		return false;
	}

	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (canShow && hasContent()) {
			if (MathUtil.contains(displayArea, mouseX, mouseY)) {
				return contents.get(current).handleMouseClicked(mouseX, mouseY, mouseButton);
			} else if (MathUtil.contains(naviArea, mouseX, mouseY)) {
				return navigation.handleMouseClickedButtons(mouseX, mouseY, mouseButton);
			}
		}
		return false;
	}

	@Override
	public boolean nextPage() {
		current++;
		if (current >= contents.size()) {
			current = 0;
		}
		navigation.updatePageState();
		return true;
	}

	@Override
	public boolean previousPage() {
		current--;
		if (current < 0) {
			current = contents.size();
		}
		navigation.updatePageState();
		return true;
	}

	@Override
	public boolean hasNext() {
		return current < contents.size() - 1;
	}

	@Override
	public boolean hasPrevious() {
		return current > 0;
	}

	@Override
	public int getPageCount() {
		return contents.size();
	}

	@Override
	public int getPageNumber() {
		return current;
	}

}

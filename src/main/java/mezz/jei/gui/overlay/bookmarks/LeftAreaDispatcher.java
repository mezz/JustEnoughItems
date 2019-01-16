package mezz.jei.gui.overlay.bookmarks;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;

public class LeftAreaDispatcher implements IShowsRecipeFocuses, IPaged {

	private static final int BORDER_PADDING = 2;
	private static final int NAVIGATION_HEIGHT = 20;

	private final List<ILeftAreaContent> contents = new ArrayList<>();
	private final GuiScreenHelper guiScreenHelper;
	private int current = 0;
	@Nullable
	private IGuiProperties guiProperties;
	private Rectangle naviArea = new Rectangle();
	private Rectangle displayArea = new Rectangle();
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

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (canShow && hasContent()) {
			contents.get(current).drawScreen(minecraft, mouseX, mouseY, partialTicks);
			if (naviArea.height > 0) {
				navigation.draw(minecraft, mouseX, mouseY, partialTicks);
			}
		}
	}

	public void drawOnForeground(GuiContainer gui, int mouseX, int mouseY) {
		if (canShow && hasContent()) {
			contents.get(current).drawOnForeground(gui, mouseX, mouseY);
		}
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (canShow && hasContent()) {
			contents.get(current).drawTooltips(minecraft, mouseX, mouseY);
		}
	}

	public void updateScreen(@Nullable GuiScreen guiScreen, boolean forceUpdate) {
		canShow = false;
		if (hasContent()) {
			IGuiProperties currentGuiProperties = guiScreenHelper.getGuiProperties(guiScreen);
			if (currentGuiProperties == null) {
				guiProperties = null;
			} else {
				ILeftAreaContent content = contents.get(current);
				if (forceUpdate || !GuiProperties.areEqual(guiProperties, currentGuiProperties)) {
					Set<Rectangle> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
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
		displayArea = new Rectangle(x, y, width, height);
		if (contents.size() > 1) {
			naviArea = new Rectangle(displayArea);
			naviArea.height = NAVIGATION_HEIGHT;
			displayArea.y += NAVIGATION_HEIGHT + BORDER_PADDING;
			displayArea.height -= NAVIGATION_HEIGHT + BORDER_PADDING;
			navigation.updateBounds(naviArea);
		} else {
			naviArea = new Rectangle();
		}
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
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

	public boolean handleMouseScrolled(int mouseX, int mouseY, int dWheel) {
		if (canShow && hasContent()) {
			if (displayArea.contains(mouseX, mouseY)) {
				return contents.get(current).handleMouseScrolled(mouseX, mouseY, dWheel);
			} else if (naviArea.contains(mouseX, mouseY)) {
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

	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (canShow && hasContent()) {
			if (displayArea.contains(mouseX, mouseY)) {
				return contents.get(current).handleMouseClicked(mouseX, mouseY, mouseButton);
			} else if (naviArea.contains(mouseX, mouseY)) {
				return navigation.handleMouseClickedButtons(mouseX, mouseY);
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

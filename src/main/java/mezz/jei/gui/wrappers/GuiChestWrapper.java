package mezz.jei.gui.wrappers;

import mezz.jei.gui.GuiContainerOverlay;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;

import java.lang.reflect.Field;

public class GuiChestWrapper extends GuiChest {
	private GuiContainerOverlay overlay;

	private static IInventory getUpperChestInventory(GuiChest chest) {
		try {
			Field f = chest.getClass().getDeclaredField("upperChestInventory");
			f.setAccessible(true);
			return (IInventory) f.get(chest);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	private static IInventory getLowerChestInventory(GuiChest chest) {
		try {
			Field f = chest.getClass().getDeclaredField("lowerChestInventory");
			f.setAccessible(true);
			return (IInventory) f.get(chest);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	public GuiChestWrapper(GuiChest gui) {
		super(getUpperChestInventory(gui), getLowerChestInventory(gui));
	}

	@Override
	public void initGui() {
		super.initGui();
		overlay = new GuiContainerOverlay(guiLeft, xSize, width, height);
		overlay.initGui(buttonList);
	}

	@Override
	public void drawScreen(int xSize, int ySize, float var3) {
		super.drawScreen(xSize, ySize, var3);
		overlay.drawScreen(mc.getTextureManager(), fontRendererObj);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		overlay.actionPerformed(button);
	}

	@Override
	protected void mouseClicked(int xPos, int yPos, int mouseButton) {
		super.mouseClicked(xPos, yPos, mouseButton);
		overlay.mouseClicked(xPos, yPos, mouseButton);
	}
}

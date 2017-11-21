package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public enum GiveMode {
	INVENTORY {
		@Override
		public int getStackSize(ItemStack itemStack, int mouseButton) {
			return (mouseButton == 0) ? itemStack.getMaxStackSize() : 1;
		}
	},
	MOUSE_PICKUP {
		@Override
		public int getStackSize(ItemStack itemStack, int mouseButton) {
			boolean modifierActive = GuiScreen.isShiftKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100);
			return modifierActive ? itemStack.getMaxStackSize() : 1;
		}
	};

	public abstract int getStackSize(ItemStack itemStack, int mouseButton);
}

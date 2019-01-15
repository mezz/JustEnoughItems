package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

public enum GiveMode {
	INVENTORY {
		@Override
		public int getStackSize(ItemStack itemStack, InputMappings.Input input) {
			if (input.getType() == InputMappings.Type.MOUSE && input.getKeyCode() == 0) {
				return itemStack.getMaxStackSize();
			}
			return 1;
		}
	},
	MOUSE_PICKUP {
		@Override
		public int getStackSize(ItemStack itemStack, InputMappings.Input input) {
			boolean modifierActive = GuiScreen.isShiftKeyDown() || Minecraft.getInstance().gameSettings.keyBindPickBlock.isActiveAndMatches(input);
			return modifierActive ? itemStack.getMaxStackSize() : 1;
		}
	};

	public abstract int getStackSize(ItemStack itemStack, InputMappings.Input input);
}

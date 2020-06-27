package mezz.jei.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

public enum GiveMode {
	INVENTORY, MOUSE_PICKUP;

	@OnlyIn(Dist.CLIENT)
	public static int getStackSize(GiveMode giveMode, ItemStack itemStack, InputMappings.Input input) {
		switch (giveMode) {
			case INVENTORY: {
				if (input.getType() == InputMappings.Type.MOUSE && input.getKeyCode() == 0) {
					return itemStack.getMaxStackSize();
				}
				return 1;
			}
			case MOUSE_PICKUP: {
				boolean modifierActive = Screen.func_231173_s_() || Minecraft.getInstance().gameSettings.keyBindPickBlock.isActiveAndMatches(input);
				return modifierActive ? itemStack.getMaxStackSize() : 1;
			}
			default:
				throw new IllegalArgumentException("Unknown give mode: " + giveMode);
		}
	}
}

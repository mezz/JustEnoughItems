package mezz.jei.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.ItemStack;

public enum GiveMode {
	INVENTORY, MOUSE_PICKUP;

	@OnlyIn(Dist.CLIENT)
	public static int getStackSize(GiveMode giveMode, ItemStack itemStack, InputConstants.Key input) {
		switch (giveMode) {
			case INVENTORY -> {
				if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
					return itemStack.getMaxStackSize();
				}
				return 1;
			}
			case MOUSE_PICKUP -> {
				boolean modifierActive = Screen.hasShiftDown() || Minecraft.getInstance().options.keyPickItem.isActiveAndMatches(input);
				return modifierActive ? itemStack.getMaxStackSize() : 1;
			}
			default -> throw new IllegalArgumentException("Unknown give mode: " + giveMode);
		}
	}
}

package mezz.jei.util;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

public class Reflection {

	public static Integer[] getDimensions(GuiContainer guiContainer) {
		return new Integer[] {
				(Integer)ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, guiContainer, "field_147003_i", "guiLeft"),
				(Integer)ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, guiContainer, "field_147009_r", "guiTop"),
				(Integer)ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, guiContainer, "field_146999_f", "xSize"),
				(Integer)ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, guiContainer, "field_147000_g", "ySize"),
		};
	}

	public static Slot getTheSlot(GuiContainer guiContainer) {
		return (Slot)ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, guiContainer, "field_147006_u", "theSlot");
	}

}

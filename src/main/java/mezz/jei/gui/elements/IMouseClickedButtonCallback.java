package mezz.jei.gui.elements;

import net.minecraft.client.Minecraft;

public interface IMouseClickedButtonCallback {
	boolean mousePressed(Minecraft mc, int mouseX, int mouseY);
}

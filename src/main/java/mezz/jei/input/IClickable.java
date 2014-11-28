package mezz.jei.input;

import net.minecraft.client.Minecraft;

public interface IClickable extends ICloseable {

	public void handleMouseClicked(Minecraft minecraft, int mouseX, int mouseY, int mouseButton);

}

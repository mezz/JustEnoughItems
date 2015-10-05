package mezz.jei.input;

import java.io.IOException;

import net.minecraft.client.Minecraft;

public interface IClickable extends ICloseable {

	public void handleMouseClicked(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) throws IOException;

}

package mezz.jei;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

	public static KeyBinding toggleOverlay;

	public static void init() {

		toggleOverlay = new KeyBinding("key.jei.toggleOverlay", Keyboard.KEY_O, "key.categories.jei");

		ClientRegistry.registerKeyBinding(toggleOverlay);
	}
}
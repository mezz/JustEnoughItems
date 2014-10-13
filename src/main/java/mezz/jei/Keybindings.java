package mezz.jei;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

	public static KeyBinding toggleOverlay;
	public static KeyBinding showRecipe;
	public static KeyBinding showUses;

	public static void init() {

		toggleOverlay = new KeyBinding("key.jei.toggleOverlay", Keyboard.KEY_O, "key.categories.jei");
		showRecipe = new KeyBinding("key.jei.showRecipe", Keyboard.KEY_R, "key.categories.jei");
		showUses = new KeyBinding("key.jei.showUses", Keyboard.KEY_U, "key.categories.jei");

		ClientRegistry.registerKeyBinding(toggleOverlay);
		ClientRegistry.registerKeyBinding(showRecipe);
		ClientRegistry.registerKeyBinding(showUses);
	}
}

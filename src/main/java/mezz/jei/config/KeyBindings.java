package mezz.jei.config;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class KeyBindings {
	private static final String categoryName = Constants.MOD_ID + " (" + Constants.NAME + ")";

	@Nonnull
	public static final KeyBinding toggleOverlay = new KeyBinding("key.jei.toggleOverlay", Keyboard.KEY_O, categoryName);
	@Nonnull
	public static final KeyBinding showRecipe = new KeyBinding("key.jei.showRecipe", Keyboard.KEY_R, categoryName);
	@Nonnull
	public static final KeyBinding showUses = new KeyBinding("key.jei.showUses", Keyboard.KEY_U, categoryName);

	public static void init() {
		ClientRegistry.registerKeyBinding(toggleOverlay);
		ClientRegistry.registerKeyBinding(showRecipe);
		ClientRegistry.registerKeyBinding(showUses);
	}
}

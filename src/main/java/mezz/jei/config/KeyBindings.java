package mezz.jei.config;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {
	private static final String categoryName = Constants.MOD_ID + " (" + Constants.NAME + ')';

	public static final KeyBinding toggleOverlay = new KeyBinding("key.jei.toggleOverlay", KeyConflictContext.GUI, KeyModifier.CONTROL, Keyboard.KEY_O, categoryName);
	public static final KeyBinding focusSearch = new KeyBinding("key.jei.focusSearch", KeyConflictContext.GUI, KeyModifier.CONTROL, Keyboard.KEY_F, categoryName);
	public static final KeyBinding showRecipe = new KeyBinding("key.jei.showRecipe", KeyConflictContext.GUI, Keyboard.KEY_R, categoryName);
	public static final KeyBinding showUses = new KeyBinding("key.jei.showUses", KeyConflictContext.GUI, Keyboard.KEY_U, categoryName);
	public static final KeyBinding recipeBack = new KeyBinding("key.jei.recipeBack", KeyConflictContext.GUI, Keyboard.KEY_BACK, categoryName);
	public static final KeyBinding toggleCheatMode = new KeyBinding("key.jei.toggleCheatMode", KeyConflictContext.GUI, Keyboard.KEY_NONE, categoryName);

	public static void init() {
		ClientRegistry.registerKeyBinding(toggleOverlay);
		ClientRegistry.registerKeyBinding(focusSearch);
		ClientRegistry.registerKeyBinding(showRecipe);
		ClientRegistry.registerKeyBinding(showUses);
		ClientRegistry.registerKeyBinding(recipeBack);
		ClientRegistry.registerKeyBinding(toggleCheatMode);
	}
}

package mezz.jei.config;

import javax.annotation.Nonnull;

import net.minecraft.client.settings.KeyBinding;

import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.input.Keyboard;

public class KeyBindings {
	private static final String categoryName = Constants.MOD_ID + " (" + Constants.NAME + ')';

	@Nonnull
	public static final KeyBinding toggleOverlay = new KeyBinding("key.jei.toggleOverlay", Keyboard.KEY_O, categoryName);
	@Nonnull
	public static final KeyBinding showRecipe = new KeyBinding("key.jei.showRecipe", Keyboard.KEY_R, categoryName);
	@Nonnull
	public static final KeyBinding showUses = new KeyBinding("key.jei.showUses", Keyboard.KEY_U, categoryName);
	@Nonnull
	public static final KeyBinding recipeBack = new KeyBinding("key.jei.recipeBack", Keyboard.KEY_BACK, categoryName);

	public static void init() {
		ClientRegistry.registerKeyBinding(toggleOverlay);
		ClientRegistry.registerKeyBinding(showRecipe);
		ClientRegistry.registerKeyBinding(showUses);
		ClientRegistry.registerKeyBinding(recipeBack);
	}
}

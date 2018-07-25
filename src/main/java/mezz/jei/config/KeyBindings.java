package mezz.jei.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

import java.util.List;

public final class KeyBindings {
	private static final String categoryName = Constants.MOD_ID + " (" + Constants.NAME + ')';

	public static final KeyBinding toggleOverlay;
	public static final KeyBinding focusSearch;
	public static final KeyBinding toggleCheatMode;
	public static final KeyBinding showRecipe;
	public static final KeyBinding showUses;
	public static final KeyBinding recipeBack;
	public static final KeyBinding previousPage;
	public static final KeyBinding nextPage;
	private static final List<KeyBinding> allBindings;

	static {
		allBindings = ImmutableList.of(
			toggleOverlay = new KeyBinding("key.jei.toggleOverlay", KeyConflictContext.GUI, KeyModifier.CONTROL, Keyboard.KEY_O, categoryName),
			focusSearch = new KeyBinding("key.jei.focusSearch", KeyConflictContext.GUI, KeyModifier.CONTROL, Keyboard.KEY_F, categoryName),
			toggleCheatMode = new KeyBinding("key.jei.toggleCheatMode", KeyConflictContext.GUI, Keyboard.KEY_NONE, categoryName),
			showRecipe = new KeyBinding("key.jei.showRecipe", KeyConflictContext.GUI, Keyboard.KEY_R, categoryName),
			showUses = new KeyBinding("key.jei.showUses", KeyConflictContext.GUI, Keyboard.KEY_U, categoryName),
			recipeBack = new KeyBinding("key.jei.recipeBack", KeyConflictContext.GUI, Keyboard.KEY_BACK, categoryName),
			previousPage = new KeyBinding("key.jei.previousPage", KeyConflictContext.GUI, Keyboard.KEY_PRIOR, categoryName),
			nextPage = new KeyBinding("key.jei.nextPage", KeyConflictContext.GUI, Keyboard.KEY_NEXT, categoryName)
		);
	}

	private KeyBindings() {
	}

	public static void init() {
		for (KeyBinding binding : allBindings) {
			ClientRegistry.registerKeyBinding(binding);
		}
	}

	public static boolean isInventoryToggleKey(int keyCode) {
		return Minecraft.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode);
	}

	public static boolean isInventoryCloseKey(int keyCode) {
		return keyCode == Keyboard.KEY_ESCAPE;
	}

	public static boolean isEnterKey(int keyCode) {
		return keyCode == Keyboard.KEY_RETURN;
	}
}

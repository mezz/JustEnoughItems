package mezz.jei.config;

import java.util.List;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

import com.google.common.collect.ImmutableList;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
	private static final String categoryName = Constants.MOD_ID + " (" + Constants.NAME + ')';

	@Deprecated // used as a hack to start JEI. waiting on proper events in Forge to start it the right way
	public static final KeyBinding startJei;
	public static final KeyBinding toggleOverlay;
	public static final KeyBinding focusSearch;
	public static final KeyBinding toggleCheatMode;
	public static final KeyBinding showRecipe;
	public static final KeyBinding showUses;
	public static final KeyBinding recipeBack;
	public static final KeyBinding previousPage;
	public static final KeyBinding nextPage;
	public static final KeyBinding bookmark;
	public static final KeyBinding toggleBookmarkOverlay;
	private static final List<KeyBinding> allBindings;

	static InputMappings.Input getKey(int key) {
		return InputMappings.Type.KEYSYM.getOrMakeInput(key);
	}

	static {
		allBindings = ImmutableList.of(
			startJei = new KeyBinding("start_jei", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_J), categoryName),
			toggleOverlay = new KeyBinding("key.jei.toggleOverlay", KeyConflictContext.GUI, KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_O), categoryName),
			focusSearch = new KeyBinding("key.jei.focusSearch", KeyConflictContext.GUI, KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_F), categoryName),
			toggleCheatMode = new KeyBinding("key.jei.toggleCheatMode", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), categoryName),
			showRecipe = new KeyBinding("key.jei.showRecipe", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_R), categoryName),
			showUses = new KeyBinding("key.jei.showUses", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_U), categoryName),
			recipeBack = new KeyBinding("key.jei.recipeBack", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_BACKSPACE), categoryName),
			previousPage = new KeyBinding("key.jei.previousPage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_PAGE_UP), categoryName),
			nextPage = new KeyBinding("key.jei.nextPage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_PAGE_DOWN), categoryName),
			bookmark = new KeyBinding("key.jei.bookmark", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_A), categoryName),
			toggleBookmarkOverlay = new KeyBinding("key.jei.toggleBookmarkOverlay", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), categoryName)
		);
	}

	private KeyBindings() {
	}

	public static void init() {
		for (KeyBinding binding : allBindings) {
			ClientRegistry.registerKeyBinding(binding);
		}
	}

	public static boolean isInventoryToggleKey(InputMappings.Input input) {
		return Minecraft.getInstance().gameSettings.keyBindInventory.isActiveAndMatches(input);
	}

	public static boolean isInventoryCloseKey(InputMappings.Input input) {
		return input.getType() == InputMappings.Type.KEYSYM && input.getKeyCode() == GLFW.GLFW_KEY_ESCAPE;
	}

	public static boolean isEnterKey(int keyCode) {
		return keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
	}
}

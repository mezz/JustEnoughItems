package mezz.jei.config;

import java.util.List;

import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.constants.ModIds;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
	private static final String categoryName = ModIds.JEI_ID + " (" + ModIds.JEI_NAME + ')';

	public static final KeyMapping toggleOverlay;
	public static final KeyMapping focusSearch;
	public static final KeyMapping toggleCheatMode;
	public static final KeyMapping toggleEditMode;
	public static final KeyMapping showRecipe;
	public static final KeyMapping showUses;
	public static final KeyMapping recipeBack;
	public static final KeyMapping previousPage;
	public static final KeyMapping nextPage;
	public static final KeyMapping previousCategory;
	public static final KeyMapping nextCategory;
	public static final KeyMapping bookmark;
	public static final KeyMapping toggleBookmarkOverlay;
	private static final List<KeyMapping> allBindings;

	static InputConstants.Key getKey(int key) {
		return InputConstants.Type.KEYSYM.getOrCreate(key);
	}

	static {
		allBindings = ImmutableList.of(
			toggleOverlay = new KeyMapping("key.jei.toggleOverlay", KeyConflictContext.GUI, KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_O), categoryName),
			focusSearch = new KeyMapping("key.jei.focusSearch", KeyConflictContext.GUI, KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_F), categoryName),
			toggleCheatMode = new KeyMapping("key.jei.toggleCheatMode", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), categoryName),
			toggleEditMode = new KeyMapping("key.jei.toggleEditMode", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), categoryName),
			showRecipe = new KeyMapping("key.jei.showRecipe", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_R), categoryName),
			showUses = new KeyMapping("key.jei.showUses", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_U), categoryName),
			recipeBack = new KeyMapping("key.jei.recipeBack", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_BACKSPACE), categoryName),
			previousPage = new KeyMapping("key.jei.previousPage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_PAGE_UP), categoryName),
			nextPage = new KeyMapping("key.jei.nextPage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_PAGE_DOWN), categoryName),
			previousCategory = new KeyMapping("key.jei.previousCategory", KeyConflictContext.GUI, KeyModifier.SHIFT, getKey(GLFW.GLFW_KEY_PAGE_UP), categoryName),
			nextCategory = new KeyMapping("key.jei.nextCategory", KeyConflictContext.GUI, KeyModifier.SHIFT, getKey(GLFW.GLFW_KEY_PAGE_DOWN), categoryName),
			bookmark = new KeyMapping("key.jei.bookmark", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_A), categoryName),
			toggleBookmarkOverlay = new KeyMapping("key.jei.toggleBookmarkOverlay", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), categoryName)
		);
	}

	private KeyBindings() {
	}

	public static void init() {
		for (KeyMapping binding : allBindings) {
			ClientRegistry.registerKeyBinding(binding);
		}
	}

	public static boolean isInventoryToggleKey(InputConstants.Key input) {
		return Minecraft.getInstance().options.keyInventory.isActiveAndMatches(input);
	}

	public static boolean isInventoryCloseKey(InputConstants.Key input) {
		return input.getType() == InputConstants.Type.KEYSYM && input.getValue() == GLFW.GLFW_KEY_ESCAPE;
	}

	public static boolean isEnterKey(int keyCode) {
		return keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
	}
}

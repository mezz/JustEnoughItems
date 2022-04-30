package mezz.jei.config;

import java.util.List;

import mezz.jei.common.util.Translator;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
	private static final String overlaysCategoryName = Translator.translateToLocal("jei.key.category.overlays");
	private static final String mouseHoverCategoryName = Translator.translateToLocal("jei.key.category.mouse.hover");
	private static final String cheatModeCategoryName = Translator.translateToLocal("jei.key.category.cheat.mode");
	private static final String hoverConfigButtonCategoryName = Translator.translateToLocal("jei.key.category.hover.config.button");
	private static final String editModeCategoryName = Translator.translateToLocal("jei.key.category.edit.mode");
	private static final String recipeCategoryName = Translator.translateToLocal("jei.key.category.recipe.gui");
	private static final String searchCategoryName = Translator.translateToLocal("jei.key.category.search");
	private static final String devToolsCategoryName = Translator.translateToLocal("jei.key.category.dev.tools");
	private static final String jeiHiddenInternalCategoryName = "jei.key.category.hidden.internal";

	public static final KeyMapping toggleOverlay;
	public static final KeyMapping focusSearch;
	public static final KeyMapping toggleCheatMode;
	public static final KeyMapping toggleEditMode;

	public static final KeyMapping toggleCheatModeConfigButton;

	public static final KeyMapping recipeBack;
	public static final KeyMapping previousCategory;
	public static final KeyMapping nextCategory;
	public static final KeyMapping previousRecipePage;
	public static final KeyMapping nextRecipePage;
	public static final KeyMapping closeRecipeGui;

	public static final KeyMapping previousPage;
	public static final KeyMapping nextPage;

	public static final KeyMapping bookmark;
	public static final KeyMapping toggleBookmarkOverlay;

	public static final List<KeyMapping> showRecipe;
	public static final List<KeyMapping> showUses;

	public static final List<KeyMapping> cheatOneItem;
	public static final List<KeyMapping> cheatItemStack;

	public static final KeyMapping toggleHideIngredient;
	public static final KeyMapping toggleWildcardHideIngredient;

	public static final KeyMapping hoveredClearSearchBar;
	public static final KeyMapping previousSearch;
	public static final KeyMapping nextSearch;

	public static final KeyMapping copyRecipeId;

	private static final List<KeyMapping> allBindings;

	// internal only, unregistered and can't be changed because they match vanilla Minecraft hard-coded keys:
	public static final KeyMapping escapeKey;
	public static final KeyMapping leftClick;
	public static final KeyMapping rightClick;
	public static final List<KeyMapping> enterKey;

	// debug only
	public static final KeyMapping reloadJeiOverTextFilter;

	static InputConstants.Key getKey(int key) {
		return InputConstants.Type.KEYSYM.getOrCreate(key);
	}

	static {
		KeyMapping showRecipe1;
		KeyMapping showRecipe2;
		KeyMapping showUses1;
		KeyMapping showUses2;
		KeyMapping cheatOneItem1;
		KeyMapping cheatOneItem2;
		KeyMapping cheatItemStack1;
		KeyMapping cheatItemStack2;

		allBindings = List.of(
			// Overlay
			toggleOverlay = new KeyMapping("key.jei.toggleOverlay", KeyConflictContext.GUI, KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_O), overlaysCategoryName),
			focusSearch = new KeyMapping("key.jei.focusSearch", KeyConflictContext.GUI, KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_F), overlaysCategoryName),
			previousPage = new KeyMapping("key.jei.previousPage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), overlaysCategoryName),
			nextPage = new KeyMapping("key.jei.nextPage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), overlaysCategoryName),
			toggleBookmarkOverlay = new KeyMapping("key.jei.toggleBookmarkOverlay", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), overlaysCategoryName),

			// Mouse Hover
			bookmark = new KeyMapping("key.jei.bookmark", JeiConflictContexts.JEI_GUI_HOVER, getKey(GLFW.GLFW_KEY_A), mouseHoverCategoryName),
			showRecipe1 = new KeyMapping("key.jei.showRecipe", JeiConflictContexts.JEI_GUI_HOVER, getKey(GLFW.GLFW_KEY_R), mouseHoverCategoryName),
			showRecipe2 = new KeyMapping("key.jei.showRecipe2", JeiConflictContexts.JEI_GUI_HOVER, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, mouseHoverCategoryName),
			showUses1 = new KeyMapping("key.jei.showUses", JeiConflictContexts.JEI_GUI_HOVER, getKey(GLFW.GLFW_KEY_U), mouseHoverCategoryName),
			showUses2 = new KeyMapping("key.jei.showUses2", JeiConflictContexts.JEI_GUI_HOVER, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, mouseHoverCategoryName),

			// Search Bar
			hoveredClearSearchBar = new KeyMapping("key.jei.clearSearchBar", JeiConflictContexts.JEI_GUI_HOVER_SEARCH, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, searchCategoryName),
			previousSearch = new KeyMapping("key.jei.previousSearch", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UP), searchCategoryName),
			nextSearch = new KeyMapping("key.jei.nextSearch", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_DOWN), searchCategoryName),

			// Cheat Mode
			toggleCheatMode = new KeyMapping("key.jei.toggleCheatMode", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), cheatModeCategoryName),
			cheatOneItem1 = new KeyMapping("key.jei.cheatOneItem", JeiConflictContexts.JEI_GUI_HOVER_CHEAT_MODE, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, cheatModeCategoryName),
			cheatOneItem2 = new KeyMapping("key.jei.cheatOneItem2", JeiConflictContexts.JEI_GUI_HOVER_CHEAT_MODE, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, cheatModeCategoryName),
			cheatItemStack1 = new KeyMapping("key.jei.cheatItemStack", JeiConflictContexts.JEI_GUI_HOVER_CHEAT_MODE, KeyModifier.SHIFT, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, cheatModeCategoryName),
			cheatItemStack2 = new KeyMapping("key.jei.cheatItemStack2", JeiConflictContexts.JEI_GUI_HOVER_CHEAT_MODE, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE, cheatModeCategoryName),

			// Hovering over config button
			toggleCheatModeConfigButton = new KeyMapping("key.jei.toggleCheatModeConfigButton", JeiConflictContexts.JEI_GUI_HOVER_CONFIG_BUTTON, KeyModifier.CONTROL, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, hoverConfigButtonCategoryName),

			// Edit Mode
			toggleEditMode = new KeyMapping("key.jei.toggleEditMode", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), editModeCategoryName),
			toggleHideIngredient = new KeyMapping("key.jei.toggleHideIngredient", JeiConflictContexts.JEI_GUI_HOVER, KeyModifier.CONTROL, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, editModeCategoryName),
			toggleWildcardHideIngredient = new KeyMapping("key.jei.toggleWildcardHideIngredient", JeiConflictContexts.JEI_GUI_HOVER, KeyModifier.CONTROL, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, editModeCategoryName),

			// Recipes
			recipeBack = new KeyMapping("key.jei.recipeBack", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_BACKSPACE), recipeCategoryName),
			previousRecipePage = new KeyMapping("key.jei.previousRecipePage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_PAGE_UP), recipeCategoryName),
			nextRecipePage = new KeyMapping("key.jei.nextRecipePage", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_PAGE_DOWN), recipeCategoryName),
			previousCategory = new KeyMapping("key.jei.previousCategory", KeyConflictContext.GUI, KeyModifier.SHIFT, getKey(GLFW.GLFW_KEY_PAGE_UP), recipeCategoryName),
			nextCategory = new KeyMapping("key.jei.nextCategory", KeyConflictContext.GUI, KeyModifier.SHIFT, getKey(GLFW.GLFW_KEY_PAGE_DOWN), recipeCategoryName),
			closeRecipeGui = new KeyMapping("key.jei.closeRecipeGui", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_ESCAPE), recipeCategoryName),

			// Dev Tools
			copyRecipeId = new KeyMapping("key.jei.copy.recipe.id", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_UNKNOWN), devToolsCategoryName)
		);

		showRecipe = List.of(showRecipe1, showRecipe2);
		showUses = List.of(showUses1, showUses2);
		cheatOneItem = List.of(cheatOneItem1, cheatOneItem2);
		cheatItemStack = List.of(cheatItemStack1, cheatItemStack2);

		escapeKey = new KeyMapping("key.jei.internal.escape.key", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_ESCAPE), jeiHiddenInternalCategoryName);
		leftClick = new KeyMapping("key.jei.internal.left.click", KeyConflictContext.GUI, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, jeiHiddenInternalCategoryName);
		rightClick = new KeyMapping("key.jei.internal.right.click", KeyConflictContext.GUI, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, jeiHiddenInternalCategoryName);
		enterKey = List.of(
			new KeyMapping("key.jei.internal.enter.key", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_ENTER), jeiHiddenInternalCategoryName),
			new KeyMapping("key.jei.internal.enter.key2", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_KP_ENTER), jeiHiddenInternalCategoryName)
		);

		reloadJeiOverTextFilter = new KeyMapping("key.jei.internal.debug.reload", KeyConflictContext.GUI, KeyModifier.SHIFT, getKey(GLFW.GLFW_KEY_F12), jeiHiddenInternalCategoryName);
	}

	private KeyBindings() {
	}

	public static void init() {
		for (KeyMapping binding : allBindings) {
			ClientRegistry.registerKeyBinding(binding);
		}
	}

	private enum JeiConflictContexts implements IKeyConflictContext {
		JEI_GUI_HOVER {
			@Override
			public boolean isActive() {
				return KeyConflictContext.GUI.isActive();
			}

			@Override
			public boolean conflicts(IKeyConflictContext other) {
				return this == other;
			}
		},

		JEI_GUI_HOVER_CHEAT_MODE {
			@Override
			public boolean isActive() {
				return KeyConflictContext.GUI.isActive();
			}

			@Override
			public boolean conflicts(IKeyConflictContext other) {
				return this == other;
			}
		},

		JEI_GUI_HOVER_CONFIG_BUTTON {
			@Override
			public boolean isActive() {
				return KeyConflictContext.GUI.isActive();
			}

			@Override
			public boolean conflicts(IKeyConflictContext other) {
				return this == other;
			}
		},

		JEI_GUI_HOVER_SEARCH {
			@Override
			public boolean isActive() {
				return KeyConflictContext.GUI.isActive();
			}

			@Override
			public boolean conflicts(IKeyConflictContext other) {
				return this == other;
			}
		}
	}
}

package mezz.jei.forge.config;

import java.util.List;

import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.util.Translator;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

public final class ForgeKeyBindings implements IKeyBindings {
	private static final String overlaysCategoryName = Translator.translateToLocal("jei.key.category.overlays");
	private static final String mouseHoverCategoryName = Translator.translateToLocal("jei.key.category.mouse.hover");
	private static final String cheatModeCategoryName = Translator.translateToLocal("jei.key.category.cheat.mode");
	private static final String hoverConfigButtonCategoryName = Translator.translateToLocal("jei.key.category.hover.config.button");
	private static final String editModeCategoryName = Translator.translateToLocal("jei.key.category.edit.mode");
	private static final String recipeCategoryName = Translator.translateToLocal("jei.key.category.recipe.gui");
	private static final String searchCategoryName = Translator.translateToLocal("jei.key.category.search");
	private static final String devToolsCategoryName = Translator.translateToLocal("jei.key.category.dev.tools");
	private static final String jeiHiddenInternalCategoryName = "jei.key.category.hidden.internal";

	private final KeyMapping toggleOverlay;
	private final KeyMapping focusSearch;
	private final KeyMapping toggleCheatMode;
	private final KeyMapping toggleEditMode;

	private final KeyMapping toggleCheatModeConfigButton;

	private final KeyMapping recipeBack;
	private final KeyMapping previousCategory;
	private final KeyMapping nextCategory;
	private final KeyMapping previousRecipePage;
	private final KeyMapping nextRecipePage;

	private final KeyMapping previousPage;
	private final KeyMapping nextPage;

	private final KeyMapping bookmark;
	private final KeyMapping toggleBookmarkOverlay;

	private final List<KeyMapping> showRecipe;
	private final List<KeyMapping> showUses;

	private final List<KeyMapping> cheatOneItem;
	private final List<KeyMapping> cheatItemStack;

	private final KeyMapping toggleHideIngredient;
	private final KeyMapping toggleWildcardHideIngredient;

	private final KeyMapping hoveredClearSearchBar;
	private final KeyMapping previousSearch;
	private final KeyMapping nextSearch;

	private final KeyMapping copyRecipeId;

	private final List<KeyMapping> allBindings;

	// internal only, unregistered and can't be changed because they match vanilla Minecraft hard-coded keys:
	private final KeyMapping escapeKey;
	private final KeyMapping leftClick;
	private final KeyMapping rightClick;
	private final List<KeyMapping> enterKey;

	private static InputConstants.Key getKey(int key) {
		return InputConstants.Type.KEYSYM.getOrCreate(key);
	}

	public ForgeKeyBindings() {
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
	}

	public void register() {
		for (KeyMapping binding : allBindings) {
			ClientRegistry.registerKeyBinding(binding);
		}
	}

	@Override
	public KeyMapping getToggleOverlay() {
		return toggleOverlay;
	}

	@Override
	public KeyMapping getFocusSearch() {
		return focusSearch;
	}

	@Override
	public KeyMapping getToggleCheatMode() {
		return toggleCheatMode;
	}

	@Override
	public KeyMapping getToggleEditMode() {
		return toggleEditMode;
	}

	@Override
	public KeyMapping getToggleCheatModeConfigButton() {
		return toggleCheatModeConfigButton;
	}

	@Override
	public KeyMapping getRecipeBack() {
		return recipeBack;
	}

	@Override
	public KeyMapping getPreviousCategory() {
		return previousCategory;
	}

	@Override
	public KeyMapping getNextCategory() {
		return nextCategory;
	}

	@Override
	public KeyMapping getPreviousRecipePage() {
		return previousRecipePage;
	}

	@Override
	public KeyMapping getNextRecipePage() {
		return nextRecipePage;
	}

	@Override
	public KeyMapping getPreviousPage() {
		return previousPage;
	}

	@Override
	public KeyMapping getNextPage() {
		return nextPage;
	}

	@Override
	public KeyMapping getBookmark() {
		return bookmark;
	}

	@Override
	public KeyMapping getToggleBookmarkOverlay() {
		return toggleBookmarkOverlay;
	}

	@Override
	public List<KeyMapping> getShowRecipe() {
		return showRecipe;
	}

	@Override
	public List<KeyMapping> getShowUses() {
		return showUses;
	}

	@Override
	public List<KeyMapping> getCheatOneItem() {
		return cheatOneItem;
	}

	@Override
	public List<KeyMapping> getCheatItemStack() {
		return cheatItemStack;
	}

	@Override
	public KeyMapping getToggleHideIngredient() {
		return toggleHideIngredient;
	}

	@Override
	public KeyMapping getToggleWildcardHideIngredient() {
		return toggleWildcardHideIngredient;
	}

	@Override
	public KeyMapping getHoveredClearSearchBar() {
		return hoveredClearSearchBar;
	}

	@Override
	public KeyMapping getPreviousSearch() {
		return previousSearch;
	}

	@Override
	public KeyMapping getNextSearch() {
		return nextSearch;
	}

	@Override
	public KeyMapping getCopyRecipeId() {
		return copyRecipeId;
	}

	@Override
	public KeyMapping getEscapeKey() {
		return escapeKey;
	}

	@Override
	public KeyMapping getLeftClick() {
		return leftClick;
	}

	@Override
	public KeyMapping getRightClick() {
		return rightClick;
	}

	@Override
	public List<KeyMapping> getEnterKey() {
		return enterKey;
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
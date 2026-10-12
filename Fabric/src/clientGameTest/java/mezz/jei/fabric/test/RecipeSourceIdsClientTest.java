package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.InputPhase;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.bookmarks.BookmarkCodec;
import mezz.jei.gui.bookmarks.BookmarkFactory;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.plugins.vanilla.crafting.CraftingCategoryExtension;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Verifies source IDs through registered recipes, bookmark JSON, tooltips, and copy controls. */
@SuppressWarnings({"UnstableApiUsage", "removal"})
final class RecipeSourceIdsClientTest {
	private static final ResourceLocation SHIELD_DECORATION = ResourceLocation.withDefaultNamespace("shield_decoration");
	private static final List<ResourceLocation> MULTIPLE_SOURCES = List.of(
		SHIELD_DECORATION,
		ResourceLocation.withDefaultNamespace("crafting_table")
	);

	private RecipeSourceIdsClientTest() {
	}

	static void run() {
		var context = new FabricClientTestContext();
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var manager = runtime.getRecipeManager();
			var category = manager.getRecipeCategory(RecipeTypes.CRAFTING);
			var recipes = manager.createRecipeLookup(RecipeTypes.CRAFTING).get()
				.filter(recipe -> recipe.id().getPath().startsWith("jei.shield.decoration."))
				.limit(2)
				.toList();
			check(recipes.size() == 2, "Expected distinct registered shield-decoration displays");
			var transferService = new RecipeTransferService(runtime.getRecipeTransferManager());
			var codecHelper = runtime.getJeiHelpers().getCodecHelper();
			var registryAccess = Objects.requireNonNull(client.level).registryAccess();
			var bookmarkFactory = new BookmarkFactory(codecHelper, registryAccess, runtime.getIngredientManager());
			var codec = BookmarkCodec.create(codecHelper, runtime.getIngredientManager(), manager, transferService, bookmarkFactory).codec();
			var ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
			var bookmarks = new HashSet<>();
			boolean advancedTooltips = client.options.advancedItemTooltips;
			try {
				client.options.advancedItemTooltips = true;
				for (var recipe : recipes) {
					check(category.getSourceRecipeIds(recipe).equals(List.of(SHIELD_DECORATION)), "Generated displays must retain the original recipe ID");
					var layout = createLayout(category, recipe);
					assertTooltipIds(layout, List.of(SHIELD_DECORATION));
					var bookmark = Objects.requireNonNull(RecipeBookmark.create(layout, runtime.getIngredientManager(), transferService));
					var encoded = codec.encodeStart(ops, bookmark).getOrThrow();
					var decoded = codec.parse(ops, encoded).getOrThrow();
					check(bookmark.equals(decoded), "Bookmark JSON must preserve the individual display identity");
					bookmarks.add(decoded);
					var expectedRecipeData = codecHelper.<RecipeHolder<CraftingRecipe>>getRecipeHolderCodec().encodeStart(ops, recipe).getOrThrow();
					check(expectedRecipeData.equals(encoded.getAsJsonObject().get("recipe")), "Crafting bookmarks must retain the existing recipe holder format");
				}
				check(bookmarks.size() == 2, "Displays with the same source must remain distinct after saving bookmarks");
				var multipleSources = createCategory(runtime.getJeiHelpers().getGuiHelper(), MULTIPLE_SOURCES);
				assertTooltipIds(createLayout(multipleSources, recipes.getFirst()), MULTIPLE_SOURCES);
				var noSources = createCategory(runtime.getJeiHelpers().getGuiHelper(), List.of());
				assertTooltipIds(createLayout(noSources, recipes.getFirst()), List.of());
				assertCopyControls(client, category, recipes.getFirst());
			} finally {
				client.options.advancedItemTooltips = advancedTooltips;
			}
		});
	}

	private static void assertCopyControls(Minecraft client, IRecipeCategory<RecipeHolder<CraftingRecipe>> category, RecipeHolder<CraftingRecipe> recipe) {
		var runtime = Internal.getJeiRuntime();
		var gui = (RecipesGui) runtime.getRecipesGui();
		Screen parent = client.screen;
		String clipboard = client.keyboardHandler.getClipboard();
		KeyMapping copyKey = java.util.Arrays.stream(client.options.keyMappings).filter(mapping -> mapping.getName().equals("key.jei.copy.recipe.id")).findFirst().orElseThrow();
		var originalKey = KeyBindingHelper.getBoundKeyOf(copyKey);
		try {
			copyKey.setKey(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE));
			client.setScreen(gui);
			copy(gui, category, recipe);
			check(client.keyboardHandler.getClipboard().equals(SHIELD_DECORATION.toString()), "Copy must use the source ID rather than the generated display ID");
			var multipleSources = createCategory(runtime.getJeiHelpers().getGuiHelper(), MULTIPLE_SOURCES);
			copy(gui, multipleSources, recipe);
			check(client.keyboardHandler.getClipboard().equals("[minecraft:shield_decoration, minecraft:crafting_table]"), "Multiple source IDs must be copied as a bracketed list");
			check(client.screen == gui, "Copying multiple source IDs must keep the recipe screen open");
			copy(gui, createCategory(runtime.getJeiHelpers().getGuiHelper(), List.of()), recipe);
			check(client.keyboardHandler.getClipboard().equals("[minecraft:shield_decoration, minecraft:crafting_table]"), "A display with no source must leave the clipboard intact");
		} finally {
			copyKey.setKey(originalKey);
			client.keyboardHandler.setClipboard(clipboard);
			client.setScreen(parent);
		}
	}

	private static void copy(RecipesGui gui, IRecipeCategory<RecipeHolder<CraftingRecipe>> category, RecipeHolder<CraftingRecipe> recipe) {
		var runtime = Internal.getJeiRuntime();
		var layout = createLayout(category, recipe);
		layout.setPosition(0, 0);
		BookmarkList bookmarks = new ReflectionUtil().getFieldWithClass(gui, BookmarkList.class).findFirst().orElseThrow();
		var buttons = RecipeLayoutWithButtons.create(layout, null, bookmarks, gui, new RecipeTransferService(runtime.getRecipeTransferManager()), List.of());
		var input = UserInput.fromVanilla(5, 5, InputConstants.MOUSE_BUTTON_MIDDLE, InputPhase.PRESS).orElseThrow();
		var interaction = buttons.createUserInputHandler().beginInput(gui, input, Internal.getKeyMappings()).orElseThrow();
		interaction.complete(UserInput.fromVanilla(5, 5, InputConstants.MOUSE_BUTTON_MIDDLE, InputPhase.RELEASE).orElseThrow());
	}

	private static CraftingRecipeCategory createCategory(IGuiHelper guiHelper, List<ResourceLocation> sources) {
		var category = new CraftingRecipeCategory(guiHelper);
		category.addExtension(CraftingRecipe.class, new CraftingCategoryExtension() {
			@Override
			public List<ResourceLocation> getSourceRecipeIds(RecipeHolder<CraftingRecipe> recipeHolder) {
				return sources;
			}
		});
		return category;
	}

	private static IRecipeLayoutDrawable<RecipeHolder<CraftingRecipe>> createLayout(IRecipeCategory<RecipeHolder<CraftingRecipe>> category, RecipeHolder<CraftingRecipe> recipe) {
		return Internal.getJeiRuntime().getRecipeManager().createRecipeLayoutDrawable(category, recipe, FocusGroup.EMPTY).orElseThrow();
	}

	private static void assertTooltipIds(IRecipeLayoutDrawable<?> layout, List<ResourceLocation> expectedIds) {
		var output = layout.getRecipeSlotsView().getSlotViews().stream().filter(slot -> slot.getRole() == RecipeIngredientRole.OUTPUT)
			.map(IRecipeSlotDrawable.class::cast).findFirst().orElseThrow();
		var tooltip = new JeiTooltip();
		output.getTooltip(tooltip);
		var lines = tooltip.getLines().stream().flatMap(line -> line.left().stream()).map(text -> text.getString()).toList();
		var expectedLines = expectedIds.stream().map(id -> Component.translatable("jei.tooltip.recipe.id", Component.literal(id.toString())).getString()).toList();
		var actualLines = lines.stream().filter(line -> line.startsWith("Recipe ID: ")).toList();
		check(actualLines.equals(expectedLines), "Tooltip recipe IDs must match the sources: " + actualLines);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}

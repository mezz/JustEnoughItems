package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
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
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"UnstableApiUsage", "removal"})
final class RecipeSourceIdsClientTest {
	private static final Identifier SHIELD_DECORATION = Identifier.withDefaultNamespace("shield_decoration");
	private static final Identifier SYNTHETIC_ID = Identifier.fromNamespaceAndPath("jei_test", "/synthetic/recipe");
	private static final Identifier NORMAL_ID = Identifier.fromNamespaceAndPath("jei_test", "folder/recipe");
	private static final List<Identifier> MULTIPLE_SOURCES = List.of(
		SHIELD_DECORATION,
		Identifier.withDefaultNamespace("crafting_table")
	);

	private RecipeSourceIdsClientTest() {
	}

	static void run(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var manager = runtime.getRecipeManager();
			var category = manager.getRecipeCategory(RecipeTypes.CRAFTING);
			var recipes = manager.createRecipeLookup(RecipeTypes.CRAFTING).get()
				.filter(recipe -> recipe.id().identifier().getPath().startsWith("jei.shield.decoration."))
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
					assertTooltipIds(createLayout(category, recipe), List.of(SHIELD_DECORATION));
				}
				var synthetic = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, SYNTHETIC_ID), recipes.getFirst().value());
				for (var recipe : List.of(recipes.getFirst(), recipes.getLast(), synthetic)) {
					var layout = createLayout(category, recipe);
					var bookmark = Objects.requireNonNull(RecipeBookmark.create(layout, runtime.getIngredientManager(), transferService));
					var encoded = codec.encodeStart(ops, bookmark).getOrThrow();
					var decoded = codec.parse(ops, encoded).getOrThrow();
					check(bookmark.equals(decoded), "Bookmark JSON must preserve the individual display identity");
					bookmarks.add(decoded);
					var expectedRecipeData = codecHelper.<RecipeHolder<CraftingRecipe>>getRecipeHolderCodec().encodeStart(ops, recipe).getOrThrow();
					check(expectedRecipeData.equals(encoded.getAsJsonObject().get("recipe")), "Crafting bookmarks must retain the existing recipe holder format");
				}
				check(bookmarks.size() == 3, "Generated and synthetic displays must remain distinct after saving bookmarks");
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
		KeyMapping copyKey = Objects.requireNonNull(KeyMapping.get("key.jei.copy.recipe.id"));
		var originalKey = KeyMappingHelper.getBoundKeyOf(copyKey);
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
			assertSyntheticDisplays(client, gui);
		} finally {
			copyKey.setKey(originalKey);
			client.keyboardHandler.setClipboard(clipboard);
			client.setScreen(parent);
		}
	}

	private static void assertSyntheticDisplays(Minecraft client, RecipesGui gui) {
		var syntheticCrafting = assertSyntheticDisplay(client, gui, RecipeTypes.CRAFTING);
		assertSyntheticDisplay(client, gui, RecipeTypes.SMOKING);
		assertSyntheticDisplay(client, gui, RecipeTypes.SMELTING);
		assertSyntheticDisplay(client, gui, RecipeTypes.SMITHING);

		var runtime = Internal.getJeiRuntime();
		var guiHelper = runtime.getJeiHelpers().getGuiHelper();
		var explicitSources = createCategory(guiHelper, MULTIPLE_SOURCES);
		assertTooltipIds(createLayout(explicitSources, syntheticCrafting), MULTIPLE_SOURCES);
		copy(gui, explicitSources, syntheticCrafting);
		check(client.keyboardHandler.getClipboard().equals("[minecraft:shield_decoration, minecraft:crafting_table]"), "Synthetic displays must retain explicit source IDs");

		var replacements = new CraftingRecipeCategory(guiHelper);
		replacements.addExtension(CraftingRecipe.class, new CraftingCategoryExtension());
		var normalCrafting = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, NORMAL_ID), syntheticCrafting.value());
		replacements.addReplacementSource(syntheticCrafting, normalCrafting);
		assertTooltipIds(createLayout(replacements, syntheticCrafting), List.of(NORMAL_ID));
		copy(gui, replacements, syntheticCrafting);
		check(client.keyboardHandler.getClipboard().equals(NORMAL_ID.toString()), "Synthetic displays must retain replacement source IDs");
	}

	private static <R extends Recipe<?>> RecipeHolder<R> assertSyntheticDisplay(Minecraft client, RecipesGui gui, IRecipeType<RecipeHolder<R>> recipeType) {
		var runtime = Internal.getJeiRuntime();
		var manager = runtime.getRecipeManager();
		var category = manager.getRecipeCategory(recipeType);
		var template = manager.createRecipeLookup(recipeType).get()
			.filter(recipe -> !recipe.value().isSpecial())
			.findFirst().orElseThrow();
		var synthetic = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, SYNTHETIC_ID), template.value());
		var normal = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, NORMAL_ID), template.value());
		var layout = createLayout(category, synthetic);
		assertTooltipIds(layout, List.of());
		String clipboard = client.keyboardHandler.getClipboard();
		copy(gui, category, synthetic);
		check(client.keyboardHandler.getClipboard().equals(clipboard), "Copying a synthetic display without sources must leave the clipboard intact");
		assertTooltipIds(createLayout(category, normal), List.of(NORMAL_ID));
		copy(gui, category, normal);
		check(client.keyboardHandler.getClipboard().equals(NORMAL_ID.toString()), "Normal IDs with nested paths must remain copyable");
		return synthetic;
	}

	private static <R> void copy(RecipesGui gui, IRecipeCategory<R> category, R recipe) {
		var runtime = Internal.getJeiRuntime();
		var layout = createLayout(category, recipe);
		layout.setPosition(0, 0);
		BookmarkList bookmarks = new ReflectionUtil().getFieldWithClass(gui, BookmarkList.class).findFirst().orElseThrow();
		var buttons = RecipeLayoutWithButtons.create(layout, null, bookmarks, gui, new RecipeTransferService(runtime.getRecipeTransferManager()), List.of());
		var event = new MouseButtonEvent(5, 5, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_MIDDLE, 0));
		var input = UserInput.fromVanilla(event, false, InputPhase.PRESS).orElseThrow();
		var interaction = buttons.createUserInputHandler().beginInput(gui, input, Internal.getKeyMappings()).orElseThrow();
		interaction.complete(UserInput.fromVanilla(event, false, InputPhase.RELEASE).orElseThrow());
	}

	private static CraftingRecipeCategory createCategory(IGuiHelper guiHelper, List<Identifier> sources) {
		var category = new CraftingRecipeCategory(guiHelper);
		category.addExtension(CraftingRecipe.class, new CraftingCategoryExtension() {
			@Override
			public List<Identifier> getSourceRecipeIds(RecipeHolder<CraftingRecipe> recipeHolder) {
				return sources;
			}
		});
		return category;
	}

	private static <R> IRecipeLayoutDrawable<R> createLayout(IRecipeCategory<R> category, R recipe) {
		return Internal.getJeiRuntime().getRecipeManager().createRecipeLayoutDrawable(category, recipe, FocusGroup.EMPTY).orElseThrow();
	}

	private static void assertTooltipIds(IRecipeLayoutDrawable<?> layout, List<Identifier> expectedIds) {
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

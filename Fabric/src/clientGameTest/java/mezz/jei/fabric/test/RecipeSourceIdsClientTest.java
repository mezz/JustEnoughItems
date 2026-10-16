package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.netty.buffer.Unpooled;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.InputPhase;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.config.file.serializers.RecipeBookmarkSerializer;
import mezz.jei.gui.config.file.serializers.TypedIngredientSerializer;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Verifies source IDs through registered recipes, bookmark JSON, tooltips, and copy controls. */
@SuppressWarnings({"UnstableApiUsage", "removal"})
final class RecipeSourceIdsClientTest {
	private static final ResourceLocation SHIELD_DECORATION = new ResourceLocation("minecraft", "shield_decoration");
	private static final ResourceLocation SYNTHETIC_ID = new ResourceLocation("jei_test", "/synthetic/recipe");
	private static final ResourceLocation NORMAL_ID = new ResourceLocation("jei_test", "folder/recipe");
	private static final List<ResourceLocation> MULTIPLE_SOURCES = List.of(
		SHIELD_DECORATION,
		new ResourceLocation("minecraft", "crafting_table")
	);

	private RecipeSourceIdsClientTest() {
	}

	static void run() {
		var context = new FabricClientTestContext();
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var manager = runtime.getRecipeManager();
			var category = manager.getRecipeCategory(RecipeTypes.CRAFTING);
			var sponge = manager.createRecipeLookup(RecipeTypes.SMELTING).get()
				.filter(recipe -> recipe.getId().equals(new ResourceLocation("minecraft", "/sponge_with_bucket")))
				.findFirst().orElseThrow();
			check(manager.getRecipeCategory(RecipeTypes.SMELTING).getSourceRecipeIds(sponge)
				.equals(List.of(new ResourceLocation("minecraft", "sponge"))), "Enhanced sponge displays must retain the underlying recipe ID");
			var recipes = manager.createRecipeLookup(RecipeTypes.CRAFTING).get()
				.filter(recipe -> recipe.getId().getPath().startsWith("/jei.shield.decoration."))
				.limit(2)
				.toList();
			check(recipes.size() == 2, "Expected distinct registered shield-decoration displays");
			var transferService = new RecipeTransferService(runtime.getRecipeTransferManager());
			var serializer = new RecipeBookmarkSerializer(manager, runtime.getJeiHelpers().getFocusFactory(),
				new TypedIngredientSerializer(runtime.getIngredientManager()), runtime.getIngredientManager(), runtime.getJeiHelpers().getGuiHelper(), transferService);
			var bookmarks = new HashSet<>();
			boolean advancedTooltips = client.options.advancedItemTooltips;
			try {
				client.options.advancedItemTooltips = true;
				for (var recipe : recipes) {
					check(category.getSourceRecipeIds(recipe).equals(List.of(SHIELD_DECORATION)), "Generated displays must retain the original recipe ID");
					var layout = createLayout(category, recipe);
					assertTooltipIds(layout, List.of(SHIELD_DECORATION));
					var bookmark = Objects.requireNonNull(RecipeBookmark.create(layout, runtime.getIngredientManager(), runtime.getRecipeManager(), runtime.getJeiHelpers().getGuiHelper(), transferService).orElseThrow());
					var encoded = serializer.serialize(bookmark);
					var decoded = serializer.deserialize(encoded).getResult().orElseThrow();
					check(bookmark.equals(decoded), "Bookmark JSON must preserve the individual display identity");
					String legacyUid = recipe.getId().getNamespace() + ":" + recipe.getId().getPath().substring(1);
					String legacy = encoded.replace(recipe.getId().toString(), legacyUid);
					var migrated = serializer.deserialize(legacy).getResult().orElseThrow();
					check(bookmark.equals(migrated), "Legacy generated recipe bookmarks must migrate to synthetic IDs");
					check(serializer.serialize(migrated).equals(encoded), "Migrated bookmarks must save the current synthetic ID");
					bookmarks.add(decoded);
					check(encoded.contains(recipe.getId().toString()), "Crafting bookmarks must retain the generated display identifier");
				}
				check(bookmarks.size() == 2, "Displays with the same source must remain distinct after saving bookmarks");
				var multipleSources = createCategory(runtime.getJeiHelpers().getGuiHelper(), MULTIPLE_SOURCES);
				assertTooltipIds(createLayout(multipleSources, recipes.get(0)), MULTIPLE_SOURCES);
				var noSources = createCategory(runtime.getJeiHelpers().getGuiHelper(), List.of());
				assertTooltipIds(createLayout(noSources, recipes.get(0)), List.of());
				assertCopyControls(client, category, recipes.get(0));
			} finally {
				client.options.advancedItemTooltips = advancedTooltips;
			}
		});
	}

	private static void assertCopyControls(Minecraft client, IRecipeCategory<CraftingRecipe> category, CraftingRecipe recipe) {
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
			assertSyntheticDisplays(client, gui);
		} finally {
			copyKey.setKey(originalKey);
			client.keyboardHandler.setClipboard(clipboard);
			client.setScreen(parent);
		}
	}

	private static void assertSyntheticDisplays(Minecraft client, RecipesGui gui) {
		CraftingRecipe syntheticCrafting = assertSyntheticDisplay(client, gui, RecipeTypes.CRAFTING);
		assertSyntheticDisplay(client, gui, RecipeTypes.SMOKING);
		assertSyntheticDisplay(client, gui, RecipeTypes.SMELTING);
		assertSyntheticDisplay(client, gui, RecipeTypes.SMITHING);

		var guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
		var explicitSources = createCategory(guiHelper, MULTIPLE_SOURCES);
		assertTooltipIds(createLayout(explicitSources, syntheticCrafting), MULTIPLE_SOURCES);
		copy(gui, explicitSources, syntheticCrafting);
		check(client.keyboardHandler.getClipboard().equals("[minecraft:shield_decoration, minecraft:crafting_table]"), "Synthetic displays must retain explicit source IDs");

		var replacements = new CraftingRecipeCategory(guiHelper);
		replacements.addCategoryExtension(CraftingRecipe.class, CraftingCategoryExtension::new);
		replacements.addReplacementSource(syntheticCrafting, withId(syntheticCrafting, NORMAL_ID));
		assertTooltipIds(createLayout(replacements, syntheticCrafting), List.of(NORMAL_ID));
		copy(gui, replacements, syntheticCrafting);
		check(client.keyboardHandler.getClipboard().equals(NORMAL_ID.toString()), "Synthetic displays must retain replacement source IDs");
	}

	private static <R extends Recipe<?>> R assertSyntheticDisplay(Minecraft client, RecipesGui gui, RecipeType<R> recipeType) {
		var manager = Internal.getJeiRuntime().getRecipeManager();
		var category = manager.getRecipeCategory(recipeType);
		var template = manager.createRecipeLookup(recipeType).get().filter(recipe -> !recipe.isSpecial()).findFirst().orElseThrow();
		R synthetic = withId(template, SYNTHETIC_ID);
		var layout = createLayout(category, synthetic);
		assertTooltipIds(layout, List.of());
		String clipboard = client.keyboardHandler.getClipboard();
		copy(gui, category, synthetic);
		check(client.keyboardHandler.getClipboard().equals(clipboard), "Copying a synthetic display without sources must leave the clipboard intact");
		R normal = withId(template, NORMAL_ID);
		assertTooltipIds(createLayout(category, normal), List.of(NORMAL_ID));
		copy(gui, category, normal);
		check(client.keyboardHandler.getClipboard().equals(NORMAL_ID.toString()), "Normal IDs with nested paths must remain copyable");
		return synthetic;
	}

	@SuppressWarnings("unchecked")
	private static <R extends Recipe<?>> R withId(R template, ResourceLocation id) {
		RecipeSerializer<R> serializer = (RecipeSerializer<R>) template.getSerializer();
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		try {
			serializer.toNetwork(buffer, template);
			return serializer.fromNetwork(id, buffer);
		} finally {
			buffer.release();
		}
	}

	private static <R> void copy(RecipesGui gui, IRecipeCategory<R> category, R recipe) {
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
		category.addCategoryExtension(CraftingRecipe.class, recipe -> new CraftingCategoryExtension<>(recipe) {
			@Override
			public List<ResourceLocation> getSourceRecipeIds() {
				return sources;
			}
		});
		return category;
	}

	private static <R> IRecipeLayoutDrawable<R> createLayout(IRecipeCategory<R> category, R recipe) {
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

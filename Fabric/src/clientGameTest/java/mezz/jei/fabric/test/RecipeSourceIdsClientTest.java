package mezz.jei.fabric.test;

import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IAdvancedRegistration;
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
import mezz.jei.library.plugins.vanilla.crafting.replacers.TippedArrowRecipeMaker;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"UnstableApiUsage", "removal"})
public final class RecipeSourceIdsClientTest implements IModPlugin {
	private static final ResourceLocation SHIELD_DECORATION = ResourceLocation.withDefaultNamespace("shield_decoration");
	private static final ResourceLocation SYNTHETIC_ID = ResourceLocation.fromNamespaceAndPath("jei_test", "/synthetic/recipe");
	private static final ResourceLocation NORMAL_ID = ResourceLocation.fromNamespaceAndPath("jei_test", "folder/recipe");
	private static final List<ResourceLocation> MULTIPLE_SOURCES = List.of(
		SHIELD_DECORATION,
		ResourceLocation.withDefaultNamespace("crafting_table")
	);
	private static final Map<ResourceLocation, List<ResourceLocation>> DECORATED_SOURCES = new HashMap<>();

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath("jei_test", "recipe_source_ids");
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		registerDecorators(registration, RecipeTypes.CRAFTING);
		registerDecorators(registration, RecipeTypes.SMELTING);
	}

	private static <R extends Recipe<?>> void registerDecorators(IAdvancedRegistration registration, RecipeType<RecipeHolder<R>> recipeType) {
		registration.addRecipeCategoryDecorator(recipeType, new IRecipeCategoryDecorator<>() {});
		registration.addRecipeCategoryDecorator(recipeType, new IRecipeCategoryDecorator<>() {
			@Override
			public void decorateSourceRecipeIds(List<ResourceLocation> sourceRecipeIds, RecipeHolder<R> recipe, IRecipeCategory<RecipeHolder<R>> recipeCategory) {
				List<ResourceLocation> sources = DECORATED_SOURCES.get(recipe.id());
				if (sources != null) {
					sourceRecipeIds.addAll(sources);
				}
			}
		});
		registration.addRecipeCategoryDecorator(recipeType, new IRecipeCategoryDecorator<>() {
			@Override
			public void decorateSourceRecipeIds(List<ResourceLocation> sourceRecipeIds, RecipeHolder<R> recipe, IRecipeCategory<RecipeHolder<R>> recipeCategory) {
				List<ResourceLocation> sources = DECORATED_SOURCES.get(recipe.id());
				if (sources != null) {
					if (sources.isEmpty()) {
						sourceRecipeIds.clear();
					} else {
						sourceRecipeIds.remove(SHIELD_DECORATION);
						sourceRecipeIds.add(NORMAL_ID);
					}
				}
			}
		});
	}

	static void run() {
		var context = new FabricClientTestContext();
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var manager = runtime.getRecipeManager();
			var category = manager.getRecipeCategory(RecipeTypes.CRAFTING);
			var recipes = manager.createRecipeLookup(RecipeTypes.CRAFTING).get()
				.filter(recipe -> recipe.id().getPath().startsWith("/jei.shield.decoration."))
				.limit(2)
				.toList();
			check(recipes.size() == 2, "Expected distinct registered shield-decoration displays");
			boolean advancedTooltips = client.options.advancedItemTooltips;
			try {
				client.options.advancedItemTooltips = true;
				List<IRecipeLayoutDrawable<?>> layouts = new ArrayList<>();
				for (var recipe : recipes) {
					var layout = createLayout(category, recipe);
					assertTooltipIds(layout, List.of(SHIELD_DECORATION));
					layouts.add(layout);
				}
				var tippedArrows = TippedArrowRecipeMaker.createRecipes(runtime.getJeiHelpers());
				layouts.add(createLayout(category, tippedArrows.getFirst()));
				for (String path : List.of("firework_star", "firework_star_fade")) {
					var layout = createLayoutForPrefix(RecipeTypes.CRAFTING, "/" + path + "/jei_samples/");
					assertTooltipIds(layout, List.of(ResourceLocation.withDefaultNamespace(path)));
					layouts.add(layout);
				}
				var sponge = createLayoutForPrefix(RecipeTypes.SMELTING, "/sponge_with_bucket");
				assertTooltipIds(sponge, List.of(ResourceLocation.withDefaultNamespace("sponge")));
				layouts.add(sponge);
				layouts.add(createLayoutForPrefix(RecipeTypes.ANVIL, "/anvil.self_repair."));
				layouts.add(createLayoutForPrefix(RecipeTypes.ANVIL, "/anvil.materials_repair."));
				layouts.add(createLayoutForPrefix(RecipeTypes.GRINDSTONE, "/grindstone.self_repair."));
				layouts.add(createLayoutForPrefix(RecipeTypes.GRINDSTONE, "/grindstone.disenchantment."));
				layouts.add(createLayoutForPrefix(RecipeTypes.COMPOSTING, "/"));
				layouts.add(createLayoutForPrefix(RecipeTypes.SMOKING, ""));
				layouts.add(createLayoutForPrefix(RecipeTypes.BREWING, ""));
				assertBookmarks(client, layouts);
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

	private static void assertBookmarks(Minecraft client, List<IRecipeLayoutDrawable<?>> layouts) {
		var runtime = Internal.getJeiRuntime();
		var manager = runtime.getRecipeManager();
		var transferService = new RecipeTransferService(runtime.getRecipeTransferManager());
		var codecHelper = runtime.getJeiHelpers().getCodecHelper();
		var registryAccess = Objects.requireNonNull(client.level).registryAccess();
		var bookmarkFactory = new BookmarkFactory(codecHelper, registryAccess, runtime.getIngredientManager());
		var codec = BookmarkCodec.create(codecHelper, runtime.getIngredientManager(), manager, transferService, bookmarkFactory).codec();
		var ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
		var bookmarks = new HashSet<>();
		for (var layout : layouts) {
			var bookmark = Objects.requireNonNull(RecipeBookmark.create(layout, runtime.getIngredientManager(), transferService));
			var encoded = codec.encodeStart(ops, bookmark).getOrThrow().getAsJsonObject();
			var decoded = codec.parse(ops, encoded).getOrThrow();
			check(bookmark.equals(decoded), "Bookmark JSON must preserve the individual display identity");
			bookmarks.add(decoded);
			if (layout.getRecipe() instanceof RecipeHolder<?> recipe) {
				var expectedRecipeData = codecHelper.getRecipeHolderCodec().encodeStart(ops, recipe).getOrThrow();
				check(expectedRecipeData.equals(encoded.get("recipe")), "Bookmarks must preserve the recipe holder data");
			}
			ResourceLocation id = getRegistryName(layout);
			if (id.getPath().startsWith("/")) {
				String legacyId = id.withPath(id.getPath().substring(1)).toString();
				var legacy = encoded.deepCopy();
				if (layout.getRecipe() instanceof RecipeHolder<?>) {
					legacy.getAsJsonArray("recipe").set(0, new JsonPrimitive(legacyId));
				} else {
					legacy.getAsJsonObject("recipe").addProperty("registryId", legacyId);
				}
				var migrated = codec.parse(ops, legacy).getOrThrow();
				check(bookmark.equals(migrated), "Legacy bookmarks must resolve to the current display: " + legacyId);
				check(encoded.equals(codec.encodeStart(ops, migrated).getOrThrow()), "Legacy bookmarks must save with synthetic IDs: " + legacyId);
			}
		}
		check(bookmarks.size() == layouts.size(), "Recipe displays must remain distinct after saving bookmarks");
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
			assertSyntheticDisplays(client, gui);
			assertDecoratedSources(client, gui, RecipeTypes.CRAFTING);
			assertDecoratedSources(client, gui, RecipeTypes.SMELTING);
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
		var normalCrafting = new RecipeHolder<>(NORMAL_ID, syntheticCrafting.value());
		replacements.addReplacementSource(syntheticCrafting, normalCrafting);
		assertTooltipIds(createLayout(replacements, syntheticCrafting), List.of(NORMAL_ID));
		copy(gui, replacements, syntheticCrafting);
		check(client.keyboardHandler.getClipboard().equals(NORMAL_ID.toString()), "Synthetic displays must retain replacement source IDs");
	}

	private static <R extends Recipe<?>> RecipeHolder<R> assertSyntheticDisplay(Minecraft client, RecipesGui gui, RecipeType<RecipeHolder<R>> recipeType) {
		var runtime = Internal.getJeiRuntime();
		var manager = runtime.getRecipeManager();
		var category = manager.getRecipeCategory(recipeType);
		var template = manager.createRecipeLookup(recipeType).get()
			.filter(recipe -> !recipe.value().isSpecial())
			.findFirst().orElseThrow();
		var synthetic = new RecipeHolder<>(SYNTHETIC_ID, template.value());
		var normal = new RecipeHolder<>(NORMAL_ID, template.value());
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

	private static <R extends Recipe<?>> void assertDecoratedSources(Minecraft client, RecipesGui gui, RecipeType<RecipeHolder<R>> recipeType) {
		var manager = Internal.getJeiRuntime().getRecipeManager();
		var category = manager.getRecipeCategory(recipeType);
		var recipe = manager.createRecipeLookup(recipeType).get()
			.filter(holder -> !holder.value().isSpecial() && !holder.id().getPath().startsWith("/"))
			.findFirst().orElseThrow();
		ResourceLocation recipeId = recipe.id();
		var expectedSources = new ArrayList<>(category.getSourceRecipeIds(recipe));
		expectedSources.add(MULTIPLE_SOURCES.getLast());
		expectedSources.add(NORMAL_ID);
		var sources = new ArrayList<>(MULTIPLE_SOURCES);
		DECORATED_SOURCES.put(recipeId, sources);
		try {
			var layout = createLayout(category, recipe);
			assertTooltipIds(layout, expectedSources);
			sources.clear();
			assertTooltipIds(layout, expectedSources);
			copy(gui, layout);
			check(client.keyboardHandler.getClipboard().equals(expectedSources.toString()), "Copying must use the same decorator IDs retained by the layout's tooltips");
			check(getRegistryName(layout).equals(recipeId), "Decorators must preserve the display identifier");
			assertBookmarks(client, List.of(layout));

			var empty = createLayout(category, recipe);
			assertTooltipIds(empty, List.of());
			copy(gui, empty);
			check(client.keyboardHandler.getClipboard().equals(expectedSources.toString()), "A decorator that clears the source IDs must leave the clipboard intact");
		} finally {
			DECORATED_SOURCES.remove(recipeId);
		}
		assertTooltipIds(createLayout(category, recipe), category.getSourceRecipeIds(recipe));
	}

	private static <R> void copy(RecipesGui gui, IRecipeCategory<R> category, R recipe) {
		copy(gui, createLayout(category, recipe));
	}

	private static <R> void copy(RecipesGui gui, IRecipeLayoutDrawable<R> layout) {
		var runtime = Internal.getJeiRuntime();
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

	private static <R> IRecipeLayoutDrawable<R> createLayout(IRecipeCategory<R> category, R recipe) {
		return Internal.getJeiRuntime().getRecipeManager().createRecipeLayoutDrawable(category, recipe, FocusGroup.EMPTY).orElseThrow();
	}

	private static <R> IRecipeLayoutDrawable<R> createLayoutForPrefix(RecipeType<R> recipeType, String prefix) {
		var manager = Internal.getJeiRuntime().getRecipeManager();
		var category = manager.getRecipeCategory(recipeType);
		var recipe = manager.createRecipeLookup(recipeType).get()
			.filter(value -> Objects.requireNonNull(category.getRegistryName(value)).getPath().startsWith(prefix))
			.findFirst().orElseThrow(() -> new AssertionError("No recipe found for " + recipeType + ": " + prefix));
		return createLayout(category, recipe);
	}

	private static <R> ResourceLocation getRegistryName(IRecipeLayoutDrawable<R> layout) {
		return Objects.requireNonNull(layout.getRecipeCategory().getRegistryName(layout.getRecipe()));
	}

	private static void assertTooltipIds(IRecipeLayoutDrawable<?> layout, List<ResourceLocation> expectedIds) {
		check(layout.getSourceRecipeIds().equals(expectedIds), "Layout source IDs must match the tooltip IDs");
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

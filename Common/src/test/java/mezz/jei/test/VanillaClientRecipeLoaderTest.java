package mezz.jei.test;

import com.mojang.serialization.Lifecycle;
import mezz.jei.common.recipes.VanillaClientRecipeLoader;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.tags.DialogTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests loading vanilla recipes from the client-bundled server-data resources.
 */
public class VanillaClientRecipeLoaderTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	public void loadsVanillaRecipesFromClientResources() {
		// Setup
		RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

		// Operation
		RecipeMap recipeMap = VanillaClientRecipeLoader.getVanillaRecipes(registryAccess);

		// Assertions
		assertFalse(recipeMap.values().isEmpty(), "Expected vanilla recipes to load from client resources.");
		ResourceKey<Recipe<?>> craftingTableKey = ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("crafting_table"));
		assertNotNull(recipeMap.byKey(craftingTableKey), "Expected the vanilla crafting table recipe to be loaded.");
	}

	@Test
	public void preservesServerTagsAndResolvesVanillaRecipeIngredients() {
		var items = new MappedRegistry<>(Registries.ITEM, Lifecycle.stable());
		BuiltInRegistries.ITEM.listElements()
			.forEach(holder -> items.register(holder.key(), holder.value(), RegistrationInfo.BUILT_IN));
		var stone = items.get(Identifier.withDefaultNamespace("stone")).orElseThrow();
		var dirt = items.get(Identifier.withDefaultNamespace("dirt")).orElseThrow();
		TagKey<Item> serverItems = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("jei", "server_items"));
		items.bindTags(Map.of(serverItems, List.of(stone, dirt), ItemTags.PLANKS, List.of(stone)));
		items.freeze();

		var dialogs = new MappedRegistry<>(Registries.DIALOG, Lifecycle.stable());
		ResourceKey<Dialog> dialogKey = ResourceKey.create(Registries.DIALOG, Identifier.fromNamespaceAndPath("jei", "server_dialog"));
		var dialogData = new CommonDialogData(Component.literal("Server dialog"), Optional.empty(), true, false, DialogAction.CLOSE, List.of(), List.of());
		var dialog = dialogs.register(dialogKey, new NoticeDialog(dialogData, NoticeDialog.DEFAULT_ACTION), RegistrationInfo.BUILT_IN);
		dialogs.bindTags(Map.of(DialogTags.PAUSE_SCREEN_ADDITIONS, List.of(dialog)));
		dialogs.freeze();

		var vanillaRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		var otherRegistries = vanillaRegistries.registries()
			.filter(entry -> !entry.key().equals(Registries.ITEM))
			.map(RegistryAccess.RegistryEntry::value);
		var connectionRegistries = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(otherRegistries, Stream.of(items, dialogs)).toList())
			.freeze();

		var recipes = VanillaClientRecipeLoader.getVanillaRecipes(connectionRegistries);

		assertEquals(List.of(stone, dirt), items.getOrThrow(serverItems).stream().toList());
		assertEquals(List.of(stone), items.getOrThrow(ItemTags.PLANKS).stream().toList());
		assertEquals(List.of(dialog), dialogs.getOrThrow(DialogTags.PAUSE_SCREEN_ADDITIONS).stream().toList());
		assertTrue(items.get(ItemTags.CLONABLE_MAPS).isEmpty(), "The fallback tag should stay out of the connection registry.");

		ResourceKey<Recipe<?>> mapCloningKey = ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("map_cloning"));
		var recipe = recipes.byKey(mapCloningKey);
		assertNotNull(recipe, "Expected the vanilla map-cloning recipe to be loaded.");
		var ingredient = recipe.value().placementInfo().ingredients().getFirst();
		var display = assertInstanceOf(SlotDisplay.TagSlotDisplay.class, ingredient.display());
		var filledMap = items.get(Identifier.withDefaultNamespace("filled_map")).orElseThrow();
		assertTrue(display.tag().stream().anyMatch(filledMap::equals), "Expected the fallback tag to resolve the connection's filled map holder.");
		assertTrue(ingredient.acceptsItem(filledMap), "Expected the fallback ingredient to accept a filled map.");
	}
}

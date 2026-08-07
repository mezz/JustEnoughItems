package mezz.jei.common.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.crafting.RecipeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Stream;

/**
 * Loads the vanilla recipes from the client recipe registry.
 * <p>
 * This is only a fallback for connections where the server does not send recipe data to JEI.
 * The returned recipes do not include server datapacks or server-side modded recipes, so they
 * may not match the recipes that are actually available on the server.
 */
public final class VanillaClientRecipeLoader {
	private static final Logger LOGGER = LogManager.getLogger();

	private VanillaClientRecipeLoader() {

	}

	/**
	 * Returns a recipe map using the given registry access.
	 */
	public static RecipeMap getVanillaRecipes(RegistryAccess registryAccess) {
		try {
			RecipeMap recipeMap = registryAccess.lookup(Registries.RECIPE)
				.map(RecipeMap::create)
				.filter(map -> !map.values().isEmpty())
				.orElseGet(() -> loadVanillaRecipeRegistry(registryAccess));
			LOGGER.info("Loaded {} vanilla recipes from the client recipe registry.", recipeMap.values().size());
			return recipeMap;
		} catch (RuntimeException e) {
			LOGGER.error("Failed to load vanilla recipes from the client recipe registry.", e);
			return RecipeMap.EMPTY;
		}
	}

	private static RecipeMap loadVanillaRecipeRegistry(RegistryAccess registryAccess) {
		var recipeRegistryData = RegistryDataLoader.RELOADABLE_REGISTRIES.stream()
			.filter(registryData -> registryData.key().equals(Registries.RECIPE))
			.toList();
		try (CloseableResourceManager resourceManager = new MultiPackResourceManager(
			PackType.SERVER_DATA,
			List.of(ServerPacksSource.createVanillaPackSource().fullResources())
		)) {
			RegistryAccess.Frozen baseRegistryAccess = registryAccess.freeze();
			List<Registry.PendingTags<?>> basePendingTags = TagLoader.loadTagsForExistingRegistries(resourceManager, baseRegistryAccess);
			List<HolderLookup.RegistryLookup<?>> baseLookups = TagLoader.buildUpdatedLookups(baseRegistryAccess, basePendingTags);
			RegistryAccess.Frozen worldRegistryAccess = RegistryDataLoader.load(resourceManager, baseLookups, RegistryDataLoader.WORLD_REGISTRIES, Runnable::run)
				.join();
			List<Registry.PendingTags<?>> worldPendingTags = TagLoader.loadTagsForExistingRegistries(resourceManager, worldRegistryAccess);
			List<HolderLookup.RegistryLookup<?>> worldLookups = TagLoader.buildUpdatedLookups(worldRegistryAccess, worldPendingTags);
			var existingRegistryKeys = baseLookups.stream()
				.map(HolderLookup.RegistryLookup::key)
				.toList();
			List<HolderLookup.RegistryLookup<?>> updatedLookups = Stream.concat(
					baseLookups.stream(),
					worldLookups.stream()
						.filter(lookup -> !existingRegistryKeys.contains(lookup.key()))
				)
				.toList();
			RegistryAccess.Frozen recipeRegistryAccess = RegistryDataLoader.load(resourceManager, updatedLookups, recipeRegistryData, Runnable::run)
				.join();
			basePendingTags.forEach(Registry.PendingTags::apply);
			worldPendingTags.forEach(Registry.PendingTags::apply);
			return RecipeMap.create(recipeRegistryAccess.lookupOrThrow(Registries.RECIPE));
		}
	}
}

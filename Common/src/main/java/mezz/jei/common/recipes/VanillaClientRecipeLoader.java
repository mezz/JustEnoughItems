package mezz.jei.common.recipes;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Loads the vanilla recipe JSON files that are bundled with the Minecraft client.
 * <p>
 * This is only a fallback for connections where the server does not send recipe data to JEI.
 * The returned recipes do not include server datapacks or server-side modded recipes, so they
 * may not match the recipes that are actually available on the server.
 */
public final class VanillaClientRecipeLoader {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final FileToIdConverter RECIPE_LISTER = FileToIdConverter.registry(Registries.RECIPE);

	private VanillaClientRecipeLoader() {

	}

	/**
	 * Parses the bundled vanilla server-data pack and returns a recipe map using the given registry access.
	 */
	public static RecipeMap getVanillaRecipes(RegistryAccess registryAccess) {
		SortedMap<Identifier, Recipe<?>> recipes = new TreeMap<>();

		try (CloseableResourceManager resourceManager = createVanillaServerDataResourceManager()) {
			RegistryOps<JsonElement> ops = createVanillaServerDataSerializationContext(registryAccess, resourceManager);
			SimpleJsonResourceReloadListener.scanDirectory(
				resourceManager,
				RECIPE_LISTER,
				ops,
				Recipe.CODEC,
				recipes
			);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to load vanilla recipes from client resources.", e);
			return RecipeMap.EMPTY;
		}

		List<RecipeHolder<?>> recipeHolders = new ArrayList<>(recipes.size());
		recipes.forEach((id, recipe) -> {
			ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
			recipeHolders.add(new RecipeHolder<>(key, recipe));
		});

		RecipeMap recipeMap = RecipeMap.create(recipeHolders);
		LOGGER.info("Loaded {} vanilla recipes from client resources.", recipeMap.values().size());
		return recipeMap;
	}

	private static CloseableResourceManager createVanillaServerDataResourceManager() {
		return new MultiPackResourceManager(
			PackType.SERVER_DATA,
			List.of(ServerPacksSource.createVanillaPackSource())
		);
	}

	private static RegistryOps<JsonElement> createVanillaServerDataSerializationContext(
		RegistryAccess registryAccess,
		CloseableResourceManager resourceManager
	) {
		HolderLookup.Provider lookupProvider = HolderLookup.Provider.create(registryAccess.registries()
			.map(entry -> createFallbackLookup(resourceManager, entry.value())));
		return lookupProvider.createSerializationContext(JsonOps.INSTANCE);
	}

	private static <T> HolderLookup.RegistryLookup<T> createFallbackLookup(ResourceManager resources, Registry<T> registry) {
		TagLoader<Holder<T>> tagLoader = new TagLoader<>((id, required) -> registry.get(id), Registries.tagsDirPath(registry.key()));
		var tagContents = tagLoader.build(tagLoader.load(resources));
		if (tagContents.isEmpty()) {
			return registry;
		}

		// Bind separate tag sets so recipes keep the original element holders and the live registry keeps its server tags.
		Map<TagKey<T>, HolderSet.Named<T>> tags = new HashMap<>();
		tagContents.forEach((id, contents) -> {
			TagKey<T> key = TagKey.create(registry.key(), id);
			tags.put(key, new FallbackTag<>(registry, key, contents));
		});
		return new FallbackLookup<>(registry, tags);
	}

	private record FallbackLookup<T>(
		Registry<T> parent,
		Map<TagKey<T>, HolderSet.Named<T>> tags
	) implements HolderLookup.RegistryLookup.Delegate<T> {
		@Override
		public Optional<HolderSet.Named<T>> get(TagKey<T> key) {
			return Optional.ofNullable(tags.get(key));
		}

		@Override
		public Stream<HolderSet.Named<T>> listTags() {
			return tags.values().stream();
		}
	}

	private static final class FallbackTag<T> extends HolderSet.Named<T> {
		private FallbackTag(Registry<T> owner, TagKey<T> key, List<Holder<T>> contents) {
			super(owner, key);
			bind(contents);
		}

		@Override
		public boolean contains(Holder<T> holder) {
			// Use the fallback tag contents because each holder still tracks the server's tag membership.
			return contents().contains(holder);
		}
	}
}

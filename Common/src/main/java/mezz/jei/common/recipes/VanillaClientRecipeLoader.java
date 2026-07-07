package mezz.jei.common.recipes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the vanilla recipe JSON files that are bundled with the Minecraft client.
 * <p>
 * This is only a fallback for connections where the server does not send recipe data to JEI.
 * The returned recipes do not include server datapacks or server-side modded recipes, so they
 * may not match the recipes that are actually available on the server.
 */
public final class VanillaClientRecipeLoader {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private static final String RECIPE_DIRECTORY = "recipes";
	private static final String JSON_EXTENSION = ".json";

	private VanillaClientRecipeLoader() {

	}

	/**
	 * Parses the bundled vanilla server-data pack and returns recipes.
	 */
	public static List<Recipe<?>> getVanillaRecipes() {
		Map<ResourceLocation, JsonElement> recipeJson;

		try (CloseableResourceManager resourceManager = createVanillaServerDataResourceManager()) {
			recipeJson = loadRecipeJson(resourceManager);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to load vanilla recipes from client resources.", e);
			return List.of();
		}

		List<Recipe<?>> recipes = new ArrayList<>(recipeJson.size());
		recipeJson.forEach((id, json) -> {
			try {
				JsonObject jsonObject = json.getAsJsonObject();
				Recipe<?> recipe = RecipeManager.fromJson(id, jsonObject);
				recipes.add(recipe);
			} catch (RuntimeException e) {
				LOGGER.error("Failed to parse vanilla recipe {} from client resources.", id, e);
			}
		});

		LOGGER.info("Loaded {} vanilla recipes from client resources.", recipes.size());
		return recipes;
	}

	private static Map<ResourceLocation, JsonElement> loadRecipeJson(ResourceManager resourceManager) {
		Map<ResourceLocation, Resource> resources = resourceManager.listResources(
			RECIPE_DIRECTORY,
			resourceLocation -> resourceLocation.getPath().endsWith(JSON_EXTENSION)
		);
		Map<ResourceLocation, JsonElement> recipeJson = new HashMap<>(resources.size());
		resources.forEach((resourceLocation, resource) -> {
			ResourceLocation recipeId = getRecipeId(resourceLocation);
			try (BufferedReader reader = resource.openAsReader()) {
				JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
				recipeJson.put(recipeId, jsonElement);
			} catch (IOException | RuntimeException e) {
				LOGGER.error("Failed to load vanilla recipe JSON {} from client resources.", recipeId, e);
			}
		});
		return recipeJson;
	}

	private static ResourceLocation getRecipeId(ResourceLocation resourceLocation) {
		String path = resourceLocation.getPath();
		String recipePath = path.substring(RECIPE_DIRECTORY.length() + 1, path.length() - JSON_EXTENSION.length());
		return new ResourceLocation(resourceLocation.getNamespace(), recipePath);
	}

	private static CloseableResourceManager createVanillaServerDataResourceManager() {
		PackResources vanillaPack = openVanillaServerDataPack();
		return new MultiPackResourceManager(
			PackType.SERVER_DATA,
			List.of(vanillaPack)
		);
	}

	private static PackResources openVanillaServerDataPack() {
		List<Pack> packs = new ArrayList<>();
		ServerPacksSource source = new ServerPacksSource();
		source.loadPacks(packs::add, (id, title, required, resources, metadata, position, packSource) ->
			new Pack(id, title, required, resources, metadata, PackType.SERVER_DATA, position, packSource)
		);
		return packs.stream()
			.filter(pack -> pack.getId().equals(ServerPacksSource.VANILLA_ID))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Failed to find vanilla server data pack."))
			.open();
	}

}

package mezz.jei.common.recipes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private VanillaClientRecipeLoader() {

	}

	/**
	 * Parses the bundled vanilla server-data pack and returns recipes.
	 */
	public static List<Recipe<?>> getVanillaRecipes() {
		Map<ResourceLocation, JsonElement> recipeJson = new HashMap<>();

		try (CloseableResourceManager resourceManager = createVanillaServerDataResourceManager()) {
			SimpleJsonResourceReloadListener.scanDirectory(
				resourceManager,
				"recipes",
				GSON,
				recipeJson
			);
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

	private static CloseableResourceManager createVanillaServerDataResourceManager() {
		ServerPacksSource source = new ServerPacksSource();
		PackResources vanillaPack = source.getVanillaPack();
		return new MultiPackResourceManager(
			PackType.SERVER_DATA,
			List.of(vanillaPack)
		);
	}

}

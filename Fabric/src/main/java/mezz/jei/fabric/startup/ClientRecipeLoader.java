package mezz.jei.fabric.startup;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client-side recipe patch.
 * <p>
 * Since Minecraft 1.21.2 recipes live server-side and reach JEI through Fabric's
 * {@code ClientRecipeSynchronizedEvent}. On servers that do not send that sync
 * (e.g. older servers reached through ViaVersion), JEI would otherwise have no
 * recipes at all. This loads the client's own built-in (vanilla) recipe files so
 * recipes can still be browsed, using the connected server's registries for
 * deserialization.
 */
public final class ClientRecipeLoader {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String RECIPE_DIR = "recipe";
	private static final String JSON_SUFFIX = ".json";

	private ClientRecipeLoader() {}

	public static Optional<RecipeMap> loadClientSideRecipes() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null) {
			return Optional.empty();
		}
		RegistryAccess.Frozen registryAccess = connection.registryAccess();
		RegistryOps<JsonElement> ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);

		VanillaPackResources vanillaPack = ServerPacksSource.createVanillaPackSource();
		List<RecipeHolder<?>> holders = new ArrayList<>();
		int failed = 0;
		try (MultiPackResourceManager resourceManager =
				 new MultiPackResourceManager(PackType.SERVER_DATA, List.of(vanillaPack))) {
			Map<Identifier, Resource> resources =
				resourceManager.listResources(RECIPE_DIR, id -> id.getPath().endsWith(JSON_SUFFIX));
			for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
				Identifier fileId = entry.getKey();
				String path = fileId.getPath();
				// "recipe/foo/bar.json" -> "foo/bar"
				String recipePath = path.substring(RECIPE_DIR.length() + 1, path.length() - JSON_SUFFIX.length());
				ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, fileId.withPath(recipePath));
				try (Reader reader = entry.getValue().openAsReader()) {
					JsonElement json = JsonParser.parseReader(reader);
					Recipe<?> recipe = Recipe.CODEC.parse(ops, json).getOrThrow();
					holders.add(makeHolder(key, recipe));
				} catch (Exception e) {
					failed++;
				}
			}
		} catch (Exception e) {
			LOGGER.error("JEI client-side recipe loading failed", e);
			return Optional.empty();
		}

		if (holders.isEmpty()) {
			return Optional.empty();
		}
		LOGGER.info("JEI loaded {} recipes from client-side data ({} skipped)", holders.size(), failed);
		return Optional.of(RecipeMap.create(holders));
	}

	private static <T extends Recipe<?>> RecipeHolder<T> makeHolder(ResourceKey<Recipe<?>> key, T recipe) {
		return new RecipeHolder<>(key, recipe);
	}
}

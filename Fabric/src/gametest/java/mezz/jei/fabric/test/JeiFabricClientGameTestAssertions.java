package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import mezz.jei.common.network.ClientConnectionHelper;
import mezz.jei.common.network.IConnectionToServer;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;

@SuppressWarnings("UnstableApiUsage")
final class JeiFabricClientGameTestAssertions {
	private static final ResourceKey<Recipe<?>> CRAFTING_TABLE_RECIPE_KEY = ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("crafting_table"));

	private JeiFabricClientGameTestAssertions() {

	}

	public static void assertJeiStartedWithSyncedRecipes(ClientGameTestContext context) {
		context.waitFor(client -> hasJeiRuntime(), ClientGameTestContext.DEFAULT_TIMEOUT);

		boolean hasSyncedRecipes = context.computeOnClient(client -> Internal.hasClientSyncedRecipes());
		if (!hasSyncedRecipes) {
			throw new AssertionError("Expected JEI to keep Fabric's synced recipes after joining a world.");
		}

		RecipeMap syncedRecipes = context.computeOnClient(client -> Internal.getClientSyncedRecipes());
		assertHasVanillaRecipes(syncedRecipes, "Expected JEI to keep Fabric's synced recipes after joining a world.");
	}

	public static void assertJeiStartedWithFallbackRecipes(ClientGameTestContext context) {
		context.waitFor(client -> hasJeiRuntime(), ClientGameTestContext.DEFAULT_TIMEOUT);

		boolean hasSyncedRecipes = context.computeOnClient(client -> Internal.hasClientSyncedRecipes());
		if (hasSyncedRecipes) {
			throw new AssertionError("Expected JEI to use fallback vanilla recipes when the server does not sync recipes.");
		}

		RecipeMap fallbackRecipes = context.computeOnClient(client -> Internal.getClientSyncedRecipes());
		assertHasVanillaRecipes(fallbackRecipes, "Expected JEI to use fallback vanilla recipes when the server does not sync recipes.");
	}

	public static void assertServerHasJei(ClientGameTestContext context) {
		assertSameModLoader(context);

		boolean isJeiOnServer = context.computeOnClient(client -> Internal.getServerConnection().isJeiOnServer());
		if (!isJeiOnServer) {
			String serverBrand = context.computeOnClient(client -> ClientConnectionHelper.getServerBrand());
			throw new AssertionError("Expected JEI to detect that JEI is installed on the " + serverBrand + " server.");
		}
	}

	public static void assertServerMissingJei(ClientGameTestContext context) {
		assertSameModLoader(context);

		boolean isJeiOnServer = context.computeOnClient(client -> Internal.getServerConnection().isJeiOnServer());
		if (isJeiOnServer) {
			String serverBrand = context.computeOnClient(client -> ClientConnectionHelper.getServerBrand());
			throw new AssertionError("Expected JEI to detect that JEI is missing from the " + serverBrand + " server.");
		}
	}

	public static void assertVanillaServer(ClientGameTestContext context) {
		boolean sameModLoader = context.computeOnClient(client -> Internal.getServerConnection().isSameModLoader());
		if (sameModLoader) {
			String serverBrand = context.computeOnClient(client -> ClientConnectionHelper.getServerBrand());
			throw new AssertionError("Expected JEI to detect a server without the client's mod loader, got: " + serverBrand);
		}

		boolean isJeiOnServer = context.computeOnClient(client -> Internal.getServerConnection().isJeiOnServer());
		if (isJeiOnServer) {
			throw new AssertionError("Expected JEI to detect that JEI is missing from the vanilla server.");
		}
	}

	public static void assertClientRecipesCleared(ClientGameTestContext context, String name) {
		context.waitFor(client -> {
			return client.level == null &&
				!hasJeiRuntime() &&
				!Internal.hasClientRecipes() &&
				Internal.getClientSyncedRecipes().values().isEmpty();
		},
			ClientGameTestContext.DEFAULT_TIMEOUT
		);

		boolean hasClientRecipes = context.computeOnClient(client -> Internal.hasClientRecipes());
		if (hasClientRecipes) {
			throw new AssertionError("Expected JEI to clear client recipes after disconnecting from " + name + ".");
		}
	}

	private static void assertSameModLoader(ClientGameTestContext context) {
		boolean sameModLoader = context.computeOnClient(client -> {
			IConnectionToServer serverConnection = Internal.getServerConnection();
			return serverConnection.isSameModLoader();
		});
		if (!sameModLoader) {
			String serverBrand = context.computeOnClient(client -> ClientConnectionHelper.getServerBrand());
			throw new AssertionError("Expected JEI to detect a server with the client's mod loader, got: " + serverBrand);
		}
	}

	private static void assertHasVanillaRecipes(RecipeMap recipeMap, String message) {
		if (recipeMap.values().isEmpty()) {
			throw new AssertionError(message);
		}
		if (recipeMap.byKey(CRAFTING_TABLE_RECIPE_KEY) == null) {
			throw new AssertionError(message);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static boolean hasJeiRuntime() {
		try {
			Internal.getJeiRuntime();
			return true;
		} catch (IllegalStateException ignored) {
			return false;
		}
	}
}

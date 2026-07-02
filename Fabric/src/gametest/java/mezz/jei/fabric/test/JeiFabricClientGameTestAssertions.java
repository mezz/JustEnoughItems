package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.world.item.crafting.RecipeMap;

final class JeiFabricClientGameTestAssertions {
	private JeiFabricClientGameTestAssertions() {

	}

	public static void assertJeiStartedWithSyncedRecipes(ClientGameTestContext context) {
		context.waitFor(client -> hasJeiRuntime(), ClientGameTestContext.DEFAULT_TIMEOUT);

		RecipeMap syncedRecipes = context.computeOnClient(client -> Internal.getClientSyncedRecipes());
		if (syncedRecipes.values().isEmpty()) {
			throw new AssertionError("Expected JEI to keep Fabric's synced recipes after joining a world.");
		}
	}

	private static boolean hasJeiRuntime() {
		try {
			Internal.getJeiRuntime();
			return true;
		} catch (IllegalStateException ignored) {
			return false;
		}
	}
}

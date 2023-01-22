package mezz.jei.common;

import com.google.common.base.Preconditions;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.config.WorldConfig;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.ClientConnectionHelper;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.DelayedExecutor;
import mezz.jei.common.util.IDelayedExecutor;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * For JEI internal use only, these are normally accessed from the API.
 */
public final class Internal {
	@Nullable
	private static Textures textures;
	@Nullable
	private static IConnectionToServer serverConnection;
	@Nullable
	private static IInternalKeyMappings keyMappings;
	@Nullable
	private static IWorldConfig worldConfig;
	@Nullable
	private static IIngredientManager ingredientManager;
	@Nullable
	private static IJeiRuntime jeiRuntime;
	@Nullable
	private static IJeiClientConfigs jeiClientConfigs;
	@Nullable
	private static ClientRecipes clientRecipes = null;
	private static final DelayedExecutor delayedExecutor = new DelayedExecutor(Duration.ofSeconds(10));

	private Internal() {

	}

	public static Textures getTextures() {
		Preconditions.checkState(textures != null, "Textures has not been created yet.");
		return textures;
	}

	public static void setTextures(Textures textures) {
		Internal.textures = textures;
	}

	public static IConnectionToServer getServerConnection() {
		Preconditions.checkState(serverConnection != null, "Server Connection has not been created yet.");
		return serverConnection;
	}

	public static void setServerConnection(IConnectionToServer serverConnection) {
		Internal.serverConnection = serverConnection;
	}

	public static IInternalKeyMappings getKeyMappings() {
		Preconditions.checkState(keyMappings != null, "Key Mappings have not been created yet.");
		return keyMappings;
	}

	public static void setKeyMappings(IInternalKeyMappings keyMappings) {
		Internal.keyMappings = keyMappings;
	}

	public static IWorldConfig getWorldConfig() {
		if (worldConfig == null) {
			worldConfig = new WorldConfig();
		}
		return worldConfig;
	}

	public static void setIngredientManager(IIngredientManager ingredientManager) {
		Internal.ingredientManager = ingredientManager;
	}

	public static IIngredientManager getIngredientManager() {
		Preconditions.checkState(ingredientManager != null, "Ingredient Manager has not been created yet.");
		return ingredientManager;
	}

	public static void setRuntime(@Nullable IJeiRuntime jeiRuntime) {
		Internal.jeiRuntime = jeiRuntime;
	}

	public static IDelayedExecutor getDelayedExecutor() {
		return delayedExecutor;
	}

	public static IJeiRuntime getJeiRuntime() {
		Preconditions.checkState(jeiRuntime != null, "Jei Runtime has not been created yet.");
		return jeiRuntime;
	}

	public static void setJeiClientConfigs(@Nullable IJeiClientConfigs jeiClientConfigs) {
		Internal.jeiClientConfigs = jeiClientConfigs;
	}

	public static IJeiClientConfigs getJeiClientConfigs() {
		Preconditions.checkState(jeiClientConfigs != null, "JEI Client Configs have not been created yet.");
		return jeiClientConfigs;
	}

	public static Optional<IJeiClientConfigs> getOptionalJeiClientConfigs() {
		return Optional.ofNullable(jeiClientConfigs);
	}

	public static Optional<IJeiRuntime> getOptionalJeiRuntime() {
		return Optional.ofNullable(jeiRuntime);
	}

	@Nullable
	private static String getRemoteConnectionId() {
		ClientPacketListener clientPacketListener = ClientConnectionHelper.getConnectedClientPacketListener();
		if (clientPacketListener != null) {
			SocketAddress remoteAddress = clientPacketListener.getConnection().getRemoteAddress();
			return String.valueOf(remoteAddress);
		}
		return null;
	}

	public static void setClientSyncedRecipes(List<Recipe<?>> clientSyncedRecipes) {
		setClientRecipes(clientSyncedRecipes, true);
	}

	public static void setClientFallbackRecipes(List<Recipe<?>> clientRecipes) {
		setClientRecipes(clientRecipes, false);
	}

	private static void setClientRecipes(List<Recipe<?>> recipes, boolean syncedWithServer) {
		String connectionId = getRemoteConnectionId();
		if (connectionId != null) {
			Internal.clientRecipes = new ClientRecipes(List.copyOf(recipes), connectionId, syncedWithServer);
		}
	}

	public static List<Recipe<?>> getClientSyncedRecipes() {
		ClientRecipes clientRecipes = getClientRecipes();
		if (clientRecipes != null) {
			return clientRecipes.recipes();
		}
		return List.of();
	}

	public static boolean hasClientSyncedRecipes() {
		ClientRecipes clientRecipes = getClientRecipes();
		return clientRecipes != null && clientRecipes.syncedWithServer();
	}

	public static boolean hasClientFallbackRecipes() {
		ClientRecipes clientRecipes = getClientRecipes();
		return clientRecipes != null && !clientRecipes.syncedWithServer();
	}

	public static boolean hasClientRecipes() {
		return getClientRecipes() != null;
	}

	public static void clearClientRecipes() {
		clientRecipes = null;
	}

	@Nullable
	private static ClientRecipes getClientRecipes() {
		if (clientRecipes != null) {
			String connectionId = getRemoteConnectionId();
			if (clientRecipes.connectionId().equals(connectionId)) {
				return clientRecipes;
			}
		}
		return null;
	}

	public static void onRuntimeStopped() {
		closeRecipeGuiIfOpen();

		if (clientRecipes != null) {
			String connectionId = getRemoteConnectionId();
			if (!clientRecipes.connectionId().equals(connectionId)) {
				clientRecipes = null;
			}
		}
		if (jeiClientConfigs != null) {
			jeiClientConfigs.onRuntimeStopped();
		}
		if (worldConfig != null) {
			worldConfig.clearListeners();
		}
		if (serverConnection != null) {
			serverConnection.onRuntimeStopped();
		}
		if (jeiRuntime != null) {
			jeiRuntime = null;
		}
	}

	private static void closeRecipeGuiIfOpen() {
		IJeiRuntime jeiRuntime = Internal.jeiRuntime;
		if (jeiRuntime == null) {
			return;
		}

		IRecipesGui recipesGui = jeiRuntime.getRecipesGui();
		if (recipesGui instanceof Screen recipesScreen) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.screen == recipesScreen) {
				recipesScreen.onClose();
			}
		}
	}

	public static void onClientStopping() {
		onRuntimeStopped();
		delayedExecutor.shutdown();
	}

	private record ClientRecipes(List<Recipe<?>> recipes, String connectionId, boolean syncedWithServer) {

	}
}

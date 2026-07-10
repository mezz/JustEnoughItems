package mezz.jei.common;

import com.google.common.base.Preconditions;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.config.ClientToggleState;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.gui.textures.JeiGuiSpriteManager;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.DelayedExecutor;
import mezz.jei.common.util.IDelayedExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

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
	private static IClientToggleState toggleState;
	@Nullable
	private static IJeiClientConfigs jeiClientConfigs;
	@Nullable
	private static IJeiRuntime jeiRuntime;
	@Nullable
	private static ClientRecipes clientRecipes = null;
	private static final JeiFeatures jeiFeatures = new JeiFeatures();
	private static final DelayedExecutor delayedExecutor = new DelayedExecutor(Duration.ofSeconds(10));

	private Internal() {

	}

	public static Textures getTextures() {
		if (textures == null) {
			Minecraft minecraft = Minecraft.getInstance();
			TextureManager textureManager = minecraft.getTextureManager();
			JeiGuiSpriteManager spriteUploader = new JeiGuiSpriteManager(textureManager);
			textures = new Textures(spriteUploader);
		}
		return textures;
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

	public static IClientToggleState getClientToggleState() {
		if (toggleState == null) {
			toggleState = new ClientToggleState();
		}
		return toggleState;
	}

	public static IJeiClientConfigs getJeiClientConfigs() {
		Preconditions.checkState(jeiClientConfigs != null, "Jei Client Configs have not been created yet.");
		return jeiClientConfigs;
	}

	public static Optional<IJeiClientConfigs> getOptionalJeiClientConfigs() {
		return Optional.ofNullable(jeiClientConfigs);
	}

	public static void setJeiClientConfigs(IJeiClientConfigs jeiClientConfigs) {
		Internal.jeiClientConfigs = jeiClientConfigs;
	}

	public static JeiFeatures getJeiFeatures() {
		return jeiFeatures;
	}

	public static IDelayedExecutor getDelayedExecutor() {
		return delayedExecutor;
	}

	public static void setRuntime(IJeiRuntime jeiRuntime) {
		Internal.jeiRuntime = jeiRuntime;
	}

	public static IJeiRuntime getJeiRuntime() {
		Preconditions.checkState(jeiRuntime != null, "Jei Runtime has not been created yet.");

		return jeiRuntime;
	}

	public static Optional<IJeiRuntime> getOptionalJeiRuntime() {
		return Optional.ofNullable(jeiRuntime);
	}

	@Nullable
	private static String getRemoteConnectionId() {
		ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
		if (clientPacketListener != null) {
			Connection connection = clientPacketListener.getConnection();
			if (connection.isConnected()) {
				return connection.getLoggableAddress(true);
			}
		}
		return null;
	}

	public static void setClientSyncedRecipes(List<RecipeHolder<?>> clientSyncedRecipes) {
		setClientRecipes(clientSyncedRecipes, true);
	}

	public static void setClientFallbackRecipes(List<RecipeHolder<?>> clientRecipes) {
		setClientRecipes(clientRecipes, false);
	}

	private static void setClientRecipes(List<RecipeHolder<?>> recipes, boolean syncedWithServer) {
		var connectionId = getRemoteConnectionId();
		if (connectionId != null) {
			Internal.clientRecipes = new ClientRecipes(List.copyOf(recipes), connectionId, syncedWithServer);
		}
	}

	public static List<RecipeHolder<?>> getClientSyncedRecipes() {
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
			var connectionId = getRemoteConnectionId();
			if (clientRecipes.connectionId().equals(connectionId)) {
				return clientRecipes;
			}
		}
		return null;
	}

	public static void onRuntimeStopped() {
		closeRecipeGuiIfOpen();

		if (clientRecipes != null) {
			var connectionId = getRemoteConnectionId();
			if (!clientRecipes.connectionId().equals(connectionId)) {
				clientRecipes = null;
			}
		}
		if (jeiClientConfigs != null) {
			jeiClientConfigs.onRuntimeStopped();
		}
		if (toggleState != null) {
			toggleState.clearListeners();
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

	private record ClientRecipes(List<RecipeHolder<?>> recipes, String connectionId, boolean syncedWithServer) {

	}
}

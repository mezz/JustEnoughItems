package mezz.jei.common;

import com.google.common.base.Preconditions;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.config.ClientToggleState;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.gui.textures.JeiAtlasManager;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.network.Connection;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

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
	private static Pair<RecipeMap, String> clientSyncedRecipes = null;
	private static final JeiFeatures jeiFeatures = new JeiFeatures();

	private Internal() {

	}

	public static Textures getTextures() {
		if (textures == null) {
			Minecraft minecraft = Minecraft.getInstance();
			TextureManager textureManager = minecraft.getTextureManager();
			JeiAtlasManager jeiAtlasManager = new JeiAtlasManager(textureManager,
				new JeiAtlasManager.Config(
					Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS,
					Constants.JEI_GUI_TEXTURE_ATLAS_ID,
					Set.of(AnimationMetadataSection.TYPE, GuiMetadataSection.TYPE)
				)
			);
			textures = new Textures(jeiAtlasManager);
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

	public static void setRuntime(IJeiRuntime jeiRuntime) {
		Internal.jeiRuntime = jeiRuntime;
	}

	public static IJeiRuntime getJeiRuntime() {
		Preconditions.checkState(jeiRuntime != null, "Jei Client Configs have not been created yet.");

		return jeiRuntime;
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

	public static void setClientSyncedRecipes(RecipeMap clientSyncedRecipes) {
		var connectionId = getRemoteConnectionId();
		ErrorUtil.checkNotNull(connectionId, "connectionId");
		Internal.clientSyncedRecipes = new Pair<>(clientSyncedRecipes, connectionId);
	}

	public static RecipeMap getClientSyncedRecipes() {
		if (clientSyncedRecipes != null) {
			var connectionId = getRemoteConnectionId();
			if (clientSyncedRecipes.second().equals(connectionId)) {
				return clientSyncedRecipes.first();
			}
		}
		return RecipeMap.EMPTY;
	}

	public static void onRuntimeStopped() {
		if (clientSyncedRecipes != null) {
			var connectionId = getRemoteConnectionId();
			if (!clientSyncedRecipes.second().equals(connectionId)) {
				clientSyncedRecipes = null;
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
}

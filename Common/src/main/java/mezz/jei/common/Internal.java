package mezz.jei.common;

import com.google.common.base.Preconditions;
import mezz.jei.common.config.WorldConfig;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IWorldConfig;
import org.jetbrains.annotations.Nullable;

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
}

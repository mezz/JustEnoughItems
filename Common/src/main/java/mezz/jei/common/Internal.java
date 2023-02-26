package mezz.jei.common;

import com.google.common.base.Preconditions;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.ClientToggleState;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
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
	private static IClientToggleState toggleState;
	@Nullable
	private static IJeiClientConfigs jeiClientConfigs;

	private Internal() {

	}

	public static Textures getTextures() {
		if (textures == null) {
			Minecraft minecraft = Minecraft.getInstance();
			TextureManager textureManager = minecraft.getTextureManager();
			JeiSpriteUploader spriteUploader = new JeiSpriteUploader(textureManager);
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

	public static void setJeiClientConfigs(IJeiClientConfigs jeiClientConfigs) {
		Internal.jeiClientConfigs = jeiClientConfigs;
	}
}

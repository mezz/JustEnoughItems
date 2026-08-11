package mezz.jei.common.gui.textures;

import com.mojang.logging.LogUtils;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Constants;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JeiGuiSpriteManager extends TextureAtlasHolder {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<ResourceLocation, GuiMetadataSection> BUNDLED_METADATA_BY_SPRITE = new ConcurrentHashMap<>();

	public JeiGuiSpriteManager(TextureManager textureManager) {
		super(textureManager, Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS, Constants.JEI_GUI_TEXTURE_ATLAS_ID, Set.of(AnimationMetadataSection.SERIALIZER, GuiMetadataSection.TYPE));
	}

	/**
	 * Overridden to make it public
	 */
	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}

	public GuiSpriteScaling getSpriteScaling(TextureAtlasSprite sprite) {
		SpriteContents contents = sprite.contents();
		return getSpriteScaling(contents.name(), contents.metadata());
	}

	static GuiSpriteScaling getSpriteScaling(ResourceLocation spriteId, ResourceMetadata metadata) {
		// Minecraft does not inherit metadata when a resource pack replaces only the PNG.
		// Fall back to JEI's bundled metadata so replacement textures keep the intended scaling.
		return metadata.getSection(GuiMetadataSection.TYPE)
			.orElseGet(() -> getBundledMetadata(spriteId))
			.scaling();
	}

	private static GuiMetadataSection getBundledMetadata(ResourceLocation spriteId) {
		if (!spriteId.getNamespace().equals(ModIds.JEI_ID)) {
			return GuiMetadataSection.DEFAULT;
		}
		return BUNDLED_METADATA_BY_SPRITE.computeIfAbsent(spriteId, JeiGuiSpriteManager::loadBundledMetadata);
	}

	private static GuiMetadataSection loadBundledMetadata(ResourceLocation spriteId) {
		String metadataPath = "/assets/jei/textures/jei/atlas/gui/" + spriteId.getPath() + ".png.mcmeta";
		try (InputStream inputStream = JeiGuiSpriteManager.class.getResourceAsStream(metadataPath)) {
			if (inputStream == null) {
				return GuiMetadataSection.DEFAULT;
			}
			return ResourceMetadata.fromJsonStream(inputStream)
				.getSection(GuiMetadataSection.TYPE)
				.orElse(GuiMetadataSection.DEFAULT);
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Failed to load JEI's bundled GUI metadata for {}", spriteId, e);
			return GuiMetadataSection.DEFAULT;
		}
	}

}

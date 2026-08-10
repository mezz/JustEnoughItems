package mezz.jei.neoforge.tests.client;

import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.common.Internal;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/**
 * Verifies that NeoForge development runs include JEI's shared client resources and refresh GUI sprites after resource reloads.
 */
public final class JeiNeoForgeClientResourceTests {
	private static final String JUNIT_SUITE_NAME = "neoforge-client-resources";
	private static final Duration RESOURCE_RELOAD_TIMEOUT = Duration.ofSeconds(30);
	private static final ResourceLocation GUI_BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/gui_background_v2.png");
	private static final String CLIENT_TEST_RESOURCE_PACK_ID = "file/jei-client-test-pack";
	private static final String JEI_MOD_RESOURCE_PACK_ID = "mod/jei";
	private static final ResourceLocation CONFIG_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/icons/config_button.png");
	private static final ResourceLocation CONFIG_BUTTON_SPRITE = ResourceLocation.fromNamespaceAndPath("jei", "icons/config_button");
	private static final int CLIENT_TEST_TEXTURE_SIZE = 32;
	private static final int DEFAULT_TEXTURE_SIZE = 16;
	private static final String FOCUS_SEARCH_TRANSLATION_KEY = "key.jei.focusSearch";

	private JeiNeoForgeClientResourceTests() {

	}

	public static void run() {
		JUnitXmlTestReporter.runAndReport(
			JUNIT_SUITE_NAME,
			JeiNeoForgeClientResourceTests.class.getSimpleName(),
			() -> {
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::assertStartupResourcePackResourcesAreLoaded);
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::disableClientTestResourcePack);
				ClientTestUtil.waitUntil(
					() -> ClientTestUtil.computeOnClient(JeiNeoForgeClientResourceTests::hasReloadedDefaultTestSprite),
					RESOURCE_RELOAD_TIMEOUT,
					() -> "Timed out waiting for JEI's default config-button sprite after disabling the client startup resource pack."
				);
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::assertDefaultTestResourcesAreLoaded);
			}
		);
	}

	private static void assertStartupResourcePackResourcesAreLoaded(Minecraft client) {
		if (client.getResourceManager().getResource(GUI_BACKGROUND_TEXTURE).isEmpty()) {
			throw new AssertionError("Expected the NeoForge development mod to include JEI's Common textures.");
		}
		if (!Language.getInstance().has(FOCUS_SEARCH_TRANSLATION_KEY)) {
			throw new AssertionError("Expected the NeoForge development mod to include JEI's Common translations.");
		}

		Resource configButtonTexture = client.getResourceManager()
			.getResource(CONFIG_BUTTON_TEXTURE)
			.orElseThrow(() -> new AssertionError("Expected the client startup resource pack to provide JEI's config-button texture."));
		if (!configButtonTexture.sourcePackId().equals(CLIENT_TEST_RESOURCE_PACK_ID)) {
			throw new AssertionError(
				"Expected JEI's config-button texture to come from the enabled startup resource pack, but it came from " + configButtonTexture.sourcePackId()
			);
		}
		assertSpriteAndDrawableMatchResource(client, configButtonTexture, CLIENT_TEST_TEXTURE_SIZE);
	}

	private static void disableClientTestResourcePack(Minecraft client) {
		if (!client.getResourcePackRepository().removePack(CLIENT_TEST_RESOURCE_PACK_ID)) {
			throw new AssertionError("Expected the client startup resource pack to be selected before disabling it.");
		}
		client.options.updateResourcePacks(client.getResourcePackRepository());
	}

	private static boolean hasReloadedDefaultTestSprite(Minecraft client) {
		return client.getResourceManager()
			.getResource(CONFIG_BUTTON_TEXTURE)
			.filter(resource -> resource.sourcePackId().equals(JEI_MOD_RESOURCE_PACK_ID))
			.map(resource -> Internal.getTextures()
				.getAtlasManager()
				.getSprite(CONFIG_BUTTON_SPRITE)
				.contents()
			)
			.filter(contents -> contents.width() == DEFAULT_TEXTURE_SIZE && contents.height() == DEFAULT_TEXTURE_SIZE)
			.isPresent();
	}

	private static void assertDefaultTestResourcesAreLoaded(Minecraft client) {
		Resource configButtonTexture = client.getResourceManager()
			.getResource(CONFIG_BUTTON_TEXTURE)
			.orElseThrow(() -> new AssertionError("Expected JEI's default config-button texture after disabling the client startup resource pack."));
		if (!configButtonTexture.sourcePackId().equals(JEI_MOD_RESOURCE_PACK_ID)) {
			throw new AssertionError(
				"Expected JEI's config-button texture to come from JEI after the resource reload, but it came from " + configButtonTexture.sourcePackId()
			);
		}
		assertSpriteAndDrawableMatchResource(client, configButtonTexture, DEFAULT_TEXTURE_SIZE);
	}

	private static void assertSpriteAndDrawableMatchResource(Minecraft client, Resource texture, int expectedTextureSize) {
		TextureAtlasSprite sprite = Internal.getTextures()
			.getAtlasManager()
			.getSprite(CONFIG_BUTTON_SPRITE);
		try (InputStream stream = texture.open();
			NativeImage image = NativeImage.read(stream)
		) {
			int resourceWidth = image.getWidth();
			int resourceHeight = image.getHeight();
			if (resourceWidth != expectedTextureSize || resourceHeight != expectedTextureSize) {
				throw new AssertionError(
					"Expected JEI's selected config-button texture to be " + expectedTextureSize + "x" + expectedTextureSize +
						", but it was " + resourceWidth + "x" + resourceHeight
				);
			}
			int spriteWidth = sprite.contents().width();
			int spriteHeight = sprite.contents().height();
			if (spriteWidth != resourceWidth || spriteHeight != resourceHeight) {
				throw new AssertionError(
					"Expected JEI's stitched config-button sprite to use the selected resource texture size " +
						resourceWidth + "x" + resourceHeight + ", but it was " + spriteWidth + "x" + spriteHeight
				);
			}
		} catch (IOException e) {
			throw new AssertionError("Failed to read JEI's selected config-button texture.", e);
		}
	}
}

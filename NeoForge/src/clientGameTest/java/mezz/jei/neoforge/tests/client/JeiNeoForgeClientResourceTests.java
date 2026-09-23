package mezz.jei.neoforge.tests.client;

import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.DarkModeResourcePack;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.platform.Services;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verifies that NeoForge development runs include JEI's shared client resources and refresh GUI sprites after resource reloads.
 */
public final class JeiNeoForgeClientResourceTests {
	private static final String JUNIT_SUITE_NAME = "neoforge-client-resources";
	private static final Duration RESOURCE_RELOAD_TIMEOUT = Duration.ofSeconds(30);
	private static final Identifier GUI_BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/gui_background.png");
	private static final List<Identifier> DARK_MODE_MONOCHROME_ICONS = List.of(
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/bookmarks_first.png"),
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/craftable_first.png"),
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/arrow_previous.png"),
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/arrow_next.png"),
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/recipe_bookmark.png"),
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/recipe_transfer.png"),
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/list_badge.png"),
		Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/tag_badge.png")
	);
	private static final String CLIENT_TEST_RESOURCE_PACK_ID = "file/jei-client-test-pack";
	private static final String JEI_MOD_RESOURCE_PACK_ID = "mod/jei";
	private static final Identifier CONFIG_BUTTON_TEXTURE = Identifier.fromNamespaceAndPath("jei", "textures/gui/sprites/icons/config_button.png");
	private static final Identifier CONFIG_BUTTON_SPRITE = Identifier.fromNamespaceAndPath("jei", "icons/config_button");
	private static final int CLIENT_TEST_TEXTURE_SIZE = 32;
	private static final int DEFAULT_TEXTURE_SIZE = 16;
	private static final int DARK_MODE_RECIPE_TEXT_COLOR = 0xFFD8DADC;
	private static final int DARK_MODE_MONOCHROME_ICON_COLOR = 0xFFADADAD;
	private static final String FOCUS_SEARCH_TRANSLATION_KEY = "key.jei.focusSearch";
	private static final AtomicInteger darkModeResourceReloadCount = new AtomicInteger();

	private JeiNeoForgeClientResourceTests() {

	}

	public static void run() {
		JUnitXmlTestReporter.runAndReport(
			JUNIT_SUITE_NAME,
			JeiNeoForgeClientResourceTests.class.getSimpleName(),
			() -> {
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::assertConfigScreenLoads);
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::assertStartupResourcePackResourcesAreLoaded);
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::disableClientTestResourcePack);
				ClientTestUtil.waitUntil(
					() -> ClientTestUtil.computeOnClient(JeiNeoForgeClientResourceTests::hasReloadedDefaultTestSprite),
					RESOURCE_RELOAD_TIMEOUT,
					() -> "Timed out waiting for JEI's default config-button sprite after disabling the client startup resource pack."
				);
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::registerDarkModeResourceReloadCounter);
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::enableDarkModeResourcePack);
				ClientTestUtil.waitUntil(
					() -> ClientTestUtil.computeOnClient(JeiNeoForgeClientResourceTests::hasReloadedDarkModeResources),
					RESOURCE_RELOAD_TIMEOUT,
					() -> "Timed out waiting for JEI's dark mode resources."
				);
				assertDarkModeResourceReloadCount(1, "enabling");
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::assertDarkModeVisualContrast);
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::disableDarkModeResourcePack);
				ClientTestUtil.waitUntil(
					() -> ClientTestUtil.computeOnClient(JeiNeoForgeClientResourceTests::hasReloadedDefaultResources),
					RESOURCE_RELOAD_TIMEOUT,
					() -> "Timed out waiting for JEI's default resources after disabling dark mode."
				);
				assertDarkModeResourceReloadCount(2, "disabling");
				ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::assertDefaultTestResourcesAreLoaded);
			}
		);
	}

	private static void assertConfigScreenLoads(Minecraft client) {
		Services.PLATFORM.getConfigHelper()
			.getConfigScreen(ModIds.JEI_ID, null)
			.orElseThrow(() -> new AssertionError("Expected MezzConfigGui to create JEI's config screen with the dark mode toggle."));
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
			.map(resource -> client.getAtlasManager()
				.getAtlasOrThrow(AtlasIds.GUI)
				.getSprite(CONFIG_BUTTON_SPRITE)
				.contents()
			)
			.filter(contents -> contents.width() == DEFAULT_TEXTURE_SIZE && contents.height() == DEFAULT_TEXTURE_SIZE)
			.isPresent();
	}

	private static void enableDarkModeResourcePack(Minecraft client) {
		DarkModeResourcePack.setEnabled(true);
		if (!client.getResourcePackRepository().getSelectedIds().contains(DarkModeResourcePack.NEOFORGE_PACK_ID)) {
			throw new AssertionError("Expected JEI's dark mode resource pack to be selected.");
		}
	}

	@SuppressWarnings("deprecation")
	private static void registerDarkModeResourceReloadCounter(Minecraft client) {
		if (!(client.getResourceManager() instanceof ReloadableResourceManager resourceManager)) {
			throw new AssertionError("Expected Minecraft's client resource manager to be reloadable.");
		}
		darkModeResourceReloadCount.set(0);
		resourceManager.registerReloadListener((ResourceManagerReloadListener) ignored -> darkModeResourceReloadCount.incrementAndGet());
	}

	private static void assertDarkModeResourceReloadCount(int expected, String action) {
		int actual = darkModeResourceReloadCount.get();
		if (actual != expected) {
			throw new AssertionError("Expected one resource reload when " + action + " dark mode, but found " + actual + ".");
		}
	}

	private static boolean hasReloadedDarkModeResources(Minecraft client) {
		return client.getResourceManager()
			.getResource(GUI_BACKGROUND_TEXTURE)
			.filter(resource -> resource.sourcePackId().equals(DarkModeResourcePack.NEOFORGE_PACK_ID))
			.isPresent() &&
			JeiGuiColors.getColor(JeiGuiColors.GuiColor.RECIPE_TEXT_WIDGET_TEXT) == DARK_MODE_RECIPE_TEXT_COLOR;
	}

	private static void assertDarkModeVisualContrast(Minecraft client) {
		Resource background = client.getResourceManager()
			.getResource(GUI_BACKGROUND_TEXTURE)
			.orElseThrow(() -> new AssertionError("Expected JEI's dark mode background texture."));
		assertOpaquePixelsAreNeutral(background);

		assertDarkModeIcons(client, DARK_MODE_MONOCHROME_ICONS, DARK_MODE_MONOCHROME_ICON_COLOR);
	}

	private static void assertDarkModeIcons(Minecraft client, List<Identifier> iconIds, int expectedColor) {
		for (Identifier iconId : iconIds) {
			Resource icon = client.getResourceManager()
				.getResource(iconId)
				.orElseThrow(() -> new AssertionError("Expected JEI's dark mode resource pack to provide " + iconId));
			if (!icon.sourcePackId().equals(DarkModeResourcePack.NEOFORGE_PACK_ID)) {
				throw new AssertionError("Expected " + iconId + " to come from JEI's dark mode resource pack, but it came from " + icon.sourcePackId());
			}
			assertContainsPixelColor(icon, iconId, expectedColor);
		}
	}

	private static void assertOpaquePixelsAreNeutral(Resource texture) {
		try (InputStream stream = texture.open();
			NativeImage image = NativeImage.read(stream)
		) {
			for (int pixel : image.getPixels()) {
				int red = pixel >> 16 & 0xFF;
				int green = pixel >> 8 & 0xFF;
				int blue = pixel & 0xFF;
				if (red != green || green != blue) {
					throw new AssertionError("Expected JEI's dark mode background to use a neutral charcoal palette.");
				}
			}
		} catch (IOException e) {
			throw new AssertionError("Failed to read JEI's dark mode background texture.", e);
		}
	}

	private static void assertContainsPixelColor(Resource texture, Identifier textureId, int expectedColor) {
		try (InputStream stream = texture.open();
			NativeImage image = NativeImage.read(stream)
		) {
			for (int pixel : image.getPixels()) {
				if (pixel == expectedColor) {
					return;
				}
			}
			throw new AssertionError("Expected " + textureId + " to contain readable light pixels.");
		} catch (IOException e) {
			throw new AssertionError("Failed to read " + textureId, e);
		}
	}

	private static boolean hasReloadedDefaultResources(Minecraft client) {
		return client.getResourceManager()
			.getResource(GUI_BACKGROUND_TEXTURE)
			.filter(resource -> resource.sourcePackId().equals(JEI_MOD_RESOURCE_PACK_ID))
			.isPresent() &&
			JeiGuiColors.getColor(JeiGuiColors.GuiColor.RECIPE_TEXT_WIDGET_TEXT) == JeiGuiColors.GuiColor.RECIPE_TEXT_WIDGET_TEXT.getDefaultColor();
	}

	private static void disableDarkModeResourcePack(Minecraft client) {
		DarkModeResourcePack.setEnabled(false);
		if (client.getResourcePackRepository().getSelectedIds().contains(DarkModeResourcePack.NEOFORGE_PACK_ID)) {
			throw new AssertionError("Expected JEI's dark mode resource pack to be deselected.");
		}
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
		TextureAtlasSprite sprite = client.getAtlasManager()
			.getAtlasOrThrow(AtlasIds.GUI)
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
			assertConfigButtonDrawableUsesSprite(client, sprite);
		} catch (IOException e) {
			throw new AssertionError("Failed to read JEI's selected config-button texture.", e);
		}
	}

	private static void assertConfigButtonDrawableUsesSprite(Minecraft client, TextureAtlasSprite expectedSprite) {
		GuiRenderState renderState = new GuiRenderState();
		GuiGraphicsExtractor guiGraphics = new GuiGraphicsExtractor(client, renderState, 0, 0);
		Internal.getTextures().getConfigButtonIcon().draw(guiGraphics, 0, 0);

		List<BlitRenderState> blits = new ArrayList<>();
		renderState.forEachElement(element -> {
			if (element instanceof BlitRenderState blit) {
				blits.add(blit);
			}
		}, GuiRenderState.TraverseRange.ALL);
		if (blits.size() != 1) {
			throw new AssertionError("Expected JEI's config-button drawable to submit one blit, but it submitted " + blits.size());
		}

		BlitRenderState blit = blits.getFirst();
		if (blit.u0() != expectedSprite.getU0() ||
			blit.u1() != expectedSprite.getU1() ||
			blit.v0() != expectedSprite.getV0() ||
			blit.v1() != expectedSprite.getV1()
		) {
			throw new AssertionError(
				"Expected JEI's config-button drawable to use the current sprite after the resource reload, but its texture coordinates were stale."
			);
		}
	}
}

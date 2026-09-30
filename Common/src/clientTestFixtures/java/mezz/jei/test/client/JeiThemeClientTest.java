package mezz.jei.test.client;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTheme;
import mezz.jei.common.gui.elements.TextWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Checks atlas lookups and submitted drawing commands across theme changes.
 */
public final class JeiThemeClientTest {
	private JeiThemeClientTest() {

	}

	@SuppressWarnings("deprecation")
	public static Runnable run(Minecraft client) {
		AtomicInteger reloads = new AtomicInteger();
		((ReloadableResourceManager) client.getResourceManager()).registerReloadListener(
			(ResourceManagerReloadListener) ignored -> reloads.incrementAndGet()
		);
		Runnable assertNoReload = () -> {
			if (reloads.get() != 0) {
				throw new AssertionError("Theme changes must not reload resources, found " + reloads.get());
			}
		};

		var repository = client.getResourcePackRepository();
		List<String> selectedPacks = List.copyOf(repository.getSelectedIds());
		List<String> savedPacks = List.copyOf(client.options.resourcePacks);
		boolean originalTheme = JeiTheme.isDarkModeEnabled();
		TextureAtlas atlas = client.getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
		TextureAtlasSprite lightSlot = getSprite(atlas, "slot");
		TextureAtlasSprite darkSlot = getSprite(atlas, "dark/slot");
		var textures = Internal.getTextures();
		var slot = textures.getSlot();
		var background = textures.getRecipeGuiBackground();
		TextWidget defaultText = new TextWidget(List.of(Component.literal("Theme")), 0, 0, 100, 20);
		TextWidget themedText = new TextWidget(List.of(Component.literal("Theme")), 0, 0, 100, 20);
		themedText.setColor(() -> JeiGuiColors.getColor(GuiColor.RECIPE_COOKING_TIME_TEXT));
		TextWidget customText = new TextWidget(List.of(Component.literal("Custom")), 0, 0, 100, 20);
		customText.setColor(0xFF123456);
		try {
			for (boolean dark : List.of(false, true, true, false, true, false)) {
				JeiTheme.setDarkModeEnabled(dark);
				String prefix = "";
				TextureAtlasSprite expectedSlot = lightSlot;
				int expectedTextColor = 0xFF000000;
				int expectedCookingColor = 0xFF808080;
				if (dark) {
					prefix = "dark/";
					expectedSlot = darkSlot;
					expectedTextColor = 0xFFD8DADC;
					expectedCookingColor = 0xFFBBC0C5;
				}
				assertSprite(client, slot, expectedSlot);
				assertBlits(client, graphics -> background.draw(graphics, 0, 0, 150, 100), getSprite(atlas, prefix + "gui_background"));
				// Icons with no dark variant must retain the light sprite.
				assertSprite(client, textures.getConfigButtonIcon(), getSprite(atlas, "icons/config_button"));
				assertTextColor(client, defaultText, expectedTextColor);
				assertTextColor(client, themedText, expectedCookingColor);
				assertTextColor(client, customText, 0xFF123456);
				if (getSprite(atlas, "slot") != lightSlot || getSprite(atlas, "dark/slot") != darkSlot) {
					throw new AssertionError("Theme changes must reuse the loaded atlas sprites.");
				}
				if (!selectedPacks.equals(List.copyOf(repository.getSelectedIds())) || !savedPacks.equals(client.options.resourcePacks)) {
					throw new AssertionError("Theme changes must not change Minecraft's resource pack selection.");
				}
			}
		} finally {
			JeiTheme.setDarkModeEnabled(originalTheme);
		}
		assertNoReload.run();
		return assertNoReload;
	}

	private static TextureAtlasSprite getSprite(TextureAtlas atlas, String path) {
		TextureAtlasSprite sprite = atlas.getSprite(Identifier.fromNamespaceAndPath("jei", path));
		if (sprite == atlas.missingSprite()) {
			throw new AssertionError("Expected preloaded JEI sprite: " + path);
		}
		return sprite;
	}

	private static void assertSprite(Minecraft client, IDrawable drawable, TextureAtlasSprite sprite) {
		assertBlits(client, graphics -> drawable.draw(graphics, 0, 0), sprite);
	}

	private static void assertBlits(Minecraft client, java.util.function.Consumer<GuiGraphicsExtractor> draw, TextureAtlasSprite sprite) {
		GuiRenderState state = new GuiRenderState();
		draw.accept(new GuiGraphicsExtractor(client, state, 0, 0));
		List<BlitRenderState> blits = new ArrayList<>();
		state.forEachElement(element -> {
			if (element instanceof BlitRenderState blit) {
				blits.add(blit);
			}
		}, GuiRenderState.TraverseRange.ALL);
		if (blits.isEmpty()) {
			throw new AssertionError("Expected the themed drawable to submit blits.");
		}
		for (BlitRenderState blit : blits) {
			if (blit.u0() < sprite.getU0() || blit.u1() > sprite.getU1() || blit.v0() < sprite.getV0() || blit.v1() > sprite.getV1()) {
				throw new AssertionError("Drawable used texture coordinates outside the selected theme sprite: " + sprite.contents().name());
			}
		}
	}

	private static void assertTextColor(Minecraft client, TextWidget widget, int expected) {
		GuiRenderState state = new GuiRenderState();
		widget.drawWidget(new GuiGraphicsExtractor(client, state, 0, 0), 0, 0);
		List<GuiTextRenderState> text = new ArrayList<>();
		state.forEachText(text::add);
		if (text.isEmpty()) {
			throw new AssertionError("Expected the existing text widget to submit text.");
		}
		try {
			Field color = GuiTextRenderState.class.getDeclaredField("color");
			color.setAccessible(true);
			for (GuiTextRenderState rendered : text) {
				if (color.getInt(rendered) != expected) {
					throw new AssertionError("Existing text widget did not use the current theme color.");
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to inspect submitted text color", e);
		}
	}
}

package mezz.jei.common.gui.textures;

import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JeiGuiSpriteManagerTest {
	private static final ResourceLocation GUI_BACKGROUND = ResourceLocation.fromNamespaceAndPath("jei", "gui_background_v2");

	@Test
	public void usesJeiMetadataWhenResourcePackMetadataIsMissing() {
		GuiSpriteScaling scaling = JeiGuiSpriteManager.getSpriteScaling(GUI_BACKGROUND, ResourceMetadata.EMPTY);

		GuiSpriteScaling.NineSlice.Border border = new GuiSpriteScaling.NineSlice.Border(16, 16, 16, 16);
		GuiSpriteScaling.NineSlice expected = new GuiSpriteScaling.NineSlice(64, 64, border);
		Assertions.assertEquals(expected, scaling);
	}

	@Test
	public void resourcePackMetadataOverridesJeiMetadata() {
		GuiSpriteScaling.Stretch expected = new GuiSpriteScaling.Stretch();
		ResourceMetadata metadata = new ResourceMetadata.Builder()
			.put(GuiMetadataSection.TYPE, new GuiMetadataSection(expected))
			.build();

		GuiSpriteScaling scaling = JeiGuiSpriteManager.getSpriteScaling(GUI_BACKGROUND, metadata);

		Assertions.assertEquals(expected, scaling);
	}
}

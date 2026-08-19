package mezz.jei.common.gui.textures;

import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class JeiAtlasManagerTest {
	private static final Identifier GUI_BACKGROUND = Identifier.fromNamespaceAndPath("jei", "gui_background");

	@Test
	public void usesJeiMetadataWhenResourcePackMetadataIsMissing() {
		GuiSpriteScaling scaling = JeiAtlasManager.getSpriteScaling(GUI_BACKGROUND, Optional.empty());

		GuiSpriteScaling.NineSlice.Border border = new GuiSpriteScaling.NineSlice.Border(16, 16, 16, 16);
		GuiSpriteScaling.NineSlice expected = new GuiSpriteScaling.NineSlice(64, 64, border, false);
		Assertions.assertEquals(expected, scaling);
	}

	@Test
	public void resourcePackMetadataOverridesJeiMetadata() {
		GuiSpriteScaling.Stretch expected = new GuiSpriteScaling.Stretch();
		GuiMetadataSection metadata = new GuiMetadataSection(expected);

		GuiSpriteScaling scaling = JeiAtlasManager.getSpriteScaling(GUI_BACKGROUND, Optional.of(metadata));

		Assertions.assertEquals(expected, scaling);
	}
}

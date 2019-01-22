package mezz.jei.gui.textures;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import mezz.jei.config.Constants;

@SideOnly(Side.CLIENT)
public class JeiTextureMap extends TextureMap {
	private final ResourceLocation location;

	public JeiTextureMap(String basePathIn) {
		super(basePathIn, null, true);
		this.location = new ResourceLocation(Constants.MOD_ID, basePathIn);
	}

	public ResourceLocation getLocation() {
		return location;
	}

	@Override
	public void loadTexture(IResourceManager resourceManager) {
		this.initMissingImage();
		this.deleteGlTexture();
		this.loadTextureAtlas(resourceManager);
	}
}

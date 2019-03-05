package mezz.jei.gui.textures;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.fml.client.ClientHooks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import mezz.jei.api.constants.ModIds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class JeiTextureMap extends TextureMap {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ResourceLocation location;
	private final Deque<ResourceLocation> loadingSprites = new ArrayDeque<>();
	private final Set<ResourceLocation> loadedSprites = new HashSet<>();

	public JeiTextureMap(String basePathIn) {
		super(basePathIn);
		this.location = new ResourceLocation(ModIds.JEI_ID, basePathIn);
	}

	public ResourceLocation getLocation() {
		return location;
	}

	public void registerSprite(ResourceLocation location) {
		Preconditions.checkNotNull(location);
		this.sprites.add(location);
	}

	@Override
	public void loadTexture(IResourceManager resourceManager) {
		this.deleteGlTexture();
		this.stitch(resourceManager);
	}

	@Override
	public void stitch(IResourceManager manager) {
		this.setMipmapLevels(0);
		int i = Minecraft.getGLMaximumTextureSize();
		Stitcher stitcher = new Stitcher(i, i, 0, 0);
		this.clear();
		int j = Integer.MAX_VALUE;
		int k = 1;

		LOGGER.info("Max texture size: {}", i);
		loadedSprites.clear();
		for (ResourceLocation resourcelocation : Sets.newHashSet(this.sprites)) {
			if (!this.missingImage.getName().equals(resourcelocation)) {
				j = loadTexture(stitcher, manager, resourcelocation, j, k);
			}
		}

		this.missingImage.generateMipmaps(0);
		stitcher.addSprite(this.missingImage);

		stitcher.doStitch();

		LOGGER.info("Created: {}x{} {}-atlas", stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), this.basePath);

		TextureUtil.allocateTextureImpl(this.getGlTextureId(), 0, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());

		for (TextureAtlasSprite textureatlassprite1 : stitcher.getStichSlots()) {
			this.mapUploadedSprites.put(textureatlassprite1.getName(), textureatlassprite1);

			try {
				textureatlassprite1.uploadMipmaps();
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Texture being stitched together");
				crashreportcategory.addDetail("Atlas path", this.basePath);
				crashreportcategory.addDetail("Sprite", textureatlassprite1);
				throw new ReportedException(crashreport);
			}

			if (textureatlassprite1.hasAnimationMetadata()) {
				this.listAnimatedSprites.add(textureatlassprite1);
			}
		}
	}

	private int loadTexture(Stitcher stitcher, IResourceManager manager, ResourceLocation resourcelocation, int j, int k) {
		if (loadedSprites.contains(resourcelocation)) {
			return j;
		}
		TextureAtlasSprite textureatlassprite;
		ResourceLocation resourcelocation1 = this.getSpritePath(resourcelocation);
		for (ResourceLocation loading : loadingSprites) {
			if (resourcelocation1.equals(loading)) {
				final String error = "circular model dependencies, stack: [" + com.google.common.base.Joiner.on(", ").join(loadingSprites) + "]";
				ClientHooks.trackBrokenTexture(resourcelocation, error);
			}
		}
		loadingSprites.addLast(resourcelocation1);
		try (IResource iresource = manager.getResource(resourcelocation1)) {
			PngSizeInfo pngsizeinfo = new PngSizeInfo(iresource);
			AnimationMetadataSection animationmetadatasection = iresource.getMetadata(AnimationMetadataSection.SERIALIZER);
			textureatlassprite = new TextureAtlasSprite(resourcelocation, pngsizeinfo, animationmetadatasection);

			for (ResourceLocation dependency : textureatlassprite.getDependencies()) {
				if (!sprites.contains(dependency)) {
					registerSprite(manager, dependency);
				}
				j = loadTexture(stitcher, manager, dependency, j, k);
			}
			if (textureatlassprite.hasCustomLoader(manager, resourcelocation)) {
				if (textureatlassprite.load(manager, resourcelocation, mapUploadedSprites::get)) {
					return j;
				}
			}
			j = Math.min(j, Math.min(textureatlassprite.getWidth(), textureatlassprite.getHeight()));
			int j1 = Math.min(Integer.lowestOneBit(textureatlassprite.getWidth()), Integer.lowestOneBit(textureatlassprite.getHeight()));
			if (j1 < k) {
				// FORGE: do not lower the mipmap level, just log the problematic textures
				LOGGER.warn("Texture {} with size {}x{} will have visual artifacts at mip level {}, it can only support level {}." +
						"Please report to the mod author that the texture should be some multiple of 16x16.",
					resourcelocation1, textureatlassprite.getWidth(), textureatlassprite.getHeight(), MathHelper.log2(k), MathHelper.log2(j1));
			}
			if (loadSprite(manager, textureatlassprite)) {
				stitcher.addSprite(textureatlassprite);
			}
			return j;
		} catch (RuntimeException runtimeexception) {
			ClientHooks.trackBrokenTexture(resourcelocation, runtimeexception.getMessage());
			return j;
		} catch (IOException ioexception) {
			ClientHooks.trackMissingTexture(resourcelocation);
			return j;
		} finally {
			loadingSprites.removeLast();
			sprites.add(resourcelocation1);
		}
	}

	private ResourceLocation getSpritePath(ResourceLocation location) {
		return new ResourceLocation(location.getNamespace(), String.format("%s/%s%s", this.basePath, location.getPath(), ".png"));
	}
}

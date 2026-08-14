package mezz.jei.common.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public final class TagRecipeUtil {
	private static final String RECIPE_TYPE_PATH_PREFIX = "tag_recipes/";

	private TagRecipeUtil() {

	}

	public static ResourceLocation getRecipeTypeUid(ResourceLocation registryId) {
		return new ResourceLocation(
			registryId.getNamespace(),
			RECIPE_TYPE_PATH_PREFIX + registryId.getPath()
		);
	}

	public static ResourceLocation getRecipeTypeUid(TagKey<?> tagKey) {
		return getRecipeTypeUid(tagKey.registry().location());
	}
}

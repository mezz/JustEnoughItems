package mezz.jei.common.recipes;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

public final class TagRecipeUtil {
	private static final String RECIPE_TYPE_PATH_PREFIX = "tag_recipes/";

	private TagRecipeUtil() {

	}

	public static Identifier getRecipeTypeUid(Identifier registryId) {
		return Identifier.fromNamespaceAndPath(
			registryId.getNamespace(),
			RECIPE_TYPE_PATH_PREFIX + registryId.getPath()
		);
	}

	public static Identifier getRecipeTypeUid(TagKey<?> tagKey) {
		return getRecipeTypeUid(tagKey.registry().identifier());
	}
}

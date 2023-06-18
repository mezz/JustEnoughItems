package mezz.jei.library.load.registration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IGlobalRecipeCategoryExtension;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class AdvancedRegistration implements IAdvancedRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IRecipeManagerPlugin> recipeManagerPlugins = new ArrayList<>();
	private final Multimap<IRecipeCategory<?>, IGlobalRecipeCategoryExtension<?>> recipeCategoryExtensions = HashMultimap.create();
	private final IJeiHelpers jeiHelpers;

	public AdvancedRegistration(IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin) {
		ErrorUtil.checkNotNull(recipeManagerPlugin, "recipeManagerPlugin");

		LOGGER.info("Added recipe manager plugin: {}", recipeManagerPlugin.getClass());
		recipeManagerPlugins.add(recipeManagerPlugin);
	}

	@Override
	public <T> void addGlobalRecipeCategoryExtension(IRecipeCategory<T> recipeCategory, IGlobalRecipeCategoryExtension<T> extension) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(extension, "extension");

		LOGGER.info("Added global recipe category extension: {} for recipe category: {}", extension.getClass(), recipeCategory.getRecipeType().getUid());
		recipeCategoryExtensions.put(recipeCategory, extension);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public <T> void addGlobalRecipeCategoryExtension(RecipeType<T> recipeType, IGlobalRecipeCategoryExtension<T> extension) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(extension, "extension");

		for (IRecipeCategory<?> recipeCategory : jeiHelpers.getRecipeCategories()) {
			var type = recipeCategory.getRecipeType();
			if (type.equals(recipeType)) {
				addGlobalRecipeCategoryExtension(recipeCategory, (IGlobalRecipeCategoryExtension) extension);
			}
		}
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Unmodifiable
	public List<IRecipeManagerPlugin> getRecipeManagerPlugins() {
		return List.copyOf(recipeManagerPlugins);
	}

	@Unmodifiable
	public Multimap<IRecipeCategory<?>, IGlobalRecipeCategoryExtension<?>> getRecipeCategoryExtensions() {
		return ImmutableMultimap.copyOf(recipeCategoryExtensions);
	}
}

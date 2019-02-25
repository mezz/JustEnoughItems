package mezz.jei.load.registration;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancedRegistration implements IAdvancedRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IRecipeManagerPlugin> RecipeManagerPlugins = new ArrayList<>();

	@Override
	public void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin) {
		ErrorUtil.checkNotNull(recipeManagerPlugin, "recipeManagerPlugin");

		LOGGER.info("Added recipe manager plugin: {}", recipeManagerPlugin.getClass());
		RecipeManagerPlugins.add(recipeManagerPlugin);
	}

	public ImmutableList<IRecipeManagerPlugin> getRecipeManagerPlugins() {
		return ImmutableList.copyOf(RecipeManagerPlugins);
	}
}

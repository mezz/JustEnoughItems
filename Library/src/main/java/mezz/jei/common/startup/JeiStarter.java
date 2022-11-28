package mezz.jei.common.startup;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.helpers.ModIdHelper;
import mezz.jei.common.ingredients.IngredientBlacklistInternal;
import mezz.jei.common.ingredients.IngredientVisibility;
import mezz.jei.common.load.PluginCaller;
import mezz.jei.common.load.PluginHelper;
import mezz.jei.common.load.PluginLoader;
import mezz.jei.common.plugins.jei.JeiInternalPlugin;
import mezz.jei.common.plugins.vanilla.VanillaPlugin;
import mezz.jei.common.recipes.RecipeManager;
import mezz.jei.common.recipes.RecipeTransferManager;
import mezz.jei.common.runtime.JeiHelpers;
import mezz.jei.common.runtime.JeiRuntime;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.LoggedTimer;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class JeiStarter {
	private static final Logger LOGGER = LogManager.getLogger();

	private final StartData data;

	public JeiStarter(StartData data) {
		ErrorUtil.checkNotEmpty(data.plugins(), "plugins");
		this.data = data;
	}

	public void start() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}

		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");
		List<IModPlugin> plugins = data.plugins();
		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins)
			.orElseThrow(() -> new IllegalStateException("vanilla plugin not found"));
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins)
			.orElse(null);
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);

		ConfigData configData = data.configData();
		IWorldConfig worldConfig = Internal.getWorldConfig();

		IModIdHelper modIdHelper = new ModIdHelper(configData.modIdFormatConfig());
		PluginLoader pluginLoader = new PluginLoader(data, modIdHelper);
		JeiHelpers jeiHelpers = pluginLoader.getJeiHelpers();

		IRegisteredIngredients registeredIngredients = pluginLoader.getRegisteredIngredients();
		IIngredientManager ingredientManager = pluginLoader.getIngredientManager();

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		ingredientManager.addIngredientListener(blacklist);

		IIngredientVisibility ingredientVisibility = new IngredientVisibility(
			blacklist,
			worldConfig,
			configData.editModeConfig(),
			registeredIngredients
		);

		RecipeManager recipeManager = pluginLoader.createRecipeManager(
			plugins,
			vanillaPlugin,
			configData.recipeCategorySortingConfig(),
			modIdHelper,
			ingredientVisibility
		);
		ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers =
			pluginLoader.createRecipeTransferHandlers(plugins);
		IRecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferHandlers);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		IScreenHelper screenHelper = pluginLoader.createGuiScreenHelper(plugins);

		JeiRuntime jeiRuntime = new JeiRuntime(
			recipeManager,
			registeredIngredients,
			ingredientManager,
			ingredientVisibility,
			data.keyBindings(),
			jeiHelpers,
			screenHelper,
			recipeTransferManager
		);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jeiRuntime));

		totalTime.stop();
	}

	public void stop() {
		LOGGER.info("Stopping JEI");
		List<IModPlugin> plugins = data.plugins();
		PluginCaller.callOnPlugins("Sending Runtime Unavailable", plugins, IModPlugin::onRuntimeUnavailable);
		Internal.setRuntime(null);
	}
}

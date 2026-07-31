package mezz.jei.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.gui.IngredientTooltipComponent;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.forge.chat.JeiChatEventHandler;
import mezz.jei.forge.chat.JeiChatTooltipEventHandler;
import mezz.jei.forge.chat.JeiInternalShowCommand;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.network.ConnectionToServer;
import mezz.jei.forge.network.NetworkHandler;
import mezz.jei.forge.plugins.forge.ForgeGuiPlugin;
import mezz.jei.forge.startup.ForgePluginFinder;
import mezz.jei.forge.startup.StartEventObserver;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.gui.overlay.bookmarks.IngredientsTooltipComponent;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.GameShuttingDownEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class JustEnoughItemsClient {
	private final PermanentEventSubscriptions subscriptions;
	private final JeiStarter jeiStarter;

	public JustEnoughItemsClient(
		NetworkHandler networkHandler,
		PermanentEventSubscriptions subscriptions,
		IServerConfig serverConfig
	) {
		this.subscriptions = subscriptions;

		ConnectionToServer serverConnection = new ConnectionToServer(networkHandler);
		Internal.setServerConnection(serverConnection);

		InternalKeyMappings keyMappings = createKeyMappings(subscriptions);
		Internal.setKeyMappings(keyMappings);

		ClientPacketRouter packetRouter = new ClientPacketRouter(serverConnection, serverConfig);
		networkHandler.registerClientPacketHandler(packetRouter);

		List<IModPlugin> plugins = ForgePluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			serverConnection,
			keyMappings
		);

		this.jeiStarter = new JeiStarter(startData);

		StartEventObserver startEventObserver = new StartEventObserver(serverConnection, this.jeiStarter::start, this.jeiStarter::stop);
		startEventObserver.register(subscriptions);
	}

	public void register() {
		JeiChatEventHandler.register(subscriptions);
		JeiChatTooltipEventHandler.register(subscriptions);
		JeiInternalShowCommand.register(subscriptions);
		subscriptions.register(RegisterClientReloadListenersEvent.class, this::onRegisterReloadListenerEvent);
		subscriptions.register(RegisterClientTooltipComponentFactoriesEvent.class, this::onRegisterClientTooltipEvent);
		subscriptions.register(RecipesUpdatedEvent.class, this::onRecipesUpdatedEvent);
		subscriptions.register(GameShuttingDownEvent.class, e -> onGameShuttingDown());
	}

	private void onGameShuttingDown() {
		jeiStarter.stop();
		Internal.onClientStopping();
	}

	private void onRecipesUpdatedEvent(RecipesUpdatedEvent event) {
		List<Recipe<?>> recipes = List.copyOf(event.getRecipeManager().getRecipes());
		if (!recipes.isEmpty()) {
			Internal.setClientSyncedRecipes(recipes);
		}
	}

	private void onRegisterReloadListenerEvent(RegisterClientReloadListenersEvent event) {
		Textures textures = Internal.getTextures();
		event.registerReloadListener(textures.getSpriteUploader());
		event.registerReloadListener(createReloadListener());
	}

	private void onRegisterClientTooltipEvent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(IngredientTooltipComponent.class, Function.identity());
		event.register(IngredientsTooltipComponent.class, Function.identity());
		event.register(PreviewTooltipComponent.class, Function.identity());
		event.register(TagContentTooltipComponent.class, Function.identity());
	}

	private ResourceManagerReloadListener createReloadListener() {
		return (ResourceManager resourceManager) -> {
			ForgeGuiPlugin.getResourceReloadHandler()
				.ifPresent(r -> r.onResourceManagerReload(resourceManager));
		};
	}

	private static InternalKeyMappings createKeyMappings(PermanentEventSubscriptions subscriptions) {
		Set<KeyMapping> keysToRegister = new HashSet<>();
		subscriptions.register(RegisterKeyMappingsEvent.class, e -> keysToRegister.forEach(e::register));
		return new InternalKeyMappings(keysToRegister::add);
	}
}

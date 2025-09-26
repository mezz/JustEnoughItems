package mezz.jei.forge;

import com.google.gson.JsonObject;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.IngredientTooltipComponent;
import mezz.jei.common.gui.IngredientsTooltipComponent;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.forge.chat.JeiChatEventHandler;
import mezz.jei.forge.chat.JeiChatTooltipEventHandler;
import mezz.jei.forge.chat.JeiInternalShowCommand;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.network.ConnectionToServer;
import mezz.jei.forge.network.NetworkHandler;
import mezz.jei.forge.startup.ForgePluginFinder;
import mezz.jei.forge.startup.StartEventObserver;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
import mezz.jei.library.plugins.vanilla.crafting.JeiShapedRecipe;
import mezz.jei.library.recipes.RecipeSerializers;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class JustEnoughItemsClient {
	private final NetworkHandler networkHandler;
	private final PermanentEventSubscriptions subscriptions;
	private final IServerConfig serverConfig;
	@Nullable
	private JeiStarter jeiStarter;

	public JustEnoughItemsClient(NetworkHandler networkHandler, PermanentEventSubscriptions subscriptions, IServerConfig serverConfig) {
		this.networkHandler = networkHandler;
		this.subscriptions = subscriptions;
		this.serverConfig = serverConfig;
	}

	public void register() {
		JeiChatEventHandler.register(subscriptions);
		JeiChatTooltipEventHandler.register(subscriptions);
		JeiInternalShowCommand.register(subscriptions);
		registerTooltipComponents();
		subscriptions.register(RegisterClientReloadListenersEvent.class, this::onRegisterReloadListenerEvent);
		subscriptions.register(RecipesUpdatedEvent.class, this::onRecipesUpdatedEvent);
		Runtime.getRuntime().addShutdownHook(new Thread(this::onGameShuttingDown, "JEI Client Shutdown"));

		IEventBus modEventBus = subscriptions.getModEventBus();
		DeferredRegister<RecipeSerializer<?>> deferredRegister = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ModIds.JEI_ID);
		deferredRegister.register(modEventBus);

		Supplier<RecipeSerializer<?>> jeiShaped = deferredRegister.register(
			"jei_shaped",
			() -> new ForgeRecipeSerializer<>(new JeiShapedRecipe.Serializer())
		);
		RecipeSerializers.register(jeiShaped);
	}

	private static class ForgeRecipeSerializer<T extends Recipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
		private final RecipeSerializer<T> delegate;

		private ForgeRecipeSerializer(RecipeSerializer<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T fromJson(ResourceLocation recipeId, JsonObject json) {
			return delegate.fromJson(recipeId, json);
		}

		@Override
		public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return delegate.fromNetwork(recipeId, buffer);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, T recipe) {
			delegate.toNetwork(buffer, recipe);
		}
	}

	private void onGameShuttingDown() {
		if (jeiStarter != null) {
			jeiStarter.stop();
		}
		Internal.onClientStopping();
	}

	private void onRecipesUpdatedEvent(RecipesUpdatedEvent event) {
		List<Recipe<?>> recipes = List.copyOf(event.getRecipeManager().getRecipes());
		if (!recipes.isEmpty()) {
			Internal.setClientSyncedRecipes(recipes);
		}
	}

	private void onRegisterReloadListenerEvent(RegisterClientReloadListenersEvent event) {
		Textures textures = createTextures(event);
		Internal.setTextures(textures);

		ConnectionToServer serverConnection = new ConnectionToServer(networkHandler);
		Internal.setServerConnection(serverConnection);

		IWorldConfig worldConfig = Internal.getWorldConfig();

		ClientPacketRouter packetRouter = new ClientPacketRouter(serverConnection, serverConfig, worldConfig);
		networkHandler.registerClientPacketHandler(packetRouter);

		InternalKeyMappings keyMappings = createKeyMappings(subscriptions);
		Internal.setKeyMappings(keyMappings);

		List<IModPlugin> plugins = ForgePluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			textures,
			serverConnection,
			keyMappings
		);

		JeiStarter jeiStarter = new JeiStarter(startData);
		this.jeiStarter = jeiStarter;
		StartEventObserver startEventObserver = new StartEventObserver(serverConnection, jeiStarter::start, jeiStarter::stop);
		startEventObserver.register(subscriptions);
		event.registerReloadListener(startEventObserver);
	}

	private static Textures createTextures(RegisterClientReloadListenersEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		TextureManager textureManager = minecraft.textureManager;
		JeiSpriteUploader spriteUploader = new JeiSpriteUploader(textureManager);
		event.registerReloadListener(spriteUploader);
		return new Textures(spriteUploader);
	}

	private static void registerTooltipComponents() {
		MinecraftForgeClient.registerTooltipComponentFactory(IngredientTooltipComponent.class, Function.identity());
		MinecraftForgeClient.registerTooltipComponentFactory(IngredientsTooltipComponent.class, Function.identity());
		MinecraftForgeClient.registerTooltipComponentFactory(PreviewTooltipComponent.class, Function.identity());
		MinecraftForgeClient.registerTooltipComponentFactory(TagContentTooltipComponent.class, Function.identity());
	}

	private static InternalKeyMappings createKeyMappings(PermanentEventSubscriptions subscriptions) {
		return new InternalKeyMappings(ClientRegistry::registerKeyBinding);
	}
}

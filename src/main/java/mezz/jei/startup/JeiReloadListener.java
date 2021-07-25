package mezz.jei.startup;

import mezz.jei.Internal;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.lang.ref.WeakReference;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

public final class JeiReloadListener implements ResourceManagerReloadListener {
	private WeakReference<ClientLifecycleHandler> handler = new WeakReference<>(null);

	private JeiReloadListener() {
	}

	void update(ClientLifecycleHandler handler) {
		this.handler = new WeakReference<>(handler);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		ClientLifecycleHandler handlerRef = handler.get();
		// Only restart JEI on a reload after initial setup
		if (handlerRef != null && handlerRef.starter.hasStarted()) {
			handlerRef.startJEI();
		}
	}

	public static void initializeJeiReloadListener(RegisterClientReloadListenersEvent event) {
		JeiReloadListener reloadListener = new JeiReloadListener();
		Internal.setReloadListener(reloadListener);
		event.registerReloadListener(reloadListener);
	}
}

package mezz.jei.startup;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.lang.ref.WeakReference;

public final class JeiReloadListener implements ResourceManagerReloadListener {
	private WeakReference<ClientLifecycleHandler> handler;

	JeiReloadListener(ClientLifecycleHandler handler) {
		this.handler = new WeakReference<>(handler);
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
}

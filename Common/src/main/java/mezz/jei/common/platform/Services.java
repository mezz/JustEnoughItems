package mezz.jei.common.platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;

public class Services {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> serviceClass) {
        T loadedService = ServiceLoader.load(serviceClass)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + serviceClass.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, serviceClass);
        return loadedService;
    }
}

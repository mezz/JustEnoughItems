package mezz.jei.library.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.library.config.JeiConfigData;

import java.util.List;

public record StartData(
	List<IModPlugin> plugins,
	IConnectionToServer serverConnection,
	JeiConfigData configData
) {
}

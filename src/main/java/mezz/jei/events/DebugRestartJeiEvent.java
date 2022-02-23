package mezz.jei.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugRestartJeiEvent extends JeiEvent {
	private static final Logger LOGGER = LogManager.getLogger();

	@SuppressWarnings("unused") // needed for event bus
	public DebugRestartJeiEvent() {

	}
}

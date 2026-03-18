package mezz.jei.api;

/**
 * Marker interface for JEI plugins that are safe to execute asynchronously.
 * <p>
 * Implementing this interface indicates that your plugin's registration methods
 * are thread-safe and do not access non-thread-safe Minecraft objects such as:
 * </p>
 * <ul>
 *     <li>{@code Minecraft.getInstance()} or any of its fields</li>
 *     <li>Level/world objects and block states</li>
 *     <li>Player entities</li>
 *     <li>GUI/rendering components</li>
 *     <li>Any other non-thread-safe Minecraft objects</li>
 * </ul>
 * <p>
 * Plugins that implement this interface may have their registration methods
 * executed on background threads for improved performance. Plugins that do not
 * implement this interface will always be executed on the main thread.
 * </p>
 * <p>
 * <strong>Safe operations for async execution:</strong>
 * </p>
 * <ul>
 *     <li>Creating ingredient objects from static data</li>
 *     <li>Reading from static final collections</li>
 *     <li>Pure computation and data transformation</li>
 *     <li>Creating recipe objects from pre-defined data</li>
 * </ul>
 * <p>
 * <strong>Unsafe operations that require main thread:</strong>
 * </p>
 * <ul>
 *     <li>Accessing the world/level state</li>
 *     <li>Querying block states or tile entities</li>
 *     <li>Accessing player data</li>
 *     <li>Creating or modifying GUI elements</li>
 *     <li>Rendering operations</li>
 * </ul>
 *
 * @since 1.20.1-async
 */
public interface IAsyncCompatiblePlugin {
	/**
	 * @return true if this plugin can be executed on a background thread.
	 *         Return false if your plugin needs main thread access for any reason.
	 */
	default boolean canExecuteAsync() {
		return true;
	}
}

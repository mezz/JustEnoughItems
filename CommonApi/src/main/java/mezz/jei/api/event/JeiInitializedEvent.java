package mezz.jei.api.event;

/**
 * Event fired when JEI has completed initialization and is ready for use.
 * <p>
 * Mods that depend on JEI being fully initialized should listen for this event
 * instead of making assumptions about initialization timing. This is especially
 * important when async loading is enabled, as initialization timing may vary.
 * </p>
 * <p>
 * This event is fired on the main thread after all JEI registration is complete
 * and the {@link mezz.jei.api.runtime.IJeiRuntime} is available.
 * </p>
 * <p>
 * <strong>Forge Usage:</strong>
 * </p>
 * <pre>{@code
 * @SubscribeEvent
 * public void onJeiInitialized(JeiInitializedEvent event) {
 *     // JEI is now fully initialized and ready to use
 *     // Safe to query JEI data, modify recipes, etc.
 * }
 * }</pre>
 * <p>
 * <strong>Fabric Usage:</strong>
 * </p>
 * <pre>{@code
 * JeiLifecycleEvents.INITIALIZED.register(() -> {
 *     // JEI is now fully initialized
 * });
 * }</pre>
 *
 * @since 1.20.1-async
 */
public class JeiInitializedEvent {
	// Marker event - no data needed
}

package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class GuiPropertiesCacheTest {
	@Test
	public void newCacheStartsWithoutScreenSpecificState() {
		// Setup: no screen updates have been applied.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();

		// Operation: read the cached state.
		boolean hasValidScreen = guiPropertiesCache.hasValidScreen();
		Set<ImmutableRect2i> guiExclusionAreas = guiPropertiesCache.getGuiExclusionAreas();
		ImmutablePoint2i mouseExclusionArea = guiPropertiesCache.getMouseExclusionArea();

		// Assertions: screen-dependent state is empty until an updater supplies it.
		assertFalse(hasValidScreen);
		assertNull(guiPropertiesCache.getGuiProperties());
		assertEquals(Set.of(), guiExclusionAreas);
		assertNull(mouseExclusionArea);
	}

	@Test
	public void validGuiPropertiesRefreshAndPopulateCache() {
		// Setup: the cache starts without valid screen properties.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		IGuiProperties guiProperties = guiProperties();
		AtomicInteger updates = new AtomicInteger();

		// Operation: apply valid GUI properties.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(guiProperties)
			.update();

		// Assertions: the update callback runs and the valid GUI properties are cached.
		assertEquals(1, updates.get());
		assertSame(guiProperties, guiPropertiesCache.getGuiProperties());
	}

	@Test
	public void unchangedGuiPropertiesDoNotRefresh() {
		// Setup: the cache already has valid screen properties.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		IGuiProperties guiProperties = guiProperties();
		AtomicInteger updates = new AtomicInteger();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(guiProperties)
			.update();

		// Operation: apply equivalent GUI properties again.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(guiProperties())
			.update();

		// Assertions: unchanged geometry is ignored.
		assertEquals(1, updates.get());
		assertSame(guiProperties, guiPropertiesCache.getGuiProperties());
	}

	@Test
	public void changedGuiPropertiesRefreshAndReplaceCache() {
		// Setup: the cache already has valid screen properties.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		IGuiProperties oldGuiProperties = guiProperties();
		IGuiProperties newGuiProperties = new TestGuiProperties(60, 20, 100, 50, 200, 100);
		AtomicInteger updates = new AtomicInteger();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(oldGuiProperties)
			.update();

		// Operation: apply changed GUI properties.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(newGuiProperties)
			.update();

		// Assertions: changed geometry refreshes layout and replaces the cached properties.
		assertEquals(2, updates.get());
		assertSame(newGuiProperties, guiPropertiesCache.getGuiProperties());
	}

	@Test
	public void invalidGuiPropertiesDoNotBecomeValidScreen() {
		// Setup: the cache starts without valid screen properties.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		IGuiProperties invalidGuiProperties = new TestGuiProperties(50, 20, 0, 50, 200, 100);
		AtomicInteger updates = new AtomicInteger();

		// Operation: apply invalid GUI properties.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(invalidGuiProperties)
			.update();

		// Assertions: invalid properties are cached internally for change detection but not exposed as a valid screen.
		assertEquals(0, updates.get());
		assertFalse(guiPropertiesCache.hasValidScreen());
		assertNull(guiPropertiesCache.getGuiProperties());
	}

	@Test
	public void invalidGuiPropertiesRefreshWhenTheyInvalidateAValidScreen() {
		// Setup: the cache already has valid screen properties.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		IGuiProperties invalidGuiProperties = new TestGuiProperties(50, 20, 0, 50, 200, 100);
		TestChangeListener updates = new TestChangeListener();
		guiPropertiesCache.createUpdater(updates)
			.updateGuiProperties(guiProperties())
			.update();

		// Operation: apply invalid GUI properties after a valid screen.
		guiPropertiesCache.createUpdater(updates)
			.updateGuiProperties(invalidGuiProperties)
			.update();

		// Assertions: invalidating an existing screen notifies callers so they can clear screen-dependent state.
		assertEquals(2, updates.count);
		assertFalse(guiPropertiesCache.hasValidScreen());
		assertNull(guiPropertiesCache.getGuiProperties());
		assertEquals(Set.of(), guiPropertiesCache.getGuiExclusionAreas());
	}

	@Test
	public void nullGuiPropertiesRefreshWhenTheyClearAValidScreen() {
		// Setup: the cache already has valid screen properties.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		AtomicInteger updates = new AtomicInteger();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(guiProperties())
			.update();

		// Operation: clear the GUI properties.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(null)
			.update();

		// Assertions: clearing a valid screen refreshes so callers can close overlay state.
		assertEquals(2, updates.get());
		assertFalse(guiPropertiesCache.hasValidScreen());
		assertNull(guiPropertiesCache.getGuiProperties());
	}

	@Test
	public void exclusionAreaChangesRefreshAndPopulateCache() {
		// Setup: the cache has one set of exclusion areas.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		Set<ImmutableRect2i> exclusionAreas = Set.of(new ImmutableRect2i(1, 2, 3, 4));
		AtomicInteger updates = new AtomicInteger();

		// Operation: apply changed exclusion areas.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateExclusionAreas(exclusionAreas)
			.update();

		// Assertions: changed exclusion areas refresh layout and are cached.
		assertEquals(1, updates.get());
		assertEquals(exclusionAreas, guiPropertiesCache.getGuiExclusionAreas());
	}

	@Test
	public void unchangedExclusionAreasDoNotRefresh() {
		// Setup: the cache already has exclusion areas.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		Set<ImmutableRect2i> exclusionAreas = Set.of(new ImmutableRect2i(1, 2, 3, 4));
		AtomicInteger updates = new AtomicInteger();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateExclusionAreas(exclusionAreas)
			.update();

		// Operation: apply the same exclusion areas again.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateExclusionAreas(exclusionAreas)
			.update();

		// Assertions: unchanged exclusion areas are ignored.
		assertEquals(1, updates.get());
		assertEquals(exclusionAreas, guiPropertiesCache.getGuiExclusionAreas());
	}

	@Test
	public void mouseExclusionAreaChangesRefreshAndPopulateCache() {
		// Setup: the cache starts without a mouse exclusion point.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		ImmutablePoint2i mouseExclusionArea = new ImmutablePoint2i(10, 20);
		AtomicInteger updates = new AtomicInteger();

		// Operation: apply a mouse exclusion point.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateMouseExclusionArea(mouseExclusionArea)
			.update();

		// Assertions: changed mouse exclusion refreshes layout and is cached.
		assertEquals(1, updates.get());
		assertEquals(mouseExclusionArea, guiPropertiesCache.getMouseExclusionArea());
	}

	@Test
	public void unchangedMouseExclusionAreaDoesNotRefresh() {
		// Setup: the cache already has a mouse exclusion point.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		ImmutablePoint2i mouseExclusionArea = new ImmutablePoint2i(10, 20);
		AtomicInteger updates = new AtomicInteger();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateMouseExclusionArea(mouseExclusionArea)
			.update();

		// Operation: apply an equivalent mouse exclusion point again.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateMouseExclusionArea(new ImmutablePoint2i(10, 20))
			.update();

		// Assertions: unchanged mouse exclusion is ignored.
		assertEquals(1, updates.get());
		assertEquals(mouseExclusionArea, guiPropertiesCache.getMouseExclusionArea());
	}

	@Test
	public void nullMouseExclusionAreaRefreshesWhenItClearsCachedPoint() {
		// Setup: the cache already has a mouse exclusion point.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		AtomicInteger updates = new AtomicInteger();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateMouseExclusionArea(new ImmutablePoint2i(10, 20))
			.update();

		// Operation: clear the mouse exclusion point.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateMouseExclusionArea(null)
			.update();

		// Assertions: clearing the cached point refreshes layout.
		assertEquals(2, updates.get());
		assertNull(guiPropertiesCache.getMouseExclusionArea());
	}

	@Test
	public void batchedChangesRefreshOnlyOnce() {
		// Setup: multiple cached properties will change in one updater batch.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		IGuiProperties guiProperties = guiProperties();
		Set<ImmutableRect2i> exclusionAreas = Set.of(new ImmutableRect2i(1, 2, 3, 4));
		ImmutablePoint2i mouseExclusionArea = new ImmutablePoint2i(10, 20);
		TestChangeListener updates = new TestChangeListener();

		// Operation: apply all changes before ending the batch.
		guiPropertiesCache.createUpdater(updates)
			.updateGuiProperties(guiProperties)
			.updateExclusionAreas(exclusionAreas)
			.updateMouseExclusionArea(mouseExclusionArea)
			.update();

		// Assertions: the callback runs once after the batch, and the updated values are cached.
		assertEquals(1, updates.count);
		assertSame(guiProperties, guiPropertiesCache.getGuiProperties());
		assertEquals(exclusionAreas, guiPropertiesCache.getGuiExclusionAreas());
		assertEquals(mouseExclusionArea, guiPropertiesCache.getMouseExclusionArea());
	}

	@Test
	public void forceUpdateRefreshesUnchangedScreenProperties() {
		// Setup: the cache already has valid screen properties, and the next screen update is geometrically unchanged.
		GuiPropertiesCache<Object> guiPropertiesCache = guiPropertiesCache();
		IGuiProperties guiProperties = new TestGuiProperties(50, 20, 100, 50, 200, 100);
		AtomicInteger updates = new AtomicInteger();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(guiProperties)
			.update();

		// Operation: first apply the same screen normally, then force a refresh for changed overlay contents.
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(guiProperties)
			.update();
		int updatesAfterUnchangedScreen = updates.get();
		guiPropertiesCache.createUpdater(countUpdates(updates))
			.updateGuiProperties(guiProperties)
			.forceUpdate();

		// Assertions: unchanged geometry is still ignored by default, but content changes can refresh layout.
		assertEquals(1, updatesAfterUnchangedScreen);
		assertEquals(2, updates.get());
	}

	private static IGuiProperties guiProperties() {
		return new TestGuiProperties(50, 20, 100, 50, 200, 100);
	}

	private static GuiPropertiesCache<Object> guiPropertiesCache() {
		return new GuiPropertiesCache<>(ignored -> null);
	}

	private static Runnable countUpdates(AtomicInteger updates) {
		return updates::incrementAndGet;
	}

	private static class TestChangeListener implements Runnable {
		int count = 0;

		@Override
		public void run() {
			this.count++;
		}
	}
}

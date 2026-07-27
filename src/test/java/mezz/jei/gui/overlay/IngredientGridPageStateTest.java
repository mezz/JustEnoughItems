package mezz.jei.gui.overlay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import org.junit.Assert;
import org.junit.Test;

public class IngredientGridPageStateTest {
	private static final int MISSING_ANCHOR = -1;

	@Test
	public void validAnchorIndexSelectsContainingPage() {
		// Setup: a remembered ingredient is still present but now belongs to an earlier page than the stale page start.
		int anchorOnSecondPage = 17;
		int itemCount = 23;
		int pageSize = 10;

		// Operation: use the anchor index as the requested item to render after the list changes.
		int firstItemIndex = IngredientGridPageState.getFirstItemIndexForValidPage(anchorOnSecondPage, itemCount, pageSize);

		// Assertions: the grid starts on the page containing the anchor.
		Assert.assertEquals(10, firstItemIndex);
	}

	@Test
	public void missingAnchorResetsToFirstPageWhenCurrentPageStillExists() {
		// Setup: the remembered ingredient was removed, but enough ingredients remain for the old page to render.
		int itemCount = 23;
		int pageSize = 10;

		// Operation: a missing anchor is represented by -1, so it has no containing page to preserve.
		int firstItemIndex = IngredientGridPageState.getFirstItemIndexForValidPage(MISSING_ANCHOR, itemCount, pageSize);

		// Assertions: missing the anchor resets to the first page instead of preserving an arbitrary old page.
		Assert.assertEquals(0, firstItemIndex);
	}

	@Test
	public void anchorIndexResetsToZeroWhenGridHasNoSlots() {
		// Setup: exclusions or a small screen leave no room for overlay slots.
		int anchorIndex = 17;
		int itemCount = 23;
		int noSlots = 0;

		// Operation: calculate the page start for a grid with no room.
		int firstItemIndex = IngredientGridPageState.getFirstItemIndexForValidPage(anchorIndex, itemCount, noSlots);

		// Assertions: without slot capacity, every anchor falls back to the neutral page start.
		Assert.assertEquals(0, firstItemIndex);
	}

	@Test
	public void firstItemIndexClampsToLastPage() {
		// Setup: page navigation previously moved forward, then filtering reduced the total ingredient count.
		int firstItemIndexPastEnd = 40;
		int itemCount = 23;
		int pageSize = 10;

		// Operation: normalize the stored first item before rendering.
		int firstItemIndex = IngredientGridPageState.getFirstItemIndexForValidPage(firstItemIndexPastEnd, itemCount, pageSize);

		// Assertions: the closest valid page is the final partial page.
		Assert.assertEquals(20, firstItemIndex);
	}

	@Test
	public void requestedPageStartResetsToZeroWhenGridHasNoSlots() {
		// Setup: ingredients exist, but the current layout cannot render any slots.
		int currentPageStart = 30;
		int itemCount = 23;
		int noSlots = 0;

		// Operation: normalize the page start for an unrenderable grid.
		int firstItemIndex = IngredientGridPageState.getFirstItemIndexForValidPage(currentPageStart, itemCount, noSlots);

		// Assertions: no slot capacity means there is no meaningful non-zero page start.
		Assert.assertEquals(0, firstItemIndex);
	}

	@Test
	public void firstItemIndexIsZeroForEmptyList() {
		// Setup: the old page start is non-zero, but filtering removed every ingredient.
		int currentPageStart = 30;
		int noItems = 0;
		int pageSize = 10;

		// Operation: normalize the page start for an empty list.
		int firstItemIndex = IngredientGridPageState.getFirstItemIndexForValidPage(currentPageStart, noItems, pageSize);

		// Assertions: an empty ingredient list always renders from index zero.
		Assert.assertEquals(0, firstItemIndex);
	}

	@Test
	public void pageNumberUsesClampedFirstItemIndex() {
		// Setup: the navigation label is refreshed after the list shrank.
		int firstItemIndexPastEnd = 40;
		int pageSize = 10;
		int itemCount = 23;

		// Operation: calculate the displayed page number from the stale first item.
		int pageNumber = IngredientGridPageState.getPageNumberForFirstItemIndex(firstItemIndexPastEnd, pageSize, itemCount);

		// Assertions: page number reporting uses the clamped render page.
		Assert.assertEquals(2, pageNumber);
	}

	@Test
	public void pageNumberIsZeroForEmptyGrid() {
		// Setup: the overlay is active but has no available slots.
		int currentPageStart = 30;
		int noSlots = 0;
		int itemCount = 23;

		// Operation: calculate the displayed page number for an unrenderable grid.
		int pageNumber = IngredientGridPageState.getPageNumberForFirstItemIndex(currentPageStart, noSlots, itemCount);

		// Assertions: page zero is reported as the stable fallback.
		Assert.assertEquals(0, pageNumber);
	}

	@Test
	public void pageCountIsOneForEmptyGrid() {
		// Setup: ingredients exist, but the current screen layout leaves the grid with no capacity.
		int itemCount = 23;
		int noSlots = 0;

		// Operation: calculate the page count for a grid with no capacity.
		int pageCount = IngredientGridPageState.getPageCount(itemCount, noSlots);

		// Assertions: navigation still exposes a single logical page instead of zero pages.
		Assert.assertEquals(1, pageCount);
	}

	@Test
	public void pageCountIsAtLeastOne() {
		// Setup: filtering or ingredient reload removed every ingredient from a normally renderable grid.
		int noItems = 0;
		int pageSize = 10;

		// Operation: calculate the page count for an empty ingredient list.
		int pageCount = IngredientGridPageState.getPageCount(noItems, pageSize);

		// Assertions: the navigation model keeps one page as its minimum.
		Assert.assertEquals(1, pageCount);
	}

	@Test
	public void explicitPageNavigationClearsTheClickedPageAnchor() {
		// Setup: the user clicked an ingredient, then manually paged away before the list changed again.
		IngredientGridPageState pageState = new IngredientGridPageState();
		IIngredientListElement clickedAnchor = element(new Object());
		List<IIngredientListElement> elements = Collections.singletonList(clickedAnchor);
		pageState.setPageAnchorElement(clickedAnchor);

		// Operation: explicit page navigation renders a page without using the clicked ingredient as an anchor.
		pageState.updateForPageNavigation(1, elements.size(), 1);
		IIngredientListElement pageAnchor = pageState.getPageAnchorElement(elements);

		// Assertions: the next relayout does not expose the stale clicked anchor from the previous lookup.
		Assert.assertNull(pageAnchor);
	}

	@Test
	public void getPageAnchorElementReturnsRememberedAnchorWhenStillPresent() {
		// Setup: the source list still contains the exact clicked ingredient element.
		IngredientGridPageState pageState = new IngredientGridPageState();
		IIngredientListElement clickedAnchor = element(new Object());
		List<IIngredientListElement> elements = Arrays.asList(element(new Object()), clickedAnchor);
		pageState.setPageAnchorElement(clickedAnchor);

		// Operation: ask the page state for its explicit anchor.
		IIngredientListElement pageAnchor = pageState.getPageAnchorElement(elements);

		// Assertions: a valid remembered anchor is returned.
		Assert.assertSame(clickedAnchor, pageAnchor);
	}

	@Test
	public void getPageAnchorElementReturnsRememberedAnchorWhenMatchingElementWasRebuilt() {
		// Setup: the list was rebuilt with a new element wrapper around the same ingredient object.
		IngredientGridPageState pageState = new IngredientGridPageState();
		Object ingredient = new Object();
		IIngredientListElement clickedAnchor = element(ingredient);
		List<IIngredientListElement> rebuiltElements = Collections.singletonList(element(ingredient));
		pageState.setPageAnchorElement(clickedAnchor);

		// Operation: ask the page state for its explicit anchor after the source list changed.
		IIngredientListElement pageAnchor = pageState.getPageAnchorElement(rebuiltElements);

		// Assertions: the original remembered anchor is still usable because lookup can find an equivalent entry.
		Assert.assertSame(clickedAnchor, pageAnchor);
	}

	@Test
	public void getPageAnchorElementClearsStaleAnchor() {
		// Setup: the remembered clicked ingredient is no longer present after filtering or bookmark changes.
		IngredientGridPageState pageState = new IngredientGridPageState();
		IIngredientListElement staleAnchor = element(new Object());
		List<IIngredientListElement> elements = Collections.singletonList(element(new Object()));
		pageState.setPageAnchorElement(staleAnchor);

		// Operation: ask for the anchor twice after it has gone stale.
		IIngredientListElement firstLookup = pageState.getPageAnchorElement(elements);
		IIngredientListElement secondLookup = pageState.getPageAnchorElement(elements);

		// Assertions: the stale anchor is not returned, and it is cleared after the first lookup.
		Assert.assertNull(firstLookup);
		Assert.assertNull(secondLookup);
	}

	@Test
	public void updateKeepingPageAnchorVisibleSelectsPageContainingAnchor() {
		// Setup: an anchor is on the second page.
		IngredientGridPageState pageState = new IngredientGridPageState();
		List<IIngredientListElement> elements = elements(23);
		IIngredientListElement anchor = elements.get(17);

		// Operation: relayout using the anchor.
		int firstItemIndex = pageState.updateKeepingPageAnchorVisible(anchor, elements, 10);

		// Assertions: the rendered page contains the anchor.
		Assert.assertEquals(10, firstItemIndex);
		Assert.assertEquals(10, pageState.getFirstItemIndex());
	}

	@Test
	public void anchorMatchesTheSameElementInstance() {
		// Setup: the remembered element still appears in the current ingredient list.
		IIngredientListElement anchor = element(new Object());
		List<IIngredientListElement> elements = Arrays.asList(element(new Object()), anchor, element(new Object()));

		// Operation: find where the remembered element appears in the current list.
		int anchorIndex = IngredientGridPageState.findIndexOfIngredientElement(anchor, elements);

		// Assertions: the same element object is found directly.
		Assert.assertEquals(1, anchorIndex);
	}

	@Test
	public void anchorMatchesTheSameIngredientInstance() {
		// Setup: the list was rebuilt with a new element object around the same ingredient object.
		Object ingredient = new Object();
		IIngredientListElement anchor = element(ingredient);
		List<IIngredientListElement> elements = Arrays.asList(element(new Object()), element(ingredient));

		// Operation: find the rebuilt element that wraps the same ingredient instance.
		int anchorIndex = IngredientGridPageState.findIndexOfIngredientElement(anchor, elements);

		// Assertions: ingredient identity is enough to match rebuilt element wrappers.
		Assert.assertEquals(1, anchorIndex);
	}

	@Test
	public void anchorDoesNotMatchEqualButDifferentIngredientInstance() {
		// Setup: two ingredients compare equal, but they are not the same in-memory ingredient.
		String anchorIngredient = new String("same");
		String matchingButDifferentIngredient = new String("same");
		IIngredientListElement anchor = element(anchorIngredient);
		List<IIngredientListElement> elements = Collections.singletonList(element(matchingButDifferentIngredient));

		// Operation: try to find the anchor using identity-based lookup.
		int anchorIndex = IngredientGridPageState.findIndexOfIngredientElement(anchor, elements);

		// Assertions: UID/equality checks are intentionally avoided in this hot layout path.
		Assert.assertEquals(-1, anchorIndex);
	}

	private static List<IIngredientListElement> elements(int itemCount) {
		IIngredientListElement[] elements = new IIngredientListElement[itemCount];
		for (int i = 0; i < itemCount; i++) {
			elements[i] = element(new Object());
		}
		return Arrays.asList(elements);
	}

	private static IIngredientListElement element(Object ingredient) {
		return new TestElement(ingredient);
	}

	private static final class TestElement implements IIngredientListElement<Object> {
		private final Object ingredient;
		private boolean visible = true;

		private TestElement(Object ingredient) {
			this.ingredient = ingredient;
		}

		@Override
		public Object getIngredient() {
			return ingredient;
		}

		@Override
		public int getOrderIndex() {
			return 0;
		}

		@Override
		public IIngredientHelper<Object> getIngredientHelper() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IIngredientRenderer<Object> getIngredientRenderer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getDisplayName() {
			return "";
		}

		@Override
		public String getModNameForSorting() {
			return "";
		}

		@Override
		public Set<String> getModNameStrings() {
			return Collections.emptySet();
		}

		@Override
		public List<String> getTooltipStrings() {
			return Collections.emptyList();
		}

		@Override
		public Collection<String> getOreDictStrings() {
			return Collections.emptyList();
		}

		@Override
		public Collection<String> getCreativeTabsStrings() {
			return Collections.emptyList();
		}

		@Override
		public Collection<String> getColorStrings() {
			return Collections.emptyList();
		}

		@Override
		public String getResourceId() {
			return "";
		}

		@Override
		public boolean isVisible() {
			return visible;
		}

		@Override
		public void setVisible(boolean visible) {
			this.visible = visible;
		}
	}
}

package mezz.jei.gui.config.screen;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.Mth;

final class ConfigScreenLayout {
	static final int NAV_ITEM_HEIGHT = 20;
	static final int NAV_ITEM_GAP = 2;
	static final int SEARCH_HEIGHT = 18;
	static final int INFO_AREA_HEIGHT = 48;

	private static final int MIN_GUI_WIDTH = 320;
	private static final int MAX_GUI_WIDTH = 380;
	private static final int MIN_HEIGHT = 230;
	private static final int NAV_WIDTH = 104;
	private static final int NAV_SCROLLBAR_WIDTH = 6;
	private static final int NAV_SCROLLBAR_GAP = 2;
	private static final int RESET_BUTTON_WIDTH = 40;
	private static final int APPLY_BUTTON_WIDTH = 40;
	private static final int TOP_BUTTON_GAP = 2;
	private static final int SEARCH_TEXT_LEFT_PADDING = 5;
	private static final int SEARCH_TEXT_RIGHT_PADDING = 4;
	private static final int INFO_AREA_GAP = 4;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 10;
	private static final double SCROLL_SPEED = 10.0;
	private static final double SCROLL_LERP = 0.35;

	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private ImmutableRect2i navArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i navScrollBarArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i contentArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i searchBackgroundArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i resetCategoryButtonArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i applyButtonArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i scrollBarArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i infoArea = ImmutableRect2i.EMPTY;

	private int totalContentHeight = 0;
	private double targetScrollY = 0;
	private double currentScrollY = 0;
	private boolean draggingContentScroll = false;
	private double scrollDragOffsetY = 0;

	private int totalNavHeight = 0;
	private double navTargetScrollY = 0;
	private double navCurrentScrollY = 0;
	private boolean draggingNavScroll = false;
	private double navScrollDragOffsetY = 0;

	public void updateScreenBounds(int screenWidth, int screenHeight, EditBox searchBox) {
		int guiWidth = Math.clamp(screenWidth - 40, MIN_GUI_WIDTH, MAX_GUI_WIDTH);
		int guiHeight = Math.clamp(screenHeight - 40, MIN_HEIGHT, 300);
		int guiLeft = (screenWidth - guiWidth) / 2;
		int guiTop = (screenHeight - guiHeight) / 2;
		area = new ImmutableRect2i(guiLeft, guiTop, guiWidth, guiHeight);

		int innerTop = area.getY() + 6;
		int innerBottom = area.getY() + area.getHeight() - 6;
		int infoTop = innerBottom - INFO_AREA_HEIGHT;

		infoArea = new ImmutableRect2i(
			area.getX() + 4,
			infoTop,
			area.getWidth() - 8,
			INFO_AREA_HEIGHT
		);

		int scrollAreaBottom = infoArea.getY() - INFO_AREA_GAP;
		int innerHeight = scrollAreaBottom - innerTop;

		navArea = new ImmutableRect2i(
			area.getX() + 4,
			innerTop,
			NAV_WIDTH,
			innerHeight
		);
		navScrollBarArea = new ImmutableRect2i(
			navArea.getX() + navArea.getWidth() - NAV_SCROLLBAR_WIDTH,
			navArea.getY(),
			NAV_SCROLLBAR_WIDTH,
			navArea.getHeight()
		);

		int contentLeft = navArea.getX() + navArea.getWidth() + 4;
		int contentWidth = area.getWidth() - NAV_WIDTH - 20;

		int applyButtonX = contentLeft + contentWidth - APPLY_BUTTON_WIDTH + 1;
		applyButtonArea = new ImmutableRect2i(
			applyButtonX,
			innerTop,
			APPLY_BUTTON_WIDTH - 3,
			SEARCH_HEIGHT
		);
		int resetButtonX = applyButtonX - TOP_BUTTON_GAP - RESET_BUTTON_WIDTH;
		resetCategoryButtonArea = new ImmutableRect2i(
			resetButtonX,
			innerTop,
			RESET_BUTTON_WIDTH - 3,
			SEARCH_HEIGHT
		);
		searchBackgroundArea = new ImmutableRect2i(
			contentLeft,
			innerTop,
			resetButtonX - contentLeft - TOP_BUTTON_GAP,
			SEARCH_HEIGHT
		);
		searchBox.setX(searchBackgroundArea.getX() + SEARCH_TEXT_LEFT_PADDING);
		searchBox.setY(innerTop + 5);
		searchBox.setWidth(searchBackgroundArea.getWidth() - SEARCH_TEXT_LEFT_PADDING - SEARCH_TEXT_RIGHT_PADDING);
		searchBox.setHeight(SEARCH_HEIGHT);

		int contentTop = innerTop + SEARCH_HEIGHT + 2;
		contentArea = new ImmutableRect2i(
			contentLeft,
			contentTop,
			contentWidth,
			scrollAreaBottom - contentTop
		);

		scrollBarArea = new ImmutableRect2i(
			area.getX() + area.getWidth() - 14,
			contentArea.getY(),
			10,
			contentArea.getHeight()
		);
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public ImmutableRect2i getNavArea() {
		return navArea;
	}

	public int getNavItemWidth() {
		return navArea.getWidth() - NAV_SCROLLBAR_WIDTH - NAV_SCROLLBAR_GAP;
	}

	public ImmutableRect2i getNavScrollBarArea() {
		return navScrollBarArea;
	}

	public ImmutableRect2i getContentArea() {
		return contentArea;
	}

	public ImmutableRect2i getSearchBackgroundArea() {
		return searchBackgroundArea;
	}

	public ImmutableRect2i getResetCategoryButtonArea() {
		return resetCategoryButtonArea;
	}

	public ImmutableRect2i getApplyButtonArea() {
		return applyButtonArea;
	}

	public ImmutableRect2i getScrollBarArea() {
		return scrollBarArea;
	}

	public ImmutableRect2i getInfoArea() {
		return infoArea;
	}

	public ImmutableRect2i getScrollMarkerArea() {
		int maxScroll = getMaxContentScroll();
		if (maxScroll <= 0) {
			return ImmutableRect2i.EMPTY;
		}
		int markerHeight = getScrollMarkerHeight();
		int trackHeight = scrollBarArea.getHeight();
		int markerY = scrollBarArea.getY() + (int) ((trackHeight - markerHeight) * currentScrollY / maxScroll);
		return new ImmutableRect2i(
			scrollBarArea.getX() + 1,
			markerY,
			scrollBarArea.getWidth() - 2,
			markerHeight
		);
	}

	public ImmutableRect2i getNavScrollMarkerArea() {
		int maxScroll = getMaxNavScroll();
		if (maxScroll <= 0) {
			return ImmutableRect2i.EMPTY;
		}
		int markerHeight = getNavScrollMarkerHeight();
		int trackHeight = navScrollBarArea.getHeight();
		int markerY = navScrollBarArea.getY() + (int) ((trackHeight - markerHeight) * navCurrentScrollY / maxScroll);
		return new ImmutableRect2i(
			navScrollBarArea.getX() + 1,
			markerY,
			navScrollBarArea.getWidth() - 2,
			markerHeight
		);
	}

	public int getTotalContentHeight() {
		return totalContentHeight;
	}

	public int getTotalNavHeight() {
		return totalNavHeight;
	}

	public double getCurrentScrollY() {
		return currentScrollY;
	}

	public double getNavCurrentScrollY() {
		return navCurrentScrollY;
	}

	public boolean setTotalContentHeight(int totalContentHeight) {
		this.totalContentHeight = totalContentHeight;
		return clampContentScroll();
	}

	public boolean setTotalNavHeight(int totalNavHeight) {
		this.totalNavHeight = totalNavHeight;
		return clampNavScroll();
	}

	public void resetContentScroll() {
		targetScrollY = 0;
		currentScrollY = 0;
	}

	public void resetNavScroll() {
		navTargetScrollY = 0;
		navCurrentScrollY = 0;
	}

	public boolean scroll(double mouseX, double mouseY, double scrollY) {
		if (navArea.contains(mouseX, mouseY)) {
			int maxNavScroll = getMaxNavScroll();
			navTargetScrollY = Mth.clamp(navTargetScrollY - scrollY * SCROLL_SPEED, 0, maxNavScroll);
			return true;
		}
		if (contentArea.contains(mouseX, mouseY)) {
			int maxScroll = Math.max(0, totalContentHeight - contentArea.getHeight());
			targetScrollY = Mth.clamp(targetScrollY - scrollY * SCROLL_SPEED, 0, maxScroll);
			return true;
		}
		return false;
	}

	public boolean startContentScrollDrag(double mouseX, double mouseY) {
		ImmutableRect2i markerArea = getScrollMarkerArea();
		if (markerArea.isEmpty() || !scrollBarArea.contains(mouseX, mouseY)) {
			return false;
		}

		if (markerArea.contains(mouseX, mouseY)) {
			scrollDragOffsetY = mouseY - markerArea.getY();
		} else {
			scrollDragOffsetY = markerArea.getHeight() / 2.0;
			setContentScrollFromMarkerY(mouseY - scrollDragOffsetY);
		}
		draggingContentScroll = true;
		return true;
	}

	public boolean dragContentScroll(double mouseY) {
		if (!draggingContentScroll) {
			return false;
		}
		setContentScrollFromMarkerY(mouseY - scrollDragOffsetY);
		return true;
	}

	public boolean stopContentScrollDrag() {
		boolean wasDragging = draggingContentScroll;
		draggingContentScroll = false;
		return wasDragging;
	}

	public boolean startNavScrollDrag(double mouseX, double mouseY) {
		ImmutableRect2i markerArea = getNavScrollMarkerArea();
		if (markerArea.isEmpty() || !navScrollBarArea.contains(mouseX, mouseY)) {
			return false;
		}

		if (markerArea.contains(mouseX, mouseY)) {
			navScrollDragOffsetY = mouseY - markerArea.getY();
		} else {
			navScrollDragOffsetY = markerArea.getHeight() / 2.0;
			setNavScrollFromMarkerY(mouseY - navScrollDragOffsetY);
		}
		draggingNavScroll = true;
		return true;
	}

	public boolean dragNavScroll(double mouseY) {
		if (!draggingNavScroll) {
			return false;
		}
		setNavScrollFromMarkerY(mouseY - navScrollDragOffsetY);
		return true;
	}

	public boolean stopNavScrollDrag() {
		boolean wasDragging = draggingNavScroll;
		draggingNavScroll = false;
		return wasDragging;
	}

	public boolean stepContentScroll() {
		double nextScroll = stepScroll(currentScrollY, targetScrollY);
		if (nextScroll == currentScrollY) {
			return false;
		}
		currentScrollY = nextScroll;
		return true;
	}

	public boolean stepNavScroll() {
		double nextScroll = stepScroll(navCurrentScrollY, navTargetScrollY);
		if (nextScroll == navCurrentScrollY) {
			return false;
		}
		navCurrentScrollY = nextScroll;
		return true;
	}

	private static double stepScroll(double current, double target) {
		if (Math.abs(target - current) > 0.5) {
			return current + (target - current) * SCROLL_LERP;
		}
		if (current != target) {
			return target;
		}
		return current;
	}

	private int getMaxContentScroll() {
		return Math.max(0, totalContentHeight - contentArea.getHeight());
	}

	private int getMaxNavScroll() {
		return Math.max(0, totalNavHeight - navArea.getHeight());
	}

	private int getScrollMarkerHeight() {
		if (totalContentHeight <= 0) {
			return scrollBarArea.getHeight();
		}
		return Math.max(MIN_SCROLL_MARKER_HEIGHT, scrollBarArea.getHeight() * contentArea.getHeight() / totalContentHeight);
	}

	private int getNavScrollMarkerHeight() {
		if (totalNavHeight <= 0) {
			return navScrollBarArea.getHeight();
		}
		return Math.max(MIN_SCROLL_MARKER_HEIGHT, navScrollBarArea.getHeight() * navArea.getHeight() / totalNavHeight);
	}

	private void setContentScrollFromMarkerY(double markerY) {
		int maxScroll = getMaxContentScroll();
		int markerHeight = getScrollMarkerHeight();
		int trackSpace = scrollBarArea.getHeight() - markerHeight;
		if (maxScroll <= 0 || trackSpace <= 0) {
			targetScrollY = 0;
			currentScrollY = 0;
			return;
		}

		double scrollPercent = (markerY - scrollBarArea.getY()) / trackSpace;
		double nextScroll = Mth.clamp(scrollPercent * maxScroll, 0, maxScroll);
		targetScrollY = nextScroll;
		currentScrollY = nextScroll;
	}

	private void setNavScrollFromMarkerY(double markerY) {
		int maxScroll = getMaxNavScroll();
		int markerHeight = getNavScrollMarkerHeight();
		int trackSpace = navScrollBarArea.getHeight() - markerHeight;
		if (maxScroll <= 0 || trackSpace <= 0) {
			navTargetScrollY = 0;
			navCurrentScrollY = 0;
			return;
		}

		double scrollPercent = (markerY - navScrollBarArea.getY()) / trackSpace;
		double nextScroll = Mth.clamp(scrollPercent * maxScroll, 0, maxScroll);
		navTargetScrollY = nextScroll;
		navCurrentScrollY = nextScroll;
	}

	private boolean clampContentScroll() {
		int maxScroll = getMaxContentScroll();
		double oldTarget = targetScrollY;
		double oldCurrent = currentScrollY;
		targetScrollY = Mth.clamp(targetScrollY, 0, maxScroll);
		currentScrollY = Mth.clamp(currentScrollY, 0, maxScroll);
		return oldTarget != targetScrollY || oldCurrent != currentScrollY;
	}

	private boolean clampNavScroll() {
		int maxNavScroll = getMaxNavScroll();
		double oldTarget = navTargetScrollY;
		double oldCurrent = navCurrentScrollY;
		navTargetScrollY = Mth.clamp(navTargetScrollY, 0, maxNavScroll);
		navCurrentScrollY = Mth.clamp(navCurrentScrollY, 0, maxNavScroll);
		return oldTarget != navTargetScrollY || oldCurrent != navCurrentScrollY;
	}
}

package mezz.jei.gui.config.screen;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.Mth;

final class ConfigScreenLayout {
	static final int ENTRY_HEIGHT = 20;
	static final int NAV_ITEM_HEIGHT = 20;
	static final int NAV_ITEM_GAP = 2;
	static final int SEARCH_HEIGHT = 18;
	static final int INFO_AREA_HEIGHT = 48;

	private static final int MIN_GUI_WIDTH = 320;
	private static final int MAX_GUI_WIDTH = 380;
	private static final int MIN_HEIGHT = 230;
	private static final int NAV_WIDTH = 96;
	private static final int RESET_BUTTON_WIDTH = 40;
	private static final int INFO_AREA_GAP = 4;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 10;
	private static final double SCROLL_SPEED = 10.0;
	private static final double SCROLL_LERP = 0.35;

	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private ImmutableRect2i navArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i contentArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i resetCategoryButtonArea = ImmutableRect2i.EMPTY;
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
			area.getWidth() - 18,
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

		int contentLeft = navArea.getX() + navArea.getWidth() + 4;
		int contentWidth = area.getWidth() - NAV_WIDTH - 20;

		resetCategoryButtonArea = new ImmutableRect2i(
			contentLeft + contentWidth - RESET_BUTTON_WIDTH + 1,
			innerTop,
			RESET_BUTTON_WIDTH - 3,
			SEARCH_HEIGHT
		);
		searchBox.setX(contentLeft + 2);
		searchBox.setY(innerTop + 5);
		searchBox.setWidth(contentWidth - RESET_BUTTON_WIDTH - 4);
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

	public ImmutableRect2i getContentArea() {
		return contentArea;
	}

	public ImmutableRect2i getResetCategoryButtonArea() {
		return resetCategoryButtonArea;
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
			int maxNavScroll = Math.max(0, totalNavHeight - navArea.getHeight());
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

	private int getScrollMarkerHeight() {
		if (totalContentHeight <= 0) {
			return scrollBarArea.getHeight();
		}
		return Math.max(MIN_SCROLL_MARKER_HEIGHT, scrollBarArea.getHeight() * contentArea.getHeight() / totalContentHeight);
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

	private boolean clampContentScroll() {
		int maxScroll = getMaxContentScroll();
		double oldTarget = targetScrollY;
		double oldCurrent = currentScrollY;
		targetScrollY = Mth.clamp(targetScrollY, 0, maxScroll);
		currentScrollY = Mth.clamp(currentScrollY, 0, maxScroll);
		return oldTarget != targetScrollY || oldCurrent != currentScrollY;
	}

	private boolean clampNavScroll() {
		int maxNavScroll = Math.max(0, totalNavHeight - navArea.getHeight());
		double oldTarget = navTargetScrollY;
		double oldCurrent = navCurrentScrollY;
		navTargetScrollY = Mth.clamp(navTargetScrollY, 0, maxNavScroll);
		navCurrentScrollY = Mth.clamp(navCurrentScrollY, 0, maxNavScroll);
		return oldTarget != navTargetScrollY || oldCurrent != navCurrentScrollY;
	}
}

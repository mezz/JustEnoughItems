package mezz.jei.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.util.CycleTimer;
import net.minecraft.client.gui.Gui;

public abstract class GuiWidget<T> extends Gui implements IGuiWidget<T> {
	protected final int xPosition;
	protected final int yPosition;
	protected final int width;
	protected final int height;

	protected boolean enabled;
	protected boolean visible;

	protected final CycleTimer cycleTimer = new CycleTimer();

	@Nonnull
	protected final List<T> contained = new ArrayList<>();

	public GuiWidget(int xPosition, int yPosition, int width, int height) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.width = width;
		this.height = height;
	}

	@Override
	public void clear() {
		visible = enabled = false;
		contained.clear();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return enabled && visible && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	@Override
	@Nullable
	public T get() {
		return cycleTimer.getCycledItem(contained);
	}

	@Override
	public void set(@Nonnull T contained, @Nullable T focus) {
		set(Collections.singleton(contained), focus);
	}

	@Override
	public void set(@Nonnull Collection<T> contained, @Nullable T focus) {
		this.contained.clear();
		contained = expandSubtypes(contained);
		T match = getMatch(contained, focus);
		if (match != null) {
			this.contained.add(match);
		} else {
			this.contained.addAll(contained);
		}
		visible = enabled = !this.contained.isEmpty();
	}

	protected abstract Collection<T> expandSubtypes(Collection<T> contained);

	protected abstract T getMatch(Iterable<T> contained, T toMatch);
}

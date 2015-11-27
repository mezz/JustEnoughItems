package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.opengl.GL11;

import mezz.jei.util.CycleTimer;
import mezz.jei.util.Log;

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
	public void set(@Nonnull T contained, @Nonnull Focus focus) {
		set(Collections.singleton(contained), focus);
	}

	@Override
	public void set(@Nonnull Collection<T> contained, @Nonnull Focus focus) {
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

	protected abstract T getMatch(Iterable<T> contained, @Nonnull Focus toMatch);

	@Override
	public void draw(@Nonnull Minecraft minecraft) {
		draw(minecraft, true);
	}

	@Override
	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		T value = get();
		if (value == null) {
			return;
		}
		draw(minecraft, false);
		drawTooltip(minecraft, mouseX, mouseY, value);
	}

	private void draw(Minecraft minecraft, boolean cycleIcons) {
		if (!visible) {
			return;
		}

		cycleTimer.onDraw(cycleIcons);

		T value = get();
		if (value == null) {
			return;
		}

		draw(minecraft, xPosition, yPosition, value);
	}

	private void drawTooltip(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull T value) {
		try {
			GlStateManager.disableDepth();

			this.zLevel = 0;
			RenderHelper.disableStandardItemLighting();
			GL11.glEnable(GL11.GL_BLEND);
			drawRect(xPosition, yPosition, xPosition + width, yPosition + width, 0x7FFFFFFF);

			this.zLevel = 0;
			List tooltip = getTooltip(minecraft, value);
			FontRenderer fontRenderer = getFontRenderer(minecraft, value);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);

			GlStateManager.enableDepth();
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.\n{}", value, e);
		}
	}

	protected abstract void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nonnull T value);

	protected abstract List getTooltip(@Nonnull Minecraft minecraft, @Nonnull T value);

	protected abstract FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull T value);
}

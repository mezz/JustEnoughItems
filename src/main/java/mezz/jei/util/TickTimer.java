package mezz.jei.util;

import mezz.jei.api.gui.ITickTimer;
import net.minecraft.client.Minecraft;

public class TickTimer implements ITickTimer {
	private final int ticksPerCycle;
	private final int maxValue;
	private final boolean countDown;

	private long lastUpdateWorldTime = 0;
	private int tickCount = 0;

	public TickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		this.ticksPerCycle = ticksPerCycle;
		this.maxValue = maxValue;
		this.countDown = countDown;
	}

	@Override
	public int getValue() {
		long worldTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
		long ticksPassed = worldTime - lastUpdateWorldTime;
		lastUpdateWorldTime = worldTime;
		tickCount += ticksPassed;
		if (tickCount >= ticksPerCycle) {
			tickCount = 0;
		}

		int value = Math.round(tickCount * maxValue / (float) ticksPerCycle);
		if (countDown) {
			return maxValue - value;
		} else {
			return value;
		}
	}

	@Override
	public int getMaxValue() {
		return maxValue;
	}
}

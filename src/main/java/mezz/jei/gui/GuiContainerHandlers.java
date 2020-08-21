package mezz.jei.gui;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiContainerHandlers {
	private final List<Entry<?>> entries = new ArrayList<>();

	public <T extends ContainerScreen<?>> void add(Class<? extends T> containerClass, IGuiContainerHandler<? super T> handler) {
		Entry<? extends T> entryForClass = getEntryForClass(containerClass);
		if (entryForClass == null) {
			entryForClass = new Entry<>(containerClass);
			this.entries.add(entryForClass);
		}
		entryForClass.addHandler(handler);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T extends ContainerScreen<?>> Entry<T> getEntryForClass(Class<? extends T> containerClass) {
		return this.entries.stream()
			.filter(entry -> entry.containerClass == containerClass)
			.map(entry -> (Entry<T>) entry)
			.findFirst()
			.orElse(null);
	}

	@SuppressWarnings("unchecked")
	private <T extends ContainerScreen<?>> Stream<Entry<? super T>> getEntriesForInstance(T containerScreen) {
		return this.entries.stream()
			.filter(entry -> entry.containerClass.isInstance(containerScreen))
			.map(entry -> (Entry<? super T>) entry);
	}

	public <T extends ContainerScreen<?>> List<IGuiContainerHandler<? super T>> getActiveGuiHandlers(T guiContainer) {
		return getActiveGuiHandlerStream(guiContainer)
			.collect(Collectors.toList());
	}

	public <T extends ContainerScreen<?>> Stream<IGuiContainerHandler<? super T>> getActiveGuiHandlerStream(T guiContainer) {
		return getEntriesForInstance(guiContainer)
			.flatMap(entry -> entry.getHandlers().stream());
	}

	@Nullable
	public <T extends ContainerScreen<?>> IGuiClickableArea getGuiClickableArea(T guiContainer, double mouseX, double mouseY) {
		return getActiveGuiHandlerStream(guiContainer)
			.flatMap(handler -> handler.getGuiClickableAreas(guiContainer, mouseX, mouseY).stream())
			.filter(guiClickableArea -> MathUtil.contains(guiClickableArea.getArea(), mouseX, mouseY))
			.findFirst()
			.orElse(null);
	}

	public <C extends Container, T extends ContainerScreen<C>> Collection<Rectangle2d> getGuiExtraAreas(T guiContainer) {
		return getActiveGuiHandlerStream(guiContainer)
			.flatMap(guiContainerHandler -> guiContainerHandler.getGuiExtraAreas(guiContainer).stream())
			.collect(Collectors.toList());
	}

	private static class Entry<T extends ContainerScreen<?>> {
		private final Class<? extends T> containerClass;
		private final List<IGuiContainerHandler<? super T>> handlers;

		private Entry(Class<? extends T> containerClass) {
			this.containerClass = containerClass;
			this.handlers = new ArrayList<>();
		}

		public void addHandler(IGuiContainerHandler<? super T> handler) {
			this.handlers.add(handler);
		}

		public Class<? extends T> getContainerClass() {
			return containerClass;
		}

		public List<IGuiContainerHandler<? super T>> getHandlers() {
			return handlers;
		}
	}
}

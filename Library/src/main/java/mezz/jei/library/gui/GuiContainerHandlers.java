package mezz.jei.library.gui;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class GuiContainerHandlers {
	private final List<Entry<?>> entries = new ArrayList<>();

	public <T extends AbstractContainerScreen<?>> void add(Class<? extends T> containerClass, IGuiContainerHandler<? super T> handler) {
		Entry<? extends T> entryForClass = getEntryForClass(containerClass);
		if (entryForClass == null) {
			entryForClass = new Entry<>(containerClass);
			this.entries.add(entryForClass);
		}
		entryForClass.addHandler(handler);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T extends AbstractContainerScreen<?>> Entry<T> getEntryForClass(Class<? extends T> containerClass) {
		return this.entries.stream()
			.filter(entry -> entry.getContainerClass() == containerClass)
			.map(entry -> (Entry<T>) entry)
			.findFirst()
			.orElse(null);
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractContainerScreen<?>> Stream<Entry<? super T>> getEntriesForInstance(T containerScreen) {
		return this.entries.stream()
			.filter(entry -> entry.containerClass.isInstance(containerScreen))
			.map(entry -> (Entry<? super T>) entry);
	}

	public <T extends AbstractContainerScreen<?>> Stream<IGuiContainerHandler<? super T>> getActiveGuiHandlerStream(T guiContainer) {
		return getEntriesForInstance(guiContainer)
			.map(Entry::getHandlers)
			.flatMap(Collection::stream);
	}

	public <T extends AbstractContainerScreen<?>> Stream<IGuiClickableArea> getGuiClickableArea(T guiContainer, double guiMouseX, double guiMouseY) {
		return getActiveGuiHandlerStream(guiContainer)
			.map(handler -> handler.getGuiClickableAreas(guiContainer, guiMouseX, guiMouseY))
			.flatMap(Collection::stream)
			.filter(guiClickableArea -> MathUtil.contains(guiClickableArea.getArea(), guiMouseX, guiMouseY));
	}

	public <C extends AbstractContainerMenu, T extends AbstractContainerScreen<C>> Stream<Rect2i> getGuiExtraAreas(T guiContainer) {
		return getActiveGuiHandlerStream(guiContainer)
			.map(guiContainerHandler -> guiContainerHandler.getGuiExtraAreas(guiContainer))
			.flatMap(Collection::stream);
	}

	private static class Entry<T extends AbstractContainerScreen<?>> {
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

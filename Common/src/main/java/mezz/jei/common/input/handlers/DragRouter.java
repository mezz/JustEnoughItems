package mezz.jei.common.input.handlers;

import mezz.jei.common.input.IDragHandler;
import mezz.jei.common.input.UserInput;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DragRouter {
    private final List<IDragHandler> handlers;
    @Nullable
    private IDragHandler dragStartedCallback;

    public DragRouter(IDragHandler... handlers) {
        this.handlers = List.of(handlers);
    }

    public void handleGuiChange() {
        cancelDrag();
    }

    public boolean startDrag(Screen screen, UserInput input) {
        cancelDrag();

        this.dragStartedCallback = this.handlers.stream()
            .map(i -> i.handleDragStart(screen, input))
            .flatMap(Optional::stream)
            .findFirst()
            .orElse(null);

        return this.dragStartedCallback != null;
    }

    public boolean completeDrag(Screen screen, UserInput input) {
        if (this.dragStartedCallback == null) {
            return false;
        }
        boolean result = this.dragStartedCallback.handleDragComplete(screen, input);
        this.dragStartedCallback = null;
        return result;
    }

    public void cancelDrag() {
        if (this.dragStartedCallback != null) {
            this.dragStartedCallback.handleDragCanceled();
            this.dragStartedCallback = null;
        }
    }
}

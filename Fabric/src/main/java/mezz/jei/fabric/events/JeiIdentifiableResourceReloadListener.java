package mezz.jei.fabric.events;

import mezz.jei.api.constants.ModIds;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JeiIdentifiableResourceReloadListener implements IdentifiableResourceReloadListener {
    private final ResourceLocation fabricId;
    private final PreparableReloadListener listener;

    public JeiIdentifiableResourceReloadListener(String id, PreparableReloadListener listener) {
        this.fabricId = new ResourceLocation(ModIds.JEI_ID, id);
        this.listener = listener;
    }

    @Override
    public ResourceLocation getFabricId() {
        return this.fabricId;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        return listener.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
    }
}

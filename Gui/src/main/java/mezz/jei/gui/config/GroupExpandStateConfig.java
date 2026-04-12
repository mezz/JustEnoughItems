package mezz.jei.gui.config;

import com.mojang.serialization.JsonOps;
import mezz.jei.common.config.file.JsonArrayFileHelper;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import mezz.jei.common.util.ServerConfigPathUtil;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GroupExpandStateConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int VERSION = 1;

    private final Path jeiConfigurationDir;
    private final Set<Identifier> expandedGroups = new HashSet<>();

    public GroupExpandStateConfig(Path jeiConfigurationDir) {
        this.jeiConfigurationDir = jeiConfigurationDir;
        load();
    }

    private static Optional<Path> getPath(Path jeiConfigurationDir) {
        return ServerConfigPathUtil.getWorldPath(jeiConfigurationDir)
                                   .flatMap(configPath -> {
                                       try {
                                           Files.createDirectories(configPath);
                                       } catch (IOException e) {
                                           LOGGER.error("Unable to create group expand state config folder: {}", configPath, e);
                                           return Optional.empty();
                                       }
                                       return Optional.of(configPath.resolve("group_expand_state.json"));
                                   });
    }

    public boolean isExpanded(IngredientGroupInfo groupInfo) {
        return isExpanded(groupInfo.id());
    }

    public boolean isExpanded(Identifier groupId) {
        return expandedGroups.contains(groupId);
    }

    public void setExpanded(Identifier groupId, boolean expanded) {
        boolean changed;
        if (expanded) {
            changed = expandedGroups.add(groupId);
        } else {
            changed = expandedGroups.remove(groupId);
        }
        if (changed) {
            save();
        }
    }

    public void load() {
        getPath(jeiConfigurationDir).ifPresent(path -> {
            if (!Files.exists(path)) {
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                List<Identifier> ids = JsonArrayFileHelper.read(
                        reader,
                        VERSION,
                        Identifier.CODEC,
                        JsonOps.INSTANCE,
                        (element, error) -> {
                            LOGGER.error("Encountered an error when loading group expand state from file {}\n{}\n{}", path, element, error);
                        },
                        (element, exception) -> {
                            LOGGER.error("Encountered an exception when loading group expand state from file {}\n{}", path, element, exception);
                        }
                );
                expandedGroups.clear();
                expandedGroups.addAll(ids);
                LOGGER.debug("Loaded group expand state from: {}", path);
            } catch (RuntimeException | IOException e) {
                LOGGER.error("Failed to load group expand state from {}", path, e);
            }
        });
    }

    private void save() {
        getPath(jeiConfigurationDir).ifPresent(path -> {
            try (BufferedWriter out = Files.newBufferedWriter(path)) {
                JsonArrayFileHelper.write(
                        out,
                        VERSION,
                        List.copyOf(expandedGroups),
                        Identifier.CODEC,
                        JsonOps.INSTANCE,
                        error -> {
                            LOGGER.error("Encountered an error when saving group expand state to file {}\n{}", path, error);
                        },
                        (element, exception) -> {
                            LOGGER.error("Encountered an exception when saving group expand state to file {}\n{}", path, element, exception);
                        }
                );
                LOGGER.debug("Saved group expand state to: {}", path);
            } catch (IOException e) {
                LOGGER.error("Failed to save group expand state to {}", path, e);
            }
        });
    }
}

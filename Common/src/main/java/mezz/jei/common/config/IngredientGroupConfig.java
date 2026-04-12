package mezz.jei.common.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.codecs.EnumCodec;
import mezz.jei.common.config.file.JsonArrayFileHelper;
import mezz.jei.common.ingredients.group.IIngredientGroupSelector;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import mezz.jei.common.ingredients.group.IngredientGroupType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class IngredientGroupConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int VERSION = 1;
	private static final Codec<IngredientGroupType> TYPE_CODEC = EnumCodec.createLower(IngredientGroupType.class);

	private record IngredientGroupFileData(
		List<IIngredientGroupSelector> selectors,
		boolean override
	) {}

	private final Map<Identifier, IngredientGroupInfo> ingredientGroups = new LinkedHashMap<>();
	private final MapCodec<IngredientGroupInfo> legacyGroupCodec;
	private final MapCodec<IngredientGroupFileData> groupFileCodec;
	private final Path jeiConfigurationDir;

	public IngredientGroupConfig(ICodecHelper codecHelper, IIngredientManager ingredientManager, Path jeiConfigurationDir) {
		this.legacyGroupCodec = getLegacyGroupCodec(codecHelper, ingredientManager);
		this.groupFileCodec = getGroupFileCodec(codecHelper, ingredientManager);
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	private static Codec<IIngredientGroupSelector> getSelectorCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
		return TYPE_CODEC.dispatch(
			IIngredientGroupSelector::getType,
			type -> type.getCodec(codecHelper, ingredientManager)
		);
	}

	private static MapCodec<IngredientGroupInfo> getLegacyGroupCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
		Codec<IIngredientGroupSelector> selectorCodec = getSelectorCodec(codecHelper, ingredientManager);
		return RecordCodecBuilder.mapCodec(instance ->
			instance.group(
				Identifier.CODEC.fieldOf("id")
					.forGetter(IngredientGroupInfo::id),
				selectorCodec.listOf().fieldOf("selectors")
					.forGetter(info -> getSerializableSelectors(info.selectors())),
				Codec.BOOL.optionalFieldOf("override", false)
					.forGetter(IngredientGroupInfo::override)
			).apply(instance, IngredientGroupInfo::new)
		);
	}

	private static MapCodec<IngredientGroupFileData> getGroupFileCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
		Codec<IIngredientGroupSelector> selectorCodec = getSelectorCodec(codecHelper, ingredientManager);
		return RecordCodecBuilder.mapCodec(instance ->
			instance.group(
				selectorCodec.listOf().fieldOf("selectors")
					.forGetter(IngredientGroupFileData::selectors),
				Codec.BOOL.optionalFieldOf("override", false)
					.forGetter(IngredientGroupFileData::override)
			).apply(instance, IngredientGroupFileData::new)
		);
	}

	private static List<IIngredientGroupSelector> getSerializableSelectors(List<IIngredientGroupSelector> selectors) {
		return selectors.stream()
			.filter(s -> s.getType() != IngredientGroupType.DYNAMIC)
			.toList();
	}

	private static RegistryOps<JsonElement> getRegistryOps(RegistryAccess registryAccess) {
		return registryAccess.createSerializationContext(JsonOps.INSTANCE);
	}

	private static Path getGroupFilePath(Path groupsDir, Identifier groupId) {
		Path namespaceDir = groupsDir.resolve(groupId.getNamespace());
		return namespaceDir.resolve(groupId.getPath() + ".json");
	}

	private static boolean isNamespacedGroupFile(Path groupsDir, Path path) {
		return groupsDir.relativize(path).getNameCount() > 1;
	}

	private static Identifier getGroupIdFromPath(Path groupsDir, Path path) {
		Path relative = groupsDir.relativize(path);
		String namespace = relative.getName(0).toString();
		Path pathWithinNamespace = relative.subpath(1, relative.getNameCount());
		int nameCount = pathWithinNamespace.getNameCount();

		String fileName = pathWithinNamespace.getName(nameCount - 1).toString();
		if (!fileName.endsWith(".json")) {
			throw new IllegalArgumentException("Expected .json file extension: " + fileName);
		}
		String fileNameWithoutExtension = fileName.substring(0, fileName.length() - ".json".length());

		StringBuilder groupPath = new StringBuilder();
		for (int i = 0; i < nameCount - 1; i++) {
			groupPath.append(pathWithinNamespace.getName(i)).append('/');
		}
		groupPath.append(fileNameWithoutExtension);

		return Identifier.fromNamespaceAndPath(namespace, groupPath.toString());
	}

	private static boolean isLegacyWorldConfigPath(Path groupsDir, Path path) {
		Path relative = groupsDir.relativize(path);
		int nameCount = relative.getNameCount();
		if (nameCount < 3) {
			return false;
		}
		if (!relative.getName(0).toString().equals("world")) {
			return false;
		}
		String type = relative.getName(1).toString();
		return type.equals("local") || type.equals("server");
	}

	private static Optional<Path> getGroupsDir(Path jeiConfigurationDir) {
		try {
			Files.createDirectories(jeiConfigurationDir);
		} catch (IOException e) {
			LOGGER.error("Unable to create group config folder: {}", jeiConfigurationDir, e);
			return Optional.empty();
		}
		return Optional.of(jeiConfigurationDir);
	}

	public void add(Identifier id, IIngredientGroupSelector selector) {
		this.ingredientGroups.computeIfAbsent(id, k -> new IngredientGroupInfo(k, new ArrayList<>(), false))
			.add(selector);
	}

	public void load(RegistryAccess registryAccess) {
		RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);
		List<IngredientGroupInfo> loaded = loadFromFile(registryOps);
		for (IngredientGroupInfo info : loaded) {
			if (info.override()) {
				ingredientGroups.put(info.id(), info);
			} else {
				IngredientGroupInfo existing = ingredientGroups.get(info.id());
				if (existing != null) {
					info.selectors().forEach(existing::add);
				} else {
					ingredientGroups.put(info.id(), info);
				}
			}
		}
	}

	public boolean save(RegistryAccess registryAccess) {
		return getGroupsDir(jeiConfigurationDir)
			.map(dir -> {
				Codec<IngredientGroupFileData> groupFileCodec = this.groupFileCodec.codec();
				RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);
				boolean success = true;

				for (IngredientGroupInfo info : ingredientGroups.values()) {
					Identifier groupId = info.id();
					Path path = getGroupFilePath(dir, groupId);
					List<IIngredientGroupSelector> selectors = getSerializableSelectors(info.selectors());

					if (selectors.isEmpty()) {
						if (Files.exists(path)) {
							try {
								Files.delete(path);
							} catch (IOException e) {
								LOGGER.error("Failed to delete group config file {}", path, e);
								success = false;
							}
						}
						continue;
					}

					try {
						Files.createDirectories(path.getParent());
					} catch (IOException e) {
						LOGGER.error("Unable to create group config folder: {}", path.getParent(), e);
						success = false;
						continue;
					}

					IngredientGroupFileData data = new IngredientGroupFileData(selectors, info.override());
					try (BufferedWriter out = Files.newBufferedWriter(path)) {
						JsonArrayFileHelper.write(
							out,
							VERSION,
							List.of(data),
							groupFileCodec,
							registryOps,
							error -> {
								LOGGER.error("Encountered an error when saving group config to file {}\n{}", path, error);
							},
							(element, exception) -> {
								LOGGER.error("Encountered an exception when saving group config to file {}\n{}", path, element, exception);
							}
						);
						LOGGER.debug("Saved group config to file: {}", path);
					} catch (IOException e) {
						LOGGER.error("Failed to save group config to file {}", path, e);
						success = false;
					}
				}

				return success;
			})
			.orElse(false);
	}

	private List<IngredientGroupInfo> loadFromFile(RegistryOps<JsonElement> registryOps) {
		return getGroupsDir(jeiConfigurationDir)
			.<List<IngredientGroupInfo>>map(dir -> {
				if (!Files.exists(dir)) {
					return List.of();
				}

				List<Path> jsonFiles;
				try (Stream<Path> paths = Files.walk(dir)) {
					jsonFiles = paths.filter(Files::isRegularFile)
						.filter(p -> p.toString().endsWith(".json"))
						.filter(p -> !isLegacyWorldConfigPath(dir, p))
						.toList();
				} catch (IOException e) {
					LOGGER.error("Failed to walk group config directory {}", dir, e);
					return List.of();
				}

				boolean hasNamespacedFiles = jsonFiles.stream()
					.anyMatch(path -> isNamespacedGroupFile(dir, path));

				Map<Identifier, IngredientGroupInfo> allGroups = new LinkedHashMap<>();
				if (hasNamespacedFiles) {
					Codec<IngredientGroupFileData> groupFileCodec = this.groupFileCodec.codec();
					for (Path path : jsonFiles) {
						if (!isNamespacedGroupFile(dir, path)) {
							continue;
						}
						loadGroupFile(allGroups, dir, path, groupFileCodec, registryOps);
					}

					Path legacyPath = dir.resolve("groups.json");
					if (Files.isRegularFile(legacyPath)) {
						Map<Identifier, IngredientGroupInfo> legacyGroups = new LinkedHashMap<>();
						Codec<IngredientGroupInfo> legacyGroupCodec = this.legacyGroupCodec.codec();
						loadLegacyGroupFile(legacyGroups, legacyPath, legacyGroupCodec, registryOps);
						for (Map.Entry<Identifier, IngredientGroupInfo> entry : legacyGroups.entrySet()) {
							allGroups.putIfAbsent(entry.getKey(), entry.getValue());
						}
					}
				} else {
					Codec<IngredientGroupInfo> legacyGroupCodec = this.legacyGroupCodec.codec();
					for (Path path : jsonFiles) {
						loadLegacyGroupFile(allGroups, path, legacyGroupCodec, registryOps);
					}
				}

				return new ArrayList<>(allGroups.values());
			})
			.orElseGet(List::of);
	}

	private static void addGroupInfo(Map<Identifier, IngredientGroupInfo> results, IngredientGroupInfo info) {
		IngredientGroupInfo existing = results.get(info.id());
		if (existing == null) {
			results.put(info.id(), info);
			return;
		}

		List<IIngredientGroupSelector> mergedSelectors = new ArrayList<>(existing.selectors());
		mergedSelectors.addAll(info.selectors());
		boolean override = existing.override() || info.override();
		results.put(info.id(), new IngredientGroupInfo(info.id(), mergedSelectors, override));
	}

	private void loadGroupFile(
		Map<Identifier, IngredientGroupInfo> results,
		Path groupsDir,
		Path path,
		Codec<IngredientGroupFileData> groupFileCodec,
		RegistryOps<JsonElement> registryOps
	) {
		final Identifier id;
		try {
			id = getGroupIdFromPath(groupsDir, path);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to create group id from file path {}", path, e);
			return;
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			List<IngredientGroupFileData> data = JsonArrayFileHelper.read(
				reader,
				VERSION,
				groupFileCodec,
				registryOps,
				(element, error) -> {
					LOGGER.error("Encountered an error when loading group config from file {}\n{}\n{}", path, element, error);
				},
				(element, exception) -> {
					LOGGER.error("Encountered an exception when loading group config from file {}\n{}", path, element, exception);
				}
			);
			if (data.isEmpty()) {
				return;
			}

			boolean override = false;
			List<IIngredientGroupSelector> selectors = new ArrayList<>();
			for (IngredientGroupFileData entry : data) {
				override |= entry.override();
				selectors.addAll(entry.selectors());
			}

			addGroupInfo(results, new IngredientGroupInfo(id, selectors, override));
			LOGGER.debug("Loaded group config from file: {}", path);
		} catch (RuntimeException | IOException e) {
			LOGGER.error("Failed to load group config from file {}", path, e);
		}
	}

	private void loadLegacyGroupFile(
		Map<Identifier, IngredientGroupInfo> results,
		Path path,
		Codec<IngredientGroupInfo> groupCodec,
		RegistryOps<JsonElement> registryOps
	) {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			List<IngredientGroupInfo> groups = JsonArrayFileHelper.read(
				reader,
				VERSION,
				groupCodec,
				registryOps,
				(element, error) -> {
					LOGGER.error("Encountered an error when loading groups config from file {}\n{}\n{}", path, element, error);
				},
				(element, exception) -> {
					LOGGER.error("Encountered an exception when loading groups config from file {}\n{}", path, element, exception);
				}
			);
			for (IngredientGroupInfo info : groups) {
				addGroupInfo(results, info);
			}
			LOGGER.debug("Loaded groups config from file: {}", path);
		} catch (RuntimeException | IOException e) {
			LOGGER.error("Failed to load groups config from file {}", path, e);
		}
	}

	public Map<Identifier, IngredientGroupInfo> getIngredientGroups() {
		return Collections.unmodifiableMap(ingredientGroups);
	}
}

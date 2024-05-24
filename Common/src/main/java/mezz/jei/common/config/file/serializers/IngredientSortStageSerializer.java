package mezz.jei.common.config.file.serializers;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.IngredientSortStage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngredientSortStageSerializer implements IJeiConfigValueSerializer<IngredientSortStage> {

	public IngredientSortStageSerializer() {
	}

	@Override
	public String serialize(IngredientSortStage value) {
		return value.name;
	}

	@Override
	public DeserializeResult<IngredientSortStage> deserialize(String string) {
		string = string.trim();
		if (string.startsWith("\"") && string.endsWith("\"")) {
			string = string.substring(1, string.length() - 1);
		}
		//Since valid values could be added after we read the config, we can't validate them yet.
		var stage = IngredientSortStage.getOrCreateStage(string);
		return new DeserializeResult<>(stage);
	}

	@Override
	public String getValidValuesDescription() {
		String names = IngredientSortStage.getAllStageNames().stream()
			.collect(Collectors.joining(", "));

		return "[%s]".formatted(names);
	}

	@Override
	public boolean isValid(IngredientSortStage value) {
		//TODO:  Not sure if this only occurs after addins have a chance to register their sorters.
		//If so, we just need to return true.
		var stage = IngredientSortStage.getStage(value.name);
		return stage != null;
	}

	@Override
	public Optional<Collection<IngredientSortStage>> getAllValidValues() {
		return Optional.of(IngredientSortStage.getAllStages());
	}
}

package mezz.jei.collect;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableTable;

public class Table<R, C, V> {
	public static <R, C, V> Table<R, C, V> hashBasedTable() {
		return new Table<>(new HashMap<>(), HashMap::new);
	}

	private final Map<R, Map<C, V>> table;
	private final Function<R, Map<C, V>> rowMappingFunction;

	public Table(Map<R, Map<C, V>> table, Supplier<Map<C, V>> rowSupplier) {
		this.table = table;
		this.rowMappingFunction = (k -> rowSupplier.get());
	}

	@Nullable
	public V get(R row, C col) {
		Map<C, V> rowMap = getRow(row);
		return rowMap.get(col);
	}

	public V computeIfAbsent(R row, C col, Supplier<V> valueSupplier) {
		Map<C, V> rowMap = getRow(row);
		return rowMap.computeIfAbsent(col, k -> valueSupplier.get());
	}

	@Nullable
	public V put(R row, C col, V val) {
		Map<C, V> rowMap = getRow(row);
		return rowMap.put(col, val);
	}

	public Map<C, V> getRow(R row) {
		return table.computeIfAbsent(row, rowMappingFunction);
	}

	public void clear() {
		table.clear();
	}

	public ImmutableTable<R, C, V> toImmutable() {
		ImmutableTable.Builder<R, C, V> builder = ImmutableTable.builder();
		for (Map.Entry<R, Map<C, V>> entry : table.entrySet()) {
			R row = entry.getKey();
			for (Map.Entry<C, V> rowEntry : entry.getValue().entrySet()) {
				C col = rowEntry.getKey();
				V val = rowEntry.getValue();
				builder.put(row, col, val);
			}
		}
		return builder.build();
	}
}

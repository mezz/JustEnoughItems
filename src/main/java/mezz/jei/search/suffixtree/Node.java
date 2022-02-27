/*
 * Copyright 2012 Alessandro Bahgat Shehata
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mezz.jei.search.suffixtree;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import mezz.jei.util.SubString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Represents a node of the generalized suffix tree graph
 *
 * @see GeneralizedSuffixTree
 * <p>
 * Edited by mezz:
 * - Use Java 6 features
 * - improve performance of search by passing a set around instead of creating new ones and using addAll
 * - only allow full searches
 * - add nullable/nonnull annotations
 * - formatting
 */
class Node<T> {

	/**
	 * The payload array used to store the data (indexes) associated with this node.
	 * In this case, it is used to store all property indexes.
	 */
	private Collection<T> data;

	/**
	 * The set of edges starting from this node
	 */
	private final Char2ObjectMap<Edge<T>> edges;

	/**
	 * The suffix link as described in Ukkonen's paper.
	 * if str is the string denoted by the path from the root to this, this.suffix
	 * is the node denoted by the path that corresponds to str without the first char.
	 */
	@Nullable
	private Node<T> suffix;

	/**
	 * Creates a new Node
	 */
	Node() {
		edges = new Char2ObjectArrayMap<>(1);
		suffix = null;
		data = List.of();
	}

	/**
	 * Gets data from the payload of both this node and its children, the string representation
	 * of the path to this node is a substring of the one of the children nodes.
	 */
	public void getData(Set<T> results) {
		results.addAll(this.data);
		for (Edge<T> edge : edges.values()) {
			Node<T> dest = edge.getDest();
			dest.getData(results);
		}
	}

	/**
	 * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
	 * returns false if this node already contains the ref
	 */
	void addRef(T value) {
		if (contains(value)) {
			return;
		}

		addValue(value);

		// add this reference to all the suffixes as well
		Node<T> iter = this.suffix;
		while (iter != null) {
			if (!iter.contains(value)) {
				iter.addValue(value);
				iter = iter.suffix;
			} else {
				break;
			}
		}
	}

	/**
	 * Tests whether a node contains a reference to the given index.
	 *
	 * @param value the value to look for
	 * @return true <tt>this</tt> contains a reference to index
	 */
	private boolean contains(T value) {
		return data.contains(value);
	}

	void addEdge(Edge<T> edge) {
		edges.put(edge.charAt(0), edge);
	}

	@Nullable
	Edge<T> getEdge(char ch) {
		return edges.get(ch);
	}

	@Nullable
	Edge<T> getEdge(SubString string) {
		if (string.isEmpty()) {
			return null;
		}
		char ch = string.charAt(0);
		return edges.get(ch);
	}

	@Nullable
	Node<T> getSuffix() {
		return suffix;
	}

	void setSuffix(Node<T> suffix) {
		this.suffix = suffix;
	}

	protected void addValue(T value) {
		data = switch (data.size()) {
			case 0 -> List.of(value);
			case 1 -> List.of(data.iterator().next(), value);
			case 2 -> {
				List<T> newData = new ArrayList<>(2);
				newData.addAll(data);
				newData.add(value);
				yield newData;
			}
			case 16 -> {
				// "upgrade" data to a Set once it's getting bigger,
				// to improve its `contains` performance.
				Collection<T> newData = Collections.newSetFromMap(new IdentityHashMap<>());
				newData.addAll(data);
				newData.add(value);
				yield newData;
			}
			default -> {
				data.add(value);
				yield data;
			}
		};
	}

	@Override
	public String toString() {
		return "Node: size:" + data.size() + " Edges: " + edges;
	}

	public IntSummaryStatistics nodeSizeStats() {
		return nodeSizes().summaryStatistics();
	}

	private IntStream nodeSizes() {
		return IntStream.concat(
			IntStream.of(data.size()),
			edges.values().stream().flatMapToInt(e -> e.getDest().nodeSizes())
		);
	}
}

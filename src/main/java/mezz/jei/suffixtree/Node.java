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
package mezz.jei.suffixtree;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Represents a node of the generalized suffix tree graph
 *
 * @see GeneralizedSuffixTree
 *
 * Edited by mezz:
 * - Use Java 6 features
 * - improve performance of search by passing a set around instead of creating new ones and using addAll
 * - only allow full searches
 * - add nullable/nonnull annotations
 * - formatting
 */
class Node {

	/**
	 * The payload array used to store the data (indexes) associated with this node.
	 * In this case, it is used to store all property indexes.
	 */
	private final IntList data;

	/**
	 * The set of edges starting from this node
	 */
	private final Char2ObjectMap<Edge> edges;

	/**
	 * The suffix link as described in Ukkonen's paper.
	 * if str is the string denoted by the path from the root to this, this.suffix
	 * is the node denoted by the path that corresponds to str without the first char.
	 */
	@Nullable
	private Node suffix;

	/**
	 * Creates a new Node
	 */
	Node() {
		edges = new Char2ObjectOpenHashMap<>();
		suffix = null;
		data = new IntArrayList(0);
	}

	/**
	 * Gets data from the payload of both this node and its children, the string representation
	 * of the path to this node is a substring of the one of the children nodes.
	 */
	void getData(final IntSet ret) {
		ret.addAll(data);

		for (Edge e : edges.values()) {
			e.getDest().getData(ret);
		}
	}

	/**
	 * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
	 * returns false if this node already contains the ref
	 */
	boolean addRef(int index) {
		if (contains(index)) {
			return false;
		}

		addIndex(index);

		// add this reference to all the suffixes as well
		Node iter = this.suffix;
		while (iter != null) {
			if (!iter.contains(index)) {
				iter.addIndex(index);
				iter = iter.suffix;
			} else {
				break;
			}
		}

		return true;
	}

	/**
	 * Tests whether a node contains a reference to the given index.
	 *
	 * @param index the index to look for
	 * @return true <tt>this</tt> contains a reference to index
	 */
	private boolean contains(int index) {
		return data.contains(index);
	}

	void addEdge(char ch, Edge e) {
		edges.put(ch, e);
	}

	@Nullable
	Edge getEdge(char ch) {
		return edges.get(ch);
	}

	@Nullable
	Node getSuffix() {
		return suffix;
	}

	void setSuffix(Node suffix) {
		this.suffix = suffix;
	}

	private void addIndex(int index) {
		data.add(index);
	}

	@Override
	public String toString() {
		return "Node: size:" + data.size() + " Edges: " + edges.toString();
	}
}

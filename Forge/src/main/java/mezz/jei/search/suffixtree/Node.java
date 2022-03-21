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

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.util.SubString;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
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
 * - refactored and optimized
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
	private Char2ObjectMap<Edge<T>> edges;

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
		edges = Char2ObjectMaps.emptyMap();
		data = List.of();
		suffix = null;
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
	protected boolean contains(T value) {
		return data.contains(value);
	}

	void addEdge(Edge<T> edge) {
		char firstChar = edge.charAt(0);

		switch (edges.size()) {
			case 0 -> edges = Char2ObjectMaps.singleton(firstChar, edge);
			case 1 -> {
				Char2ObjectMap<Edge<T>> newEdges = new Char2ObjectOpenHashMap<>(edges);
				newEdges.put(firstChar, edge);
				edges = newEdges;
			}
			default -> edges.put(firstChar, edge);
		}
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
		switch (data.size()) {
			case 0 -> data = List.of(value);
			case 1 -> data = List.of(data.iterator().next(), value);
			case 2 -> {
				List<T> newData = new ArrayList<>(4);
				newData.addAll(data);
				newData.add(value);
				data = newData;
			}
			case 16 -> {
				// "upgrade" data to a Set once it's getting bigger,
				// to improve its `contains` performance.
				Collection<T> newData = Collections.newSetFromMap(new IdentityHashMap<>());
				newData.addAll(data);
				newData.add(value);
				data = newData;
			}
			default -> data.add(value);
		}
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

	public String nodeEdgeStats() {
		IntSummaryStatistics edgeCounts = nodeEdgeCounts().summaryStatistics();
		IntSummaryStatistics edgeLengths = nodeEdgeLengths().summaryStatistics();
		return "Edge counts: " + edgeCounts +
			"\nEdge lengths: " + edgeLengths;
	}

	private IntStream nodeEdgeCounts() {
		return IntStream.concat(
			IntStream.of(edges.size()),
			edges.values().stream().map(Edge::getDest).flatMapToInt(Node::nodeEdgeCounts)
		);
	}

	private IntStream nodeEdgeLengths() {
		return IntStream.concat(
			edges.values().stream().mapToInt(Edge::length),
			edges.values().stream().map(Edge::getDest).flatMapToInt(Node::nodeEdgeLengths)
		);
	}

	public void printTree(PrintWriter out, boolean includeSuffixLinks) {
		out.println("digraph {");
		out.println("\trankdir = LR;");
		out.println("\tordering = out;");
		out.println("\tedge [arrowsize=0.4,fontsize=10]");
		out.println("\t" + nodeId(this) + " [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.1,height=.1];");
		out.println("//------leaves------");
		printLeaves(out);
		out.println("//------internal nodes------");
		printInternalNodes(this, out);
		out.println("//------edges------");
		printEdges(out);
		if (includeSuffixLinks) {
			out.println("//------suffix links------");
			printSLinks(out);
		}
		out.println("}");
	}

	private void printLeaves(PrintWriter out) {
		if (edges.size() == 0) {
			out.println("\t" + nodeId(this) + " [label=\"" + data + "\",shape=point,style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");
		} else {
			for (Edge<T> edge : edges.values()) {
				edge.getDest().printLeaves(out);
			}
		}
	}

	private void printInternalNodes(Node<T> root, PrintWriter out) {
		if (this != root && edges.size() > 0) {
			out.println("\t" + nodeId(this) + " [label=\"" + data + "\",style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");
		}

		for (Edge<T> edge : edges.values()) {
			edge.getDest().printInternalNodes(root, out);
		}
	}

	private void printEdges(PrintWriter out) {
		for (Edge<T> edge : edges.values()) {
			Node<T> child = edge.getDest();
			out.println("\t" + nodeId(this) + " -> " + nodeId(child) + " [label=\"" + edge.commit() + "\",weight=10]");
			child.printEdges(out);
		}
	}

	private void printSLinks(PrintWriter out) {
		if (suffix != null) {
			out.println("\t" + nodeId(this) + " -> " + nodeId(suffix) + " [label=\"\",weight=0,style=dotted]");
		}
		for (Edge<T> edge : edges.values()) {
			edge.getDest().printSLinks(out);
		}
	}

	private static <T> String nodeId(Node<T> node) {
		return "node" + Integer.toHexString(node.hashCode()).toUpperCase();
	}
}

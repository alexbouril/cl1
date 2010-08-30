package uk.ac.rhul.cs.cl1.seeding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import uk.ac.rhul.cs.cl1.ClusterGrowthProcess;
import uk.ac.rhul.cs.cl1.MutableNodeSet;
import uk.ac.rhul.cs.graph.Graph;
import uk.ac.rhul.cs.graph.GraphAlgorithm;
import uk.ac.rhul.cs.utils.StringUtils;

/**
 * Abstract seed nodeset generator class.
 * 
 * A seed nodeset generator is an abstract algorithm that produces a set of candidate
 * seed nodesets from a graph. These candidate nodesets will be passed on to a
 * {@link ClusterGrowthProcess} during a {@link ClusterONEAlgorithm} to produce
 * the clusters.
 * 
 * Seed nodeset generators implement the {@link Iterable} interface, so the easiest
 * way to get the set of seed nodesets is to iterate over it in a for loop.
 *  
 * @author tamas
 */
public abstract class SeedGenerator extends GraphAlgorithm
implements Iterable<MutableNodeSet>, Serializable {
	/**
	 * Constructs a seed generator that is not associated to any graph yet.
	 */
	public SeedGenerator() {
		this(null);
	}

	/**
	 * Constructs a seed generator that is associated to a given graph.
	 * @param graph   the graph the seed generator will operate on
	 */
	public SeedGenerator(Graph graph) {
		super(graph);
	}
	
	/**
	 * Returns the number of seeds that will be generated.
	 * If the number of seeds cannot be known in advance, -1 will be returned.
	 * @return   the expected number of seeds generated by the generator, or -1 if the
	 *           number of seeds is not known in advance.
	 */
	public abstract int size();
	
	/**
	 * Returns an iterator that will generate seeds
	 */
	public abstract SeedIterator iterator();
	
	/**
	 * Factory method that can construct seed generators from a simple string description.
	 * The following specifiers are recognised at the moment:
	 * <ul>
	 * <li><tt>nodes</tt> - generates a singleton seed for each node of the graph</li>
	 * <li><tt>file(<i>filename.txt</i>)</tt> - opens <tt>filename.txt</tt> and interprets
	 *     each line as a seed set. Lines in the file must contain node names separated by
	 *     spaces.</li> 
	 * <li><tt>unused_nodes</tt> - generates a singleton seed for each node of the graph
	 *     if it wasn't found so far as part of a cluster</li>
	 * <li><tt>edges</tt> - generates a seed containing the two endpoints for each edge of the graph</li>
	 * </ul>
	 * 
	 * @param  specification   the specification string
	 * @param  graph           the graph used by the constructed seed generator
	 * @throws InstantiationException if the specification string is invalid or some error
	 *                                occurred (e.g., file not found for a file based seed
	 *                                generator)
	 */
	public static SeedGenerator fromString(String specification, Graph graph) throws InstantiationException {
		if (specification.equals("nodes"))
			return new EveryNodeSeedGenerator(graph);
		
		if (specification.equals("unused_nodes"))
			return new UnusedNodesSeedGenerator(graph);
		
		if (specification.equals("edges"))
			return new EveryEdgeSeedGenerator(graph);
		
		if (specification.equals("stdin"))
			return new StreamBasedSeedGenerator(graph, System.in);
		if (specification.startsWith("file(") && specification.endsWith(")")) {
			String filename = StringUtils.substring(specification, 5, -1);
			try {
				return new FileBasedSeedGenerator(graph, filename);
			} catch (FileNotFoundException ex) {
				throw new InstantiationException("file not found: "+filename);
			} catch (IOException ex) {
				throw new InstantiationException("IO error while reading file: "+filename);
			}
		}
		throw new InstantiationException("unknown seed generator type: "+specification);
	}
	
	/**
	 * Factory method that can construct seed generators from a simple string description.
	 * 
	 * The constructed seed generator will not be associated to any given graph yet.
	 * 
	 * @param  specification   the specification string
	 * @throws InstantiationException if the specification string is invalid
	 */
	public static SeedGenerator fromString(String specification) throws InstantiationException {
		return fromString(specification, null);
	}
}

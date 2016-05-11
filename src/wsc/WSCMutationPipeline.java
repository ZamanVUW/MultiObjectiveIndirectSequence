package wsc;

import java.util.Arrays;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

public class WSCMutationPipeline extends BreedingPipeline {

	private static final long serialVersionUID = 1L;

	@Override
	public Parameter defaultBase() {
		return new Parameter("wscmutationpipeline");
	}

	@Override
	public int numSources() {
		return 1;
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {

		int n = sources[0].produce(min, max, start, subpopulation, inds, state, thread);

        if (!(sources[0] instanceof BreedingPipeline)) {
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());
        }

        if (!(inds[start] instanceof SequenceVectorIndividual))
            // uh oh, wrong kind of individual
            state.output.fatal("WSCMutationPipeline didn't get a SequenceVectorIndividual. The offending individual is in subpopulation "
            + subpopulation + " and it's:" + inds[start]);

        WSCInitializer init = (WSCInitializer) state.initializer;

        // Perform mutation
        for(int q=start;q<n+start;q++) {
        	SequenceVectorIndividual tree = (SequenceVectorIndividual)inds[q];

        	double bestFitness = tree.fitness.fitness();
        	Service[] bestNeighbour = tree.genome;

        	Service[] neighbour = null;

        	for (int i = 0; i < tree.genome.length; i++) {
        		for (int j = i + 1; j < tree.genome.length; j++) {
        			neighbour = Arrays.copyOf(tree.genome, tree.genome.length);
        			swapServices(neighbour, i, j);

        			// Calculate fitness, and update the best neighbour if necessary
        			tree.calculateSequenceFitness(init.numLayers, init.endServ, neighbour, init, state, true);
        			if (tree.fitness.fitness() > bestFitness)
        				bestNeighbour = Arrays.copyOf(neighbour, tree.genome.length);
        		}
        	}
            // Update the tree to contain the best genome found
        	tree.genome = bestNeighbour;
            tree.evaluated=false;
        }
        return n;
	}

	private void swapServices(Service[] genome, int indexA, int indexB) {
		Service temp = genome[indexA];
		genome[indexA] = genome[indexB];
		genome[indexB] = temp;
	}

}

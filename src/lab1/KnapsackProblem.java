package lab1;

import org.jgap.*;
import org.jgap.impl.*;

public class KnapsackProblem {
    public static void main(String[] args) throws Exception {
        int n = 10;
        int capacity = 30;

        int[] volumes = new int[n];
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            volumes[i] = i + 1;
            values[i] = (i + 1) * (i + 1);
        }

        Configuration conf = new DefaultConfiguration();
        conf.setPreservFittestIndividual(true);

        FitnessFunction myFunc = new KnapsackFitnessFunction(volumes, values, capacity);
        conf.setFitnessFunction(myFunc);

        Gene[] sampleGenes = new Gene[n];
        for (int i = 0; i < n; i++) {
            sampleGenes[i] = new BooleanGene(conf);
        }

        IChromosome sampleChromosome = new Chromosome(conf, sampleGenes);
        conf.setSampleChromosome(sampleChromosome);
        conf.setPopulationSize(100);

        Genotype population = Genotype.randomInitialGenotype(conf);

        for (int gen = 0; gen < 100; gen++) {
            population.evolve();
        }

        IChromosome best = population.getFittestChromosome();
        System.out.println("Best solution found:");
        printSolution(best, volumes, values, capacity);
    }

    static void printSolution(IChromosome chrom, int[] volumes, int[] values, int capacity) {
        int totalVol = 0;
        int totalVal = 0;
        System.out.print("Items taken: ");
        for (int i = 0; i < chrom.size(); i++) {
            BooleanGene gene = (BooleanGene) chrom.getGene(i);
            if (gene.booleanValue()) {
                System.out.print((i + 1) + " ");
                totalVol += volumes[i];
                totalVal += values[i];
            }
        }
        System.out.println("\nTotal volume: " + totalVol + " / " + capacity);
        System.out.println("Total value: " + totalVal);
    }
}


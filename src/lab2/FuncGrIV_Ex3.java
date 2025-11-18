package lab2;

import org.jgap.*;
import org.jgap.impl.*;
import java.util.*;

public class FuncGrIV_Ex3 {
    // număr mai mic de gene = precizie mai slabă => mai ușor de observat comportamentul
    private static final int NR_GENES = 12;
    private static final int POP_SIZE = 10; // populație redusă
    private static final int MAX_EVOLUTIONS = 50;

    public static void main(String[] args) throws InvalidConfigurationException {
        Configuration conf = new DefaultConfiguration();
        Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
        conf.setFitnessEvaluator(new DeltaFitnessEvaluator()); // fitness mic = mai bun
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(true);
        conf.setFitnessFunction(new FitnessFunctionGrIV());

        // configurare cromozom
        IChromosome sampleChromosome = new Chromosome(conf, new BooleanGene(conf), NR_GENES);
        conf.setSampleChromosome(sampleChromosome);
        conf.setPopulationSize(POP_SIZE);

        // ==============================
        //   EXERCIȚIUL 3 – populație inițială în jurul minimului local (~x=-1)
        // ==============================

        Population population = new Population(conf);

        // generăm valori în jur de x ≈ -1.0 ± 0.05
        Random rnd = new Random();
        for (int i = 0; i < POP_SIZE; i++) {
            double val = -1.0 + (rnd.nextDouble() - 0.5) * 0.1; // interval [-1.05, -0.95]
            IChromosome chr = createChromosomeForValue(conf, val);
            population.addChromosome(chr);
        }

        // creăm populația controlată
        Genotype genotype = new Genotype(conf, population);

        // urmărim momentul când algoritmul "scapă" din minimul local
        boolean escaped = false;
        int generationEscaped = -1;

        for (int i = 0; i < MAX_EVOLUTIONS; i++) {
            genotype.evolve();
            IChromosome best = genotype.getFittestChromosome();
            double x = FitnessFunctionGrIV.Mapping(best);
            double fx = Math.pow(x, 4) - 2 * Math.pow(x, 2) - x;

            System.out.printf("Generatia %2d | x=%.6f | f(x)=%.6f | fitness=%.4f%n",
                    i, x, fx, best.getFitnessValue());

            // dacă x a trecut de 0 => a scăpat de minimul local (~x=-1)
            if (!escaped && x > 0) {
                escaped = true;
                generationEscaped = i;
                System.out.printf(" Algoritmul a ieșit din minimul local la generația %d%n", i);
            }
        }

        if (escaped) {
            System.out.printf(" A scăpat din minimul local la generația %d și a mers spre minimul global!%n", generationEscaped);
        } else {
            System.out.println(" Algoritmul NU a reușit să iasă din minimul local în 50 de generații.");
        }
    }

    // =========================
    // Helper: creează un cromozom care corespunde valorii x date
    // =========================
    private static IChromosome createChromosomeForValue(Configuration conf, double x)
            throws InvalidConfigurationException {
        Gene[] genes = new Gene[NR_GENES];
        double min = -2.0, max = 2.0;

        long maxInt = (1L << NR_GENES) - 1;
        long code = Math.round((x - min) / (max - min) * maxInt);
        for (int i = 0; i < NR_GENES; i++) {
            long mask = 1L << i;
            boolean bit = (code & mask) != 0;
            genes[i] = new BooleanGene(conf);
            genes[i].setAllele(bit);
        }
        return new Chromosome(conf, genes);
    }
}



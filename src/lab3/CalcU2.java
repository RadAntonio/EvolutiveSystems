package lab3;

import org.jgap.*;
import org.jgap.impl.*;

/**
 * EX.2: Zgomot uniform în [-0.2, 0.2] care acționează pe starea x1.
 * Pentru consistență, evaluăm fitness-ul ca media pe mai multe simulări (Monte Carlo),
 * altfel aceeași soluție ar primi scoruri diferite dacă "pică" zgomote diferite.
 */
public class CalcU2 {
    public static void main(String[] args) throws InvalidConfigurationException {
        Configuration conf = new DefaultConfiguration();
        Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
        conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(true);

        conf.setFitnessFunction(new SSTFitnessNoise(10)); // 10 rulări / individ

        int nrGenes = 13;
        IChromosome sample = new Chromosome(conf, new DoubleGene(conf, -2, 2), nrGenes);
        conf.setSampleChromosome(sample);
        conf.setPopulationSize(50);

        Genotype pop = Genotype.randomInitialGenotype(conf);
        for (int gen = 0; gen < 200; gen++) {
            pop.evolve();
            System.out.println("Gen " + gen + " | best = " +
                    pop.getFittestChromosome().getFitnessValue());
        }
    }
}

/** Fitness cu zgomot pe x1 și mediere pe N rulari */
class SSTFitnessNoise extends FitnessFunction {
    private static final int H = 13;
    private static final double[] yref = SSTFitness.yref;
    private final int runs;

    public SSTFitnessNoise(int runs) { this.runs = Math.max(1, runs); }

    @Override
    protected double evaluate(IChromosome chr) {
        double[] u = SSTFitness.Mapping(chr);
        double total = 0.0;
        for (int r = 0; r < runs; r++) total += errorOnce(u);
        return total / runs; // medie pe rulări
    }

    private double errorOnce(double[] u) {
        double x1 = 0.0, x2 = 0.0, err = 0.0;
        for (int k = 0; k < H; k++) {
            double y = 0.3 * x1 - 0.7 * x2;
            err += Math.abs(y - yref[k]);

            // zgomot uniform în [-0.2, 0.2]
            double noise = -0.2 + Math.random() * 0.4;

            double x1n = 0.8 * x1 + 0.1 * x2 + 5.0 * u[k] + noise;
            double x2n = 0.2 * x1 + 0.7 * x2 + 3.0 * u[k];
            x1 = x1n; x2 = x2n;
        }
        return err;
    }
}
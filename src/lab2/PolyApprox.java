package lab2;

import org.jgap.*;
import org.jgap.impl.*;

public class PolyApprox {
    public static final int DEG = 5;             // gradul polinomului
    public static final int BITS_PER_COEF = 11;  // biți pentru fiecare coeficient
    public static final int GENOME_SIZE = (DEG + 1) * BITS_PER_COEF; // 6 * 11 = 66
    public static final int POP_SIZE = 200;
    public static final int GENERATIONS = 100;

    public static void main(String[] args) throws Exception {
        Configuration conf = new DefaultConfiguration();
        Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);

        conf.setFitnessEvaluator(new DeltaFitnessEvaluator()); // fitness mic = mai bun
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(true);
        conf.setFitnessFunction(new PolyFitness());

        //  Structura cromozomului: 66 gene BooleanGene
        Gene[] genes = new Gene[GENOME_SIZE];
        for (int i = 0; i < GENOME_SIZE; i++)
            genes[i] = new BooleanGene(conf);

        conf.setSampleChromosome(new Chromosome(conf, genes));
        conf.setPopulationSize(POP_SIZE);

        Genotype pop = Genotype.randomInitialGenotype(conf);

        System.out.println("=== Evoluția polinomului p(x) ≈ sin(x) ===");
        for (int gen = 0; gen < GENERATIONS; gen++) {
            pop.evolve();
            IChromosome best = pop.getFittestChromosome();
            double fitness = best.getFitnessValue();
            System.out.printf("Generatia %3d | Fitness: %.6f%n", gen, fitness);
        }

        //  Afișăm coeficienții finali
        IChromosome best = pop.getFittestChromosome();
        double[] coef = PolyFitness.decodeCoefficients(best);
        System.out.println("\nCoeficienții polinomului final:");
        for (int i = 0; i < coef.length; i++)
            System.out.printf("a%d = %.4f%n", i, coef[i]);
    }
}

// ===========================
//  Funcția de fitness (evaluare performanță polinom)
// ===========================
class PolyFitness extends FitnessFunction {
    private static final int DEG = PolyApprox.DEG;
    private static final int BITS_PER_COEF = PolyApprox.BITS_PER_COEF;
    private static final double COEF_MIN = -10.0;
    private static final double COEF_MAX = 10.0;
    private static final int N_POINTS = 1000; // discretizare integrală
    private static final double POSITIVE_BIAS = 100000.0; // ca fitness-ul să fie pozitiv

    @Override
    protected double evaluate(IChromosome chr) {
        double[] a = decodeCoefficients(chr);
        double error = computeError(a);
        return error + POSITIVE_BIAS; // DeltaFitnessEvaluator -> fitness mic = mai bun
    }

    // Decodifică cromozomul în coeficienți reali [-10, 10]
    public static double[] decodeCoefficients(IChromosome chr) {
        double[] coef = new double[DEG + 1];
        for (int i = 0; i <= DEG; i++) {
            long val = 0;
            for (int b = 0; b < BITS_PER_COEF; b++) {
                Boolean bit = (Boolean) chr.getGene(i * BITS_PER_COEF + b).getAllele();
                if (bit != null && bit)
                    val |= (1L << (BITS_PER_COEF - 1 - b));
            }
            long maxInt = (1L << BITS_PER_COEF) - 1;
            double ratio = (double) val / (double) maxInt;
            coef[i] = COEF_MIN + ratio * (COEF_MAX - COEF_MIN);
        }
        return coef;
    }

    //  Metoda cerută în enunț — calculează valoarea polinomului pentru x0
    private double poly(double[] a, double x) {
        double result = 0;
        for (int i = DEG; i >= 0; i--) {
            result = result * x + a[i];
        }
        return result;
    }

    //  Calculează eroarea integrală aproximată prin sumă discretă
    private double computeError(double[] a) {
        double sum = 0;
        for (int k = 0; k < N_POINTS; k++) {
            double x = Math.PI * k / N_POINTS;
            double px = poly(a, x);
            double fx = Math.sin(x);
            sum += Math.abs(px - fx);
        }
        return sum; // cât mai mic -> mai bun
    }
}

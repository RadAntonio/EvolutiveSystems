package lab3;

import java.text.DecimalFormat;
import org.jgap.*;
import org.jgap.impl.*;

/**
 * EX.1: Calculează vectorul U = [u0..u12] care urmărește traiectoria y_ref.
 * DeltaFitnessEvaluator (fitness mai mic = mai bun),
 * păstrarea celui mai bun individ, populație constantă etc.
 */
public class CalcU {
    private static final int MAX_ALLOWED_EVOLUTIONS = 200;

    public static void main(String[] args) throws InvalidConfigurationException {
        Configuration conf = new DefaultConfiguration();

        // JGAP housekeeping: reset evaluator explicit
        Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);

        // Fitness mic = mai bun (DeltaFitnessEvaluator inversează comparația)
        conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(true);

        // Funcția de evaluare din EX.1 (fără zgomot)
        conf.setFitnessFunction(new SSTFitness());

        // Cromozom cu 13 gene reale în [-2, 2] (u(k) ∈ [-2,2])
        int nrGenes = 13;
        IChromosome sampleChromosome =
                new Chromosome(conf, new DoubleGene(conf, -2, 2), nrGenes);
        conf.setSampleChromosome(sampleChromosome);

        // Poți crește populația (ex. 200/500/1000) dacă vrei convergență mai rapidă
        conf.setPopulationSize(20);

        Genotype population = Genotype.randomInitialGenotype(conf);

        for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++) {
            population.evolve();
            IChromosome bestChrSoFar = population.getFittestChromosome();
            System.out.println(
                    "Gen " + i + " | Fitness: " + bestChrSoFar.getFitnessValue() + " | " +
                            "U*: " + SSTFitness.toString(bestChrSoFar));
        }

        IChromosome best = population.getFittestChromosome();
        System.out.println("\n== REZULTAT FINAL ==");
        System.out.println("Fitness: " + best.getFitnessValue());
        System.out.println("U*: " + SSTFitness.toString(best));
    }
}

/**
 * Fitness EX.1:
 * minimizează Σ |y(k) - y_ref(k)| pentru k=0..12 (orizont 13 momente).
 * ATENȚIE: actualizarea stărilor folosește valorile x(k) (nu x(k+1) parțial),
 * deci lucrăm cu variabile temporare (x1n, x2n) – e forma corectă a ecuațiilor.
 */
class SSTFitness extends FitnessFunction {
    // Traiectoria de referință (14 puncte), evaluăm pe k=0..12
    public static final double[] yref = { 0, 0, 2, 2, 1, 1, 1.5, 2, 2, 1, 1, 1, 0, 0 };
    private static final int H = 13;

    @Override
    protected double evaluate(IChromosome chr) {
        double[] u = Mapping(chr);
        double[] y = GetY(u);
        double errorSum = 0.0;
        for (int i = 0; i < H; i++) {
            errorSum += Math.abs(y[i] - yref[i]);
        }
        // DeltaFitnessEvaluator: valoare mai mică = mai bun
        return errorSum;
    }

    /** Extrage alelele DoubleGene ca vector de intrări u[0..12] */
    public static double[] Mapping(IChromosome chr) {
        double[] u = new double[H];
        for (int i = 0; i < chr.size(); i++) {
            u[i] = ((Double) chr.getGene(i).getAllele()).doubleValue();
        }
        return u;
    }

    /**
     * Simulează sistemul discret:
     * x(k+1) = A x(k) + B u(k)
     * y(k)   = C x(k) + D u(k)  (D=0 aici)
     * cu x(0) = [0,0]^T
     */
    public double[] GetY(double[] u) {
        double[] y = new double[H];
        double x1 = 0.0, x2 = 0.0;

        for (int k = 0; k < H; k++) {
            // y(k) din stările curente
            y[k] = 0.3 * x1 - 0.7 * x2;

            // calculează x(k+1) din x(k), u(k) — folosind variabile temporare
            double x1n = 0.8 * x1 + 0.1 * x2 + 5.0 * u[k];
            double x2n = 0.2 * x1 + 0.7 * x2 + 3.0 * u[k];
            x1 = x1n;
            x2 = x2n;
        }
        return y;
    }

    /** Afișare prietenoasă pentru vectorul U optim */
    public static String toString(IChromosome chr) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        double[] u = Mapping(chr);
        StringBuilder sb = new StringBuilder();
        for (double v : u) sb.append(' ').append(df2.format(v));
        return sb.toString();
    }
}
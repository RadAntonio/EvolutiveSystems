package lab2;

import org.jgap.*;
import org.jgap.impl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class FuncGrIV {
    private static final int MAX_ALLOWED_EVOLUTIONS = 10;

    public static void main(String[] args) throws InvalidConfigurationException, IOException {
        Configuration conf = new DefaultConfiguration();
        Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
        // low value for fitness = better  (DeltaFitnessEvaluator)
        conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(true);
        conf.setFitnessFunction(new FitnessFunctionGrIV());

        int nrGenes = 12;
        IChromosome sampleChromosome = new Chromosome(conf,
                new BooleanGene(conf), nrGenes);
        conf.setSampleChromosome(sampleChromosome);
        conf.setPopulationSize(20);

        Genotype population = Genotype.randomInitialGenotype(conf);

        // =============================
        //   EXERCIȚIUL 1 – Rularea de bază
        //   Observă precizia soluției și convergența algoritmului
        // =============================

        // Soluția teoretică de referință (din WolframAlpha): x* ≈ 1.10716
        double xExact = 1.10716;

        // Log pentru grafic (folosit și în Exercițiul 2)
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream("lab2_convergenta.csv"), StandardCharsets.UTF_8));
        writer.println("Generatie,x,f(x),Abatere,Durata_ms,Fitness");

        for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++) {

            //  Pornim cronometrul pentru Ex.2 (măsurare timp rulare)
            long start = System.nanoTime();

            population.evolve(); // evoluția unei generații

            // cel mai bun individ din populație
            IChromosome bestChrSoFar = population.getFittestChromosome();
            double individ = FitnessFunctionGrIV.Mapping(bestChrSoFar);

            // calculăm valoarea funcției
            double fx = Math.pow(individ, 4) - 2 * Math.pow(individ, 2) - individ;

            // =============================
            //   EXERCIȚIUL 2 – Calculul abaterii și timpului
            // =============================

            // abaterea față de soluția exactă
            double abatere = Math.abs(individ - xExact);

            // durată în milisecunde per generație
            long durata = (System.nanoTime() - start) / 1_000_000;

            // afișare în consolă
            System.out.printf("Generatia %d | x = %.6f | f(x) = %.6f | abatere = %.6f | durata = %d ms | fitness = %.4f%n",
                    i, individ, fx, abatere, durata, bestChrSoFar.getFitnessValue());

            // salvăm pentru grafic
            writer.printf("%d,%.8f,%.8f,%.8f,%d,%.6f%n",
                    i, individ, fx, abatere, durata, bestChrSoFar.getFitnessValue());
        }

        writer.close();
        System.out.println("\nDatele pentru grafic sunt salvate în 'lab2_convergenta.csv'");
    }
}

// ============================
// Clasa de fitness (identică cu cea din laborator)
// ============================
class FitnessFunctionGrIV extends FitnessFunction {
    private final double POSITIVE_BIAS = 5;

    @Override
    public double evaluate(IChromosome chr) {
        double individ = Mapping(chr);
        double fitness = Math.pow(individ, 4) - 2 * Math.pow(individ, 2) - individ;
        return fitness + POSITIVE_BIAS; // trebuie să fie pozitiv
    }

    public static double Mapping(IChromosome chr) {
        double base10 = 0;
        for (int i = 0; i < chr.size(); i++) {
            boolean allele = ((Boolean) chr.getGene(i).getAllele()).booleanValue();
            if (allele)
                base10 += Math.pow(2, i);
        }
        // mapare din cod binar în domeniul [-2, 2]
        double individ = base10 / (Math.pow(2, 12) - 1) * 4 - 2;
        return individ;
    }
}

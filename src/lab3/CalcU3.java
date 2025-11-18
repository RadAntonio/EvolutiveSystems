package lab3;

import java.text.DecimalFormat;
import org.jgap.*;
import org.jgap.impl.*;

/**
 * EX.3 (oprire adaptivă):
 * Oprește evoluția dacă, timp de 10 generații consecutive, NU apare o
 * îmbunătățire de ≥5% a celui mai bun fitness (mai mic = mai bun).
 *
 * Observație: în JGAP standard nu există Genotype#getGenerationNr(),
 * așa că numerotăm generațiile manual cu o variabilă locală.
 */
public class CalcU3 {
    public static void main(String[] args) throws InvalidConfigurationException {
        // Config JGAP
        Configuration conf = new DefaultConfiguration();
        Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);

        // Fitness mic = mai bun (conform cerinței din lab)
        conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(true);

        // Folosim aceeași funcție de fitness ca la Ex.1 (fără zgomot)
        conf.setFitnessFunction(new SSTFitness());

        // Cromozom cu 13 gene reale în [-2, 2] (u(k) ∈ [-2,2], k=0..12)
        IChromosome sample =
                new Chromosome(conf, new DoubleGene(conf, -2.0, 2.0), 13);
        conf.setSampleChromosome(sample);

        // Populație puțin mai mare ajută la ieșirea din minime locale
        conf.setPopulationSize(50);

        Genotype pop = Genotype.randomInitialGenotype(conf);

        // === criteriu de oprire adaptiv ===
        double best = Double.POSITIVE_INFINITY; // best fitness (mai mic = mai bun)
        int stagnant = 0;                       // câte generații la rând fără îmbunătățire ≥5%
        int generation = 0;                     // numerotare manuală a generațiilor

        while (true) {
            pop.evolve();

            double cur = pop.getFittestChromosome().getFitnessValue();

            // Îmbunătățire dacă scade cu ≥5% față de cel mai bun de până acum
            boolean improved = (cur <= best * 0.95);

            if (improved) {
                best = cur;
                stagnant = 0;
            } else {
                stagnant++;
            }

            System.out.println("Gen " + generation +
                    " | best = " + best +
                    " | stagnant = " + stagnant);

            // Condiția de oprire: 10 generații consecutive fără îmbunătățire ≥5%
            if (stagnant >= 10) {
                System.out.println("Stop: 10 generații fără îmbunătățire ≥5%.");
                break;
            }

            generation++;
        }

        IChromosome bestChr = pop.getFittestChromosome();
        System.out.println("\n== REZULTAT FINAL ==");
        System.out.println("Generații rulate (aprox.): " + (generation + 1));
        System.out.println("Fitness final: " + bestChr.getFitnessValue());
        System.out.println("U*: " + SSTFitness.toString(bestChr));
    }
}
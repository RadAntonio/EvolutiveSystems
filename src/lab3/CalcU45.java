package lab3;

import org.jgap.*;
import org.jgap.impl.*;

/**
 * EX.4–5: Identificare de sistem (model de ordin 2 în spațiul stărilor) prin GA.
 * - Gene: [a0,a1,a2,a3,b0,b1,c0,c1,d,x10,x20] ∈ [-10,10]
 * - Semnale de antrenare: treaptă și rampă (τ=20)
 * - Sistem "adevărat" (blackbox): sistemul din cap. 2 cu x1(0)=2, x2(0)=-2
 * - EX.5: după identificare, test periodic și aleator + observații
 */
public class CalcU45 {
    public static void main(String[] args) throws InvalidConfigurationException {
        Configuration conf = new DefaultConfiguration();
        Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
        conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(true);

        IdentifyFitness fit = new IdentifyFitness();
        conf.setFitnessFunction(fit);

        // 11 gene în [-10, 10]
        Gene[] genes = new Gene[11];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = new DoubleGene(conf, -10.0, 10.0);
        }
        IChromosome sample = new Chromosome(conf, genes);
        conf.setSampleChromosome(sample);
        conf.setPopulationSize(150);

        Genotype pop = Genotype.randomInitialGenotype(conf);

        for (int g = 0; g < 300; g++) {
            pop.evolve();
            if (g % 20 == 0) {
                System.out.println("Gen " + g + " | best = " +
                        pop.getFittestChromosome().getFitnessValue());
            }
        }

        IChromosome best = pop.getFittestChromosome();
        System.out.println("\n== MODEL IDENTIFICAT ==");
        System.out.println(IdentifyFitness.pretty(best));

        // EX.5: test periodic și aleator
        System.out.println("\n== TEST PERIODIC (sinus) ==");
        fit.testPeriodic(best);

        System.out.println("\n== TEST ALEATOR (white noise) ==");
        fit.testRandom(best);
    }
}

class IdentifyFitness extends FitnessFunction {
    private static final int H = 20;

    // Semnale de antrenare: treaptă și rampă
    private static final double[] uStep = new double[H];
    private static final double[] uRamp = new double[H];
    static {
        for (int k = 0; k < H; k++) {
            uStep[k] = (k <= 5) ? 0.0 : 1.0;
            uRamp[k] = 0.25 * k; // scale pentru a rămâne în interval
        }
    }

    @Override
    protected double evaluate(IChromosome chr) {
        // extrage genele
        double[] g = new double[11];
        for (int i = 0; i < 11; i++) g[i] = ((Double) chr.getGene(i).getAllele());

        // eroare totală MSE pe cele două scenarii (treaptă + rampă)
        double err = mseScenario(uStep, g) + mseScenario(uRamp, g);
        return err; // DeltaFitnessEvaluator => mai mic = mai bun
    }

    /** MSE între ieșirea modelului candidat și ieșirea sistemului "adevărat" */
    private double mseScenario(double[] u, double[] g) {
        double a0=g[0],a1=g[1],a2=g[2],a3=g[3],b0=g[4],b1=g[5],c0=g[6],c1=g[7],d=g[8];
        double x1=g[9], x2=g[10]; // stări inițiale candidate
        double e = 0.0;

        // stările inițiale ale sistemului adevărat (EX.4): x1(0)=2, x2(0)=-2
        double tx1 = 2.0, tx2 = -2.0;

        for (int k = 0; k < H; k++) {
            double y_est = c0 * x1 + c1 * x2 + d * u[k];
            double y_true = 0.3 * tx1 - 0.7 * tx2; // ieșirea "adevărată"

            e += Math.pow(y_est - y_true, 2);

            // evoluția modelului candidat
            double x1n = a0 * x1 + a1 * x2 + b0 * u[k];
            double x2n = a2 * x1 + a3 * x2 + b1 * u[k];
            x1 = x1n; x2 = x2n;

            // evoluția "adevărată" (sistemul din cap.2)
            double tx1n = 0.8 * tx1 + 0.1 * tx2 + 5.0 * u[k];
            double tx2n = 0.2 * tx1 + 0.7 * tx2 + 3.0 * u[k];
            tx1 = tx1n; tx2 = tx2n;
        }
        return e / H;
    }

    /** Afișare parametri model */
    public static String pretty(IChromosome chr) {
        String[] names = {"a0","a1","a2","a3","b0","b1","c0","c1","d","x10","x20"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            sb.append(names[i]).append('=')
                    .append(((Double) chr.getGene(i).getAllele())).append(' ');
        }
        return sb.toString();
    }

    /** EX.5: test periodic (sinus) al modelului identificat */
    public void testPeriodic(IChromosome chr) {
        double[] g = new double[11];
        for (int i = 0; i < 11; i++) g[i] = ((Double) chr.getGene(i).getAllele());
        double mse = mseOnCustomInput(g, k -> Math.sin(0.3 * k));
        System.out.println("MSE(sinus) = " + mse);
    }

    /** EX.5: test aleator (white noise) al modelului identificat */
    public void testRandom(IChromosome chr) {
        double[] g = new double[11];
        for (int i = 0; i < 11; i++) g[i] = ((Double) chr.getGene(i).getAllele());
        java.util.Random rng = new java.util.Random(42);
        double mse = mseOnCustomInput(g, k -> -0.5 + rng.nextDouble()); // ~U[-0.5,0.5]
        System.out.println("MSE(aleator) = " + mse);
    }

    // -------- utilitare --------
    private interface InputFn { double u(int k); }

    private double mseOnCustomInput(double[] g, InputFn uFun) {
        double a0=g[0],a1=g[1],a2=g[2],a3=g[3],b0=g[4],b1=g[5],c0=g[6],c1=g[7],d=g[8];
        double x1=g[9], x2=g[10];
        double tx1=2.0, tx2=-2.0; // sistemul "adevărat"
        double e=0.0;
        for (int k=0;k<H;k++){
            double uk = uFun.u(k);
            double y_est = c0*x1 + c1*x2 + d*uk;
            double y_true = 0.3*tx1 - 0.7*tx2;
            e += Math.pow(y_est - y_true, 2);

            double x1n=a0*x1 + a1*x2 + b0*uk;
            double x2n=a2*x1 + a3*x2 + b1*uk;
            x1=x1n; x2=x2n;

            double tx1n=0.8*tx1 + 0.1*tx2 + 5.0*uk;
            double tx2n=0.2*tx1 + 0.7*tx2 + 3.0*uk;
            tx1=tx1n; tx2=tx2n;
        }
        return e/H;
    }
}
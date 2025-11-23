/**
 * FireflyJGAP.java
 *
 * A minimal Firefly Algorithm implemented *inside the JGAP framework*.
 * Each firefly is stored as a JGAP Chromosome with two DoubleGenes (x and y).
 *
 * Problem: find facility location minimizing sum of distances to customers.
 *
 * Compile:
 *   javac -cp jgap.jar FireflyJGAP.java
 * Run:
 *   java  -cp .:jgap.jar FireflyJGAP
 */
package try2;
import org.jgap.*;
import org.jgap.impl.*;
import javax.swing.*;
import java.util.*;

public class FireflyJGAP {

    // Simple 2D point
    static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    // Customer locations (real-world example)
    static Point[] CLIENTS = {
            new Point(1.0, 4.0),   // tail top
            new Point(1.0, 3.0),   // tail bottom
            new Point(2.0, 2.5),   // bottom body curve
            new Point(2.5, 2.0),
            new Point(4.0, 2.0),   // belly
            new Point(5.5, 2.5),
            new Point(6.0, 3.0),   // head bottom
            new Point(6.0, 4.0),   // head center
            new Point(5.5, 5.0),   // head top
            new Point(4.0, 5.5),   // back curve
            new Point(2.5, 5.5),
            new Point(2.0, 5.0),   // top body curve
            new Point(1.0, 4.8),   // tail top curve
            new Point(1.0, 4.5),
            new Point(1.0, 4.2),
            new Point(1.2, 4.0),
            new Point(1.5, 3.8),
            new Point(2.0, 3.5),
            new Point(2.5, 3.2),
            new Point(3.0, 3.0),
            new Point(4.0, 2.8),
            new Point(5.0, 3.2),
            new Point(5.5, 3.5),
            new Point(5.8, 3.8),
            new Point(6.0, 4.0)    // back to head center



    };

    // Objective function: sum of distances - hypot - distanta euclidiana - sqrt(x^2+y^2) - minimize this
    public static double objective(double x, double y) {
        double s = 0;
        for (Point c : CLIENTS) {
            s += Math.hypot(x - c.x, y - c.y); //suma totala dintre spital si cartiere
        }
        return s;
    }

    // Firefly attractiveness
    private static double attractiveness(double beta0, double gamma, double r) {
        return beta0 * Math.exp(-gamma * r * r);
    }

    public static void main(String[] args) throws Exception {
        // --- GUI SETUP ---
        double[][] clientCoords = new double[CLIENTS.length][2];
        for (int i = 0; i < CLIENTS.length; i++) {
            clientCoords[i][0] = CLIENTS[i].x;
            clientCoords[i][1] = CLIENTS[i].y;
        }
        FireflyPanel panel = new FireflyPanel(clientCoords);
        JFrame frame = new JFrame("Firefly Algorithm Animation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

        /***********************
         * 1. JGAP CONFIG
         ***********************/
        Configuration.reset();
        Configuration conf = new DefaultConfiguration();
        conf.setPreservFittestIndividual(true);
        conf.setKeepPopulationSizeConstant(false);

        // Chromosome: [x, y] continuous genes
        // define the grid area where the solution will be found - city 8km x 8km
        Gene[] genes = new Gene[2];
        genes[0] = new DoubleGene(conf, 0.0, 8.0); // x
        genes[1] = new DoubleGene(conf, 0.0, 8.0); // y
        Chromosome sample = new Chromosome(conf, genes);
        conf.setSampleChromosome(sample);

        int POPSIZE = 20;
        conf.setPopulationSize(POPSIZE);

        // Dummy fitness — we override evolution manually
        //smaller distance → bigger fitness
        //bigger distance → smaller fitness
        conf.setFitnessFunction(new FitnessFunction() {
            protected double evaluate(IChromosome aChromosome) {
                double x = (Double) aChromosome.getGene(0).getAllele();
                double y = (Double) aChromosome.getGene(1).getAllele();
                return 1.0 / (objective(x, y) + 1e-9);
            }
        });

        /***********************
         * 2. INITIAL POPULATION
         ***********************/
        Genotype population = Genotype.randomInitialGenotype(conf);
        Population pop = population.getPopulation();

        /***********************
         * 3. FIREFLY PARAMETERS
         ***********************/
        double alpha = 0.2;      // random factor
        double beta0 = 1.0;      // attractiveness at r=0
        double gamma = 1.0;      // absorption
        int MAXGEN = 1000;

        java.util.Random rnd = new java.util.Random(42);

        Chromosome best = null;

        /***********************
         * 4. FIREFLY EVOLUTION LOOP
         ***********************/
        for (int gen = 1; gen <= MAXGEN; gen++) {

            // Evaluate all brightness
            for (IChromosome chr : pop.getChromosomes()) {
                double x = (Double) chr.getGene(0).getAllele();
                double y = (Double) chr.getGene(1).getAllele();
                chr.setFitnessValue(1.0 / (objective(x, y) + 1e-9));
            }

            // Sort by fitness (higher fitness = lower objective)
            pop.sortByFitness();

            // Update best
            IChromosome fittest = pop.getChromosome(0);
            if (best == null || fittest.getFitnessValue() > best.getFitnessValue()) {
                best = (Chromosome) fittest.clone();
            }

            // FIREFLY MOVEMENT LOGIC
            for (int i = 0; i < pop.size(); i++) {
                IChromosome fireflyI = pop.getChromosome(i);

                double xi = (Double) fireflyI.getGene(0).getAllele();
                double yi = (Double) fireflyI.getGene(1).getAllele();
                double bri = fireflyI.getFitnessValue();

                for (int j = 0; j < pop.size(); j++) {
                    IChromosome fireflyJ = pop.getChromosome(j);

                    double xj = (Double) fireflyJ.getGene(0).getAllele();
                    double yj = (Double) fireflyJ.getGene(1).getAllele();
                    double brj = fireflyJ.getFitnessValue();

                    // Move only toward brighter i->j
                    if (brj > bri) {
                        double dx = xi - xj;
                        double dy = yi - yj;
                        double r = Math.hypot(dx, dy);

                        double beta = attractiveness(beta0, gamma, r);

                        xi += beta * (xj - xi) + alpha * (rnd.nextDouble() - 0.5);
                        yi += beta * (yj - yi) + alpha * (rnd.nextDouble() - 0.5);

                        // clamp
                        xi = Math.max(0, Math.min(8, xi));
                        yi = Math.max(0, Math.min(8, yi));
                    }
                }

                // Update chromosome genes
                fireflyI.getGene(0).setAllele(xi);
                fireflyI.getGene(1).setAllele(yi);
            }
            // --- ADD THIS ---
            panel.updateFireflies(convertPopulation(pop));
            Thread.sleep(30);

            alpha *= 0.99; // gradually reduce randomness

            if (gen % 20 == 0 || gen == 1 || gen == MAXGEN) {
                double bx = (Double) best.getGene(0).getAllele();
                double by = (Double) best.getGene(1).getAllele();
                System.out.printf(
                        "Gen %3d best = (%.4f, %.4f)  objective = %.7f%n",
                        gen, bx, by, objective(bx, by)
                );
            }
        }


        /***********************
         * 5. FINAL RESULT
         ***********************/
        double bx = (Double) best.getGene(0).getAllele();
        double by = (Double) best.getGene(1).getAllele();

        System.out.println("\nFINAL BEST LOCATION:");
        System.out.printf("x = %.6f  y = %.6f%n", bx, by);
        System.out.printf("Total distance = %.7f%n", objective(bx, by));
    }
    public static java.util.List<FireflyPanel.Dot> convertPopulation(Population pop) {
        java.util.List<FireflyPanel.Dot> dots = new ArrayList<>();

        for (IChromosome chr : pop.getChromosomes()) {
            FireflyPanel.Dot d = new FireflyPanel.Dot();
            d.x = (Double) chr.getGene(0).getAllele();
            d.y = (Double) chr.getGene(1).getAllele();
            d.fitness = chr.getFitnessValue();
            dots.add(d);
        }
        return dots;
    }
}

package lab1;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

public class SampleFitnessFunction extends FitnessFunction {
    private final double targetAmount;

    public SampleFitnessFunction(double targetAmount) {
        this.targetAmount = targetAmount;
    }
    public double evaluate(IChromosome chromosome) {
        double changeAmount = amountOfChange(chromosome);
        int totalCoins = getTotalNumberOfCoins(chromosome);

        double changeDifference = Math.abs(targetAmount - changeAmount);
        double fitness = 300 * changeDifference * changeDifference;
        fitness += totalCoins > 1 ? totalCoins : 0;
        return fitness;
    }

    public static double amountOfChange(IChromosome chromosome) {
        int numQuarters = getNrCoinsAtGene(chromosome, 0);
        int numDimes = getNrCoinsAtGene(chromosome, 1);
        int numNickels = getNrCoinsAtGene(chromosome, 2);
        int numPennies = getNrCoinsAtGene(chromosome, 3);
        return (numQuarters * 0.25) + (numDimes * 0.1) + (numNickels * 0.05) + (numPennies * 0.01);
    }

    public static int getNrCoinsAtGene(IChromosome chromosome, int position) {
        Integer numCoins = (Integer)chromosome.getGene(position).getAllele();
        return numCoins.intValue();
    }
    public static int getTotalNumberOfCoins(IChromosome chromosome) {
        int totalCoins = 0;
        for (int i = 0; i < chromosome.size(); i++)
            totalCoins += getNrCoinsAtGene(chromosome, i);
        return totalCoins;
    }
}

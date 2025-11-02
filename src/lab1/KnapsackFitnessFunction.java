package lab1;

import org.jgap.*;
import org.jgap.impl.*;

public class KnapsackFitnessFunction extends FitnessFunction {
    private final int[] volumes;
    private final int[] values;
    private final int capacity;

    public KnapsackFitnessFunction(int[] volumes, int[] values, int capacity) {
        this.volumes = volumes;
        this.values = values;
        this.capacity = capacity;
    }

    @Override
    protected double evaluate(IChromosome chromosome) {
        int totalVolume = 0;
        int totalValue = 0;

        for (int i = 0; i < chromosome.size(); i++) {
            BooleanGene gene = (BooleanGene) chromosome.getGene(i);
            boolean selected = gene.booleanValue();

            if (selected) {
                totalVolume += volumes[i];
                totalValue += values[i];
            }
        }

        if (totalVolume <= capacity) {
            return totalValue;
        } else {
            int excess = totalVolume - capacity;
            double penalty = 1000 * excess;  // penalizare progresivă
            return Math.max(0, totalValue - penalty);
        }
    }
}

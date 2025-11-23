package try2;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FireflyPanel extends JPanel {
    public static class Dot {
        public double x, y;
        public double fitness;
    }


    private List<Dot> dots;

    // Constructor with optional clients
    private double[][] clients;

    public FireflyPanel(double[][] clients) {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.BLACK);
        this.clients = clients;
    }

    public FireflyPanel() {
        this(null); // no clients by default
    }

    public void updateFireflies(List<Dot> newDots) {
        this.dots = newDots;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // --- Draw clients first (red squares) ---
        if (clients != null) {
            g2.setColor(Color.RED);
            for (double[] c : clients) {
                int cx = (int) (c[0] / 8.0 * width);
                int cy = (int) (c[1] / 8.0 * height);
                g2.fillRect(cx - 4, cy - 4, 8, 8); // small 8x8 square
            }
        }

        if (dots == null || dots.size() == 0) return;

        // --- 1. Find best + worst firefly ---
        Dot best = dots.get(0);
        Dot worst = dots.get(0);
        for (Dot d : dots) {
            if (d.fitness > best.fitness) best = d;
            if (d.fitness < worst.fitness) worst = d;
        }

        // --- 2. Compute nearest neighbours ---
        Dot[] nearest = new Dot[dots.size()];
        for (int i = 0; i < dots.size(); i++) {
            Dot a = dots.get(i);
            Dot closest = null;
            double bestDist = Double.MAX_VALUE;

            for (int j = 0; j < dots.size(); j++) {
                if (i == j) continue;
                Dot b = dots.get(j);

                double dist = Math.hypot(a.x - b.x, a.y - b.y);
                if (dist < bestDist) {
                    bestDist = dist;
                    closest = b;
                }
            }
            nearest[i] = closest;
        }

        // --- 3. Draw neighbours first (cyan) ---
        g2.setColor(Color.CYAN);
        for (int i = 0; i < dots.size(); i++) {
            Dot n = nearest[i];
            if (n == null) continue;

            int px = (int) (n.x / 8.0 * width);
            int py = (int) (n.y / 8.0 * height);
            g2.fillOval(px - 5, py - 5, 10, 10);
        }

        // --- 4. Draw all fireflies in main color (yellow) ---
        g2.setColor(Color.YELLOW);
        for (Dot d : dots) {
            int px = (int) (d.x / 8.0 * width);
            int py = (int) (d.y / 8.0 * height);
            g2.fillOval(px - 4, py - 4, 8, 8);
        }

        // --- 5. Best firefly = green ---
        g2.setColor(Color.GREEN);
        int bx = (int) (best.x / 8.0 * width);
        int by = (int) (best.y / 8.0 * height);
        g2.fillOval(bx - 7, by - 7, 14, 14);

        // --- 6. Worst firefly = optional red ---
        g2.setColor(Color.MAGENTA); // optional different color
        int wx = (int) (worst.x / 8.0 * width);
        int wy = (int) (worst.y / 8.0 * height);
        g2.fillOval(wx - 6, wy - 6, 12, 12);
    }
}

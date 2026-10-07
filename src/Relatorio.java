import java.util.Locale;

public class Relatorio {
    public static void imprimir(Modelo modelo, Simulador sim) {
        for (Fila f : modelo.filas) {
            System.out.println("=========================================");
            System.out.println("Queue:   " + f.nome + " (" + f.notacao() + ")");
            if (f.temChegadaExterna)
                System.out.printf(Locale.US, "Arrival: %.1f ... %.1f%n", f.minChegada, f.maxChegada);
            System.out.printf(Locale.US, "Service: %.1f ... %.1f%n", f.minAtendimento, f.maxAtendimento);
            System.out.println("=========================================");
            System.out.println("   State           Time               Probability");
            double total = 0;
            for (double t : f.tempos) total += t;
            int ultimo = f.infinita() ? f.maxEstado : f.capacidade;
            for (int i = 0; i <= ultimo; i++) {
                double t = i < f.tempos.length ? f.tempos[i] : 0;
                double p = total > 0 ? t / total * 100 : 0;
                System.out.printf(Locale.US, "%8d %18.4f %17.2f%%%n", i, t, p);
            }
            System.out.println();
            System.out.println("Number of losses: " + f.perdas);
            System.out.println();
        }
        System.out.println("=========================================");
        System.out.printf(Locale.US, "Global simulation time: %.4f%n", sim.getRelogio());
        System.out.println("Random numbers used: " + sim.getGerador().getUsados());
    }
}

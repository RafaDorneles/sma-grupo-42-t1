import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fila {
    public final String nome;
    public int servidores = 1;
    public int capacidade = -1;    public boolean temChegadaExterna = false;
    public double minChegada, maxChegada, minAtendimento, maxAtendimento;
    public final List<Rota> rotas = new ArrayList<>();

    public int clientes = 0;
    public long perdas = 0;
    public double[] tempos;
    public int maxEstado = 0;

    public Fila(String nome) {
        this.nome = nome;
    }

    public boolean infinita() {
        return capacidade < 0;
    }

    public void inicializar() {
        tempos = new double[infinita() ? 16 : capacidade + 1];
        clientes = 0;
        perdas = 0;
        maxEstado = 0;
    }

    public boolean temEspaco() {
        return infinita() || clientes < capacidade;
    }

    public void acumular(double dt) {
        if (clientes >= tempos.length) tempos = Arrays.copyOf(tempos, Math.max(tempos.length * 2, clientes + 1));
        tempos[clientes] += dt;
        if (clientes > maxEstado) maxEstado = clientes;
    }

    public String notacao() {
        return "G/G/" + servidores + (infinita() ? "" : "/" + capacidade);
    }
}

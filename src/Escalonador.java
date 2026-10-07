import java.util.PriorityQueue;

public class Escalonador {
    private final PriorityQueue<Evento> eventos = new PriorityQueue<>((a, b) -> {
        int c = Double.compare(a.tempo, b.tempo);
        return c != 0 ? c : Long.compare(a.sequencia, b.sequencia);
    });
    private long sequencia = 0;

    public void agendar(double tempo, TipoEvento tipo, Fila fila) {
        eventos.add(new Evento(tempo, tipo, fila, sequencia++));
    }

    public Evento proximo() {
        return eventos.poll();
    }

    public boolean vazio() {
        return eventos.isEmpty();
    }
}

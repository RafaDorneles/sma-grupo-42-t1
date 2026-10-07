public class Evento {
    public final double tempo;
    public final TipoEvento tipo;
    public final Fila fila;
    public final long sequencia;

    public Evento(double tempo, TipoEvento tipo, Fila fila, long sequencia) {
        this.tempo = tempo;
        this.tipo = tipo;
        this.fila = fila;
        this.sequencia = sequencia;
    }
}

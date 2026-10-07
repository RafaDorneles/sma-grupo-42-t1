public class Simulador {
    private final Modelo modelo;
    private final Escalonador esc = new Escalonador();
    private final GeradorAleatorio rnd;
    private double relogio = 0;

    public Simulador(Modelo modelo) {
        this.modelo = modelo;
        this.rnd = modelo.gerador;
    }

    public double getRelogio() {
        return relogio;
    }

    public GeradorAleatorio getGerador() {
        return rnd;
    }

    public void executar() {
        for (Fila f : modelo.filas) f.inicializar();
        modelo.primeirasChegadas.forEach((f, t) -> esc.agendar(t, TipoEvento.CHEGADA, f));

        while (!esc.vazio() && !rnd.esgotado()) {
            Evento e = esc.proximo();
            double dt = e.tempo - relogio;
            for (Fila f : modelo.filas) f.acumular(dt);
            relogio = e.tempo;
            try {
                if (e.tipo == TipoEvento.CHEGADA) tratarChegada(e.fila);
                else tratarSaida(e.fila);
            } catch (FimDosAleatoriosException ex) {
                break;
            }
        }
    }

    private void tratarChegada(Fila f) {
        clienteEntra(f);
        esc.agendar(relogio + rnd.uniforme(f.minChegada, f.maxChegada), TipoEvento.CHEGADA, f);
    }

    private void clienteEntra(Fila f) {
        if (f.temEspaco()) {
            f.clientes++;
            if (f.clientes <= f.servidores) agendarSaida(f);
        } else {
            f.perdas++;
        }
    }

    private void agendarSaida(Fila f) {
        esc.agendar(relogio + rnd.uniforme(f.minAtendimento, f.maxAtendimento), TipoEvento.SAIDA, f);
    }

    private void tratarSaida(Fila f) {
        f.clientes--;
        if (f.clientes >= f.servidores) agendarSaida(f);
        Fila destino = sortearDestino(f);
        if (destino != null) clienteEntra(destino);
    }

    private Fila sortearDestino(Fila f) {
        if (f.rotas.isEmpty()) return null;
        if (f.rotas.size() == 1) return f.rotas.get(0).destino;
        double r = rnd.proximo();
        double acc = 0;
        for (Rota rota : f.rotas) {
            acc += rota.probabilidade;
            if (r < acc) return rota.destino;
        }
        return f.rotas.get(f.rotas.size() - 1).destino;
    }
}

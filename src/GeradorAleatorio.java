public class GeradorAleatorio {
    private static final long A = 1664525L;
    private static final long C = 1013904223L;
    private static final long M = 1L << 32;

    private long x;
    private final long limite;
    private long usados = 0;
    private final double[] fixos;

    public GeradorAleatorio(long semente, long limite) {
        this.x = semente;
        this.limite = limite;
        this.fixos = null;
    }

    public GeradorAleatorio(double[] fixos) {
        this.fixos = fixos;
        this.limite = fixos.length;
    }

    public double proximo() {
        if (usados >= limite) throw new FimDosAleatoriosException();
        double r;
        if (fixos != null) {
            r = fixos[(int) usados];
        } else {
            x = (A * x + C) % M;
            r = (double) x / M;
        }
        usados++;
        return r;
    }

    public double uniforme(double a, double b) {
        return a + (b - a) * proximo();
    }

    public boolean esgotado() {
        return usados >= limite;
    }

    public long getUsados() {
        return usados;
    }
}

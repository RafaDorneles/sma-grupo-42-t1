public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -cp out Main modelo.yml");
            System.exit(1);
        }
        try {
            Modelo modelo = Modelo.carregar(args[0]);
            Simulador sim = new Simulador(modelo);
            sim.executar();
            Relatorio.imprimir(modelo, sim);
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }
}

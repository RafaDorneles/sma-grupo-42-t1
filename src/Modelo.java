import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Modelo {
    public final List<Fila> filas = new ArrayList<>();
    public final Map<Fila, Double> primeirasChegadas = new LinkedHashMap<>();
    public GeradorAleatorio gerador;

    public static Modelo carregar(String caminho) throws IOException {
        return new Modelo(LeitorYaml.lerArquivo(caminho));
    }

    @SuppressWarnings("unchecked")
    private Modelo(Map<String, Object> raiz) {
        Map<String, Fila> porNome = new LinkedHashMap<>();

        Object q = raiz.get("queues");
        if (!(q instanceof Map)) throw new IllegalArgumentException("Campo obrigatorio ausente: 'queues'");
        Map<String, Object> arrivals = new LinkedHashMap<>();
        if (raiz.get("arrivals") instanceof Map) arrivals = (Map<String, Object>) raiz.get("arrivals");
        else if (raiz.get("arrivals") != null) throw new IllegalArgumentException("'arrivals' deve ser um mapa");

        for (Map.Entry<String, Object> e : ((Map<String, Object>) q).entrySet()) {
            if (!(e.getValue() instanceof Map))
                throw new IllegalArgumentException("Fila '" + e.getKey() + "' sem configuracao");
            Map<String, Object> c = (Map<String, Object>) e.getValue();
            Fila f = new Fila(e.getKey());
            f.servidores = c.containsKey("servers") ? (int) numero(c, "servers", f.nome) : 1;
            if (f.servidores < 1) throw new IllegalArgumentException("Fila '" + f.nome + "': servers deve ser >= 1");
            if (c.get("capacity") != null) {
                f.capacidade = (int) numero(c, "capacity", f.nome);
                if (f.capacidade < 1) throw new IllegalArgumentException("Fila '" + f.nome + "': capacity deve ser >= 1");
            }
            f.minAtendimento = numero(c, "minService", f.nome);
            f.maxAtendimento = numero(c, "maxService", f.nome);
            if (f.minAtendimento > f.maxAtendimento)
                throw new IllegalArgumentException("Fila '" + f.nome + "': minService > maxService");
            if (arrivals.containsKey(f.nome)) {
                f.temChegadaExterna = true;
                f.minChegada = numero(c, "minArrival", f.nome);
                f.maxChegada = numero(c, "maxArrival", f.nome);
                if (f.minChegada > f.maxChegada)
                    throw new IllegalArgumentException("Fila '" + f.nome + "': minArrival > maxArrival");
            }
            filas.add(f);
            porNome.put(f.nome, f);
        }

        for (Map.Entry<String, Object> e : arrivals.entrySet()) {
            Fila f = porNome.get(e.getKey());
            if (f == null) throw new IllegalArgumentException("'arrivals' refere fila desconhecida: " + e.getKey());
            primeirasChegadas.put(f, parseDouble(e.getValue(), "arrivals." + e.getKey()));
        }

        Object net = raiz.get("network");
        if (net != null) {
            if (!(net instanceof List)) throw new IllegalArgumentException("'network' deve ser uma lista");
            for (Object o : (List<Object>) net) {
                if (!(o instanceof Map)) throw new IllegalArgumentException("Item de 'network' invalido");
                Map<String, Object> r = (Map<String, Object>) o;
                Fila origem = porNome.get(String.valueOf(obrigatorio(r, "source", "network")));
                if (origem == null) throw new IllegalArgumentException("network: fila de origem desconhecida: " + r.get("source"));
                String alvo = String.valueOf(obrigatorio(r, "target", "network"));
                Fila destino = null;
                if (!alvo.equalsIgnoreCase("OUT")) {
                    destino = porNome.get(alvo);
                    if (destino == null) throw new IllegalArgumentException("network: fila de destino desconhecida: " + alvo);
                }
                origem.rotas.add(new Rota(destino, parseDouble(obrigatorio(r, "probability", "network"), "probability")));
            }
        }
        for (Fila f : filas) {
            double soma = 0;
            for (Rota r : f.rotas) {
                if (r.probabilidade < 0) throw new IllegalArgumentException("Fila '" + f.nome + "': probabilidade negativa");
                soma += r.probabilidade;
            }
            if (soma > 1.0 + 1e-9)
                throw new IllegalArgumentException("Fila '" + f.nome + "': soma das probabilidades de saida > 1 (" + soma + ")");
            if (f.rotas.size() > 1 && soma < 1.0 - 1e-9) f.rotas.add(new Rota(null, 1.0 - soma));        }

        if (raiz.get("rndnumbers") instanceof List) {
            List<Object> l = (List<Object>) raiz.get("rndnumbers");
            double[] v = new double[l.size()];
            for (int i = 0; i < v.length; i++) v[i] = parseDouble(l.get(i), "rndnumbers");
            gerador = new GeradorAleatorio(v);
        } else {
            long semente = raiz.containsKey("seed") ? (long) parseDouble(raiz.get("seed"), "seed") : 12345L;
            long n = (long) parseDouble(obrigatorio(raiz, "rndnumbersPerSeed", "raiz"), "rndnumbersPerSeed");
            gerador = new GeradorAleatorio(semente, n);
        }
        if (primeirasChegadas.isEmpty())
            throw new IllegalArgumentException("Nenhuma chegada externa definida em 'arrivals'");
    }

    private static Object obrigatorio(Map<String, Object> m, String chave, String onde) {
        Object v = m.get(chave);
        if (v == null) throw new IllegalArgumentException("Campo obrigatorio ausente '" + chave + "' em " + onde);
        return v;
    }

    private static double numero(Map<String, Object> m, String chave, String fila) {
        return parseDouble(obrigatorio(m, chave, "fila '" + fila + "'"), chave + " da fila '" + fila + "'");
    }

    private static double parseDouble(Object v, String campo) {
        try {
            return Double.parseDouble(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numerico invalido em " + campo + ": " + v);
        }
    }
}

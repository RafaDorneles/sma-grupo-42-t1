import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LeitorYaml {
    private static class Linha {
        final int indent;
        final String texto;
        Linha(int indent, String texto) { this.indent = indent; this.texto = texto; }
    }

    private final List<Linha> linhas = new ArrayList<>();
    private int pos = 0;

    @SuppressWarnings("unchecked")
    public static Map<String, Object> lerArquivo(String caminho) throws IOException {
        String conteudo = new String(Files.readAllBytes(Paths.get(caminho)), StandardCharsets.UTF_8);
        Object r = new LeitorYaml().parse(conteudo);
        if (!(r instanceof Map)) throw new IllegalArgumentException("YAML invalido: raiz deve ser um mapa");
        return (Map<String, Object>) r;
    }

    public Object parse(String conteudo) {
        for (String bruta : conteudo.split("\r?\n")) {
            String s = semComentario(bruta.replace("\t", "  "));
            if (s.trim().isEmpty()) continue;
            int ind = 0;
            while (ind < s.length() && s.charAt(ind) == ' ') ind++;
            linhas.add(new Linha(ind, s.trim()));
        }
        if (linhas.isEmpty()) return new LinkedHashMap<String, Object>();
        return parseBloco(linhas.get(0).indent);
    }

    private static String semComentario(String s) {
        boolean aspas = false;
        char q = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (aspas) { if (c == q) aspas = false; }
            else if (c == '"' || c == '\'') { aspas = true; q = c; }
            else if (c == '#' && (i == 0 || Character.isWhitespace(s.charAt(i - 1)))) return s.substring(0, i);
        }
        return s;
    }

    private static boolean ehItem(Linha l) {
        return l.texto.equals("-") || l.texto.startsWith("- ");
    }

    private Object parseBloco(int indent) {
        return ehItem(linhas.get(pos)) ? parseLista(indent) : parseMapa(indent);
    }

    private Map<String, Object> parseMapa(int indent) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        while (pos < linhas.size()) {
            Linha l = linhas.get(pos);
            if (l.indent != indent || ehItem(l)) break;
            int dp = l.texto.indexOf(':');
            if (dp < 0) throw new IllegalArgumentException("YAML invalido, esperado 'chave: valor': " + l.texto);
            String chave = desaspar(l.texto.substring(0, dp).trim());
            String valor = l.texto.substring(dp + 1).trim();
            pos++;
            if (valor.isEmpty()) {
                if (pos < linhas.size()) {
                    Linha prox = linhas.get(pos);
                    if (prox.indent > indent || (prox.indent == indent && ehItem(prox))) {
                        mapa.put(chave, parseBloco(prox.indent));
                        continue;
                    }
                }
                mapa.put(chave, null);
            } else {
                mapa.put(chave, escalar(valor));
            }
        }
        return mapa;
    }

    private List<Object> parseLista(int indent) {
        List<Object> lista = new ArrayList<>();
        while (pos < linhas.size()) {
            Linha l = linhas.get(pos);
            if (l.indent != indent || !ehItem(l)) break;
            String resto = l.texto.substring(1).trim();
            int offset = l.texto.length() - resto.length();            if (resto.isEmpty()) {
                pos++;
                if (pos < linhas.size() && linhas.get(pos).indent > indent) lista.add(parseBloco(linhas.get(pos).indent));
                else lista.add(null);
            } else if (!resto.startsWith("[") && !resto.startsWith("\"") && !resto.startsWith("'") && resto.contains(":")) {
                int novoIndent = indent + offset;
                linhas.set(pos, new Linha(novoIndent, resto));
                lista.add(parseMapa(novoIndent));
            } else {
                pos++;
                lista.add(escalar(resto));
            }
        }
        return lista;
    }

    private Object escalar(String v) {
        if (v.startsWith("[") && v.endsWith("]")) {
            List<Object> l = new ArrayList<>();
            String interno = v.substring(1, v.length() - 1).trim();
            if (!interno.isEmpty())
                for (String p : interno.split(",")) l.add(desaspar(p.trim()));
            return l;
        }
        return desaspar(v);
    }

    private static String desaspar(String s) {
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))))
            return s.substring(1, s.length() - 1);
        return s;
    }
}

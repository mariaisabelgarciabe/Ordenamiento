package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import modelos.Documento;

public class DocumentosServicio {

    private static List<Documento> documentos = Collections.emptyList();
    private static String[] encabezados = new String[] { "#", "Primer Apellido", "Segundo Apellido", "Nombres",
            "Documento" };

    public static String[] getEncabezados() {
        return encabezados;
    }

    public static void cargar(String nombreArchivo) {
        try {
            var lineas = Files.lines(Paths.get(nombreArchivo));
            documentos = lineas
                    .skip(1)
                    .map(linea -> linea.split(";"))
                    .filter(partes -> partes.length >= 4)
                    .map(partes -> new Documento(
                            partes[0].trim(),
                            partes[1].trim(),
                            partes[2].trim(),
                            partes[3].trim()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            documentos = Collections.emptyList();
        }
    }

    public static void mostrar(JTable tbl) {

        var datos = IntStream.range(0, documentos.size())
                .mapToObj(i -> {
                    Documento documento = documentos.get(i);
                    return new String[] {
                            String.valueOf(i + 1),
                            documento.getApellido1(),
                            documento.getApellido2(),
                            documento.getNombre(),
                            documento.getDocumento()
                    };
                })
                .collect(Collectors.toList())
                .toArray(String[][]::new);

        var dtm = new DefaultTableModel(datos, encabezados);
        tbl.setModel(dtm);
    }

    private static boolean esMayor(Documento d1, Documento d2, int criterio) {
        if (criterio == 0) {
            return d1.getNombreCompleto().compareTo(d2.getNombreCompleto()) > 0 ||
                    (d1.getNombreCompleto().equals(d2.getNombreCompleto())
                            && d1.getDocumento().compareTo(d2.getDocumento()) > 0);
        }
        return d1.getDocumento().compareTo(d2.getDocumento()) > 0 ||
                (d1.getDocumento().equals(d2.getDocumento())
                        && d1.getNombreCompleto().compareTo(d2.getNombreCompleto()) > 0);
    }

    private static void intercambiar(int i, int j) {
        if (0 <= i && i < documentos.size() && 0 <= j && j < documentos.size()) {
            var aux = documentos.get(i);
            documentos.set(i, documentos.get(j));
            documentos.set(j, aux);
        }
    }

    public static void ordenarBurbuja(int criterio) {
        for (int i = 0; i < documentos.size() - 1; i++) {
            for (int j = i + 1; j < documentos.size(); j++) {
                if (esMayor(documentos.get(i), documentos.get(j), criterio)) {
                    intercambiar(i, j);
                }
            }
        }
    }

    public static void ordenarInsercion(int criterio) {
        for (int i = 1; i < documentos.size(); i++) {
            int j = i;
            while (j > 0 && esMayor(documentos.get(j - 1), documentos.get(j), criterio)) {
                intercambiar(j - 1, j);
                j--;
            }
        }
    }

    private static int getPivote(int inicio, int fin, int criterio) {
        int pivote = inicio;
        var documentoPivote = documentos.get(pivote);
        for (int i = inicio + 1; i <= fin; i++) {
            if (esMayor(documentoPivote, documentos.get(i), criterio)) {
                pivote++;
                if (i != pivote)
                    intercambiar(i, pivote);
            }
        }
        if (inicio != pivote)
            intercambiar(inicio, pivote);
        return pivote;
    }

    private static void ordenarRapido(int inicio, int fin, int criterio) {
        if (inicio >= fin) {
            return;
        } else {
            var pivote = getPivote(inicio, fin, criterio);
            ordenarRapido(inicio, pivote - 1, criterio);
            ordenarRapido(pivote + 1, fin, criterio);
        }
    }

    public static void ordenarRapido(int criterio) {
        ordenarRapido(0, documentos.size() - 1, criterio);
    }

    public static List<Documento> buscar(String textoBuscado) {
        String textoLower = textoBuscado.toLowerCase();
        return documentos.stream()
                .filter(d -> d.getNombreCompleto().toLowerCase().contains(textoLower) ||
                        d.getApellido1().toLowerCase().contains(textoLower) ||
                        d.getApellido2().toLowerCase().contains(textoLower) ||
                        d.getDocumento().toLowerCase().contains(textoLower))
                .collect(Collectors.toList());
    }

    public static void ordenarMezcla(int criterio) {
        if (documentos == null || documentos.isEmpty()) {
            return;
        }
        ordenarMezcla(0, documentos.size() - 1, criterio);
    }

    private static void ordenarMezcla(int inicio, int fin, int criterio) {
        if (inicio < fin) {
            int mitad = (inicio + fin) / 2;
            ordenarMezcla(inicio, mitad, criterio);
            ordenarMezcla(mitad + 1, fin, criterio);
            mezclar(inicio, mitad, fin, criterio);
        }
    }

    private static void mezclar(int inicio, int mitad, int fin, int criterio) {
        int n1 = mitad - inicio + 1;
        int n2 = fin - mitad;

        Documento[] izquierda = new Documento[n1];
        Documento[] derecha = new Documento[n2];

        for (int i = 0; i < n1; i++) {
            izquierda[i] = documentos.get(inicio + i);
        }
        for (int j = 0; j < n2; j++) {
            derecha[j] = documentos.get(mitad + 1 + j);
        }

        int i = 0, j = 0;
        int k = inicio;
        while (i < n1 && j < n2) {
            if (!esMayor(izquierda[i], derecha[j], criterio)) {
                documentos.set(k, izquierda[i]);
                i++;
            } else {
                documentos.set(k, derecha[j]);
                j++;
            }
            k++;
        }

        while (i < n1) {
            documentos.set(k, izquierda[i]);
            i++;
            k++;
        }

        while (j < n2) {
            documentos.set(k, derecha[j]);
            j++;
            k++;
        }
    }

}
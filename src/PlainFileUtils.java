import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades de lectura y escritura de archivos de texto plano en UTF-8,
 * compatibles con Java 8.
 *
 * @author Cristian
 * @version 1.0
 */
public final class PlainFileUtils {

    private static final Charset UTF_8 = Charset.forName(AppConstants.CHARSET_NAME);

    private PlainFileUtils() {
        // Utilidad estatica.
    }

    /**
     * Escribe todas las lineas de un archivo, reemplazando el contenido previo.
     *
     * @param fileName nombre del archivo relativo al directorio de trabajo
     * @param lines    lineas a persistir, sin salto de linea final en cada elemento
     * @throws IOException si ocurre un error de entrada o salida
     */
    public static void writeLines(String fileName, List<String> lines) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), UTF_8));
            for (int index = 0; index < lines.size(); index++) {
                writer.write(lines.get(index));
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Lee todas las lineas de un archivo de texto.
     *
     * @param file archivo a leer
     * @return lista de lineas, sin el salto de linea
     * @throws IOException si ocurre un error de entrada o salida
     */
    public static List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
            String currentLine = reader.readLine();
            while (currentLine != null) {
                lines.add(currentLine);
                currentLine = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return lines;
    }

    /**
     * Agrega una linea al final de un archivo de bitacora.
     *
     * @param fileName nombre del archivo
     * @param message  texto a registrar
     * @throws IOException si ocurre un error de entrada o salida
     */
    public static void appendLine(String fileName, String message) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), UTF_8));
            writer.write(message);
            writer.newLine();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

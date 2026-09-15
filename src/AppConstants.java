/**
 * Constantes de nombres de archivo, delimitadores y mensajes usados por el
 * generador de datos y por el procesador de reportes.
 *
 * @author Cristian
 * @version 1.0
 */
public final class AppConstants {

    /** Delimitador exigido por el enunciado. */
    public static final String DELIMITER = ";";

    /** Codificacion de los archivos planos. */
    public static final String CHARSET_NAME = "UTF-8";

    /** Archivo de catalogo de vendedores. */
    public static final String SALESMEN_INFO_FILE = "vendedores.txt";

    /** Archivo de catalogo de productos. */
    public static final String PRODUCTS_FILE = "productos.txt";

    /** Copia serializada del catalogo de productos (extra del enunciado). */
    public static final String PRODUCTS_SERIALIZED_FILE = "productos.ser";

    /** Prefijo de los archivos de ventas por vendedor. */
    public static final String SALES_FILE_PREFIX = "ventas_";

    /** Extension de los archivos planos de ventas. */
    public static final String SALES_FILE_EXTENSION = ".txt";

    /** Reporte CSV de recaudo por vendedor. */
    public static final String SALESMAN_REPORT_FILE = "reporte_vendedores.csv";

    /** Reporte CSV de productos ordenados por cantidad vendida. */
    public static final String PRODUCT_REPORT_FILE = "reporte_productos.csv";

    /** Bitacora de filas o archivos invalidos. */
    public static final String ERROR_LOG_FILE = "errores.log";

    private AppConstants() {
        // Clase de constantes: no se instancia.
    }
}

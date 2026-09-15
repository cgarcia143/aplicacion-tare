import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Primera clase con metodo {@code main} del proyecto. Al ejecutarse genera los
 * archivos planos pseudoaleatorios que sirven como entrada del procesador de
 * reportes. No solicita informacion al usuario.
 *
 * <p>
 * Metodos exigidos por el enunciado:
 * </p>
 * <ul>
 * <li>{@link #createSalesMenFile(int, String, long)}</li>
 * <li>{@link #createProductsFile(int)}</li>
 * <li>{@link #createSalesManInfoFile(int)}</li>
 * </ul>
 *
 * @author Cristian
 * @version 1.0
 */
public class GenerateInfoFiles {

    private static final Random RANDOM = new Random();

    private static final List<String> DOCUMENT_TYPES = Arrays.asList("CC", "CE", "PP");

    private static final List<String> FIRST_NAMES = Arrays.asList(
            "Cristian", "Andres", "Maria", "Camila", "Juan", "Laura", "Carlos", "Sofia",
            "Diego", "Valentina", "Felipe", "Daniela", "Sebastian", "Ana", "Miguel", "Carolina");

    private static final List<String> LAST_NAMES = Arrays.asList(
            "Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez", "Perez", "Sanchez",
            "Ramirez", "Torres", "Diaz", "Vargas", "Castro", "Moreno", "Rojas", "Herrera");

    private static final List<String> PRODUCT_NAMES = Arrays.asList(
            "Arroz premium 5kg", "Aceite vegetal 1L", "Leche entera 1L", "Cafe molido 500g",
            "Azucar blanca 1kg", "Harina de trigo 1kg", "Pasta spaghetti 500g", "Atun en lata",
            "Jabon liquido 750ml", "Papel higienico x12", "Detergente en polvo 2kg",
            "Galletas surtidas", "Jugo de naranja 1L", "Queso mozzarella 500g",
            "Pan tajado integral", "Huevos x30", "Sal refinada 1kg", "Chocolate de mesa",
            "Cereal de maiz 400g", "Agua embotellada 6L");

    /** Catalogo de productos generado en la ultima ejecucion. */
    private static final List<Product> GENERATED_PRODUCTS = new ArrayList<Product>();

    /** Catalogo de vendedores generado en la ultima ejecucion. */
    private static final List<Salesman> GENERATED_SALESMEN = new ArrayList<Salesman>();

    /**
     * Punto de entrada. Genera catalogos y archivos de ventas de prueba. Informa
     * exito o error por consola y no pide datos al usuario.
     *
     * @param args no se utilizan
     */
    public static void main(String[] args) {
        try {
            GENERATED_PRODUCTS.clear();
            GENERATED_SALESMEN.clear();
            deletePreviousGeneratedFiles();

            createProductsFile(12);
            createSalesManInfoFile(6);

            for (int index = 0; index < GENERATED_SALESMEN.size(); index++) {
                Salesman salesman = GENERATED_SALESMEN.get(index);
                int salesCount = 4 + RANDOM.nextInt(8);
                createSalesMenFile(salesCount, salesman.getFullName(), salesman.getDocumentNumber());
            }

            // Extra: mas de un archivo de ventas para los dos primeros vendedores.
            if (GENERATED_SALESMEN.size() >= 2) {
                Salesman firstSalesman = GENERATED_SALESMEN.get(0);
                createSalesMenFile(5, firstSalesman.getFullName() + "_extra", firstSalesman.getDocumentNumber());

                Salesman secondSalesman = GENERATED_SALESMEN.get(1);
                createSalesMenFile(3, secondSalesman.getFullName() + "_extra", secondSalesman.getDocumentNumber());
            }

            writeSerializedProducts();

            String successMessage = "Proceso de generacion de archivos finalizado exitosamente.";
            System.out.println(successMessage);
            GenerationView.display(true, successMessage);
        } catch (Exception exception) {
            String errorMessage = "Error al generar los archivos de entrada: " + exception.getMessage();
            System.out.println(errorMessage);
            GenerationView.display(false, errorMessage);
        }
    }

    /**
     * Crea un archivo plano de ventas de un vendedor. La primera linea contiene el
     * tipo y el numero de documento; las lineas siguientes tienen id de producto y
     * cantidad vendida.
     *
     * @param randomSalesCount cantidad de lineas de venta a generar
     * @param name             nombre usado en el nombre del archivo
     * @param id               numero de documento del vendedor
     * @throws IOException si no es posible escribir el archivo
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        if (randomSalesCount < 0) {
            throw new IllegalArgumentException("La cantidad de ventas no puede ser negativa.");
        }
        if (GENERATED_PRODUCTS.isEmpty()) {
            throw new IllegalStateException("Debe generarse primero el archivo de productos.");
        }

        String documentType = DOCUMENT_TYPES.get(RANDOM.nextInt(DOCUMENT_TYPES.size()));
        for (int index = 0; index < GENERATED_SALESMEN.size(); index++) {
            Salesman salesman = GENERATED_SALESMEN.get(index);
            if (salesman.getDocumentNumber() == id) {
                documentType = salesman.getDocumentType();
                break;
            }
        }

        List<String> lines = new ArrayList<String>();
        lines.add(documentType + AppConstants.DELIMITER + id);

        for (int saleIndex = 0; saleIndex < randomSalesCount; saleIndex++) {
            Product product = GENERATED_PRODUCTS.get(RANDOM.nextInt(GENERATED_PRODUCTS.size()));
            int quantity = 1 + RANDOM.nextInt(20);
            lines.add(product.getProductId() + AppConstants.DELIMITER + quantity + AppConstants.DELIMITER);
        }

        String safeName = sanitizeFileName(name);
        String fileName = AppConstants.SALES_FILE_PREFIX + safeName + "_" + id + AppConstants.SALES_FILE_EXTENSION;
        // Si el archivo ya existe (segundo archivo del mismo vendedor), se distingue.
        fileName = uniqueFileName(fileName);
        PlainFileUtils.writeLines(fileName, lines);
    }

    /**
     * Crea el archivo de informacion de productos con datos pseudoaleatorios.
     *
     * @param productsCount cantidad de productos a generar
     * @throws IOException si no es posible escribir el archivo
     */
    public static void createProductsFile(int productsCount) throws IOException {
        if (productsCount <= 0) {
            throw new IllegalArgumentException("La cantidad de productos debe ser mayor que cero.");
        }

        GENERATED_PRODUCTS.clear();
        List<String> lines = new ArrayList<String>();
        Set<String> usedNames = new HashSet<String>();

        for (int index = 1; index <= productsCount; index++) {
            String productId = "P" + index;
            String productName = pickUniqueProductName(usedNames, index);
            long unitPrice = 1500L + (RANDOM.nextInt(80) * 500L);
            Product product = new Product(productId, productName, unitPrice);
            GENERATED_PRODUCTS.add(product);
            lines.add(productId + AppConstants.DELIMITER + productName + AppConstants.DELIMITER + unitPrice);
        }

        PlainFileUtils.writeLines(AppConstants.PRODUCTS_FILE, lines);
    }

    /**
     * Crea el archivo de informacion de vendedores con nombres y apellidos reales
     * tomados de listas predefinidas. Los documentos son unicos.
     *
     * @param salesmanCount cantidad de vendedores a generar
     * @throws IOException si no es posible escribir el archivo
     */
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        if (salesmanCount <= 0) {
            throw new IllegalArgumentException("La cantidad de vendedores debe ser mayor que cero.");
        }

        GENERATED_SALESMEN.clear();
        List<String> lines = new ArrayList<String>();
        Set<Long> usedIds = new HashSet<Long>();

        for (int index = 0; index < salesmanCount; index++) {
            String documentType = DOCUMENT_TYPES.get(RANDOM.nextInt(DOCUMENT_TYPES.size()));
            long documentNumber = nextUniqueDocument(usedIds);
            String firstNames = FIRST_NAMES.get(RANDOM.nextInt(FIRST_NAMES.size()));
            String lastNames = LAST_NAMES.get(RANDOM.nextInt(LAST_NAMES.size())) + " "
                    + LAST_NAMES.get(RANDOM.nextInt(LAST_NAMES.size()));
            Salesman salesman = new Salesman(documentType, documentNumber, firstNames, lastNames);
            GENERATED_SALESMEN.add(salesman);
            lines.add(documentType + AppConstants.DELIMITER + documentNumber + AppConstants.DELIMITER
                    + firstNames + AppConstants.DELIMITER + lastNames);
        }

        PlainFileUtils.writeLines(AppConstants.SALESMEN_INFO_FILE, lines);
    }

    /**
     * Extra: persiste el catalogo de productos en un archivo serializado.
     *
     * @throws IOException si no es posible escribir el archivo
     */
    private static void writeSerializedProducts() throws IOException {
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(AppConstants.PRODUCTS_SERIALIZED_FILE));
            outputStream.writeObject(new ArrayList<Product>(GENERATED_PRODUCTS));
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private static String pickUniqueProductName(Set<String> usedNames, int fallbackIndex) {
        for (int attempt = 0; attempt < PRODUCT_NAMES.size() * 3; attempt++) {
            String candidate = PRODUCT_NAMES.get(RANDOM.nextInt(PRODUCT_NAMES.size()));
            if (!usedNames.contains(candidate)) {
                usedNames.add(candidate);
                return candidate;
            }
        }
        String fallbackName = "Producto generico " + fallbackIndex;
        usedNames.add(fallbackName);
        return fallbackName;
    }

    private static long nextUniqueDocument(Set<Long> usedIds) {
        long candidate;
        do {
            candidate = 10000000L + RANDOM.nextInt(90000000);
        } while (usedIds.contains(candidate));
        usedIds.add(candidate);
        return candidate;
    }

    /**
     * Elimina archivos de una ejecucion anterior para que no queden ventas de
     * vendedores que ya no estan en el catalogo.
     */
    private static void deletePreviousGeneratedFiles() {
        File workingDirectory = new File(".");
        File[] generatedFiles = workingDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File directory, String fileName) {
                boolean isSalesFile = fileName.startsWith(AppConstants.SALES_FILE_PREFIX)
                        && fileName.endsWith(AppConstants.SALES_FILE_EXTENSION);
                boolean isCatalog = fileName.equals(AppConstants.PRODUCTS_FILE)
                        || fileName.equals(AppConstants.SALESMEN_INFO_FILE)
                        || fileName.equals(AppConstants.PRODUCTS_SERIALIZED_FILE);
                boolean isReport = fileName.equals(AppConstants.SALESMAN_REPORT_FILE)
                        || fileName.equals(AppConstants.PRODUCT_REPORT_FILE)
                        || fileName.equals(AppConstants.ERROR_LOG_FILE);
                return isSalesFile || isCatalog || isReport;
            }
        });
        if (generatedFiles == null) {
            return;
        }
        for (int index = 0; index < generatedFiles.length; index++) {
            generatedFiles[index].delete();
        }
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "vendedor";
        }
        return name.trim().replaceAll("[^A-Za-z0-9_\\-]", "_");
    }

    private static String uniqueFileName(String desiredName) {
        File candidate = new File(desiredName);
        if (!candidate.exists()) {
            return desiredName;
        }
        int suffix = 2;
        String baseName = desiredName.substring(0, desiredName.length() - AppConstants.SALES_FILE_EXTENSION.length());
        String nextName = baseName + "_" + suffix + AppConstants.SALES_FILE_EXTENSION;
        while (new File(nextName).exists()) {
            suffix++;
            nextName = baseName + "_" + suffix + AppConstants.SALES_FILE_EXTENSION;
        }
        return nextName;
    }
}

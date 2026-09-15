import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orquesta la lectura de los archivos de entrada, la validacion y la escritura
 * de los dos reportes CSV exigidos por el enunciado.
 *
 * @author Cristian
 * @version 1.0
 */
public class SalesReportService {

    private final InputValidator inputValidator;

    /**
     * Crea el servicio de reportes.
     */
    public SalesReportService() {
        this.inputValidator = new InputValidator();
    }

    /**
     * Genera {@code reporte_vendedores.csv} y {@code reporte_productos.csv}.
     *
     * @throws IOException              si falla la lectura o escritura
     * @throws IllegalStateException    si faltan catalogos o estan vacios
     * @throws ClassNotFoundException   si el archivo serializado no es compatible
     */
    public void generateReports() throws IOException, ClassNotFoundException {
        File productsFile = new File(AppConstants.PRODUCTS_FILE);
        File salesmenFile = new File(AppConstants.SALESMEN_INFO_FILE);

        if (!productsFile.exists() || !salesmenFile.exists()) {
            throw new IllegalStateException(
                    "Faltan archivos de catalogo. Ejecute primero GenerateInfoFiles.");
        }

        List<String> errorLog = new ArrayList<String>();

        InputValidator.ProductCatalogResult productCatalog = inputValidator
                .validateProducts(PlainFileUtils.readLines(productsFile));
        errorLog.addAll(productCatalog.getErrors());

        Map<String, Product> productsById = productCatalog.getProductsById();
        productsById = mergeSerializedProductsIfPresent(productsById, errorLog);

        if (productsById.isEmpty()) {
            throw new IllegalStateException("El catalogo de productos quedo vacio despues de validar.");
        }

        InputValidator.SalesmanCatalogResult salesmanCatalog = inputValidator
                .validateSalesmen(PlainFileUtils.readLines(salesmenFile));
        errorLog.addAll(salesmanCatalog.getErrors());
        Map<String, Salesman> salesmenByIdentity = salesmanCatalog.getSalesmenByIdentity();

        if (salesmenByIdentity.isEmpty()) {
            throw new IllegalStateException("El catalogo de vendedores quedo vacio despues de validar.");
        }

        Map<String, Long> moneyBySalesman = initializeMoneyMap(salesmenByIdentity);
        Map<String, Long> quantityByProductId = initializeQuantityMap(productsById);

        File[] salesFiles = listSalesFiles();
        for (int index = 0; index < salesFiles.length; index++) {
            File salesFile = salesFiles[index];
            List<String> lines = PlainFileUtils.readLines(salesFile);
            InputValidator.SalesFileParseResult parseResult = inputValidator.validateSalesFile(
                    salesFile, lines, productsById, salesmenByIdentity, errorLog);

            mergeLongMaps(moneyBySalesman, parseResult.getMoneyBySalesman());
            mergeLongMaps(quantityByProductId, parseResult.getQuantityByProductId());
        }

        writeSalesmanReport(salesmenByIdentity, moneyBySalesman);
        writeProductReport(productsById, quantityByProductId);
        writeErrorLog(errorLog);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Product> mergeSerializedProductsIfPresent(Map<String, Product> productsById,
            List<String> errorLog) throws IOException, ClassNotFoundException {
        File serializedFile = new File(AppConstants.PRODUCTS_SERIALIZED_FILE);
        if (!serializedFile.exists()) {
            return productsById;
        }

        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(serializedFile));
            Object loadedObject = inputStream.readObject();
            if (!(loadedObject instanceof List)) {
                errorLog.add("productos.ser: contenido inesperado.");
                return productsById;
            }
            List<Product> serializedProducts = (List<Product>) loadedObject;
            for (int index = 0; index < serializedProducts.size(); index++) {
                Product product = serializedProducts.get(index);
                if (product == null || product.getProductId() == null) {
                    continue;
                }
                if (!productsById.containsKey(product.getProductId())) {
                    productsById.put(product.getProductId(), product);
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return productsById;
    }

    private Map<String, Long> initializeMoneyMap(Map<String, Salesman> salesmenByIdentity) {
        Map<String, Long> moneyBySalesman = new HashMap<String, Long>();
        for (String identity : salesmenByIdentity.keySet()) {
            moneyBySalesman.put(identity, Long.valueOf(0L));
        }
        return moneyBySalesman;
    }

    private Map<String, Long> initializeQuantityMap(Map<String, Product> productsById) {
        Map<String, Long> quantityByProductId = new HashMap<String, Long>();
        for (String productId : productsById.keySet()) {
            quantityByProductId.put(productId, Long.valueOf(0L));
        }
        return quantityByProductId;
    }

    private void mergeLongMaps(Map<String, Long> target, Map<String, Long> source) {
        for (Map.Entry<String, Long> entry : source.entrySet()) {
            Long previous = target.get(entry.getKey());
            if (previous == null) {
                target.put(entry.getKey(), entry.getValue());
            } else {
                target.put(entry.getKey(), Long.valueOf(previous.longValue() + entry.getValue().longValue()));
            }
        }
    }

    private File[] listSalesFiles() {
        File workingDirectory = new File(".");
        File[] salesFiles = workingDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File directory, String fileName) {
                return fileName.startsWith(AppConstants.SALES_FILE_PREFIX)
                        && fileName.endsWith(AppConstants.SALES_FILE_EXTENSION);
            }
        });
        if (salesFiles == null) {
            return new File[0];
        }
        return salesFiles;
    }

    private void writeSalesmanReport(Map<String, Salesman> salesmenByIdentity,
            Map<String, Long> moneyBySalesman) throws IOException {
        List<Salesman> orderedSalesmen = new ArrayList<Salesman>(salesmenByIdentity.values());
        Collections.sort(orderedSalesmen, new Comparator<Salesman>() {
            @Override
            public int compare(Salesman left, Salesman right) {
                long leftMoney = moneyBySalesman.get(left.getIdentityKey()).longValue();
                long rightMoney = moneyBySalesman.get(right.getIdentityKey()).longValue();
                if (leftMoney < rightMoney) {
                    return 1;
                }
                if (leftMoney > rightMoney) {
                    return -1;
                }
                return left.getFullName().compareToIgnoreCase(right.getFullName());
            }
        });

        List<String> reportLines = new ArrayList<String>();
        for (int index = 0; index < orderedSalesmen.size(); index++) {
            Salesman salesman = orderedSalesmen.get(index);
            long collectedMoney = moneyBySalesman.get(salesman.getIdentityKey()).longValue();
            reportLines.add(salesman.getFullName() + AppConstants.DELIMITER + collectedMoney);
        }
        PlainFileUtils.writeLines(AppConstants.SALESMAN_REPORT_FILE, reportLines);
    }

    private void writeProductReport(Map<String, Product> productsById,
            Map<String, Long> quantityByProductId) throws IOException {
        List<Product> orderedProducts = new ArrayList<Product>();
        for (Product product : productsById.values()) {
            Long quantity = quantityByProductId.get(product.getProductId());
            if (quantity != null && quantity.longValue() > 0) {
                orderedProducts.add(product);
            }
        }

        Collections.sort(orderedProducts, new Comparator<Product>() {
            @Override
            public int compare(Product left, Product right) {
                long leftQuantity = quantityByProductId.get(left.getProductId()).longValue();
                long rightQuantity = quantityByProductId.get(right.getProductId()).longValue();
                if (leftQuantity < rightQuantity) {
                    return 1;
                }
                if (leftQuantity > rightQuantity) {
                    return -1;
                }
                return left.getProductName().compareToIgnoreCase(right.getProductName());
            }
        });

        List<String> reportLines = new ArrayList<String>();
        for (int index = 0; index < orderedProducts.size(); index++) {
            Product product = orderedProducts.get(index);
            reportLines.add(product.getProductName() + AppConstants.DELIMITER + product.getUnitPrice());
        }
        PlainFileUtils.writeLines(AppConstants.PRODUCT_REPORT_FILE, reportLines);
    }

    private void writeErrorLog(List<String> errorLog) throws IOException {
        File previousLog = new File(AppConstants.ERROR_LOG_FILE);
        if (previousLog.exists() && !previousLog.delete()) {
            throw new IOException("No fue posible limpiar " + AppConstants.ERROR_LOG_FILE);
        }
        if (errorLog.isEmpty()) {
            return;
        }
        PlainFileUtils.writeLines(AppConstants.ERROR_LOG_FILE, errorLog);
    }
}

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extra del enunciado: detecta archivos con formato erróneo o información
 * incoherente (ids inexistentes, precios o cantidades negativas, columnas
 * incompletas).
 *
 * @author Cristian
 * @version 1.0
 */
public class InputValidator {

    /**
     * Resultado de validar y cargar un catalogo de productos.
     */
    public static class ProductCatalogResult {
        private final Map<String, Product> productsById;
        private final List<String> errors;

        ProductCatalogResult(Map<String, Product> productsById, List<String> errors) {
            this.productsById = productsById;
            this.errors = errors;
        }

        public Map<String, Product> getProductsById() {
            return productsById;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Resultado de validar y cargar un catalogo de vendedores.
     */
    public static class SalesmanCatalogResult {
        private final Map<String, Salesman> salesmenByIdentity;
        private final List<String> errors;

        SalesmanCatalogResult(Map<String, Salesman> salesmenByIdentity, List<String> errors) {
            this.salesmenByIdentity = salesmenByIdentity;
            this.errors = errors;
        }

        public Map<String, Salesman> getSalesmenByIdentity() {
            return salesmenByIdentity;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Valida el archivo de productos. Descarta filas con precio negativo, id vacio
     * o columnas incompletas.
     *
     * @param lines lineas del archivo de productos
     * @return catalogo valido y lista de errores
     */
    public ProductCatalogResult validateProducts(List<String> lines) {
        Map<String, Product> productsById = new HashMap<String, Product>();
        List<String> errors = new ArrayList<String>();

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String rawLine = lines.get(lineNumber);
            String trimmedLine = rawLine == null ? "" : rawLine.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            String[] columns = trimmedLine.split(AppConstants.DELIMITER, -1);
            if (columns.length < 3) {
                errors.add("productos.txt linea " + (lineNumber + 1) + ": formato incompleto.");
                continue;
            }

            String productId = columns[0].trim();
            String productName = columns[1].trim();
            String priceText = columns[2].trim();

            if (productId.isEmpty() || productName.isEmpty()) {
                errors.add("productos.txt linea " + (lineNumber + 1) + ": id o nombre vacio.");
                continue;
            }

            Long unitPrice = parseNonNegativeLong(priceText);
            if (unitPrice == null) {
                errors.add("productos.txt linea " + (lineNumber + 1) + ": precio invalido o negativo.");
                continue;
            }

            if (productsById.containsKey(productId)) {
                errors.add("productos.txt linea " + (lineNumber + 1) + ": id duplicado " + productId + ".");
                continue;
            }

            productsById.put(productId, new Product(productId, productName, unitPrice.longValue()));
        }

        return new ProductCatalogResult(productsById, errors);
    }

    /**
     * Valida el archivo de vendedores.
     *
     * @param lines lineas del archivo de vendedores
     * @return catalogo valido y lista de errores
     */
    public SalesmanCatalogResult validateSalesmen(List<String> lines) {
        Map<String, Salesman> salesmenByIdentity = new HashMap<String, Salesman>();
        List<String> errors = new ArrayList<String>();

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String rawLine = lines.get(lineNumber);
            String trimmedLine = rawLine == null ? "" : rawLine.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            String[] columns = trimmedLine.split(AppConstants.DELIMITER, -1);
            if (columns.length < 4) {
                errors.add("vendedores.txt linea " + (lineNumber + 1) + ": formato incompleto.");
                continue;
            }

            String documentType = columns[0].trim();
            String documentText = columns[1].trim();
            String firstNames = columns[2].trim();
            String lastNames = columns[3].trim();

            if (documentType.isEmpty() || firstNames.isEmpty() || lastNames.isEmpty()) {
                errors.add("vendedores.txt linea " + (lineNumber + 1) + ": campos vacios.");
                continue;
            }

            Long documentNumber = parsePositiveLong(documentText);
            if (documentNumber == null) {
                errors.add("vendedores.txt linea " + (lineNumber + 1) + ": numero de documento invalido.");
                continue;
            }

            Salesman salesman = new Salesman(documentType, documentNumber.longValue(), firstNames, lastNames);
            String identityKey = salesman.getIdentityKey();
            if (salesmenByIdentity.containsKey(identityKey)) {
                errors.add("vendedores.txt linea " + (lineNumber + 1) + ": vendedor duplicado.");
                continue;
            }
            salesmenByIdentity.put(identityKey, salesman);
        }

        return new SalesmanCatalogResult(salesmenByIdentity, errors);
    }

    /**
     * Valida un archivo de ventas. Permite acumular ventas de un mismo vendedor
     * desde varios archivos.
     *
     * @param salesFile         archivo analizado
     * @param lines             contenido
     * @param productsById      catalogo de productos validos
     * @param salesmenByIdentity catalogo de vendedores validos
     * @param collectedErrors   bitacora de errores a completar
     * @return arreglo de dos mapas: cantidades por producto y recaudo por vendedor
     *         de este archivo; el primer mapa es idProducto-&gt;cantidad, el segundo
     *         identity-&gt;dinero. Si el archivo es invalido, ambos salen vacios.
     */
    public SalesFileParseResult validateSalesFile(File salesFile, List<String> lines,
            Map<String, Product> productsById, Map<String, Salesman> salesmenByIdentity,
            List<String> collectedErrors) {

        Map<String, Long> quantityByProductId = new HashMap<String, Long>();
        Map<String, Long> moneyBySalesman = new HashMap<String, Long>();

        if (lines.isEmpty()) {
            collectedErrors.add(salesFile.getName() + ": archivo vacio.");
            return new SalesFileParseResult(quantityByProductId, moneyBySalesman);
        }

        String header = lines.get(0) == null ? "" : lines.get(0).trim();
        String[] headerColumns = header.split(AppConstants.DELIMITER, -1);
        if (headerColumns.length < 2 || headerColumns[0].trim().isEmpty() || headerColumns[1].trim().isEmpty()) {
            collectedErrors.add(salesFile.getName() + " linea 1: encabezado de vendedor invalido.");
            return new SalesFileParseResult(quantityByProductId, moneyBySalesman);
        }

        String identityKey = headerColumns[0].trim() + AppConstants.DELIMITER + headerColumns[1].trim();
        if (!salesmenByIdentity.containsKey(identityKey)) {
            collectedErrors.add(salesFile.getName() + ": vendedor " + identityKey
                    + " no existe en el catalogo.");
            return new SalesFileParseResult(quantityByProductId, moneyBySalesman);
        }

        long fileMoney = 0L;

        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            String rawLine = lines.get(lineNumber);
            String trimmedLine = rawLine == null ? "" : rawLine.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            String[] columns = trimmedLine.split(AppConstants.DELIMITER, -1);
            if (columns.length < 2) {
                collectedErrors.add(salesFile.getName() + " linea " + (lineNumber + 1) + ": formato incompleto.");
                continue;
            }

            String productId = columns[0].trim();
            String quantityText = columns[1].trim();

            if (!productsById.containsKey(productId)) {
                collectedErrors.add(salesFile.getName() + " linea " + (lineNumber + 1)
                        + ": id de producto inexistente (" + productId + ").");
                continue;
            }

            Long quantity = parseNonNegativeLong(quantityText);
            if (quantity == null) {
                collectedErrors.add(salesFile.getName() + " linea " + (lineNumber + 1)
                        + ": cantidad invalida o negativa.");
                continue;
            }

            Product product = productsById.get(productId);
            long lineMoney = product.getUnitPrice() * quantity.longValue();
            fileMoney += lineMoney;

            Long previousQuantity = quantityByProductId.get(productId);
            if (previousQuantity == null) {
                quantityByProductId.put(productId, quantity);
            } else {
                quantityByProductId.put(productId, previousQuantity.longValue() + quantity.longValue());
            }
        }

        moneyBySalesman.put(identityKey, fileMoney);
        return new SalesFileParseResult(quantityByProductId, moneyBySalesman);
    }

    /**
     * Contenedor del parseo de un archivo de ventas.
     */
    public static class SalesFileParseResult {
        private final Map<String, Long> quantityByProductId;
        private final Map<String, Long> moneyBySalesman;

        SalesFileParseResult(Map<String, Long> quantityByProductId, Map<String, Long> moneyBySalesman) {
            this.quantityByProductId = quantityByProductId;
            this.moneyBySalesman = moneyBySalesman;
        }

        public Map<String, Long> getQuantityByProductId() {
            return quantityByProductId;
        }

        public Map<String, Long> getMoneyBySalesman() {
            return moneyBySalesman;
        }
    }

    private Long parseNonNegativeLong(String text) {
        try {
            long value = Long.parseLong(text);
            if (value < 0) {
                return null;
            }
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long parsePositiveLong(String text) {
        Long value = parseNonNegativeLong(text);
        if (value == null || value.longValue() <= 0) {
            return null;
        }
        return value;
    }
}

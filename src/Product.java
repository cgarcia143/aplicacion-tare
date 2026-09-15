import java.io.Serializable;

/**
 * Representa un producto del catalogo: identificador, nombre y precio unitario.
 *
 * @author Cristian
 * @version 1.0
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String productId;
    private final String productName;
    private final long unitPrice;

    /**
     * Crea un producto con sus datos basicos.
     *
     * @param productId identificador unico del producto
     * @param productName nombre comercial
     * @param unitPrice precio por unidad; debe ser mayor que cero
     */
    public Product(String productId, String productName, long unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
    }

    /**
     * @return identificador del producto
     */
    public String getProductId() {
        return productId;
    }

    /**
     * @return nombre del producto
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return precio por unidad
     */
    public long getUnitPrice() {
        return unitPrice;
    }
}

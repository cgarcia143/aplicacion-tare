import java.io.Serializable;

/**
 * Representa un vendedor con tipo y numero de documento, nombres y apellidos.
 *
 * @author Cristian
 * @version 1.0
 */
public class Salesman implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String documentType;
    private final long documentNumber;
    private final String firstNames;
    private final String lastNames;

    /**
     * Crea un vendedor con la informacion del archivo maestro.
     *
     * @param documentType tipo de documento (por ejemplo CC o CE)
     * @param documentNumber numero de documento
     * @param firstNames nombres
     * @param lastNames apellidos
     */
    public Salesman(String documentType, long documentNumber, String firstNames, String lastNames) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
    }

    /**
     * @return tipo de documento
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * @return numero de documento
     */
    public long getDocumentNumber() {
        return documentNumber;
    }

    /**
     * @return nombres
     */
    public String getFirstNames() {
        return firstNames;
    }

    /**
     * @return apellidos
     */
    public String getLastNames() {
        return lastNames;
    }

    /**
     * Une nombres y apellidos para los reportes.
     *
     * @return nombre completo
     */
    public String getFullName() {
        return firstNames + " " + lastNames;
    }

    /**
     * Clave unica usada para agrupar varios archivos de un mismo vendedor.
     *
     * @return tipo de documento y numero, separados por punto y coma
     */
    public String getIdentityKey() {
        return documentType + AppConstants.DELIMITER + documentNumber;
    }
}

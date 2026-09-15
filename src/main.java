/**
 * Segunda clase con metodo {@code main} del proyecto. Al ejecutarse lee los
 * archivos de vendedores, productos y ventas, y crea los reportes CSV
 * solicitados. No solicita informacion al usuario.
 *
 * <p>
 * El nombre de la clase es {@code main}, tal como lo exige el enunciado.
 * </p>
 *
 * @author Cristian
 * @version 1.0
 */
public class main {

    /**
     * Punto de entrada del procesador de reportes. Informa exito o error por
     * consola.
     *
     * @param args no se utilizan
     */
    public static void main(String[] args) {
        try {
            SalesReportService salesReportService = new SalesReportService();
            salesReportService.generateReports();
            String successMessage = "Proceso de generacion de reportes finalizado exitosamente.";
            System.out.println(successMessage);
            GenerationView.display(true, successMessage);
        } catch (Exception exception) {
            String errorMessage = "Error al generar los reportes: " + exception.getMessage();
            System.out.println(errorMessage);
            GenerationView.display(false, errorMessage);
        }
    }
}

package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;


public class Main extends Application {

    private TableView<Empleado> tableView;

    private static final String URL      = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER     = "RIBERA";
    private static final String PASSWORD = "ribera";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ejercicio 26 - Eliminar empleado seleccionado");

        tableView = new TableView<>();

        // Definir columnas
        TableColumn<Empleado, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Empleado, String> nombreCol = new TableColumn<>("Nombre");
        TableColumn<Empleado, Integer> salarioCol = new TableColumn<>("Salario");

        // Asignar las propiedades del modelo a las columnas
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        salarioCol.setCellValueFactory(new PropertyValueFactory<>("salario"));

        tableView.getColumns().addAll(idCol, nombreCol, salarioCol);

        // Boton cargar datos
        Button btnCargar = new Button("Cargar datos");
        btnCargar.setOnAction(event -> cargarDatos());

        // Boton eliminar: actua sobre la fila seleccionada en el TableView
        Button btnEliminar = new Button("Eliminar seleccionado");
        btnEliminar.setOnAction(event -> eliminarSeleccionado());

        VBox vbox = new VBox(10, tableView, btnCargar, btnEliminar);
        vbox.setStyle("-fx-padding: 20;");
        Scene scene = new Scene(vbox, 450, 380);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Carga inicial al arrancar la aplicacion
        cargarDatos();
    }

    private void cargarDatos() {
        tableView.getItems().clear();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Statement stmt = conn.createStatement();
            ResultSet rs   = stmt.executeQuery("SELECT id, nombre, salario FROM EJEMPLOCONEXION");

            while (rs.next()) {
                int    id      = rs.getInt("id");
                String nombre  = rs.getString("nombre");
                int    salario = rs.getInt("salario");
                tableView.getItems().add(new Empleado(id, nombre, salario));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void eliminarSeleccionado() {
        Empleado seleccionado = tableView.getSelectionModel().getSelectedItem();

        // Comprobar que hay una fila seleccionada
        if (seleccionado == null) {
            Alert aviso = new Alert(Alert.AlertType.WARNING);
            aviso.setTitle("Sin selección");
            aviso.setHeaderText(null);
            aviso.setContentText("Por favor, selecciona un empleado de la tabla.");
            aviso.showAndWait();
            return;
        }

        // Pedir confirmacion antes de borrar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar empleado?");
        confirmacion.setContentText("Se eliminará: "
                + seleccionado.getNombre() + " (ID: " + seleccionado.getId() + ")");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {

            // Borrar de la base de datos con DELETE
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM EJEMPLOCONEXION WHERE id = ?");
                ps.setInt(1, seleccionado.getId());
                ps.executeUpdate();
                System.out.println("Empleado eliminado de la base de datos.");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Borrar tambien de la lista del TableView
            tableView.getItems().remove(seleccionado);
            System.out.println("Empleado eliminado del TableView.");

        } else {
            System.out.println("Eliminación cancelada por el usuario.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Empleado {
        private final int    id;
        private final String nombre;
        private final int    salario;

        public Empleado(int id, String nombre, int salario) {
            this.id      = id;
            this.nombre  = nombre;
            this.salario = salario;
        }

        public int    getId()      { return id; }
        public String getNombre()  { return nombre; }
        public int    getSalario() { return salario; }
    }
}
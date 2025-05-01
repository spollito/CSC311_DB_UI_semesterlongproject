package viewmodel;

import dao.DbConnectivityClass;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Person;
import service.MyLogger;
import javafx.scene.control.ComboBox;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {
    @FXML private Label statusLabel;

    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button addButton;

    @FXML private MenuItem editMenuItem;
    @FXML private MenuItem deleteMenuItem;

    @FXML private MenuItem importCSVItem;
    @FXML private MenuItem exportCSVItem;



    @FXML TextField first_name, last_name, department, email, imageURL;
    @FXML ComboBox<Major> majorComboBox;

    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    // Define enum
    public enum Major {
        CS, CPIS, English
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(true);

        editMenuItem.setDisable(true);
        deleteMenuItem.setDisable(true);

        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));
        majorComboBox.getSelectionModel().selectFirst();
        setupFormValidation();
    }
    private void setupFormValidation() {
        ChangeListener<String> validator = (obs, oldVal, newVal) -> validateFormFields();

        first_name.textProperty().addListener(validator);
        last_name.textProperty().addListener(validator);
        email.textProperty().addListener(validator);
        department.textProperty().addListener(validator);
        imageURL.textProperty().addListener(validator);

        majorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFormFields());
    }

    private void validateFormFields() {
        boolean valid = validateName(first_name.getText()) &&
                validateName(last_name.getText()) &&
                validateEmail(email.getText()) &&
                validateDept(department.getText()) &&
                validateURL(imageURL.getText()) &&
                majorComboBox.getValue() != null;
        addButton.setDisable(!valid);
    }

    // Regex validation methods
    private boolean validateName(String name) {
        return Pattern.matches("^[A-Z][a-z]+(?: [A-Z][a-z]+)*$", name); // e.g., John Smith
    }

    private boolean validateEmail(String email) {
        return Pattern.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$", email);
    }

    private boolean validateDept(String dept) {
        return Pattern.matches("^[A-Z][a-zA-Z ]{2,}$", dept);
    }

    private boolean validateURL(String url) {
        return Pattern.matches("^(http|https)://.*$", url) || url.isEmpty(); // allow empty for optional field
    }



    @FXML
    protected void addNewRecord() {

        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().name(), email.getText(), imageURL.getText());

        cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();
        statusLabel.setText("Record added successfully.");


    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        majorComboBox.getSelectionModel().clearSelection();
        email.setText("");
        imageURL.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().name(), email.getText(),  imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
        statusLabel.setText("Record updated successfully.");

    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        boolean selected = p != null;

        editButton.setDisable(!selected);
        deleteButton.setDisable(!selected);
        editMenuItem.setDisable(!selected);
        deleteMenuItem.setDisable(!selected);

        if (selected) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            majorComboBox.setValue(Major.valueOf(p.getMajor()));
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
        }
    }


    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> statusLabel.setText("Ready."));
        pause.play();
    }



    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    @FXML
    private void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String[] values = scanner.nextLine().split(",");
                    if (values.length >= 6) {
                        Person p = new Person(
                                values[0].trim(),
                                values[1].trim(),
                                values[2].trim(),
                                values[3].trim(),
                                values[4].trim(),
                                values[5].trim()
                        );
                        cnUtil.insertUser(p);
                        p.setId(cnUtil.retrieveId(p));
                        data.add(p);
                    }
                }
                showStatus("CSV imported successfully.");
            } catch (Exception e) {
                showStatus("Error importing CSV.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to CSV");
        fileChooser.setInitialFileName("export.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                for (Person p : data) {
                    writer.println(String.join(",",
                            p.getFirstName(),
                            p.getLastName(),
                            p.getDepartment(),
                            p.getMajor(),
                            p.getEmail(),
                            p.getImageURL()
                    ));
                }
                showStatus("Data exported successfully.");
            } catch (Exception e) {
                showStatus("Error exporting CSV.");
                e.printStackTrace();
            }
        }
    }


    //private static enum Major {Business, CSC, CPIS}

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}
package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserSession;

public class SignUpController {
    public void createNewAccount(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Info for the user. Message goes here");
        alert.showAndWait();
    }

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    protected void handleSignUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            UserSession.signUp(username, password, "USER");
            statusLabel.setText("Signup successful!");
            // Navigate to the main dashboard here
        } catch (IllegalStateException e) {
            statusLabel.setText("User already exists.");
        } catch (Exception e) {
            statusLabel.setText("Signup failed.");
            e.printStackTrace();
        }
    }
    @FXML
    protected void handleSignIn() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            UserSession.signIn(username, password);
            statusLabel.setText("Sign-in successful!");
            // Navigate to main UI
        } catch (SecurityException e) {
            statusLabel.setText("Invalid credentials.");
        } catch (Exception e) {
            statusLabel.setText("Sign-in failed.");
            e.printStackTrace();
        }
    }



    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package io.github.broskipoker.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.broskipoker.utils.UserService;

public class LoginDialog extends Dialog {
    private final UserService userService;
    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;
    private Runnable onLoginSuccess;

    public LoginDialog(String title, Skin skin) {
        super(title, skin);
        this.userService = UserService.getInstance();
        createLoginForm();
    }

    private void createLoginForm() {
        // Create form elements
        usernameField = new TextField("", getSkin());
        usernameField.setMessageText("Username");

        passwordField = new TextField("", getSkin());
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        statusLabel = new Label("", getSkin());
        statusLabel.setColor(1, 0, 0, 1); // Red color for error messages

        // Create buttons
        TextButton loginButton = new TextButton("Login", getSkin());
        TextButton registerButton = new TextButton("Register", getSkin());
        TextButton cancelButton = new TextButton("Cancel", getSkin());

        // Layout the dialog
        getContentTable().add(new Label("Username:", getSkin())).padRight(10);
        getContentTable().add(usernameField).width(200).padBottom(10);
        getContentTable().row();

        getContentTable().add(new Label("Password:", getSkin())).padRight(10);
        getContentTable().add(passwordField).width(200).padBottom(10);
        getContentTable().row();

        getContentTable().add(statusLabel).colspan(2).padBottom(10);
        getContentTable().row();

        // Add buttons to button table
        getButtonTable().add(loginButton).padRight(10);
        getButtonTable().add(registerButton).padRight(10);
        getButtonTable().add(cancelButton);

        // Add listeners
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleLogin();
            }
        });

        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openRegisterDialog();
            }
        });

        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        // Set dialog properties
        setModal(true);
        setMovable(true);
        setResizable(false);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields");
            return;
        }

        if (userService.loginUser(username, password)) {
            statusLabel.setText("Login successful!");
            statusLabel.setColor(0, 1, 0, 1); // Green color for success

            // Delay hiding the dialog to show success message
            addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(1f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                    hide();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                })
            ));
        } else {
            statusLabel.setText("Invalid username or password");
            statusLabel.setColor(1, 0, 0, 1); // Red color for error
            passwordField.setText(""); // Clear password field
        }
    }

    private void openRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog("Register", getSkin());
        registerDialog.show(getStage());
        registerDialog.setOnRegisterSuccess(() -> {
            // After successful registration, close this login dialog
            statusLabel.setText("Registration successful! Please login.");
            statusLabel.setColor(0, 1, 0, 1);
        });
        hide();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    @Override
    public void hide() {
        super.hide();
        // Clear fields when dialog is hidden
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
        statusLabel.setColor(1, 0, 0, 1);
    }
}

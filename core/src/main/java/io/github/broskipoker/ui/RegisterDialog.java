package io.github.broskipoker.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.broskipoker.utils.UserService;

public class RegisterDialog extends Dialog {
    private final UserService userService;
    private TextField usernameField;
    private TextField emailField;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private Label statusLabel;
    private Runnable onRegisterSuccess;

    public RegisterDialog(String title, Skin skin) {
        super(title, skin);
        this.userService = UserService.getInstance();
        createRegisterForm();
    }

    private void createRegisterForm() {
        // Create form elements
        usernameField = new TextField("", getSkin());
        usernameField.setMessageText("Username");

        emailField = new TextField("", getSkin());
        emailField.setMessageText("Email (optional)");

        passwordField = new TextField("", getSkin());
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        confirmPasswordField = new TextField("", getSkin());
        confirmPasswordField.setMessageText("Confirm Password");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');

        statusLabel = new Label("", getSkin());
        statusLabel.setColor(1, 0, 0, 1); // Red color for error messages

        // Create buttons
        TextButton registerButton = new TextButton("Register", getSkin());
        TextButton backToLoginButton = new TextButton("Back to Login", getSkin());
        TextButton cancelButton = new TextButton("Cancel", getSkin());

        // Layout the dialog
        getContentTable().add(new Label("Username:", getSkin())).padRight(10);
        getContentTable().add(usernameField).width(200).padBottom(10);
        getContentTable().row();

        getContentTable().add(new Label("Email:", getSkin())).padRight(10);
        getContentTable().add(emailField).width(200).padBottom(10);
        getContentTable().row();

        getContentTable().add(new Label("Password:", getSkin())).padRight(10);
        getContentTable().add(passwordField).width(200).padBottom(10);
        getContentTable().row();

        getContentTable().add(new Label("Confirm:", getSkin())).padRight(10);
        getContentTable().add(confirmPasswordField).width(200).padBottom(10);
        getContentTable().row();

        getContentTable().add(statusLabel).colspan(2).padBottom(10);
        getContentTable().row();

        // Add buttons to button table
        getButtonTable().add(registerButton).padRight(10);
        getButtonTable().add(backToLoginButton).padRight(10);
        getButtonTable().add(cancelButton);

        // Add listeners
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleRegister();
            }
        });

        backToLoginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openLoginDialog();
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

        // Center the title
        getTitleLabel().setAlignment(com.badlogic.gdx.utils.Align.center);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required");
            return;
        }

        if (username.length() < 3) {
            statusLabel.setText("Username must be at least 3 characters");
            return;
        }

        if (password.length() < 3) {
            statusLabel.setText("Password must be at least 3 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match");
            return;
        }

        // Check if username already exists
        if (userService.usernameExists(username)) {
            statusLabel.setText("Username already exists");
            return;
        }

        // Check if email already exists (if provided)
        if (!email.isEmpty() && userService.emailExists(email)) {
            statusLabel.setText("Email already registered");
            return;
        }

        // Attempt registration
        String emailToUse = email.isEmpty() ? null : email;
        if (userService.registerUser(username, emailToUse, password)) {
            statusLabel.setText("Registration successful!");
            statusLabel.setColor(0, 1, 0, 1); // Green color for success

            // Delay hiding the dialog to show success message
            addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(1f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                    hide();
                    if (onRegisterSuccess != null) {
                        onRegisterSuccess.run();
                    }
                })
            ));
        } else {
            statusLabel.setText("Registration failed. Please try again.");
            statusLabel.setColor(1, 0, 0, 1); // Red color for error
        }
    }

    private void openLoginDialog() {
        LoginDialog loginDialog = new LoginDialog("Login", getSkin());
        loginDialog.show(getStage());
        hide();
    }

    public void setOnRegisterSuccess(Runnable callback) {
        this.onRegisterSuccess = callback;
    }

    @Override
    public void hide() {
        super.hide();
        // Clear fields when dialog is hidden
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText("");
        statusLabel.setColor(1, 0, 0, 1);
    }
}

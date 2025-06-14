package io.github.broskipoker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.broskipoker.game.User;
import io.github.broskipoker.ui.LoginDialog;
import io.github.broskipoker.ui.MultiplayerDialog;
import io.github.broskipoker.utils.UserService;

public class Menu {
    public static Music menuMusic;
    private static float menuVolume = 0.5f;
    public static long clickSoundId;
    private Table table;
    private final Stage stage;
    private TextButton singleplayerButton;
    private TextButton exitButton;
    private TextButton settingsButton;
    private TextButton friendsButton;
    private TextButton loginButton;
    private TextButton multiplayerButton;
    private Label userInfoLabel;
    public static Sound clickSound;
    private boolean gameStarted = false;
    private Skin skin;
    private final UserService userService;
    private Texture backgroundTexture;
    private Image backgroundImage;

    public Menu(Stage stage) {
        this.stage = stage;
        this.userService = UserService.getInstance();
        loadSounds();
        createMenu();
    }

    private void loadSounds() {
        clickSound = Gdx.audio.newSound(Gdx.files.internal("click.wav"));
    }

    private void createMenu() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("kevin-macleod-investigations.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(menuVolume);
//        menuMusic.play();

        skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));
        setBackground("menu_background.png");

        table = new Table();
        table.setFillParent(true);
        repositionMenu();

        singleplayerButton = new TextButton("Singleplayer", skin);
        multiplayerButton = new TextButton("Multiplayer", skin);
        friendsButton = new TextButton("Friends", skin);
        settingsButton = new TextButton("Settings", skin);
        exitButton = new TextButton("Exit", skin);
        loginButton = new TextButton("Login", skin);
        userInfoLabel = new Label("", skin);

        float buttonWidth = Math.min(350, Gdx.graphics.getWidth() * 0.3f);

        table.add(userInfoLabel).padBottom(20);
        table.row();
        table.add(singleplayerButton).width(buttonWidth).padBottom(40);
        table.row();
        table.add(multiplayerButton).width(buttonWidth).padBottom(40);
        table.row();
        table.add(loginButton).width(buttonWidth).padBottom(40);
        table.row();
        table.add(friendsButton).width(buttonWidth).padBottom(40);
        table.row();
        table.add(settingsButton).width(buttonWidth).padBottom(40);
        table.row();
        table.add(exitButton).width(buttonWidth);

        stage.addActor(table);
        updateLoginState();

        singleplayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                gameStarted = true;
                // Hide the menu and start the game
                table.remove();
                if (backgroundImage != null) {
                    backgroundImage.remove();
                }
            }
        });

        multiplayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);

                if(userService.isLoggedIn()){
                    MultiplayerDialog multiplayerDialog = new MultiplayerDialog("Multiplayer", skin);
                    multiplayerDialog.show(stage);
                }
                else {
                    Dialog messageDialog = new Dialog("Attention!", skin);
                    messageDialog.getTitleLabel().setAlignment(com.badlogic.gdx.utils.Align.center);

                    // Text label to control
                    Label textLabel = new Label("You need to login first", skin);
                    messageDialog.getContentTable().add(textLabel).pad(30, 20, 20, 20);

                    messageDialog.button("OK");
                    messageDialog.show(stage);
                }
            }
        });

        friendsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
            }
        });

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                SettingsMenu settingsMenu = new SettingsMenu(skin);
                stage.addActor(settingsMenu);
                settingsMenu.setSlidersVolume(menuMusic.getVolume(), menuVolume);
                settingsMenu.setVisible(true);
            }
        });

        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);

                if (userService.isLoggedIn()) {
                    userService.logoutUser();
                    updateLoginState();
                } else {
                    LoginDialog loginDialog = new LoginDialog("Login", skin);
                    loginDialog.setOnLoginSuccess(() -> updateLoginState());
                    loginDialog.show(stage);
                }
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                Gdx.app.exit();
            }
        });
    }

    private void setBackground(String imagePath) {
        backgroundTexture = new Texture(Gdx.files.internal(imagePath));
        backgroundImage = new Image(backgroundTexture);

        // Size the background to fill the screen
        backgroundImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Add the background to the stage first so it appears behind other elements
        stage.addActor(backgroundImage);

        // Move the background to the back
        backgroundImage.toBack();
    }

    private void updateLoginState() {
        if (userService.isLoggedIn()) {
            User user = userService.getCurrentUser().get();
//            userInfoLabel.setText("Welcome, " + user.getUsername() + " | Chips: " + user.getChips());
            userInfoLabel.setText("Welcome, " + user.getUsername()); // user chips reset after every game
            loginButton.setText("Logout");
        } else {
            userInfoLabel.setText("");
            loginButton.setText("Login");
        }
    }

    public void repositionMenu() {
        if (table != null) {
            table.setPosition(-300, -70,  200);
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;

        // hide the menu if the game has started
        if (started) {
            // hide buttons
            for (TextButton button : getButtons()) {
                button.setVisible(false);
            }

            // hide background
            if (backgroundImage != null) {
                backgroundImage.setVisible(false);
            }

            // hide user info label
            if (userInfoLabel != null) {
                userInfoLabel.setVisible(false);
            }
        } else {
            // show buttons
            for (TextButton button : getButtons()) {
                button.setVisible(true);
            }

            // show background
            if (backgroundImage != null) {
                backgroundImage.setVisible(true);
            }

            // show user info label
            if (userInfoLabel != null) {
                userInfoLabel.setVisible(true);
            }
        }
    }

    public void dispose() {
        clickSound.dispose();
        if (menuMusic != null) {
            menuMusic.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    public TextButton[] getButtons() {
        return new TextButton[]{singleplayerButton, multiplayerButton, settingsButton, friendsButton, loginButton, exitButton};
    }

    public static float getMenuVolume() {
        return menuVolume;
    }

    public static void setMenuVolume(float newVolume) {
        menuVolume = newVolume;
    }
}

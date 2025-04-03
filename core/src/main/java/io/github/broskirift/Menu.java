package io.github.broskirift;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Menu {
    public static Music menuMusic;
    private static float menuVolume = 0.5f; // Default volume
    public static long clickSoundId;
    private Table table;
    private final Stage stage;  // The Stage to hold UI components
    private TextButton playButton;  // Start button
    private TextButton exitButton;   // Exit button
    private TextButton settingsButton;    // Settings button
    private TextButton friendsButton;    // Friends button
    public static Sound clickSound;
    private boolean gameStarted = false;  // Flag to check if the game has started

    public Menu(Stage stage) {
        this.stage = stage;  // Set the stage for the menu
        loadSounds();        // Load the sound effects
        createMenu();        // Call the method to create menu components
    }

    private void loadSounds() {
        // Load the click sound effect
        clickSound = Gdx.audio.newSound(Gdx.files.internal("click.wav"));
    }

    private void createMenu() {
        // Load the menu music
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menuMusic.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();

        // Set the table to fill the stage
        table = new Table();
        table.setFillParent(true);
        table.center().left();

        // Create the textures for the buttons
        Skin skin = new Skin(Gdx.files.internal("skin/star-soldier-ui.json"));
        Skin skinStart = new Skin(Gdx.files.internal("skin/star-soldier-ui.json"));
        playButton = new TextButton("Start", skinStart);
        friendsButton = new TextButton("Friends", skin);
        settingsButton = new TextButton("Settings", skin);
        exitButton = new TextButton("Exit", skin);

        table.row();
        table.add(playButton).fillX();
        table.row();
        table.add(friendsButton).fillX();
        table.row();
        table.add(settingsButton).fillX();
        table.row();
        table.add(exitButton).fillX();

        stage.addActor(table);

//        // Create the settings menu
//        SettingsMenu settingsMenu = new SettingsMenu(skin);
//        settingsMenu.setVisible(false);
//
//        stage.addActor(settingsMenu);

        // Add listener for the play button
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                gameStarted = true;  // Set gameStarted to true when the start button is clicked
            }
        });

        // Add listener for the friends button
        friendsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                // Add code to show friends menu
            }
        });

        // Add listener for the settings button
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                SettingsMenu settingsMenu = new SettingsMenu(skin);
                stage.addActor(settingsMenu);
                settingsMenu.setSlidersVolume(menuMusic.getVolume(), menuVolume); // Set the sliders to the current volume
                settingsMenu.setVisible(true);  // Show the settings menu when the settings button is clicked
                clickSound.setVolume(clickSoundId, menuVolume);
            }
        });

        // Add listener for the exit button
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                Gdx.app.exit();  // Exit the application when the exit button is clicked
            }
        });


    }

    // Method to check if the game has started
    public boolean isGameStarted() {
        return gameStarted;
    }

    public void dispose() {
        // Dispose of resources when done
        clickSound.dispose();
    }

    public TextButton[] getButtons() {
        return new TextButton[]{playButton, settingsButton, friendsButton, exitButton};
    }

    public static float getMenuVolume() {
        return menuVolume;
    }

    public static void setMenuVolume(float menuVolume) {
        Menu.menuVolume = menuVolume;
    }
}

package io.github.broskipoker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Menu {
    public static Music menuMusic;
    private static float menuVolume = 0.5f;
    public static long clickSoundId;
    private Table table;
    private final Stage stage;
    private TextButton playButton;
    private TextButton exitButton;
    private TextButton settingsButton;
    private TextButton friendsButton;
    public static Sound clickSound;
    private boolean gameStarted = false;
    private Skin skin;

    public Menu(Stage stage) {
        this.stage = stage;
        loadSounds();
        createMenu();
    }

    private void loadSounds() {
        clickSound = Gdx.audio.newSound(Gdx.files.internal("click.wav"));
    }

    private void createMenu() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menuMusic.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(menuVolume);
        menuMusic.play();

        skin = new Skin(Gdx.files.internal("skin/star-soldier-ui.json"));

        table = new Table();
        table.setFillParent(true);
        repositionMenu();

        playButton = new TextButton("Start", skin);
        friendsButton = new TextButton("Friends", skin);
        settingsButton = new TextButton("Settings", skin);
        exitButton = new TextButton("Exit", skin);

        float buttonWidth = Math.min(300, Gdx.graphics.getWidth() * 0.3f);

        table.add(playButton).width(buttonWidth).padBottom(20);
        table.row();
        table.add(friendsButton).width(buttonWidth).padBottom(20);
        table.row();
        table.add(settingsButton).width(buttonWidth).padBottom(20);
        table.row();
        table.add(exitButton).width(buttonWidth);

        stage.addActor(table);

        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                gameStarted = true;
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

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSoundId = clickSound.play();
                clickSound.setVolume(clickSoundId, menuVolume);
                Gdx.app.exit();
            }
        });
    }

    public void repositionMenu() {
        if (table != null) {
            table.center().left().pad(50);
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void dispose() {
        clickSound.dispose();
        if (menuMusic != null) {
            menuMusic.dispose();
        }
    }

    public TextButton[] getButtons() {
        return new TextButton[]{playButton, settingsButton, friendsButton, exitButton};
    }

    public static float getMenuVolume() {
        return menuVolume;
    }

    public static void setMenuVolume(float newVolume) {
        menuVolume = newVolume;
    }
}

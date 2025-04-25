package io.github.broskipoker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.awt.*;

public class SettingsMenu extends Window {

    private Slider musicVolumeSlider;
    private Slider menuVolumeSlider;
    private SelectBox<String> resolutionSelect;

    public SettingsMenu(Skin skin) {
        // Create a window with the title "Settings"
        super("Settings", skin);
        getTitleLabel().setAlignment(Align.center);
        this.setSize(400, 500);
        float centerX = (Gdx.graphics.getWidth() - this.getWidth()) / 2;
        float centerY = (Gdx.graphics.getHeight() - this.getHeight()) / 2;
        this.setPosition(centerX, centerY);


        // Creating a slider for volume
        musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicVolumeSlider.setValue(Menu.menuMusic.getVolume()); // Implicit value of 1f

        menuVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        menuVolumeSlider.setValue(Menu.getMenuVolume()); // Implicit value of 1f

        // Creating a select box for resolution
        String[] resolutions = new String[] { "800x600", "1280x720", "1920x1080" };
        resolutionSelect = new SelectBox<>(skin);
        resolutionSelect.setItems(resolutions);
        resolutionSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedResolution = resolutionSelect.getSelected();
                if (selectedResolution.equals("800x600")) {
                    Gdx.graphics.setWindowedMode(800, 600);
                } else if (selectedResolution.equals("1280x720")) {
                    Gdx.graphics.setWindowedMode(1280, 720);
                } else if (selectedResolution.equals("1920x1080")) {
                    Gdx.graphics.setWindowedMode(1920, 1080);
                }
            }
        });

        // Add UI elements to the window
        this.add("Music volume:").padTop(20);
        this.row().padTop(10);
        this.add(musicVolumeSlider).width(200);

        this.row().padTop(20);
        this.add("Menu volume:").padTop(20);
        this.row().padTop(10);
        this.add(menuVolumeSlider).width(200);

        this.row().padTop(20);
        this.add("Resolution:").padTop(10);
        this.row().padTop(10);
        this.add(resolutionSelect).width(200);

        // Apply and Close button
        TextButton closeButton = new TextButton("Close", skin);
        TextButton applyButton = new TextButton("Apply", skin);

        applyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Apply the settings
                Menu.setMenuVolume(menuVolumeSlider.getValue());
                Menu.clickSoundId = Menu.clickSound.play();
                Menu.clickSound.setVolume(Menu.clickSoundId, Menu.getMenuVolume());
                Menu.menuMusic.setVolume(musicVolumeSlider.getValue());
            }
        });

        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Menu.clickSoundId = Menu.clickSound.play();
                Menu.clickSound.setVolume(Menu.clickSoundId, Menu.getMenuVolume());
                musicVolumeSlider.setValue(Menu.menuMusic.getVolume());
                setVisible(false); // Hide the settings menu
            }
        });

        this.row().padTop(20);
        this.add(applyButton).colspan(2).center();
        this.row().padTop(-20);
        this.add(closeButton).colspan(2).center();


    }


    void setSlidersVolume(float musicVolume, float menuVolume){
        musicVolumeSlider.setValue(musicVolume);
        menuVolumeSlider.setValue(menuVolume);
    }

}

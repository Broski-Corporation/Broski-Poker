package io.github.broskirift;

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
    private Slider volumeSlider;
    private SelectBox<String> resolutionSelect;

    public SettingsMenu(Skin skin) {
        // Create a window with the title "Settings"
        super("Settings", skin);
        getTitleLabel().setAlignment(Align.center);
        this.setSize(400, 300);
        float centerX = (Gdx.graphics.getWidth() - this.getWidth()) / 2;
        float centerY = (Gdx.graphics.getHeight() - this.getHeight()) / 2;
        this.setPosition(centerX, centerY);



        // Creating a slider for volume
        volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(Menu.menuMusic.getVolume()); // Implicit value of 0.1f

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
        this.add("Volume:").padTop(20);
        this.row().padTop(10);
        this.add(volumeSlider).width(200);

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
                Menu.clickSound.play();
                Menu.menuMusic.setVolume(volumeSlider.getValue());
            }
        });

        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Menu.clickSound.play();
                volumeSlider.setValue(Menu.menuMusic.getVolume());
                setVisible(false); // Hide the settings menu
            }
        });

        this.row().padTop(20);
        this.add(applyButton).colspan(2).center();
        this.row().padTop(-20);
        this.add(closeButton).colspan(2).center();


    }
}

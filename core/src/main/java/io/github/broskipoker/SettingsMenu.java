package io.github.broskipoker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class SettingsMenu extends Window {
    private Slider musicVolumeSlider;
    private Slider menuVolumeSlider;
    private SelectBox<String> resolutionSelect;
    private CheckBox fullscreenCheckbox;

    private final String[] RESOLUTIONS = new String[] {
        "800x600",
        "1280x720",
        "1366x768",
        "1600x900",
        "1920x1080",
        "2560x1440",
        "3840x2160"
    };

    public SettingsMenu(Skin skin) {
        super("Settings", skin);
        getTitleLabel().setAlignment(Align.center);
        this.setSize(400, 500);
        centerWindow();

        musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicVolumeSlider.setValue(Menu.menuMusic.getVolume());

        menuVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        menuVolumeSlider.setValue(Menu.getMenuVolume());

        resolutionSelect = new SelectBox<>(skin);
        resolutionSelect.setItems(RESOLUTIONS);

        String currentResolution = Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            if (RESOLUTIONS[i].equals(currentResolution)) {
                resolutionSelect.setSelectedIndex(i);
                break;
            }
        }

        fullscreenCheckbox = new CheckBox("Fullscreen", skin);
        fullscreenCheckbox.setChecked(Gdx.graphics.isFullscreen());

        this.add("Music volume:").padTop(20).left();
        this.row().padTop(10);
        this.add(musicVolumeSlider).width(300).fillX();

        this.row().padTop(20);
        this.add("Menu volume:").padTop(20).left();
        this.row().padTop(10);
        this.add(menuVolumeSlider).width(300).fillX();

        this.row().padTop(20);
        this.add("Resolution:").padTop(10).left();
        this.row().padTop(10);
        this.add(resolutionSelect).width(300).fillX();

        this.row().padTop(20);
        this.add(fullscreenCheckbox).left();

        TextButton closeButton = new TextButton("Close", skin);
        TextButton applyButton = new TextButton("Apply", skin);

        applyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Menu.setMenuVolume(menuVolumeSlider.getValue());
                Menu.clickSoundId = Menu.clickSound.play();
                Menu.clickSound.setVolume(Menu.clickSoundId, Menu.getMenuVolume());
                Menu.menuMusic.setVolume(musicVolumeSlider.getValue());

                String selectedRes = resolutionSelect.getSelected();
                String[] dimensions = selectedRes.split("x");
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1]);

                if (fullscreenCheckbox.isChecked()) {
                    Graphics.DisplayMode displayMode = findBestDisplayMode(width, height);
                    Gdx.graphics.setFullscreenMode(displayMode);
                } else {
                    Gdx.graphics.setWindowedMode(width, height);
                }

                centerWindow();
            }
        });

        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Menu.clickSoundId = Menu.clickSound.play();
                Menu.clickSound.setVolume(Menu.clickSoundId, Menu.getMenuVolume());
                musicVolumeSlider.setValue(Menu.menuMusic.getVolume());
                setVisible(false);
            }
        });

        Table buttonTable = new Table();
        buttonTable.add(applyButton).padRight(20);
        buttonTable.add(closeButton);

        this.row().padTop(30);
        this.add(buttonTable).fillX();
    }

    private Graphics.DisplayMode findBestDisplayMode(int targetWidth, int targetHeight) {
        Graphics.DisplayMode[] displayModes = Gdx.graphics.getDisplayModes();
        Graphics.DisplayMode bestMode = Gdx.graphics.getDisplayMode();

        for (Graphics.DisplayMode mode : displayModes) {
            if (mode.width == targetWidth && mode.height == targetHeight) {
                return mode;
            }
        }

        int bestDiff = Integer.MAX_VALUE;
        for (Graphics.DisplayMode mode : displayModes) {
            int diff = Math.abs(mode.width - targetWidth) + Math.abs(mode.height - targetHeight);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestMode = mode;
            }
        }

        return bestMode;
    }

    private void centerWindow() {
        float centerX = (Gdx.graphics.getWidth() - this.getWidth()) / 2;
        float centerY = (Gdx.graphics.getHeight() - this.getHeight()) / 2;
        this.setPosition(centerX, centerY);
    }

    void setSlidersVolume(float musicVolume, float menuVolume) {
        musicVolumeSlider.setValue(musicVolume);
        menuVolumeSlider.setValue(menuVolume);
    }
}

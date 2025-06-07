package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class ExitDialog extends Window {
    private final Runnable onBackToGame;
    private final Runnable onExitToLobby;

    public ExitDialog(Skin skin, Runnable onBackToGame, Runnable onExitToLobby) {
        super("Exit", skin);
        this.onBackToGame = onBackToGame;
        this.onExitToLobby = onExitToLobby;

        // Titlul centrat
        getTitleLabel().setAlignment(Align.center);

        // Etichetă cu mesaj complet + wrap
        Label message = new Label("Are you sure you want to exit to the lobby?", skin);
        message.setWrap(true);
        message.setAlignment(Align.center);
        message.setWidth(400); // sau orice altă lățime care încapă bine

        // Butoane
        TextButton backButton = new TextButton("Back to Game", skin);
        TextButton exitButton = new TextButton("Exit to Lobby", skin);
        backButton.getLabel().setWrap(true);
        backButton.getLabel().setAlignment(Align.center);

        exitButton.getLabel().setWrap(true);
        exitButton.getLabel().setAlignment(Align.center);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onBackToGame != null) onBackToGame.run();
                remove();
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onExitToLobby != null) onExitToLobby.run();
                remove();
            }
        });

        // Organizare butoane
        Table buttonTable = new Table();
        buttonTable.add(backButton).width(220).height(100).padRight(20);
        buttonTable.add(exitButton).width(220).height(100);

        // Layout general
        this.pad(40);
        this.add(message).width(400).expandX().center().row();
        this.add(buttonTable).padTop(40).expandX().center();

        // Dimensiune și poziționare
        setSize(500, 300);
        centerWindow();
    }

    private void centerWindow() {
        float centerX = (Gdx.graphics.getWidth() - this.getWidth()) / 2;
        float centerY = (Gdx.graphics.getHeight() - this.getHeight()) / 2;
        this.setPosition(centerX, centerY);
    }
}


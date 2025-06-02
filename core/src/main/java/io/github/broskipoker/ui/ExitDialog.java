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

        getTitleLabel().setAlignment(Align.center);
        setSize(300, 200);
        centerWindow();

        Label message = new Label("Are you sure you want to exit to the lobby?", skin);
        message.setAlignment(Align.center);

        TextButton backButton = new TextButton("Back to Game", skin);
        TextButton exitButton = new TextButton("Exit to Lobby", skin);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onBackToGame != null) onBackToGame.run();
                setVisible(false);
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onExitToLobby != null) onExitToLobby.run();
                setVisible(false);
            }
        });

        // Layout
        this.add(message).padTop(30);
        this.row().padTop(30);
        Table buttonTable = new Table();
        buttonTable.add(backButton).padRight(20);
        buttonTable.add(exitButton);
        this.add(buttonTable);
    }

    private void centerWindow() {
        float centerX = (Gdx.graphics.getWidth() - this.getWidth()) / 2;
        float centerY = (Gdx.graphics.getHeight() - this.getHeight()) / 2;
        this.setPosition(centerX, centerY);
    }
}

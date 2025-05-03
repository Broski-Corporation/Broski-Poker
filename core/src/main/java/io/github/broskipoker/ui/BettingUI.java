/**
 * BettingUI.java
 * <p>
 * Handles the user interface for betting interactions in the poker game.
 * <p>
 * Responsibilities:
 * - Display betting controls (fold, check/call, raise buttons)
 * - Show current pot size and bet amounts
 * - Handle player betting input
 * - Animate chip movements for bets
 * - Display turn indicator
 */

package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;

public class BettingUI {
    private final PokerGame pokerGame;
    private final Stage stage;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Skin skin;

    // UI Components
    private final Table bettingTable;
    private Table backgroundTable;  // Adăugat pentru a controla vizibilitatea întregului UI
    private TextButton foldButton;
    private TextButton checkCallButton;
    private TextButton raiseButton;
    private Slider betSlider;
    private Label betAmountLabel;
    private Label potLabel;
    private Label currentBetLabel;
    private Label playerChipsLabel;
    private Label turnInfoLabel;

    // Chip textures for bet visualization
    private final Texture chipTexture;
    private final TextureRegion[] chipRegions;

    // Button textures
    private Texture buttonTexture;
    private Texture buttonDownTexture;

    // Player index for the human player
    private static final int HUMAN_PLAYER_INDEX = 3;

    // Constants for UI layout
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final int PADDING = 10;

    public BettingUI(PokerGame pokerGame, Stage stage) {
        this.pokerGame = pokerGame;
        this.stage = stage;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        font.getData().setScale(1.5f);

        // Create a simple skin
        skin = createSimpleSkin();

        // Load chip textures
        chipTexture = new Texture(Gdx.files.internal("textures/2x/chips.png"));
        chipRegions = new TextureRegion[5]; // Assuming 5 chip denominations
        for (int i = 0; i < 5; i++) {
            chipRegions[i] = new TextureRegion(chipTexture, i * 100, 0, 100, 100);
        }

        // Create main table for betting controls
        bettingTable = new Table();
        bettingTable.setFillParent(false);

        // Position the betting UI on the right side of the screen
        bettingTable.setPosition(
            Gdx.graphics.getWidth() * 0.75f,
            Gdx.graphics.getHeight() * 0.1f
        );
        bettingTable.setSize(400, 400);

        // Initialize betting UI components
        createBettingControls();

        // Add table to stage
        stage.addActor(bettingTable);

        // Initially hide the betting UI until the game starts
        setVisible(false);

        // Initially disable betting controls
        setButtonsEnabled(false);
    }

    private Skin createSimpleSkin() {
        Skin simpleSkin = new Skin();

        // Load button textures
        try {
            buttonTexture = new Texture(Gdx.files.internal("textures/button.png"));
            buttonDownTexture = new Texture(Gdx.files.internal("textures/button_down.png"));
        } catch (Exception e) {
            // Create default button textures if not found
            buttonTexture = createSolidColorTexture(Color.DARK_GRAY);
            buttonDownTexture = createSolidColorTexture(Color.GRAY);
        }

        // Create drawables
        NinePatchDrawable buttonUp = new NinePatchDrawable(
            new NinePatch(new TextureRegion(buttonTexture), 9, 9, 9, 9));
        NinePatchDrawable buttonDown = new NinePatchDrawable(
            new NinePatch(new TextureRegion(buttonDownTexture), 9, 9, 9, 9));
        NinePatchDrawable buttonDisabled = new NinePatchDrawable(
            new NinePatch(new TextureRegion(createSolidColorTexture(Color.GRAY)), 9, 9, 9, 9));

        // Add font to skin
        simpleSkin.add("default-font", font);

        // Create button styles
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = buttonUp;
        textButtonStyle.down = buttonDown;
        textButtonStyle.disabled = buttonDisabled;
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.disabledFontColor = Color.LIGHT_GRAY;

        // Create label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;

        // Create slider style
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = buttonUp;
        sliderStyle.knob = buttonDown;
        sliderStyle.disabledBackground = buttonDisabled;
        sliderStyle.disabledKnob = buttonDisabled;

        // Add styles to skin
        simpleSkin.add("default", textButtonStyle);
        simpleSkin.add("default", labelStyle);
        simpleSkin.add("default-horizontal", sliderStyle);

        return simpleSkin;
    }

    private Texture createSolidColorTexture(Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void createBettingControls() {
        // Create labels for game information
        potLabel = new Label("Pot: $0", skin);
        currentBetLabel = new Label("Current Bet: $0", skin);
        playerChipsLabel = new Label("Your Chips: $0", skin);
        turnInfoLabel = new Label("Waiting for other players...", skin);
        turnInfoLabel.setColor(Color.YELLOW);

        // Create bet slider and amount label
        betSlider = new Slider(0, 1000, 25, false, skin, "default-horizontal");
        betAmountLabel = new Label("Bet: $0", skin);

        // Create buttons
        foldButton = new TextButton("Fold", skin);
        checkCallButton = new TextButton("Check", skin);
        raiseButton = new TextButton("Raise", skin);

        // Set up button listeners
        foldButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!foldButton.isDisabled()) {
                    pokerGame.performAction(PokerGame.PlayerAction.FOLD, 0);
                    setButtonsEnabled(false);
                }
            }
        });

        checkCallButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!checkCallButton.isDisabled()) {
                    if (checkCallButton.getText().toString().equals("Check")) {
                        pokerGame.performAction(PokerGame.PlayerAction.CHECK, 0);
                    } else {
                        pokerGame.performAction(PokerGame.PlayerAction.CALL, 0);
                    }
                    setButtonsEnabled(false);
                }
            }
        });

        raiseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!raiseButton.isDisabled()) {
                    pokerGame.performAction(PokerGame.PlayerAction.RAISE, (int) betSlider.getValue());
                    setButtonsEnabled(false);
                }
            }
        });

        // Set up slider listener
        betSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateBetAmountLabel();
            }
        });

        // Add components to table with a clear layout
        bettingTable.add(turnInfoLabel).colspan(3).padBottom(PADDING * 2).row();
        bettingTable.add(currentBetLabel).colspan(3).padBottom(PADDING).row();
        bettingTable.add(playerChipsLabel).colspan(3).padBottom(PADDING * 2).row();

        bettingTable.add(betSlider).colspan(2).width(BUTTON_WIDTH * 2 + PADDING);
        bettingTable.add(betAmountLabel).padLeft(PADDING).row();

        bettingTable.add(foldButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(PADDING);
        bettingTable.add(checkCallButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(PADDING);
        bettingTable.add(raiseButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(PADDING);

        // Add a background to the table
        backgroundTable = new Table();
        backgroundTable.add(bettingTable).pad(PADDING * 2);
        backgroundTable.setPosition(
            Gdx.graphics.getWidth() * 0.75f,
            Gdx.graphics.getHeight() * 0.5f
        );
        backgroundTable.setSize(420, 420);
        stage.addActor(backgroundTable);
    }

    public void update() {
        // Update labels with current game state
        currentBetLabel.setText("Current Bet: $" + pokerGame.getCurrentBet());

        Player humanPlayer = pokerGame.getPlayers().get(HUMAN_PLAYER_INDEX);
        playerChipsLabel.setText("Your Chips: $" + humanPlayer.getChips());

        // Update turn info label
        if (pokerGame.getCurrentPlayerIndex() == HUMAN_PLAYER_INDEX && pokerGame.needsPlayerAction()) {
            turnInfoLabel.setText("It's your turn!");
            turnInfoLabel.setColor(Color.GREEN);
            setButtonsEnabled(true);
        } else if (pokerGame.needsPlayerAction()) {
            String currentPlayerName = pokerGame.getCurrentPlayer().getName();
            turnInfoLabel.setText("Waiting for " + currentPlayerName + "...");
            turnInfoLabel.setColor(Color.YELLOW);
            setButtonsEnabled(false);
        } else {
            turnInfoLabel.setText(getGameStateDescription());
            turnInfoLabel.setColor(Color.ORANGE);
            setButtonsEnabled(false);
        }

        // Update slider max value based on player's chips
        betSlider.setRange(pokerGame.getCurrentBet(), humanPlayer.getChips());

        // Update check/call button text based on current bet
        if (humanPlayer.getCurrentBet() < pokerGame.getCurrentBet()) {
            checkCallButton.setText("Call $" + (pokerGame.getCurrentBet() - humanPlayer.getCurrentBet()));
        } else {
            checkCallButton.setText("Check");
        }

        // If player doesn't have enough chips to raise, disable raise button
        raiseButton.setDisabled(humanPlayer.getChips() <= pokerGame.getCurrentBet());

        // Update bet amount label
        updateBetAmountLabel();
    }

    private String getGameStateDescription() {
        PokerGame.GameState state = pokerGame.getGameState();
        switch (state) {
            case BETTING_PRE_FLOP: return "Pre-Flop Betting";
            case BETTING_FLOP: return "Flop Betting";
            case BETTING_TURN: return "Turn Betting";
            case BETTING_RIVER: return "River Betting";
            case FLOP: return "Dealing Flop";
            case TURN: return "Dealing Turn";
            case RIVER: return "Dealing River";
            case SHOWDOWN: return "Showdown";
            default: return "Waiting...";
        }
    }

    private void updateBetAmountLabel() {
        betAmountLabel.setText("Bet: $" + (int) betSlider.getValue());
    }

    private int determineChipType(int value) {
        // Simple logic to determine chip color based on value
        if (value >= 500) return 4;      // Black
        else if (value >= 100) return 3; // Blue
        else if (value >= 50) return 2;  // Green
        else if (value >= 25) return 1;  // Red
        else return 0;                   // White
    }

    public void setButtonsEnabled(boolean enabled) {
        foldButton.setDisabled(!enabled);
        checkCallButton.setDisabled(!enabled);
        raiseButton.setDisabled(!enabled || pokerGame.getPlayers().get(HUMAN_PLAYER_INDEX).getChips() <= pokerGame.getCurrentBet());
        betSlider.setDisabled(!enabled);
    }

    // Metodă nouă pentru a controla vizibilitatea întregului BettingUI
    public void setVisible(boolean visible) {
        backgroundTable.setVisible(visible);
        bettingTable.setVisible(visible);
    }

    public boolean isVisible() {
        return backgroundTable.isVisible();
    }

    public void dispose() {
        chipTexture.dispose();
        buttonTexture.dispose();
        buttonDownTexture.dispose();
    }

    // Method to handle bot decisions automatically
    public void handleBotDecision(int playerIndex) {
        // Simple bot logic: always check if possible, otherwise call
        if (pokerGame.getCurrentPlayerIndex() == playerIndex) {
            Player botPlayer = pokerGame.getCurrentPlayer();
            if (botPlayer.getCurrentBet() < pokerGame.getCurrentBet()) {
                pokerGame.performAction(PokerGame.PlayerAction.CALL, 0);
            } else {
                pokerGame.performAction(PokerGame.PlayerAction.CHECK, 0);
            }
        }
    }
}

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
    private final Skin skin;
    private final FontManager fontManager;

    // Different fonts for different UI elements
    private final BitmapFont defaultFont;
    private final BitmapFont buttonFont;
    private final BitmapFont headerFont;
    private final BitmapFont valueFont;

    // UI Components
    private final Table bettingTable;
    private Table backgroundTable;  // Adăugat pentru a controla vizibilitatea întregului UI
    private TextButton foldButton;
    private TextButton checkCallButton;
    private TextButton raiseButton;
    private Label betAmountLabel;
    private Label potLabel;
    private Label currentBetLabel;
    private Label playerChipsLabel;
    private Label turnInfoLabel;

    // Preset bet buttons
    private TextButton minBetButton;
    private TextButton halfPotButton;
    private TextButton potButton;
    private TextButton allInButton;

    // Current bet amount
    private int currentBetAmount;

    // Chip textures for bet visualization
    private final Texture chipTexture;
    private final TextureRegion[] chipRegions;

    // Button textures
    private Texture buttonTexture;
    private Texture buttonDownTexture;

    // Player index for the human player
    private static final int HUMAN_PLAYER_INDEX = 3;

    // Bot delay
    private float botDecisionTimer = 0;
    private boolean isBotThinking = false;
    private int thinkingBotIndex = -1;
    private static final float BOT_THINKING_TIME = 3.0f;

    // Constants for UI layout
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final int PADDING = 10;

    public BettingUI(PokerGame pokerGame, Stage stage) {
        this.pokerGame = pokerGame;
        this.stage = stage;
        this.batch = new SpriteBatch();
        this.currentBetAmount = 0;

        // Initialize FontManager and create specific fonts for different UI elements
        this.fontManager = FontManager.getInstance();
        this.defaultFont = fontManager.getFont(16, Color.WHITE);
        this.buttonFont = fontManager.getFont(18, Color.WHITE);
        this.headerFont = fontManager.getFont(20, new Color(1, 0.8f, 0.2f, 1)); // Gold color
        this.valueFont = fontManager.getFont(16, new Color(0.2f, 0.8f, 1f, 1)); // Light blue

        // Create a simple skin with the fonts
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
            Gdx.graphics.getHeight() * 0.005f
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

        // Add fonts to skin
        simpleSkin.add("default-font", defaultFont);
        simpleSkin.add("button-font", buttonFont);
        simpleSkin.add("header-font", headerFont);
        simpleSkin.add("value-font", valueFont);

        // Create button style with better font
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = buttonUp;
        textButtonStyle.down = buttonDown;
        textButtonStyle.disabled = buttonDisabled;
        textButtonStyle.font = buttonFont; // Use the button-specific font
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.disabledFontColor = Color.LIGHT_GRAY;

        // Create different label styles
        Label.LabelStyle defaultLabelStyle = new Label.LabelStyle();
        defaultLabelStyle.font = defaultFont;
        defaultLabelStyle.fontColor = Color.WHITE;

        Label.LabelStyle headerLabelStyle = new Label.LabelStyle();
        headerLabelStyle.font = headerFont;
        headerLabelStyle.fontColor = new Color(1, 0.8f, 0.2f, 1); // Gold color

        Label.LabelStyle valueLabelStyle = new Label.LabelStyle();
        valueLabelStyle.font = valueFont;
        valueLabelStyle.fontColor = new Color(0.2f, 0.8f, 1f, 1); // Light blue

        // Create slider style
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = buttonUp;
        sliderStyle.knob = buttonDown;
        sliderStyle.disabledBackground = buttonDisabled;
        sliderStyle.disabledKnob = buttonDisabled;

        // Add styles to skin
        simpleSkin.add("default", textButtonStyle);
        simpleSkin.add("default", defaultLabelStyle);
        simpleSkin.add("header", headerLabelStyle);
        simpleSkin.add("value", valueLabelStyle);
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
        // Create labels for game information using different styles
        potLabel = new Label("Pot: $0", skin, "header");
        currentBetLabel = new Label("Current Bet: $0", skin, "value");
        playerChipsLabel = new Label("Your Chips: $0", skin, "value");
        turnInfoLabel = new Label("Waiting for other players...", skin, "header");

        // Create bet amount label
        betAmountLabel = new Label("Bet: $0", skin, "value");

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
                    pokerGame.performAction(PokerGame.PlayerAction.RAISE, currentBetAmount);
                    setButtonsEnabled(false);
                }
            }
        });

        // Create preset bet buttons table
        Table betButtonsTable = new Table();

        // Create preset bet buttons
        minBetButton = new TextButton("Min", skin);
        halfPotButton = new TextButton("1/2 Pot", skin);
        potButton = new TextButton("Pot", skin);
        allInButton = new TextButton("All-In", skin);

        // Add listeners to buttons
        minBetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setBetAmount(pokerGame.getCurrentBet());
            }
        });

        halfPotButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setBetAmount(pokerGame.getPot() / 2);
            }
        });

        potButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setBetAmount(pokerGame.getPot());
            }
        });

        allInButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Player humanPlayer = pokerGame.getPlayers().get(HUMAN_PLAYER_INDEX);
                setBetAmount(humanPlayer.getChips());
            }
        });

        // Add buttons to table
        betButtonsTable.add(minBetButton).pad(5);
        betButtonsTable.add(halfPotButton).pad(5);
        betButtonsTable.add(potButton).pad(5);
        betButtonsTable.add(allInButton).pad(5);

        // Add components to table with a clear layout
        bettingTable.add(turnInfoLabel).colspan(3).padBottom(PADDING * 2).row();
        bettingTable.add(currentBetLabel).colspan(3).padBottom(PADDING).row();
        bettingTable.add(playerChipsLabel).colspan(3).padBottom(PADDING * 2).row();

        // Add bet buttons table
        bettingTable.add(betButtonsTable).colspan(3).row();
        bettingTable.add(betAmountLabel).colspan(3).pad(10).row();

        bettingTable.add(foldButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(PADDING);
        bettingTable.add(checkCallButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(PADDING);
        bettingTable.add(raiseButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(PADDING);

        // Add a background to the table
        backgroundTable = new Table();
        backgroundTable.add(bettingTable).pad(PADDING * 1.25f);
        backgroundTable.setPosition(
            Gdx.graphics.getWidth() * 0.75f,
            Gdx.graphics.getHeight() * 0.03f
        );
        backgroundTable.setSize(420, 420);

        stage.addActor(backgroundTable);
    }

    public void update() {
        // Update labels with current game state
        currentBetLabel.setText("Current Bet: $" + pokerGame.getCurrentBet());

        Player humanPlayer = pokerGame.getPlayers().get(HUMAN_PLAYER_INDEX);
        playerChipsLabel.setText("Your Chips: $" + humanPlayer.getChips());

        // Update turn info label and button states
        if (pokerGame.needsPlayerAction()) {
            int currentPlayerIndex = pokerGame.getCurrentPlayerIndex();
            if (currentPlayerIndex == HUMAN_PLAYER_INDEX) {
                turnInfoLabel.setText("It's your turn!");
                setButtonsEnabled(true);
                isBotThinking = false; // Stop any bot thinking if it's human's turn
                thinkingBotIndex = -1;
            } else {
                // It's a bot's turn
                if (!isBotThinking || thinkingBotIndex != currentPlayerIndex) {
                    // Start thinking if not already or if the bot changed
                    handleBotDecision(currentPlayerIndex);
                }
                // Update label while bot is thinking
                String botName = pokerGame.getCurrentPlayer().getName();
                turnInfoLabel.setText(botName + " is thinking...");
                setButtonsEnabled(false);
            }
        } else {
            // No player action needed (e.g., dealing, showdown)
            turnInfoLabel.setText(getGameStateDescription());
            setButtonsEnabled(false);
            isBotThinking = false; // Stop thinking if action is no longer needed
            thinkingBotIndex = -1;
        }

        // Update check/call button text based on current bet for human player
        if (humanPlayer.getChips() <= pokerGame.getCurrentBet()) {
            checkCallButton.setText("All-In $" + humanPlayer.getChips());
            raiseButton.setDisabled(true); // Also disable raise if going all-in via call
        } else if (humanPlayer.getCurrentBet() < pokerGame.getCurrentBet()) {
            checkCallButton.setText("Call $" + (pokerGame.getCurrentBet() - humanPlayer.getCurrentBet()));
        } else {
            checkCallButton.setText("Check");
        }

        // If player doesn't have enough chips to make a minimum raise, disable raise button
        // A minimum raise is doubling the current bet level, or going all-in if less
        int minRaiseAmount = pokerGame.getCurrentBet() * 2;
        if (humanPlayer.getChips() + humanPlayer.getCurrentBet() <= pokerGame.getCurrentBet() || humanPlayer.getChips() <= 0) {
             raiseButton.setDisabled(true);
        } else if (raiseButton.isDisabled() && pokerGame.getCurrentPlayerIndex() == HUMAN_PLAYER_INDEX && pokerGame.needsPlayerAction()) {
             // Re-enable if conditions allow (and it's human's turn)
             raiseButton.setDisabled(false);
        }

        // Set minimum bet amount by default for raise slider/input (if implemented)
        if (currentBetAmount == 0) {
            setBetAmount(pokerGame.getCurrentBet()); // Default to current bet level
        }

        // Handle bot thinking timer
        if (isBotThinking && thinkingBotIndex == pokerGame.getCurrentPlayerIndex() && pokerGame.needsPlayerAction()) {
            botDecisionTimer -= Gdx.graphics.getDeltaTime();
            if (botDecisionTimer <= 0) {
                executeBotDecision(thinkingBotIndex);
                isBotThinking = false;
                thinkingBotIndex = -1;
            }
        }
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

    /**
     * Sets the bet amount and updates the bet amount label
     * @param amount The bet amount to set
     */
    private void setBetAmount(int amount) {
        Player humanPlayer = pokerGame.getPlayers().get(HUMAN_PLAYER_INDEX);

        // Ensure bet amount is not greater than player's chips
        int maxBet = humanPlayer.getChips();
        int minBet = pokerGame.getCurrentBet();

        // Constrain bet amount between min bet and max chips
        currentBetAmount = Math.min(maxBet, Math.max(minBet, amount));

        // Update bet amount label
        betAmountLabel.setText("Bet: $" + currentBetAmount);
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

        // Enable/disable bet preset buttons
        minBetButton.setDisabled(!enabled);
        halfPotButton.setDisabled(!enabled);
        potButton.setDisabled(!enabled);
        allInButton.setDisabled(!enabled);
    }

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
        // FontManager handles font disposal
    }

    // Method to handle bot decisions automatically
    public void handleBotDecision(int playerIndex) {
        // Check if it's the correct bot's turn and action is needed
        if (pokerGame.getCurrentPlayerIndex() == playerIndex && pokerGame.needsPlayerAction() && !isBotThinking) {
            // Start the timer for bot thinking
            isBotThinking = true;
            thinkingBotIndex = playerIndex;
            botDecisionTimer = BOT_THINKING_TIME; // Reset timer

            String botName = pokerGame.getPlayers().get(playerIndex).getName();
            turnInfoLabel.setText(botName + " is thinking..."); // Update label
            setButtonsEnabled(false); // Ensure human controls are disabled
        }
    }

    private void executeBotDecision(int playerIndex) {
        // Ensure it's still this bot's turn before acting
        if (pokerGame.getCurrentPlayerIndex() == playerIndex && pokerGame.needsPlayerAction()) {
            // Simple bot logic: always check if possible, otherwise call
            Player botPlayer = pokerGame.getCurrentPlayer();

            // Basic decision logic (can be expanded)
            if (botPlayer.getCurrentBet() < pokerGame.getCurrentBet()) {
                pokerGame.performAction(PokerGame.PlayerAction.CALL, 0);
            } else {
                pokerGame.performAction(PokerGame.PlayerAction.CHECK, 0);
            }
        }
        // Reset thinking state after action attempt
        isBotThinking = false;
        thinkingBotIndex = -1;
    }
}

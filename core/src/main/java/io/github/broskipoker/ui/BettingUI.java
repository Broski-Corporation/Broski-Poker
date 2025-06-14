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
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.game.PokerHand;
import io.github.broskipoker.server.ClientConnection;
import io.github.broskipoker.shared.PlayerAction;
import java.util.List;

import java.util.Objects;

public class BettingUI {
    private final PokerGame pokerGame;
    private final Stage stage;
    private final SpriteBatch batch;
    private final Skin skin;
    private final FontManager fontManager;
    private final GameController gameController;

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
    private TextField betTextField;
    private TextButton setBetButton;

    // Current bet amount
    private int currentBetAmount;

    // For displaying the winning hand at showdown
    private GameRenderer gameRenderer;
    private List<Card> winningCards = null;

    // Chip textures for bet visualization
    private final Texture chipTexture;
    private final TextureRegion[] chipRegions;

    // Button textures
    private Texture buttonTexture;
    private Texture buttonDownTexture;
    private Texture lineCursorTexture;

    // Player index for the human player
    private int humanPlayerIndex = 3;

    // Constants for UI layout
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final int PADDING = 10;

    // Sound manager
    private final SoundManager soundManager = SoundManager.getInstance();

    // Multiplayer
    private ClientConnection clientConnection;
    private boolean isMultiplayer = false;
    private String currentUsername;

    public BettingUI(PokerGame pokerGame, Stage stage, GameRenderer gameRenderer) {
        this(pokerGame, stage, null, gameRenderer);
    }

    public BettingUI(PokerGame pokerGame, Stage stage, GameController gameController, GameRenderer gameRenderer) {
        this.pokerGame = pokerGame;
        this.stage = stage;
        this.batch = new SpriteBatch();
        this.currentBetAmount = 0;
        this.gameController = gameController;
        this.gameRenderer = gameRenderer;

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

    public void setMultiplayerMode(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
        this.isMultiplayer = true;

        // Update the betting UI to reflect multiplayer mode
//        if (clientConnection != null) {
//            clientConnection.addGameStateListener(this::updateFromServer); // TODO
//        }
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

        // Text field for custom bet
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        lineCursorTexture = createLineCursorTexture();
        textFieldStyle.font = valueFont;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.messageFont = fontManager.getFont(16, Color.GRAY); // Use a dedicated gray font
        textFieldStyle.background = buttonUp;
        textFieldStyle.cursor = new TextureRegionDrawable(new TextureRegion(lineCursorTexture));
        textFieldStyle.disabledBackground = buttonDisabled; // Add disabled background
        textFieldStyle.disabledFontColor = Color.WHITE; // Add disabled font color

        // Add styles to skin
        simpleSkin.add("default", textButtonStyle);
        simpleSkin.add("default", defaultLabelStyle);
        simpleSkin.add("header", headerLabelStyle);
        simpleSkin.add("value", valueLabelStyle);
        simpleSkin.add("default", textFieldStyle);
        simpleSkin.add("default-horizontal", sliderStyle);

        return simpleSkin;
    }

    // Create a custom narrow cursor texture
    private Texture createLineCursorTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(2, 32,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.2f, 0.6f, 1.0f, 0.8f));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
                    SoundManager.getInstance().playButtonSound();
                    if (isMultiplayer) {
                        PlayerAction action = new PlayerAction();
                        action.action = PokerGame.PlayerAction.FOLD;
                        action.amount = 0;
                        action.tableCode = pokerGame.getTableCode();
                        clientConnection.sendAction(action);
                    } else {
                        pokerGame.performAction(PokerGame.PlayerAction.FOLD, 0);
                    }
                    setButtonsEnabled(false);
                }
            }
        });

        checkCallButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!checkCallButton.isDisabled()) {
                    SoundManager.getInstance().playButtonSound();
                    if (checkCallButton.getText().toString().equals("Check")) {
                        if (isMultiplayer) {
                            PlayerAction action = new PlayerAction();
                            action.action = PokerGame.PlayerAction.CHECK;
                            action.amount = 0;
                            action.tableCode = pokerGame.getTableCode();
                            clientConnection.sendAction(action);
                        } else {
                            pokerGame.performAction(PokerGame.PlayerAction.CHECK, 0);
                        }
                    } else {
                        if (isMultiplayer) {
                            PlayerAction action = new PlayerAction();
                            action.action = PokerGame.PlayerAction.CALL;
                            int playerIndex = findHumanPlayerIndex();
                            action.amount = pokerGame.getCurrentBet() - pokerGame.getPlayers().get(playerIndex).getCurrentBet();
                            action.tableCode = pokerGame.getTableCode();
                            clientConnection.sendAction(action);
                        } else {
                            pokerGame.performAction(PokerGame.PlayerAction.CALL, 0);
                        }
                    }
                    setButtonsEnabled(false);
                }
            }
        });

        raiseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!raiseButton.isDisabled()) {
                    SoundManager.getInstance().playButtonSound();
                    if (isMultiplayer) {
                        PlayerAction action = new PlayerAction();
                        action.action = PokerGame.PlayerAction.RAISE;
                        action.amount = currentBetAmount;
                        action.tableCode = pokerGame.getTableCode();
                        clientConnection.sendAction(action);
                    } else {
                        pokerGame.performAction(PokerGame.PlayerAction.RAISE, currentBetAmount);
                    }
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
                if (!minBetButton.isDisabled()) {
                    SoundManager.getInstance().playButtonSound();
                    setBetAmount(pokerGame.getCurrentBet());
                }
            }
        });

        halfPotButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!halfPotButton.isDisabled()) {
                    SoundManager.getInstance().playButtonSound();
                    setBetAmount(pokerGame.getPot() / 2);
                }
            }
        });

        potButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!potButton.isDisabled()) {
                    SoundManager.getInstance().playButtonSound();
                    setBetAmount(pokerGame.getPot());
                }
            }
        });

        allInButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!allInButton.isDisabled()) {
                    SoundManager.getInstance().playButtonSound();
                    int playerIndex = findHumanPlayerIndex();
                    Player humanPlayer = pokerGame.getPlayers().get(playerIndex);
                    setBetAmount(humanPlayer.getChips());
                }
            }
        });

        // Create text field
        betTextField = new TextField("", skin);
        betTextField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        betTextField.setMaxLength(6);
        betTextField.setMessageText("Custom");

        // Add listener to update bet amount as user types
        betTextField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().playButtonSound();
                try {
                    String text = betTextField.getText().trim();
                    if (!text.isEmpty()) {
                        int amount = Integer.parseInt(text);
                        setBetAmount(amount);
                    }
                } catch (NumberFormatException e) {
                    // Ignore parsing errors while typing
                }
            }
        });

        // Change cursor when hovering over the text field
        betTextField.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !betTextField.isDisabled()) { // Only respond to mouse events, not touch
                    Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Ibeam);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow);
                }
            }
        });

        // Change the cursor when hovering over the button
        addCursorChangeListener(foldButton);
        addCursorChangeListener(checkCallButton);
        addCursorChangeListener(raiseButton);
        addCursorChangeListener(minBetButton);
        addCursorChangeListener(halfPotButton);
        addCursorChangeListener(potButton);
        addCursorChangeListener(allInButton);

        // Add buttons to table
        betButtonsTable.add(minBetButton).pad(5);
        betButtonsTable.add(halfPotButton).pad(5);
        betButtonsTable.add(potButton).pad(5);
        betButtonsTable.add(allInButton).pad(5);
        betButtonsTable.add(betTextField).width(80).height(43).pad(5);

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

    // Changes the cursor when hovering over the button
    private void addCursorChangeListener(Button button) {
        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !button.isDisabled()) {
                    Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Hand);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow);
                }
            }
        });
    }

    public void update() {
        // Update labels with current game state
        currentBetLabel.setText("Current Bet: $" + pokerGame.getCurrentBet());

        int playerIndex = findHumanPlayerIndex();
        Player humanPlayer = pokerGame.getPlayers().get(playerIndex);
        playerChipsLabel.setText("Your Chips: $" + humanPlayer.getChips());

        // Update turn info label and button states based on whose turn it is
        if (pokerGame.needsPlayerAction()) {
//            System.out.println("pokerGame.getCurrentPlayerIndex() " + pokerGame.getCurrentPlayerIndex());
//            System.out.println("playerIndex " + playerIndex); // 0
            int currentPlayerIndex = pokerGame.getCurrentPlayerIndex();

            if (currentPlayerIndex == playerIndex) {
                // It's human player's turn
                turnInfoLabel.setText("It's your turn!");
                setButtonsEnabled(true);
            } else {
                // It's a bot's turn - use GameController to check bot thinking status
                if (gameController != null && gameController.isBotThinking()) {
                    String botName = pokerGame.getPlayers().get(currentPlayerIndex).getName();
                    turnInfoLabel.setText(botName + " is thinking...");
                } else {
                    turnInfoLabel.setText("Waiting for " + pokerGame.getCurrentPlayer().getName());
                }
                setButtonsEnabled(false);
            }
        } else {
            // No player action needed (e.g., dealing, showdown)
            turnInfoLabel.setText(getGameStateDescription());
            setButtonsEnabled(false);
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
        // int playerIndex = findHumanPlayerIndex(); // to see what is @playerIndex
        if (humanPlayer.getChips() + humanPlayer.getCurrentBet() <= pokerGame.getCurrentBet() || humanPlayer.getChips() <= 0) {
             raiseButton.setDisabled(true);
        } else if (raiseButton.isDisabled() && pokerGame.getCurrentPlayerIndex() == playerIndex && pokerGame.needsPlayerAction()) {
             // Re-enable if conditions allow (and it's human's turn)
             raiseButton.setDisabled(false);
        }

        // Set minimum bet amount by default for raise slider/input (if implemented)
        if (currentBetAmount == 0) {
            setBetAmount(pokerGame.getCurrentBet()); // Default to current bet level
        }

        if (pokerGame.getGameState() == PokerGame.GameState.SHOWDOWN) {
            List<Player> winners = pokerGame.determineWinners();
            int pot = pokerGame.getPot();

            if (!winners.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                if (winners.size() == 1) {
                    Player winner = winners.get(0);
                    PokerHand hand = new PokerHand(winner.getHoleCards(), pokerGame.getCommunityCards());

                    // Get the best hand for rendering during showdown
                    List<Card> bestHand = hand.getBestHand();
                    gameRenderer.renderWinningHand(bestHand);

                    sb.append(winner.getName())
                        .append(" wins the pot ($")
                        .append(pot)
                        .append(") with ")
                        .append(hand.getRank().toString().replace('_', ' ').toLowerCase());
                } else {
                    sb.append("Split pot ($").append(pot).append(") between: ");
                    for (Player winner : winners) {
                        PokerHand hand = new PokerHand(winner.getHoleCards(), pokerGame.getCommunityCards());
                        sb.append(winner.getName())
                            .append(" (")
                            .append(hand.getRank().toString().replace('_', ' ').toLowerCase())
                            .append("), ");
                    }
                    // Remove trailing comma and space
                    sb.setLength(sb.length() - 2);
                }
                turnInfoLabel.setText(sb.toString());
            } else {
                turnInfoLabel.setText("No winner.");
            }
            setButtonsEnabled(false);
            return;
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
        int playerIndex = findHumanPlayerIndex();
        Player humanPlayer = pokerGame.getPlayers().get(playerIndex);

        // Ensure bet amount is not greater than player's chips
        int maxBet = humanPlayer.getChips();
        int minBet = pokerGame.getCurrentBet();

        // Constrain bet amount between min bet and max chips
        currentBetAmount = Math.min(maxBet, Math.max(minBet, amount));

        // Update bet amount label
        betAmountLabel.setText("Your current bet: $" + currentBetAmount);
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
        int playerIndex = findHumanPlayerIndex();
        raiseButton.setDisabled(!enabled || pokerGame.getPlayers().get(playerIndex).getChips() <= pokerGame.getCurrentBet());

        // Enable/disable bet preset buttons
        minBetButton.setDisabled(!enabled);
        halfPotButton.setDisabled(!enabled);
        potButton.setDisabled(!enabled);
        allInButton.setDisabled(!enabled);
        betTextField.setDisabled(!enabled);

        // (For custom bet field) Change the placeholder text color based on the enabled state
        TextField.TextFieldStyle style = betTextField.getStyle();
        if (enabled) {
            style.messageFont = fontManager.getFont(16, Color.GRAY);
        } else {
            style.messageFont = fontManager.getFont(16, Color.WHITE);
            betTextField.setText("");
        }
        betTextField.setStyle(style);
        betTextField.setDisabled(!enabled);

        betAmountLabel.setVisible(enabled);
    }


    public void setVisible(boolean visible) {
        backgroundTable.setVisible(visible);
        bettingTable.setVisible(visible);
    }

    public boolean isVisible() {
        return backgroundTable.isVisible();
    }

    public void setMultiplayerMode(ClientConnection clientConnection, String username) {
        this.clientConnection = clientConnection;
        this.isMultiplayer = true;
        this.currentUsername = username;
    }

    private int findHumanPlayerIndex() {
        if (!isMultiplayer) {
            return 3; // default for singleplayer
        }

        // in multiplayer, find player by username
        List<Player> players = pokerGame.getPlayers();
        if (players.isEmpty()) {
            return 0; // no players yet, return the first index
        }

        // search current player by the username
        if (currentUsername != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).getName().equals(currentUsername)) {
                    return i;
                }
            }
        }

        // default to first player if not found
        return 0;
    }

    /**
     * Maps server-side player indices to UI positions, accounting for dealer position rotation
     * @param serverPlayerIndex The player index from the server's perspective
     * @return The UI position where this player should be displayed
     */
    private int getUIPositionForPlayer(int serverPlayerIndex) {
        if (!isMultiplayer) {
            return serverPlayerIndex; // keep original index for singleplayer
        }

        int localPlayerIndex = findHumanPlayerIndex();
        int playerCount = pokerGame.getPlayers().size();
        int dealerPosition = PokerGame.getDealerPosition();

        // Adjust player positions based on dealer position to maintain correct betting order
        // In poker, the order of play rotates with the dealer button
        // We need to calculate positions relative to dealer + current player to get proper order
        int adjustedServerIndex = (serverPlayerIndex + playerCount - dealerPosition) % playerCount;
        int adjustedLocalIndex = (localPlayerIndex + playerCount - dealerPosition) % playerCount;

        // Calculate relative position with the dealer position adjustment
        int relativePosition = (adjustedServerIndex - adjustedLocalIndex + playerCount) % playerCount;

        // Map relative position to fixed UI positions
        // 0 = top left, 1 = top right, 2 = middle right, 3 = bottom right, 4 = bottom left
        // We want current player (relative position 0) to always be at position 3 (bottom right)
        return switch (relativePosition) {
            case 0 -> 3; // current player bottom right
            case 1 -> 4; // next player clockwise bottom left
            case 2 -> 0; // next player clockwise top left
            case 3 -> 1; // next player clockwise top right
            case 4 -> 2; // next player clockwise middle right
            default -> 3; // fallback to current player position
        };
    }

    public void dispose() {
        chipTexture.dispose();
        buttonTexture.dispose();
        buttonDownTexture.dispose();
        lineCursorTexture.dispose();
        // FontManager handles font disposal
    }
}

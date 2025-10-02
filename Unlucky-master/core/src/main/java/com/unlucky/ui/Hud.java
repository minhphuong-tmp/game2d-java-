package com.unlucky.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.unlucky.effects.Moving;
import com.unlucky.entity.Player;
import com.unlucky.event.EventState;
import com.unlucky.inventory.Inventory;
import com.unlucky.inventory.Item;
import com.unlucky.main.Unlucky;
import com.unlucky.map.TileMap;
import com.unlucky.map.WeatherType;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.GameScreen;
import com.unlucky.screen.homework.DefenseSystemsManager;

/**
 * Handles button input and everything not in the game camera
 *
 * @author Ming Li
 */
public class Hud extends UI {

    // directional pad: index i 0 - down, 1 - up, 2 - right, 3 - left
    private ImageButton[] dirPad;
    // if dir pad is held down
    public boolean touchDown = false;
    public int dirIndex = -1;
    // for changing the player's facing direction with a short tap like in pokemon
    private float dirTime = 0;
    private ImageButton shieldButton;

    private ImageButton spawnSlowObjectsButton;

    // private ImageButton slowObjectsButton; // nút mới - không dùng nữa
    public boolean slowMotion = false; // trạng thái slow motion, public để GameScreen truy cập
    // shoot button
    private ImageButton shootButton;
    private ImageButton leftButton1;
    private ImageButton leftButton2;

    private ImageButton spawnWindWallButton;

    private Image windWallImage;

    // window that slides on the screen to show the world and level
    private Window levelDescriptor;
    private Label levelDesc;
    private Moving levelMoving;
    private boolean ld = false;
    private float showTime = 0;

    // death screen that is a prompt with the map background dimmed out
    public Group deathGroup;
    private Image dark;
    private Image frame;
    private Label youDied;
    private Label loss;
    
    // Game over statistics labels
    private Label currentStatsLabel;
    private Label highScoresLabel;

    public Image shade;
    public Dialog settingsDialog;
    
    // Debug text display
    private String debugText = "";
    private float debugTextTimer = 0f;

    // NEW: Reference to DefenseSystemsManager for modular defense controls
    private DefenseSystemsManager defenseSystems;
    
    // === Score and Timer System ===
    private Label scoreLabel;
    private int score = 0;
    private Label timerLabel;
    private float gameTime = 0f; // Game time in seconds
    
    // === HP System ===
    private Label hpLabel;
    
    // === Mana System ===
    private Label manaLabel;

    public Hud(final GameScreen gameScreen, TileMap tileMap, final Player player, final ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);
        // Dùng hình icon wind wall từ rm.shopitems[9][0] hoặc texture riêng
        windWallImage = new Image(rm.shopitems[9][0]);

// Kích thước giống tường gió (tùy chỉnh theo map)
        windWallImage.setSize(32, 32);

// Vị trí cố định trên map (không theo player)
        windWallImage.setPosition(100, 150); // chỉnh theo map

// Ban đầu ẩn
        windWallImage.setVisible(false);

// Thêm animation nhấp nháy (giống tường gió động)
        windWallImage.addAction(Actions.forever(Actions.sequence(
                Actions.fadeOut(0.5f),
                Actions.fadeIn(0.5f)
        )));

// Thêm vào stage
        stage.addActor(windWallImage);

        createDirPad();
        createOptionButtons();
        createShootButton();
        createLeftButtons();
        createShieldButton(); // Phải tạo trước
        createSlowMotionButton();
        createWindWallButton();
        
        // Create score and timer UI elements
        createScoreLabel();
        createTimerLabel();
        createHpLabel();
        createManaLabel();
        
        // Initialize high scores system
        loadHighScores();
        
        // Initialize player HP system
        initializePlayerHP();
        
        // Initialize player mana system
        initializePlayerMana();
        
        // Load high scores
        loadHighScores();

        Gdx.app.log("Hud", "Hud created with kick button");
        createLevelDescriptor();
        createDeathPrompt();

        shade = new Image(rm.shade);
        shade.setVisible(false);
        shade.setTouchable(Touchable.disabled);
        stage.addActor(shade);

        settingsDialog = new Dialog("Paused", rm.dialogSkin) {
            {
                getButtonTable().defaults().width(50);
                getButtonTable().defaults().height(15);
                TextButton b = new TextButton("Back", rm.dialogSkin);
                b.getLabel().setFontScale(0.75f);
                button(b, "back");
                getButtonTable().padTop(-6).row();
                TextButton s = new TextButton("Settings", rm.dialogSkin);
                s.getLabel().setFontScale(0.75f);
                button(s, "settings");
                getButtonTable().row();
                TextButton q = new TextButton("Quit", rm.dialogSkin);
                q.getLabel().setFontScale(0.75f);
                button(q, "quit");
            }
            @Override
            protected void result(Object object) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                if (object.equals("back")) {
                    shade.setVisible(false);
                    toggle(true);

                    // play music and sfx
                    if (!player.settings.muteMusic) gameScreen.gameMap.mapTheme.play();
                    if (!player.settings.muteSfx) {
                        if (gameScreen.gameMap.weather == WeatherType.RAIN) {
                            gameScreen.gameMap.soundId = rm.lightrain.play(player.settings.sfxVolume);
                            rm.lightrain.setLooping(gameScreen.gameMap.soundId, true);
                        }
                        else if (gameScreen.gameMap.weather == WeatherType.HEAVY_RAIN || gameScreen.gameMap.weather == WeatherType.THUNDERSTORM) {
                            gameScreen.gameMap.soundId = rm.heavyrain.play(player.settings.sfxVolume);
                            rm.heavyrain.setLooping(gameScreen.gameMap.soundId, true);
                        }
                    }

                    gameScreen.setCurrentEvent(EventState.MOVING);
                }
                else if (object.equals("settings")) {
                    game.settingsScreen.inGame = true;
                    game.settingsScreen.worldIndex = gameScreen.gameMap.worldIndex;
                    if (gameScreen.isClickable()) {
                        gameScreen.setClickable(false);
                        gameScreen.setBatchFade(false);
                        // fade out animation
                        stage.addAction(Actions.sequence(Actions.fadeOut(0.3f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    gameScreen.setClickable(true);
                                    game.setScreen(game.settingsScreen);
                                }
                            })));
                    }
                }
                else if (object.equals("quit")) {
                    quit();
                }
            }
        };
        settingsDialog.getTitleLabel().setAlignment(Align.center);
        settingsDialog.getBackground().setMinWidth(70);
    }
    private void createWindWallButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(rm.shopitems[9][0]); // icon wind wall
        style.down = new TextureRegionDrawable(rm.shopitems[9][0]);

        spawnWindWallButton = new ImageButton(style);
        spawnWindWallButton.setSize(24, 24);

        // Đặt bên phải nút Slow Motion
        float x = spawnSlowObjectsButton.getX() + spawnSlowObjectsButton.getWidth() + 5;
        float y = spawnSlowObjectsButton.getY();
        spawnWindWallButton.setPosition(x, y);

        spawnWindWallButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Hud", "=== WIND WALL BUTTON CLICKED - PUSHING OBJECTS AWAY ===");

                // NEW: Use DefenseSystemsManager if available
                if (defenseSystems != null) {
                    defenseSystems.activateWindWall();
                } else {
                    // Fallback to old method
                    if (gameScreen != null) {
                        gameScreen.pushAllObjectsAway();
                    } else {
                        Gdx.app.log("Hud", "ERROR: gameScreen is null!");
                    }
                }
            }
        });

        stage.addActor(spawnWindWallButton);
    }

    private void createSlowMotionButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(rm.shopitems[8][0]); // icon slow
        style.down = new TextureRegionDrawable(rm.shopitems[8][0]);

        spawnSlowObjectsButton = new ImageButton(style);
        spawnSlowObjectsButton.setSize(24, 24);

        // Đặt bên phải nút Shield
        float x = shieldButton.getX() + shieldButton.getWidth() + 5;
        float y = shieldButton.getY();
        spawnSlowObjectsButton.setPosition(x, y);

        spawnSlowObjectsButton.setPosition(x, y);

        spawnSlowObjectsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // NEW: Use DefenseSystemsManager if available
                if (defenseSystems != null) {
                    defenseSystems.activateSlowMotion();
                } else {
                    // Fallback to old method
                    slowMotion = !slowMotion; // toggle trạng thái chậm
                    Gdx.app.log("Hud", "Slow Motion toggled: " + slowMotion);
                }
            }
        });

        stage.addActor(spawnSlowObjectsButton);
    }


    private void createShieldButton() {
        ImageButton.ImageButtonStyle shieldStyle = new ImageButton.ImageButtonStyle();
        shieldStyle.up = new TextureRegionDrawable(rm.shopitems[7][0]);
        shieldStyle.down = new TextureRegionDrawable(rm.shopitems[7][0]);

        shieldButton = new ImageButton(shieldStyle);
        shieldButton.setSize(24, 24);

        // Đặt ở góc trên phải (dịch sang trái một tí tẹo)
        float shieldX = 155 - 30 - 20 + 15 - 10; // Dịch sang trái 10px
        float shieldY = 100 - 15; // Giữ nguyên vị trí Y
        shieldButton.setPosition(shieldX, shieldY);

        Gdx.app.log("ShieldButton", "Position: " + shieldX + ", " + shieldY);

        shieldButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("ShieldButton", "=== SHIELD BUTTON CLICKED ===");
                
                // Shield is now completely controlled by DefenseSystemsManager
                if (defenseSystems != null) {
                    boolean before = defenseSystems.isShieldActive();
                    defenseSystems.activateShield();
                    boolean after = defenseSystems.isShieldActive();
                    Gdx.app.log("ShieldButton", "Shield state: " + before + " -> " + after);
                } else {
                    Gdx.app.log("ShieldButton", "ERROR: DefenseSystemsManager is null!");
                }
            }
        });

        stage.addActor(shieldButton);
    }
    public void update(float dt) {
        if (touchDown) {
            dirTime += dt;
            // quick tap to change direction
            if (dirTime > 0 && dirTime <= 0.15f) player.getAm().setAnimation(dirIndex);
            // move the player
            else movePlayer(dirIndex);
        }
        else {
            player.getAm().stopAnimation();
        }

        if (ld) {
            levelMoving.update(dt);
            levelDescriptor.setPosition(levelMoving.position.x, levelMoving.position.y);
            if (!levelMoving.shouldStart) showTime += dt;
            // after 3 seconds of showing moves back to the starting position
            if (showTime >= 3) {
                showTime = 0;
                float y = 116 - levelDescriptor.getPrefHeight();
                levelMoving.origin.set(4, y);
                levelMoving.target.set(-levelDescriptor.getPrefWidth(), y);
                levelMoving.start();
            }
            if (levelMoving.target.x == -levelDescriptor.getPrefWidth() &&
                levelMoving.position.x == levelMoving.target.x) {
                ld = false;
                showTime = 0;
                levelDescriptor.setVisible(false);
            }
        }
        
        // Update game timer only if game is not over
        if (!isGameOver) {
            updateTimer(dt);
            
            // Update mana regeneration
            updateManaRegeneration(dt);
        }
    }

    public void render(float dt) {
        // Update debug text timer
        if (debugTextTimer > 0) {
            debugTextTimer -= dt;
            if (debugTextTimer <= 0) {
                debugText = "";
            }
        }
        
        stage.act(dt);
        stage.draw();
        
        // Draw debug text on screen
        if (debugTextTimer > 0 && !debugText.isEmpty()) {
            // Simple debug text rendering - you can enhance this later
            Gdx.app.log("HudRender", "DEBUG: " + debugText);
        }
    }

    /**
     * Starts the slide in animation of the level descriptor when
     * the player first enters the level.
     */
    public void startLevelDescriptor() {
        int worldIndex = gameScreen.gameMap.worldIndex;
        int levelIndex = gameScreen.gameMap.levelIndex;
        String worldName = rm.worlds.get(worldIndex).name;
        String levelName = rm.worlds.get(worldIndex).levels[levelIndex].name;

        levelDescriptor.getTitleLabel().setText("WORLD " + (worldIndex + 1) + " : " + "LEVEL " + (levelIndex + 1));
        levelDesc.setText(worldName + "\n" + levelName + "\n" + "AVG LVL: " + rm.worlds.get(worldIndex).levels[levelIndex].avgLevel);
        levelDescriptor.setVisible(true);
        levelDescriptor.pack();

        float y = 116 - levelDescriptor.getPrefHeight();
        levelMoving.origin.set(-levelDescriptor.getPrefWidth(), y);
        levelMoving.target.set(4, y);

        ld = true;
        levelMoving.start();
    }

    /**
     * Turns the HUD on and off when another event occurs
     *
     * @param toggle
     */
    public void toggle(boolean toggle) {
        if (toggle) {
            gameScreen.getGame().fps.setPosition(5, 5);
            stage.addActor(gameScreen.getGame().fps);
        }
        for (int i = 0; i < 4; i++) dirPad[i].setVisible(toggle);
        // Không cần toggle optionButtons nữa vì đã bỏ
        levelDescriptor.setVisible(toggle);
    }

    /**
     * Draws the directional pad and applies Drawable effects
     * Unfortunately have to do each one separately
     */
    private void createDirPad() {
        dirPad = new ImageButton[4];

        // when each button is pressed it changes for a more visible effect
        ImageButton.ImageButtonStyle[] styles = rm.loadImageButtonStyles(4, rm.dirpad20x20);

        // down
        dirPad[0] = new ImageButton(styles[0]);
        dirPad[0].setPosition(Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET, Util.DIR_PAD_OFFSET);
        // up
        dirPad[1] = new ImageButton(styles[1]);
        dirPad[1].setPosition(Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET, (Util.DIR_PAD_SIZE * 2) + Util.DIR_PAD_OFFSET);
        // right
        dirPad[2] = new ImageButton(styles[2]);
        dirPad[2].setPosition((Util.DIR_PAD_SIZE * 2) + Util.DIR_PAD_OFFSET, Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET);
        // left
        dirPad[3] = new ImageButton(styles[3]);
        dirPad[3].setPosition(Util.DIR_PAD_OFFSET, Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET);

        handleDirPadEvents();

        for (int i = 0; i < dirPad.length; i++) {
            stage.addActor(dirPad[i]);
        }
    }

    /**
     * Creates the two option buttons: inventoryUI and settings
     */
    private void createOptionButtons() {
        // Bỏ luôn cả inventory và settings buttons để có chỗ cho 3 nút mới
        // No buttons created anymore
        
        // Không cần handleOptionEvents() nữa vì không có button
    }

    /**
     * Creates the shoot button
     */
    private void createShootButton() {
        // Create shoot button using gun icon (lightning)
        ImageButton.ImageButtonStyle shootStyle = new ImageButton.ImageButtonStyle();
        // Use Wooden Bow as shoot icon
        shootStyle.up = new TextureRegionDrawable(rm.shopitems[3][1]);
        shootStyle.down = new TextureRegionDrawable(rm.shopitems[3][1]);
        shootButton = new ImageButton(shootStyle);
        
        // Make button smaller
        shootButton.setSize(24, 24);
        
        // Position the shoot button at bottom right corner
        // Screen width: 200, button size: 24, margin: 5
        shootButton.setPosition(200 - 24 - 5, 5);
        
        // Add click listener
        shootButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                    // Only shoot when in MOVING or BATTLING state
                    if (gameScreen.currentEvent == EventState.MOVING ||
                        gameScreen.currentEvent == EventState.BATTLING) {

                        // Get player position
                        float playerX = gameScreen.gameMap.player.getPosition().x;
                        float playerY = gameScreen.gameMap.player.getPosition().y;

                        // NEW: Use AttackSystemsManager if available
                        if (gameScreen.attackSystems != null) {
                            gameScreen.attackSystems.fireBullet(playerX, playerY + 8);
                            Gdx.app.log("ShootButton", "Bullet fired via AttackSystemsManager at: " + playerX + ", " + playerY);
                        } else {
                            Gdx.app.log("ShootButton", "ERROR: AttackSystemsManager is null!");
                        }
                    }
            }
        });
        
        stage.addActor(shootButton);
    }

    /**
     * Creates the left buttons
     */
    private void createLeftButtons() {
        Gdx.app.log("Hud", "Creating left buttons...");
        // Create left button 1 (Inferno Chestplate - type 3, imgIndex 8)
        ImageButton.ImageButtonStyle leftStyle1 = new ImageButton.ImageButtonStyle();
        leftStyle1.up = new TextureRegionDrawable(rm.shopitems[3][8]);
        leftStyle1.down = new TextureRegionDrawable(rm.shopitems[3][8]);
        leftButton1 = new ImageButton(leftStyle1);
        leftButton1.setSize(24, 24);
        leftButton1.setPosition(200 - 24 - 5 - 24 - 5, 5);
        
        // Create left button 2 (Inferno Greaves - type 6, imgIndex 6)
        ImageButton.ImageButtonStyle leftStyle2 = new ImageButton.ImageButtonStyle();
        leftStyle2.up = new TextureRegionDrawable(rm.shopitems[5][6]);
        leftStyle2.down = new TextureRegionDrawable(rm.shopitems[5][6]);
        leftButton2 = new ImageButton(leftStyle2);
        leftButton2.setSize(24, 24);
        leftButton2.setPosition(200 - 24 - 5 - 24 - 5 - 24 - 5, 5);
        
        // Add click listeners - Sword flash effect
        leftButton1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("TEST", "=== SWORD BUTTON CLICKED ===");
                Gdx.app.log("Sword", "Sword button clicked - creating flash effect");
                Gdx.app.log("Sword", "Current event: " + gameScreen.currentEvent);
                
                // Show debug text
                showDebugText("SWORD BUTTON CLICKED!");
                
                // Always try to create flash effect (remove state check for testing)
                Gdx.app.log("Sword", "Calling player.createSwordFlash()");
                Gdx.app.log("Sword", "gameScreen: " + (gameScreen != null ? "OK" : "NULL"));
                Gdx.app.log("Sword", "gameMap: " + (gameScreen != null && gameScreen.gameMap != null ? "OK" : "NULL"));
                Gdx.app.log("Sword", "player: " + (gameScreen != null && gameScreen.gameMap != null && gameScreen.gameMap.player != null ? "OK" : "NULL"));
                
                if (gameScreen != null && gameScreen.gameMap != null && gameScreen.gameMap.player != null) {
                    gameScreen.gameMap.player.createSwordFlash();
                    Gdx.app.log("Sword", "Player sword flash created!");
                } else {
                    Gdx.app.log("Sword", "ERROR: Player is null!");
                }
            }
        });
        
        leftButton2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("TEST", "=== KICK BUTTON CLICKED ===");
                Gdx.app.log("Hud", "LeftButton2 (kick button) clicked!");
                Gdx.app.log("Hud", "Current event: " + gameScreen.currentEvent);
                
                // Show debug text
                showDebugText("KICK BUTTON CLICKED!");
                
                // Always try to kick (remove state check for testing)
                Gdx.app.log("Hud", "Calling player.kickEnemy()");
                Gdx.app.log("Hud", "gameScreen: " + (gameScreen != null ? "OK" : "NULL"));
                Gdx.app.log("Hud", "gameMap: " + (gameScreen != null && gameScreen.gameMap != null ? "OK" : "NULL"));
                Gdx.app.log("Hud", "player: " + (gameScreen != null && gameScreen.gameMap != null && gameScreen.gameMap.player != null ? "OK" : "NULL"));
                
                if (gameScreen != null && gameScreen.gameMap != null && gameScreen.gameMap.player != null) {
                    gameScreen.gameMap.player.kickEnemy();
                    Gdx.app.log("KickButton", "Player kicked enemy off screen!");
                } else {
                    Gdx.app.log("KickButton", "ERROR: Player is null!");
                }
            }
        });
        
        stage.addActor(leftButton1);
        stage.addActor(leftButton2);
        
        Gdx.app.log("Hud", "Left buttons created and added to stage");
        Gdx.app.log("Hud", "LeftButton1 position: " + leftButton1.getX() + ", " + leftButton1.getY());
        Gdx.app.log("Hud", "LeftButton2 position: " + leftButton2.getX() + ", " + leftButton2.getY());
        Gdx.app.log("Hud", "LeftButton1 visible: " + leftButton1.isVisible());
        Gdx.app.log("Hud", "LeftButton2 visible: " + leftButton2.isVisible());
    }

    /**
     * Creates the score label - positioned to the right of music/SFX buttons
     */
    private void createScoreLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(rm.pixel10, Color.WHITE);
        scoreLabel = new Label("Score: 0", labelStyle);
        scoreLabel.setFontScale(0.8f);
        scoreLabel.setPosition(50, 99); // Right of SFX button (button at 26+16=42, so 50 gives spacing)
        scoreLabel.setAlignment(Align.left);
        stage.addActor(scoreLabel);
        Gdx.app.log("Score", "Score label created and positioned to right of music/SFX buttons");
    }

    /**
     * Creates the timer label - positioned next to score label
     */
    private void createTimerLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(rm.pixel10, Color.WHITE);
        timerLabel = new Label("Time: 00:00", labelStyle);
        timerLabel.setFontScale(0.8f);
        timerLabel.setPosition(120, 99); // Next to score label (score at 50, timer at 120)
        timerLabel.setAlignment(Align.left);
        stage.addActor(timerLabel);
        Gdx.app.log("Timer", "Timer label created and positioned next to score");
    }

    /**
     * Creates the HP label - positioned below the score label
     */
    private void createHpLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(rm.pixel10, Color.WHITE);
        hpLabel = new Label("HP: 20/20", labelStyle);
        hpLabel.setFontScale(0.8f);
        hpLabel.setPosition(50, 99 - 12); // Below score label (score at y=99, so HP at y=87)
        hpLabel.setAlignment(Align.left);
        stage.addActor(hpLabel);
        Gdx.app.log("HP", "HP label created and positioned below score");
    }

    /**
     * Creates the mana label - positioned below the HP label
     */
    private void createManaLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(rm.pixel10, Color.CYAN);
        manaLabel = new Label("Mana: 100/100", labelStyle);
        manaLabel.setFontScale(0.8f);
        manaLabel.setPosition(50, 99 - 24); // Below HP label (HP at y=87, so Mana at y=75)
        manaLabel.setAlignment(Align.left);
        stage.addActor(manaLabel);
        Gdx.app.log("Mana", "Mana label created and positioned below HP");
    }
    
    // Show debug text on screen
    public void showDebugText(String text) {
        debugText = text;
        debugTextTimer = 3f; // Show for 3 seconds
        Gdx.app.log("HudDebug", text);
    }

    /**
     * Creates the level descriptor window
     */
    private void createLevelDescriptor() {
        levelDescriptor = new Window("", rm.skin);
        levelDescriptor.getTitleLabel().setFontScale(0.5f);
        levelDescriptor.getTitleLabel().setAlignment(Align.center);
        levelDescriptor.setMovable(false);
        levelDescriptor.setTouchable(Touchable.disabled);
        levelDescriptor.setKeepWithinStage(false);
        levelDescriptor.setVisible(false);
        levelDesc = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        levelDesc.setFontScale(0.5f);
        levelDesc.setAlignment(Align.center);
        levelDescriptor.left();
        levelDescriptor.padTop(12);
        levelDescriptor.padLeft(2);
        levelDescriptor.padBottom(4);
        levelDescriptor.add(levelDesc).width(70);
        stage.addActor(levelDescriptor);
        levelMoving = new Moving(new Vector2(), new Vector2(), 150.f);
    }

    /**
     * Creates the death screen message
     */
    private void createDeathPrompt() {
        deathGroup = new Group();
        deathGroup.setTransform(false);
        deathGroup.setVisible(false);
        deathGroup.setSize(Unlucky.V_WIDTH, Unlucky.V_HEIGHT);
        deathGroup.setTouchable(Touchable.enabled);

        dark = new Image(rm.shade);
        deathGroup.addActor(dark);

        frame = new Image(rm.skin, "textfield");
        frame.setSize(120, 80); // Make dialog bigger to fit all text
        frame.setPosition(Unlucky.V_WIDTH / 2 - 60, Unlucky.V_HEIGHT / 2 - 40);
        deathGroup.addActor(frame);

        youDied = new Label("YOU DIED!", new Label.LabelStyle(rm.pixel10, Color.RED));
        youDied.setSize(120, 10);
        youDied.setPosition(40, 85); // Position at top of dialog
        youDied.setAlignment(Align.center);
        youDied.setTouchable(Touchable.disabled);
        deathGroup.addActor(youDied);

        // Current game statistics - positioned below "YOU DIED!"
        currentStatsLabel = new Label("", new Label.LabelStyle(rm.pixel10, Color.YELLOW));
        currentStatsLabel.setFontScale(0.7f);
        currentStatsLabel.setAlignment(Align.center);
        currentStatsLabel.setSize(120, 25);
        currentStatsLabel.setPosition(40, 55); // Below YOU DIED
        currentStatsLabel.setTouchable(Touchable.disabled);
        deathGroup.addActor(currentStatsLabel);

        // High scores - positioned at bottom of dialog
        highScoresLabel = new Label("", new Label.LabelStyle(rm.pixel10, Color.GREEN));
        highScoresLabel.setFontScale(0.6f);
        highScoresLabel.setAlignment(Align.center);
        highScoresLabel.setSize(120, 20);
        highScoresLabel.setPosition(40, 25); // Bottom of dialog
        highScoresLabel.setTouchable(Touchable.disabled);
        deathGroup.addActor(highScoresLabel);

        // Keep the original loss label hidden (we're using our new labels instead)
        loss = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        loss.setFontScale(0.5f);
        loss.setWrap(true);
        loss.setSize(1, 1); // Make it tiny and invisible
        loss.setAlignment(Align.top);
        loss.setPosition(-100, -100); // Position it off-screen
        loss.setTouchable(Touchable.disabled);
        deathGroup.addActor(loss);

        // click to continue
        deathGroup.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                showHighScoreScreen();
            }
        });

        stage.addActor(deathGroup);
    }

    /**
     * Death text displays the items the player would've got, the gold and exp lost
     * @param text
     */
    public void setDeathText(String text) {
        loss.setText(text);
    }

    /**
     * Set reference to DefenseSystemsManager for modular defense controls
     */
    public void setDefenseSystemsManager(DefenseSystemsManager defenseSystems) {
        this.defenseSystems = defenseSystems;
    }
    
    /**
     * Shows high score screen after death
     */
    private void showHighScoreScreen() {
        // Hide death screen
        if (deathGroup != null) {
            deathGroup.setVisible(false);
        }
        if (shade != null) {
            shade.setVisible(false);
        }
        
        // Show high score screen with current data
        if (gameScreen != null && gameScreen.highScoreScreen != null && highScores != null) {
            gameScreen.setCurrentEvent(EventState.HIGH_SCORE_SCREEN);
            
            // Find current entry (should be the most recent one added)
            com.unlucky.save.HighScoreEntry currentEntry = null;
            if (!highScores.isEmpty()) {
                // The current entry should be the one we just added
                currentEntry = highScores.get(0); // Assuming it's sorted and current is first
                
                // If not first, find it by checking if it matches current game stats
                for (com.unlucky.save.HighScoreEntry entry : highScores) {
                    // This is a simple way to identify current entry - could be improved
                    if (Math.abs(entry.survivalTime - gameTime) < 1.0f) {
                        currentEntry = entry;
                        break;
                    }
                }
            }
            
            if (currentEntry == null) {
                // Fallback: create current entry
                currentEntry = new com.unlucky.save.HighScoreEntry(gameTime, score);
            }
            
            gameScreen.highScoreScreen.show(highScores, currentEntry);
            Gdx.app.log("Hud", "High score screen shown");
        } else {
            Gdx.app.log("Hud", "ERROR: Cannot show high score screen - missing components");
            // Fallback to menu
            backToMenu();
        }
    }
    
    /**
     * Shows high score screen when player wins (score = 5)
     */
    private void showWinHighScoreScreen() {
        // Hide HUD
        toggle(false);
        
        // Load high scores and add current entry
        loadHighScores();
        
        // Create current entry for win
        com.unlucky.save.HighScoreEntry currentEntry = new com.unlucky.save.HighScoreEntry(gameTime, score);
        
        // Add to high scores list
        highScores.add(currentEntry);
        
        // Sort by survival time (longest first)
        java.util.Collections.sort(highScores);
        
        // Keep only top 6
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
        }
        
        // Save updated high scores
        saveHighScores();
        
        // Set game state to high score screen
        gameScreen.setCurrentEvent(EventState.HIGH_SCORE_SCREEN);
        
        // Show high score screen with "YOU WIN" message
        gameScreen.highScoreScreen.showWin(highScores, currentEntry);
        
        Gdx.app.log("WinHighScore", "YOU WIN! High score screen shown with " + highScores.size() + " entries");
    }

    public void backToMenu() {
        // Reset all game state for next game
        resetGameState();
        
        game.menuScreen.transitionIn = 0;
        if (gameScreen.gameMap.weather != WeatherType.NORMAL) {
            rm.lightrain.stop(gameScreen.gameMap.soundId);
            rm.heavyrain.stop(gameScreen.gameMap.soundId);
        }
        if (gameScreen.isClickable()) {
            gameScreen.setClickable(false);
            gameScreen.setBatchFade(false);
            // fade out animation
            stage.addAction(Actions.sequence(Actions.fadeOut(0.3f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        gameScreen.setClickable(true);
                        gameScreen.gameMap.mapTheme.stop();
                        game.setScreen(game.menuScreen);
                    }
                })));
        }
    }

    private void loseObtained() {
        player.addGold(-gameScreen.gameMap.goldObtained);
        player.addExp(-gameScreen.gameMap.expObtained);
        if (gameScreen.gameMap.itemsObtained.size != 0) {
            for (Item item : gameScreen.gameMap.itemsObtained) {
                for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
                    if (player.inventory.getItem(i) == item)
                        player.inventory.removeItem(i);
                }
            }
        }
    }

    /**
     * Player quits the level and returns to the main menu screen
     * The player will lose all gold, exp, and items obtained during the level
     */
    private void quit() {
        final String text = "If you quit, you will lose all \ngold, exp, and items obtained in this level.\n" +
            "Are you sure you want to quit?";
            new Dialog("Warning", rm.dialogSkin) {
            {
                Label l = new Label(text, rm.dialogSkin);
                l.setFontScale(0.5f);
                l.setAlignment(Align.center);
                text(l);
                getButtonTable().defaults().width(40);
                getButtonTable().defaults().height(15);
                button("Yes", "yes");
                button("No", "no");
            }
            @Override
            protected void result(Object object) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                if (object.equals("yes")) {
                    loseObtained();
                    player.setHp(player.getMaxHp());
                    player.inMap = false;
                    if (gameScreen.gameMap.weather != WeatherType.NORMAL) {
                        rm.lightrain.stop(gameScreen.gameMap.soundId);
                        rm.heavyrain.stop(gameScreen.gameMap.soundId);
                    }
                    backToMenu();
                }
                else settingsDialog.show(stage);
            }
        }.show(stage).getTitleLabel().setAlignment(Align.center);
    }

    /**
     * Handles player movement commands
     */
    private void handleDirPadEvents() {
        for (int i = 0; i < 4; i++) {
            final int index = i;
            dirPad[i].addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    dirTime = 0;
                    touchDown = true;
                    dirIndex = index;
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    touchDown = false;
                }
            });
        }
    }

    private void movePlayer(int dir) {
        if (player.canMove()) player.getAm().setAnimation(dir);
        if (player.canMove() && !player.nextTileBlocked(dir)) {
            player.move(dir);
        }
    }

    // Method handleOptionEvents() đã bị xóa vì không còn option buttons

    /**
     * Adds points to the current score
     */
    public void addScore(int points) {
        score += points;
        updateScoreLabel();
        
        // Check win condition - show high score screen when score reaches 50
        if (score >= 50) {
            showWinHighScoreScreen();
        }
        
        Gdx.app.log("Score", "Score increased by " + points + ". Total: " + score);
    }

    /**
     * Resets the score to 0
     */
    public void resetScore() {
        score = 0;
        updateScoreLabel();
        Gdx.app.log("Score", "Score reset to 0");
    }

    /**
     * Updates the score label display
     */
    private void updateScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }

    /**
     * Updates the timer with elapsed game time
     */
    public void updateTimer(float deltaTime) {
        gameTime += deltaTime;
        updateTimerLabel();
    }

    /**
     * Updates the timer label display
     */
    private void updateTimerLabel() {
        if (timerLabel != null) {
            int minutes = (int) (gameTime / 60);
            int seconds = (int) (gameTime % 60);
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
        }
    }

    /**
     * Resets the game timer
     */
    public void resetTimer() {
        gameTime = 0f;
        updateTimerLabel();
        Gdx.app.log("Timer", "Game timer reset");
    }
    
    /**
     * Resets all game statistics for a new game
     */
    public void resetGameStats() {
        // Reset game over flag first
        isGameOver = false;
        
        resetScore();
        resetTimer();
        initializePlayerHP();
        initializePlayerMana();
        Gdx.app.log("GameStats", "All game statistics reset for new game");
    }

    /**
     * Resets the complete game state when returning to menu
     */
    private void resetGameState() {
        // Reset all game statistics
        resetGameStats();
        
        // Hide the death dialog
        if (deathGroup != null) {
            deathGroup.setVisible(false);
        }
        if (shade != null) {
            shade.setVisible(false);
        }
        
        // Clear current stats labels
        if (currentStatsLabel != null) {
            currentStatsLabel.setText("");
        }
        if (highScoresLabel != null) {
            highScoresLabel.setText("");
        }
        
        Gdx.app.log("GameState", "Complete game state reset for new game");
    }

    /**
     * Public method to reset game state when starting a new game
     */
    public void resetForNewGame() {
        resetGameState();
        Gdx.app.log("GameState", "Game state reset for new game session");
    }

    /**
     * Gets the current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the current game time
     */
    public float getGameTime() {
        return gameTime;
    }

    // === HP System Methods ===
    
    /**
     * Updates the HP label display
     */
    public void updateHpLabel() {
        if (hpLabel != null) {
            hpLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        }
    }
    
    /**
     * Damages the player and updates HP display
     */
    public void damagePlayer(int damage) {
        int newHp = player.getHp() - damage;
        if (newHp < 0) newHp = 0;
        player.setHp(newHp);
        updateHpLabel();
        
        Gdx.app.log("HP", "Player took " + damage + " damage. HP: " + player.getHp() + "/" + player.getMaxHp());
        
        // Check for game over
        if (player.getHp() <= 0) {
            triggerGameOver();
        }
    }
    
    /**
     * Triggers game over dialog
     */
    private void triggerGameOver() {
        Gdx.app.log("GameOver", "Player HP reached 0 - triggering game over");
        
        // Set game over flag to stop timer updates
        isGameOver = true;
        
        // Capture current stats at the moment of death (stop the timer)
        float finalGameTime = gameTime;
        int finalScore = score;
        
        // Update and show game over statistics with captured values
        updateGameOverStats(finalScore, finalGameTime);
        
        // Show the existing death dialog
        if (deathGroup != null) {
            deathGroup.setVisible(true);
            shade.setVisible(true);
        }
    }
    
    /**
     * Initializes player HP to full
     */
    public void initializePlayerHP() {
        player.setMaxHp(20);
        player.setHp(20);
        updateHpLabel();
        Gdx.app.log("HP", "Player HP initialized to " + player.getHp() + "/" + player.getMaxHp());
    }

    // === Mana System Methods ===
    
    /**
     * Updates the mana label display
     */
    public void updateManaLabel() {
        if (manaLabel != null) {
            manaLabel.setText("Mana: " + player.getMana() + "/" + player.getMaxMana());
        }
    }
    
    /**
     * Consumes mana and updates display
     */
    public boolean consumeMana(int cost) {
        if (player.hasMana(cost)) {
            player.consumeMana(cost);
            updateManaLabel();
            Gdx.app.log("Mana", "Consumed " + cost + " mana. Remaining: " + player.getMana() + "/" + player.getMaxMana());
            return true;
        } else {
            Gdx.app.log("Mana", "Not enough mana! Need " + cost + ", have " + player.getMana());
            return false;
        }
    }
    
    /**
     * Initializes player mana to full
     */
    public void initializePlayerMana() {
        player.setMaxMana(100);
        player.setMana(100);
        updateManaLabel();
        Gdx.app.log("Mana", "Player mana initialized to " + player.getMana() + "/" + player.getMaxMana());
    }
    
    /**
     * Checks if player has enough mana for a skill
     */
    public boolean hasEnoughMana(int cost) {
        return player.hasMana(cost);
    }
    
    // Mana regeneration
    private float manaRegenTimer = 0f;
    private final float MANA_REGEN_INTERVAL = 2f; // Regenerate every 2 seconds
    private final int MANA_REGEN_AMOUNT = 5; // Regenerate 5 mana per interval
    
    // Game over flag to stop timer updates
    private boolean isGameOver = false;
    
    /**
     * Updates mana regeneration over time
     */
    private void updateManaRegeneration(float dt) {
        manaRegenTimer += dt;
        
        if (manaRegenTimer >= MANA_REGEN_INTERVAL) {
            manaRegenTimer = 0f;
            
            int currentMana = player.getMana();
            int maxMana = player.getMaxMana();
            
            if (currentMana < maxMana) {
                int newMana = Math.min(maxMana, currentMana + MANA_REGEN_AMOUNT);
                player.setMana(newMana);
                updateManaLabel();
                
                if (newMana > currentMana) {
                    Gdx.app.log("Mana", "Mana regenerated: +" + (newMana - currentMana) + " (" + newMana + "/" + maxMana + ")");
                }
            }
        }
    }

    // === High Score System ===
    
    private java.util.List<com.unlucky.save.HighScoreEntry> highScores;
    private final String HIGH_SCORES_FILE = "highscores.txt";
    private final int MAX_HIGH_SCORES = 6;
    
    /**
     * Loads high scores from file
     */
    private void loadHighScores() {
        highScores = new java.util.ArrayList<>();
        
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.local(HIGH_SCORES_FILE);
            if (file.exists()) {
                String content = file.readString();
                String[] lines = content.split("\n");
                
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    
                    com.unlucky.save.HighScoreEntry entry = com.unlucky.save.HighScoreEntry.fromFileString(line.trim());
                    if (entry != null) {
                        highScores.add(entry);
                    }
                }
                
                // Sắp xếp theo thời gian sống (lâu nhất trên đầu)
                java.util.Collections.sort(highScores);
                
                Gdx.app.log("HighScores", "Loaded " + highScores.size() + " high score entries");
            }
        } catch (Exception e) {
            Gdx.app.log("HighScores", "Error loading high scores: " + e.getMessage());
            highScores = new java.util.ArrayList<>();
        }
    }
    
    /**
     * Saves high scores to file
     */
    private void saveHighScores() {
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.local(HIGH_SCORES_FILE);
            StringBuilder content = new StringBuilder();
            
            for (com.unlucky.save.HighScoreEntry entry : highScores) {
                content.append(entry.toFileString()).append("\n");
            }
            
            file.writeString(content.toString(), false);
            Gdx.app.log("HighScores", "Saved " + highScores.size() + " high score entries");
        } catch (Exception e) {
            Gdx.app.log("HighScores", "Error saving high scores: " + e.getMessage());
        }
    }
    
    /**
     * Updates high scores and shows game over statistics
     */
    private void updateGameOverStats(int finalScore, float finalTime) {
        // Load current high scores
        loadHighScores();
        
        // Create new entry for current game
        com.unlucky.save.HighScoreEntry newEntry = new com.unlucky.save.HighScoreEntry(finalTime, finalScore);
        
        // Add to list and sort
        highScores.add(newEntry);
        java.util.Collections.sort(highScores);
        
        // Keep only top 6
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
        }
        
        // Check if this is a new record (top survival time)
        boolean isNewRecord = highScores.get(0) == newEntry;
        
        // Save updated high scores
        saveHighScores();
        
        // Format current stats
        String currentStats = String.format("Score: %d\nSurvived: %s", 
            finalScore, newEntry.getFormattedTime());
        if (isNewRecord) {
            currentStats += "\nNEW SURVIVAL RECORD!";
        }
        currentStatsLabel.setText(currentStats);
        
        // Hide high scores label in death screen (will be shown in separate screen)
        highScoresLabel.setText("Click to view High Scores");
        
        Gdx.app.log("GameOver", "Game Over Stats - Final: " + finalScore + "/" + finalTime + "s");
    }

}
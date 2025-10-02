package com.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.unlucky.entity.Entity;
// import com.unlucky.entity.Player;
import com.unlucky.entity.enemy.Enemy;
import com.unlucky.event.Battle;
import com.unlucky.event.EventState;
import com.unlucky.main.Unlucky;
import com.unlucky.map.GameMap;
import com.unlucky.parallax.Background;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.game.DialogScreen;
import com.unlucky.screen.game.HighScoreScreen;
import com.unlucky.screen.game.LevelUpScreen;
import com.unlucky.screen.game.TransitionScreen;
import com.unlucky.ui.Hud;
import com.unlucky.ui.battleui.BattleUIHandler;

// NEW: Thêm thư viện âm thanh
import com.badlogic.gdx.audio.Sound;

// NEW: Import homework managers for modular architecture
import com.unlucky.screen.homework.AttackSystemsManager;
import com.unlucky.screen.homework.DefenseSystemsManager;
import com.unlucky.screen.homework.WeatherEffectsManager;
import com.unlucky.screen.homework.HomeworkInputHandler;

public class GameScreen extends AbstractScreen {

    // Static instance for score system access
    private static GameScreen instance;

    public EventState currentEvent;

    // private Player player;
    public GameMap gameMap;
    public Hud hud;
    public BattleUIHandler battleUIHandler;
    public Battle battle;
    public TransitionScreen transition;
    public LevelUpScreen levelUp;
    public DialogScreen dialog;
    public HighScoreScreen highScoreScreen;

    // input
    public InputMultiplexer multiplexer;

    // NEW: Homework manager instances for modular architecture
    public AttackSystemsManager attackSystems;
    public DefenseSystemsManager defenseSystems;
    public WeatherEffectsManager weatherEffects;
    public HomeworkInputHandler homeworkInput;

    // battle background
    private Background[] bg;

    // key
    private int worldIndex;
    private int levelIndex;

    // whether or not to reset the game map on show
    public boolean resetGame = true;

    // Camera and batch variables (khôi phục cách dùng camera ban đầu)
    private OrthographicCamera cam;

    // OLD: Bullet System moved to AttackSystemsManager

    // OLD: Raindrop and Explosion systems moved to homework managers

    // === Sliding Slimes ===
    public Array<Enemy> slidingSlimes;
    
    // === Moving Objects (public for Hud access) ===
    public Array<MovingObject> movingObjects;

    // NEW: Sound for bullet end
    private Sound bulletEndSound;

    // NEW: === Toggle Buttons for Teacher Requirements ===
    private ImageButton musicToggleButton;
    private ImageButton sfxToggleButton;

    // NEW: === Moving Objects System for Teacher Requirements ===
    private final int MAX_OBJECTS = 3; // Maximum 3 objects as per requirements

    // NEW: Sound arrays for random collision sounds
    private Sound[] collisionSounds;

    // OLD: Inner classes moved to AttackSystemsManager and other homework managers



    // NEW: --- Inner MovingObject Class for Teacher Requirements ---
    private class MovingObject {
        float x, y;
        float speed = 60f; // Speed toward player
        float size = 20f; // Size for collision detection
        TextureRegion sprite; // The sprite texture
        
        // === Sliding Animation Properties ===
        boolean isSliding = false;
        float slideTimer = 0f;
        float slideDuration = 1.5f; // Duration of sliding animation
        float slideSpeed = 120f; // Speed when sliding away
        float slideDirectionX = 0f; // Direction X when sliding
        float slideDirectionY = 0f; // Direction Y when sliding
        float slideRotation = 0f; // Rotation during slide

        MovingObject(float x, float y) {
            this.x = x;
            this.y = y;
            // Safely select a random sprite from [0][1] to [0][3] with bounds checking
            try {
                if (rm.items20x20 != null && rm.items20x20.length > 0 && rm.items20x20[0] != null && rm.items20x20[0].length > 3) {
                    // Random selection from [0][1], [0][2], [0][3]
                    int randomIndex = 1 + (int)(Math.random() * 3); // Random number between 1-3
                    this.sprite = rm.items20x20[1][randomIndex];
                    Gdx.app.log("MovingObject", "Using random item sprite [0][" + randomIndex + "]");
                } else {
                    // Fallback to raindrop sprite if items array is not available
                    this.sprite = rm.raindrop;
                    Gdx.app.log("MovingObject", "Using fallback sprite (raindrop)");
                }
            } catch (Exception e) {
                this.sprite = rm.raindrop; // Safe fallback
                Gdx.app.log("MovingObject", "Error accessing items20x20, using fallback: " + e.getMessage());
            }
        }
        
        /**
         * Start sliding animation away from player
         */
        void startSlidingAway(float playerX, float playerY) {
            Gdx.app.log("MovingObject", "=== STARTING SLIDE ANIMATION ===");
            
            // Calculate direction away from player
            float deltaX = x - playerX;
            float deltaY = y - playerY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            
            if (distance > 0) {
                // Normalize direction
                slideDirectionX = deltaX / distance;
                slideDirectionY = deltaY / distance;
                
                // Start sliding animation
                isSliding = true;
                slideTimer = 0f;
                slideRotation = 0f;
                
                Gdx.app.log("MovingObject", "Slide direction: " + slideDirectionX + ", " + slideDirectionY);
                Gdx.app.log("MovingObject", "Starting slide from: " + x + ", " + y);
            }
        }

        void update(float dt) {
            // Handle sliding animation first
            if (isSliding) {
                slideTimer += dt;
                
                // Update position during slide
                x += slideDirectionX * slideSpeed * dt;
                y += slideDirectionY * slideSpeed * dt;
                
                // Add rotation for visual effect
                slideRotation += 360f * dt; // Rotate 360 degrees per second
                
                // Check if slide animation is complete
                if (slideTimer >= slideDuration) {
                    Gdx.app.log("MovingObject", "Slide animation complete, removing object");
                    // Mark for removal (will be handled by GameScreen)
                    isSliding = false;
                }
                
                return; // Don't do normal movement during slide
            }
            
            // Normal movement toward player (only when not sliding)
            try {
                if (gameMap != null && gameMap.player != null && gameMap.player.getPosition() != null) {
                    float playerX = gameMap.player.getPosition().x;
                    float playerY = gameMap.player.getPosition().y;

                    // Calculate direction to player
                    float deltaX = playerX - x;
                    float deltaY = playerY - y;
                    float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                    // Move toward player if not already there
                    if (distance > speed * dt) {
                        float oldX = x;
                        float oldY = y;
                        x += (deltaX / distance) * speed * dt;
                        y += (deltaY / distance) * speed * dt;
                        
                        // Debug log every 60 frames (1 second at 60fps)
                        if (Math.random() < 0.016f) { // ~1% chance per frame
                            Gdx.app.log("MovingObject", "Moving from (" + oldX + "," + oldY + ") to (" + x + "," + y + ")");
                            Gdx.app.log("MovingObject", "Player at: (" + playerX + "," + playerY + "), Distance: " + distance);
                        }
                    }
                }
            } catch (Exception e) {
                Gdx.app.log("MovingObject", "Error in update: " + e.getMessage());
                // Don't crash, just stop moving
            }
        }

        void render(SpriteBatch batch) {
            try {
                if (sprite != null && batch != null) {
                    if (isSliding) {
                        // Render with rotation during slide animation
                        batch.draw(sprite, 
                            x - size / 2, y - size / 2,  // Position
                            size / 2, size / 2,         // Origin for rotation
                            size, size,                 // Size
                            1f, 1f,                     // Scale
                            slideRotation);              // Rotation in degrees
                    } else {
                        // Normal rendering without rotation
                        batch.draw(sprite, x - size / 2, y - size / 2, size, size);
                    }
                }
            } catch (Exception e) {
                Gdx.app.log("MovingObject", "Error rendering sprite: " + e.getMessage());
            }
        }

        // Rectangle collision detection
        Rectangle getRectangle() {
            return new Rectangle(x - size / 2, y - size / 2, size, size);
        }

        // Check collision with another MovingObject
        boolean collidesWith(MovingObject other) {
            return this.getRectangle().overlaps(other.getRectangle());
        }

        // Check collision with player
        boolean collidesWithPlayer() {
            try {
                if (gameMap == null || gameMap.player == null || gameMap.player.getPosition() == null) {
                    return false; // No collision if player data is not available
                }

                // Player position and approximate size
                float playerX = gameMap.player.getPosition().x;
                float playerY = gameMap.player.getPosition().y;
                float playerSize = 16f; // Approximate player size

                Rectangle playerRect = new Rectangle(
                    playerX - playerSize / 2, playerY - playerSize / 2, playerSize, playerSize);

                return this.getRectangle().overlaps(playerRect);
            } catch (Exception e) {
                Gdx.app.log("MovingObject", "Error in collidesWithPlayer: " + e.getMessage());
                return false; // Safe fallback - no collision
            }
        }
    }

    public GameScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);
        
        // Set static instance for score system
        instance = this;

        currentEvent = EventState.MOVING;

        gameMap = new GameMap(this, game.player, rm);
        battle = new Battle(this, gameMap.tileMap, gameMap.player);
        hud = new Hud(this, gameMap.tileMap, gameMap.player, rm);
        battleUIHandler = new BattleUIHandler(this, gameMap.tileMap, gameMap.player, battle, rm);
        transition = new TransitionScreen(this, battle, battleUIHandler, hud, gameMap.player, rm);
        levelUp = new LevelUpScreen(this, gameMap.tileMap, gameMap.player, rm);
        dialog = new DialogScreen(this, gameMap.tileMap, gameMap.player, rm);
        highScoreScreen = new HighScoreScreen(this, gameMap.tileMap, gameMap.player, rm);

        // NEW: Initialize homework managers for modular architecture
        // Note: Camera initialization will be fixed after battleUIHandler creates the camera
        attackSystems = null; // Will be initialized later
        defenseSystems = null; // Will be initialized later  
        weatherEffects = null; // Will be initialized later

        // NEW: load bullet end sound with error handling
        try {
            bulletEndSound = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.ogg"));
            if (bulletEndSound == null) {
                Gdx.app.log("GameScreen", "Bullet end sound failed to load (null)!");
            } else {
                Gdx.app.log("GameScreen", "Bullet end sound loaded successfully");
            }
        } catch (Exception e) {
            Gdx.app.log("GameScreen", "Error loading bullet end sound: " + e.getMessage());
            bulletEndSound = null; // Fallback nếu lỗi
        }

        // Khôi phục cách dùng camera ban đầu từ battleUIHandler
        cam = (OrthographicCamera) battleUIHandler.getStage().getCamera();

        // NEW: Initialize homework managers now that camera is available
        attackSystems = new AttackSystemsManager(game, rm, gameMap, cam);
        defenseSystems = new DefenseSystemsManager(game, rm, gameMap.player);
        weatherEffects = new WeatherEffectsManager(rm, cam);
        homeworkInput = new HomeworkInputHandler(this, hud, attackSystems, defenseSystems);
        
        // NEW: Link DefenseSystemsManager to Hud for integrated button controls
        hud.setDefenseSystemsManager(defenseSystems);
        
        // NEW: Link Hud to DefenseSystemsManager for mana checking
        defenseSystems.setHud(hud);
        
        // NEW: Add wind wall to Hud stage for proper rendering
        defenseSystems.addWindWallToStage(hud.getStage());

        // create bg
        bg = new Background[2];
        bg[0] = new Background(cam, new Vector2(0.3f, 0));
        bg[1] = new Background(cam, new Vector2(0, 0));

        // OLD: Bullet init moved to AttackSystemsManager

        // OLD: Raindrop and Explosion init moved to homework managers

        // === Sliding Slimes init ===
        slidingSlimes = new Array<>();

        // NEW: === MovingObjects init for Teacher Requirements ===
        movingObjects = new Array<>();

        // NEW: === Collision Sounds init ===
        collisionSounds = new Sound[]{rm.hit};

        // NEW: === Button init ===
        initToggleButtons();

        // input multiplexer
        multiplexer = new InputMultiplexer();

        // NEW: Add homework input handler first (higher priority)
        multiplexer.addProcessor(homeworkInput);

        // Bullet input
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (pointer > 0)
                    return false;

                Vector2 stageCoords = hud.getStage().screenToStageCoordinates(new Vector2(screenX, screenY));

                boolean hitUI = false;
                if (currentEvent == EventState.MOVING
                        && hud.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.BATTLING
                        && battleUIHandler.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.LEVEL_UP
                        && levelUp.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.TILE_EVENT
                        && dialog.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.HIGH_SCORE_SCREEN
                        && highScoreScreen.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.INVENTORY
                        && game.inventoryUI.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                }

                // OLD: Bullet firing moved to HomeworkInputHandler
                // (HomeworkInputHandler now handles this with higher priority)

                return false;
            }
        });

        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(battleUIHandler.getStage());
        multiplexer.addProcessor(levelUp.getStage());
        multiplexer.addProcessor(dialog.getStage());
    }

    /**
     * Initialize music and SFX toggle buttons in top-left corner
     */
    private void initToggleButtons() {
        // Create buttons with default style (will be updated based on settings)
        ImageButton.ImageButtonStyle defaultStyle = new ImageButton.ImageButtonStyle();
        defaultStyle.up = new TextureRegionDrawable(rm.musicOnButton);
        defaultStyle.down = new TextureRegionDrawable(rm.musicOnButton);

        musicToggleButton = new ImageButton(defaultStyle);
        sfxToggleButton = new ImageButton(defaultStyle);

        // Set button positions (top-left corner using virtual coordinates)
        float buttonSize = 16f;
        float padding = 5f; // Distance from edges

        musicToggleButton.setSize(buttonSize, buttonSize);
        musicToggleButton.setPosition(padding, Unlucky.V_HEIGHT - buttonSize - padding);

        sfxToggleButton.setSize(buttonSize, buttonSize);
        sfxToggleButton.setPosition(padding + buttonSize + 5f, Unlucky.V_HEIGHT - buttonSize - padding);

        // Set initial button styles based on current volume settings
        if (game.player.settings.muteMusic || game.player.settings.musicVolume <= 0) {
            // Music is OFF - show OFF icon
            ImageButton.ImageButtonStyle musicOffStyle = new ImageButton.ImageButtonStyle();
            musicOffStyle.up = new TextureRegionDrawable(rm.musicOffButton);
            musicOffStyle.down = new TextureRegionDrawable(rm.musicOffButton);
            musicToggleButton.setStyle(musicOffStyle);
        } else {
            // Music is ON - show ON icon
            ImageButton.ImageButtonStyle musicOnStyle = new ImageButton.ImageButtonStyle();
            musicOnStyle.up = new TextureRegionDrawable(rm.musicOnButton);
            musicOnStyle.down = new TextureRegionDrawable(rm.musicOnButton);
            musicToggleButton.setStyle(musicOnStyle);
        }

        if (game.player.settings.muteSfx || game.player.settings.sfxVolume <= 0) {
            // SFX is OFF - show OFF icon
            ImageButton.ImageButtonStyle sfxOffStyle = new ImageButton.ImageButtonStyle();
            sfxOffStyle.up = new TextureRegionDrawable(rm.sfxOffButton);
            sfxOffStyle.down = new TextureRegionDrawable(rm.sfxOffButton);
            sfxToggleButton.setStyle(sfxOffStyle);
        } else {
            // SFX is ON - show ON icon
            ImageButton.ImageButtonStyle sfxOnStyle = new ImageButton.ImageButtonStyle();
            sfxOnStyle.up = new TextureRegionDrawable(rm.sfxOnButton);
            sfxOnStyle.down = new TextureRegionDrawable(rm.sfxOnButton);
            sfxToggleButton.setStyle(sfxOnStyle);
        }

        // Add click listeners
        musicToggleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Toggle music mute state (following SettingsScreen pattern)
                if (game.player.settings.muteMusic) {
                    // Currently muted - turn ON
                    game.player.settings.muteMusic = false;
                    rm.setMusicVolume(game.player.settings.musicVolume);
                    // Change button style to show ON icon
                    ImageButton.ImageButtonStyle onStyle = new ImageButton.ImageButtonStyle();
                    onStyle.up = new TextureRegionDrawable(rm.musicOnButton);
                    onStyle.down = new TextureRegionDrawable(rm.musicOnButton);
                    musicToggleButton.setStyle(onStyle);
                    Gdx.app.log("Audio", "Music turned ON");
                } else {
                    // Currently playing - turn OFF 
                    game.player.settings.muteMusic = true;
                    rm.setMusicVolume(0f);
                    // Change button style to show OFF icon
                    ImageButton.ImageButtonStyle offStyle = new ImageButton.ImageButtonStyle();
                    offStyle.up = new TextureRegionDrawable(rm.musicOffButton);
                    offStyle.down = new TextureRegionDrawable(rm.musicOffButton);
                    musicToggleButton.setStyle(offStyle);
                    Gdx.app.log("Audio", "Music turned OFF");
                }
            }
        });

        sfxToggleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Toggle SFX mute state (following SettingsScreen pattern)  
                if (game.player.settings.muteSfx) {
                    // Currently muted - turn ON
                    game.player.settings.muteSfx = false;
                    // Change button style to show ON icon
                    ImageButton.ImageButtonStyle onStyle = new ImageButton.ImageButtonStyle();
                    onStyle.up = new TextureRegionDrawable(rm.sfxOnButton);
                    onStyle.down = new TextureRegionDrawable(rm.sfxOnButton);
                    sfxToggleButton.setStyle(onStyle);
                    Gdx.app.log("Audio", "SFX turned ON");
                } else {
                    // Currently playing - turn OFF
                    game.player.settings.muteSfx = true;
                    // Change button style to show OFF icon
                    ImageButton.ImageButtonStyle offStyle = new ImageButton.ImageButtonStyle();
                    offStyle.up = new TextureRegionDrawable(rm.sfxOffButton);
                    offStyle.down = new TextureRegionDrawable(rm.sfxOffButton);
                    sfxToggleButton.setStyle(offStyle);
                    Gdx.app.log("Audio", "SFX turned OFF");
                }
            }
        });

        // Add buttons to HUD stage (same as movement buttons)
        hud.getStage().addActor(musicToggleButton);
        hud.getStage().addActor(sfxToggleButton);
    }    public void init(int worldIndex, int levelIndex) {
        this.worldIndex = worldIndex;
        this.levelIndex = levelIndex;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);

        hud.getStage().addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.5f)));

        if (resetGame) {
            setCurrentEvent(EventState.MOVING);
            hud.deathGroup.setVisible(false);
            gameMap.init(worldIndex, levelIndex);
            gameMap.player.moving = -1;
            battle.tileMap = gameMap.tileMap;
            hud.setTileMap(gameMap.tileMap);
            battleUIHandler.setTileMap(gameMap.tileMap);
            levelUp.setTileMap(gameMap.tileMap);
            dialog.setTileMap(gameMap.tileMap);

            // Cập nhật camera theo player (dựa trên cách ban đầu)
            updateCamera();

            createBackground(gameMap.worldIndex);

            hud.toggle(true);
            hud.touchDown = false;
            hud.shade.setVisible(false);
            hud.startLevelDescriptor();

            // NEW: Initialize weather effects manager for level
            if (weatherEffects != null) {
                weatherEffects.initializeLevel();
            }

            // NEW: Initialize moving objects system with 3 objects immediately
            movingObjects.clear();

            // Spawn 3 objects immediately to meet teacher requirements
            for (int i = 0; i < MAX_OBJECTS; i++) {
                spawnRandomObject();
            }
            Gdx.app.log("MovingObjects", "Initialized with " + MAX_OBJECTS + " objects");
        }
    }

    private void createBackground(int bgIndex) {
        TextureRegion[] images = rm.battleBackgrounds400x240[bgIndex];
        for (int i = 0; i < 2; i++)
            bg[i].setImage(images[i]);
        if (bgIndex == 0)
            bg[0].setVector(160, 0);
        else if (bgIndex == 1)
            bg[0].setVector(0, 0);
        else if (bgIndex == 2)
            bg[0].setVector(40, 0);
        bg[1].setVector(0, 0);
    }

    public void die() {
        gameMap.player.setHp(gameMap.player.getMaxHp());
        setCurrentEvent(EventState.DEATH);
        hud.toggle(false);
        hud.deathGroup.setVisible(true);
    }

    public void updateCamera() {
        float playerX = gameMap.player.getPosition().x;
        float playerY = gameMap.player.getPosition().y;

        float halfHeight = cam.viewportHeight / 2f;
        float halfWidth = cam.viewportWidth / 2f;

        // Center camera on player - remove any offset
        cam.position.x = playerX;
        cam.position.y = playerY;

        // Keep camera within map bounds - but don't force minimum bounds
        if (cam.position.y > gameMap.tileMap.mapHeight * 16 - halfHeight)
            cam.position.y = gameMap.tileMap.mapHeight * 16 - halfHeight;

        if (cam.position.x > gameMap.tileMap.mapWidth * 16 - halfWidth)
            cam.position.x = gameMap.tileMap.mapWidth * 16 - halfWidth;

        cam.update();
    }

    public void update(float dt) {
        if (currentEvent != EventState.PAUSE) {
            gameMap.time += dt;
        }

        if (currentEvent == EventState.MOVING) {
            updateCamera();
            gameMap.update(dt);
            hud.update(dt);

            // Update sliding slimes - automatically manage the list
            for (int i = slidingSlimes.size - 1; i >= 0; i--) {
                Enemy slidingSlime = slidingSlimes.get(i);
                if (slidingSlime == null || !slidingSlime.isSliding || slidingSlime.isDead()) {
                    slidingSlimes.removeIndex(i);
                    if (slidingSlime != null) {
                        Gdx.app.log("GameScreen", "Removed completed sliding slime: " + slidingSlime.getId());
                    }
                }
            }
        }

        for (Background bgi : bg) {
            bgi.update(dt);
        }

        if (currentEvent == EventState.BATTLING)
            battleUIHandler.update(dt);
        if (currentEvent == EventState.TRANSITION)
            transition.update(dt);
        if (currentEvent == EventState.LEVEL_UP)
            levelUp.update(dt);
        if (currentEvent == EventState.TILE_EVENT)
            dialog.update(dt);
        if (currentEvent == EventState.HIGH_SCORE_SCREEN)
            highScoreScreen.update(dt);
        if (currentEvent == EventState.INVENTORY)
            game.inventoryUI.update(dt);

        // NEW: Update homework manager systems with proper coordination
        updateHomeworkSystems(dt);
    }

    /**
     * Update homework systems with proper coordination between attack and defense
     */
    private void updateHomeworkSystems(float dt) {
        // Update defense systems first
        if (defenseSystems != null) {
            defenseSystems.update(dt);
        }
        
        // Update weather effects
        if (weatherEffects != null) {
            weatherEffects.update(dt);
        }
        
        // Update attack systems with defense coordination
        if (attackSystems != null) {
            // Apply slow motion if active
            if (defenseSystems != null && defenseSystems.isSlowMotionActive()) {
                float timeMultiplier = defenseSystems.getTimeMultiplier();
                attackSystems.update(dt, timeMultiplier);
                Gdx.app.log("HomeworkSystems", "🐌 Applying slow motion: " + timeMultiplier + "x speed");
            } else {
                attackSystems.update(dt);
            }
            
            // Handle defense interactions with moving objects
            handleDefenseInteractions();
        }
    }
    
    /**
     * Handle interactions between defense systems and moving objects
     */
    private void handleDefenseInteractions() {
        if (attackSystems == null || defenseSystems == null) return;
        
        Array<AttackSystemsManager.MovingObject> objects = attackSystems.getMovingObjects();
        
        for (int i = objects.size - 1; i >= 0; i--) {
            AttackSystemsManager.MovingObject obj = objects.get(i);
            
            // Check wind wall barrier blocking
            if (defenseSystems.checkWindWallBarrierCollision(obj)) {
                attackSystems.removeMovingObjectAt(i);
                Gdx.app.log("DefenseInteraction", "🌪️ Wind wall barrier blocked object!");
                continue;
            }
            
            // Check all player collisions - shield gets priority
            if (obj.collidesWithPlayer()) {
                if (defenseSystems.isShieldActive()) {
                    // Shield handles collision - object gets destroyed
                    defenseSystems.handleShieldCollision(obj);
                    attackSystems.removeMovingObjectAt(i);
                    playRandomCollisionSound(); // Play shield block sound
                    Gdx.app.log("DefenseInteraction", "🛡️ Shield blocked and destroyed object!");
                } else {
                    // No shield - player takes damage, object is destroyed
                    attackSystems.removeMovingObjectAt(i);
                    playRandomCollisionSound(); // Play hit sound
                    Gdx.app.log("DefenseInteraction", "💥 Object hit unprotected player!");
                    
                    // Damage player HP
                    if (hud != null) {
                        hud.damagePlayer(1); // Deal 1 damage per hit
                    }
                }
                continue; // Skip to next object since this one was removed
            }
        }
    }

    // NEW: Spawn a random object at random position
    private void spawnRandomObject() {
        Gdx.app.log("MovingObject", "=== SPAWNING NEW OBJECT ===");
        
        // Spawn objects around the camera view, but not too close to player
        float camX = cam.position.x;
        float camY = cam.position.y;
        float spawnRadius = 80f; // Distance from camera center
        float minDistFromPlayer = 30f; // Minimum distance from player
        // float minDistFromOtherObjects = 25f; // Minimum distance from other objects

        int attempts = 0;
        float spawnX, spawnY;
        
        do {
            float angle = (float) (Math.random() * 2 * Math.PI);
            spawnX = camX + (float) Math.cos(angle) * spawnRadius;
            spawnY = camY + (float) Math.sin(angle) * spawnRadius;
            attempts++;
        } while (attempts < 10 && (tooCloseToPlayer(spawnX, spawnY, minDistFromPlayer) || 
                                   tooCloseToOtherObjects(spawnX, spawnY, 10f)));

        MovingObject newObject = new MovingObject(spawnX, spawnY);
        movingObjects.add(newObject);
        Gdx.app.log("MovingObject", "=== OBJECT SPAWNED SUCCESSFULLY ===");
        Gdx.app.log("MovingObject", "Position: " + spawnX + ", " + spawnY + " (attempt " + attempts + ")");
        Gdx.app.log("MovingObject", "Total objects: " + movingObjects.size);
    }

    // NEW: Check if position is too close to player
    private boolean tooCloseToPlayer(float x, float y, float minDistance) {
        try {
            if (gameMap == null || gameMap.player == null || gameMap.player.getPosition() == null) {
                return false; // Safe default if player is not available
            }
            
            float playerX = gameMap.player.getPosition().x;
            float playerY = gameMap.player.getPosition().y;
            float distance = (float) Math.sqrt((x - playerX) * (x - playerX) + (y - playerY) * (y - playerY));
            return distance < minDistance;
        } catch (Exception e) {
            Gdx.app.log("GameScreen", "Error in tooCloseToPlayer: " + e.getMessage());
            return false;
        }
    }

    // NEW: Check if position is too close to other objects
    private boolean tooCloseToOtherObjects(float x, float y, float minDistance) {
        for (MovingObject obj : movingObjects) {
            float distance = (float) Math.sqrt((x - obj.x) * (x - obj.x) + (y - obj.y) * (y - obj.y));
            if (distance < minDistance) {
                return true;
            }
        }
        return false;
    }

    // NEW: Play random collision sound
    private void playRandomCollisionSound() {
        try {
            if (collisionSounds != null && collisionSounds.length > 0 && !game.player.settings.muteSfx) {
                Sound randomSound = collisionSounds[0]; // Use the first (and only) sound for now
                if (randomSound != null) {
                    randomSound.play(game.player.settings.sfxVolume);
                }
            }
        } catch (Exception e) {
            Gdx.app.log("GameScreen", "Error playing collision sound: " + e.getMessage());
            // Don't crash on audio errors
        }
    }


    /**
     * Push all moving objects away from player (called by Hud)
     */
    public void pushAllObjectsAway() {
        Gdx.app.log("GameScreen", "=== PUSHING ALL OBJECTS AWAY ===");
        
        if (movingObjects != null && gameMap != null && gameMap.player != null) {
            Gdx.app.log("GameScreen", "Found " + movingObjects.size + " moving objects to push away");
            
            float playerX = gameMap.player.getPosition().x;
            float playerY = gameMap.player.getPosition().y;
            
            for (int i = movingObjects.size - 1; i >= 0; i--) {
                MovingObject obj = movingObjects.get(i);
                if (obj != null && !obj.isSliding) { // Only push objects that aren't already sliding
                    // Start sliding animation instead of immediate push
                    obj.startSlidingAway(playerX, playerY);
                    Gdx.app.log("GameScreen", "Started slide animation for object " + i);
                }
            }
            
            Gdx.app.log("GameScreen", "All objects started sliding away!");
        } else {
            Gdx.app.log("GameScreen", "ERROR: movingObjects, gameMap, or player is null!");
        }
    }

    // OLD: Bullet collision and shooting methods moved to AttackSystemsManager
    
    // Visual feedback method for button actions
    public void flashScreen(float r, float g, float b, float duration) {
        // Simple screen flash effect - you can enhance this later
        Gdx.app.log("GameScreen", "Screen flash: RGB(" + r + "," + g + "," + b + ") for " + duration + "s");
    }
    
    // Add sliding slime to high-priority rendering list
    public void addSlidingSlime(Enemy enemy) {
        if (!slidingSlimes.contains(enemy, true)) {
            slidingSlimes.add(enemy);
            Gdx.app.log("GameScreen", "Added sliding slime: " + enemy.getId());
        }
    }
    
    // Remove sliding slime from list
    public void removeSlidingSlime(Enemy enemy) {
        slidingSlimes.removeValue(enemy, true);
        Gdx.app.log("GameScreen", "Removed sliding slime: " + enemy.getId());
    }

    public void render(float dt) {
        update(dt);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.app.log("Render", "=== RENDER METHOD CALLED ===");
        
        if (game.batch != null) {
            game.batch.begin();
            
            game.batch.setColor(Color.WHITE);
            game.batch.setProjectionMatrix(cam.combined);
            for (Background bgi : bg)
                bgi.render(game.batch);

            if (currentEvent == EventState.MOVING || currentEvent == EventState.INVENTORY ||
                    transition.renderMap || currentEvent == EventState.TILE_EVENT ||
                    currentEvent == EventState.DEATH || currentEvent == EventState.PAUSE) {
                game.batch.setProjectionMatrix(cam.combined);
                gameMap.render(dt, game.batch, cam);
            }

            // NEW: Render homework manager systems
            if (attackSystems != null) {
                attackSystems.render(game.batch);
            }
            if (defenseSystems != null) {
                defenseSystems.render(game.batch);
            }
            if (weatherEffects != null) {
                weatherEffects.render(game.batch);
            }

            // OLD: Bullet, raindrop, explosion rendering moved to homework managers

            // NEW: render sliding slimes - high priority rendering so they don't get covered
            for (Enemy slidingSlime : slidingSlimes) {
                if (slidingSlime != null && slidingSlime.isSliding) {
                    slidingSlime.render(game.batch, true);
                }
            }

            // NEW: render moving objects - teacher requirements system
            try {
                Gdx.app.log("Render", "Rendering " + movingObjects.size + " moving objects");
                for (MovingObject obj : movingObjects) {
                    if (obj != null && obj.sprite != null) {
                        Gdx.app.log("Render", "Rendering object at: " + obj.x + ", " + obj.y);
                        obj.render(game.batch);
                    } else {
                        Gdx.app.log("Render", "Object is null or sprite is null!");
                    }
                }
            } catch (Exception e) {
                Gdx.app.log("GameScreen", "Error rendering moving objects: " + e.getMessage());
            }

            game.batch.end();
        }

        // render HUD / UI
        if (currentEvent == EventState.MOVING)
            hud.render(dt);
        else if (currentEvent == EventState.BATTLING)
            battleUIHandler.render(dt);
        else if (currentEvent == EventState.TRANSITION)
            transition.render(dt);
        else if (currentEvent == EventState.LEVEL_UP)
            levelUp.render(dt);
        else if (currentEvent == EventState.TILE_EVENT)
            dialog.render(dt);
        else if (currentEvent == EventState.HIGH_SCORE_SCREEN)
            highScoreScreen.render(dt);
        else if (currentEvent == EventState.INVENTORY)
            game.inventoryUI.render(dt);
        else if (currentEvent == EventState.PAUSE)
            hud.render(dt);  // Render HUD when paused to show settings dialog
    }

    public void setCurrentEvent(EventState event) {
        currentEvent = event;
    }

    // === Score System Methods ===
    
    /**
     * Gets the static GameScreen instance for score access
     */
    public static GameScreen getInstance() {
        return instance;
    }
    
    /**
     * Adds score points through the HUD
     */
    public void addScore(int points) {
        if (hud != null) {
            hud.addScore(points);
        }
    }
    
    /**
     * Resets the game score
     */
    public void resetScore() {
        if (hud != null) {
            hud.resetScore();
        }
    }
    
    /**
     * Checks if an enemy has died and awards score if needed
     */
    public void checkEnemyDeath(Enemy enemy) {
        if (enemy.isDead() && !enemy.hasScoreBeenAdded) {
            // Award points based on enemy type/level
            int baseScore = 10;
            int levelBonus = enemy.getLevel() * 5;
            int totalScore = baseScore + levelBonus;
            
            addScore(totalScore);
            enemy.hasScoreBeenAdded = true;
            
            Gdx.app.log("Score", "Enemy killed! Awarded " + totalScore + " points");
        }
    }

    @Override
    public void dispose() {
        // NEW: Giải phóng âm thanh khi dispose
        if (bulletEndSound != null) {
            bulletEndSound.dispose();
        }
        super.dispose();
    }
}
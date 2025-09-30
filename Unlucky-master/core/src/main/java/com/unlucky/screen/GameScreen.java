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
import com.unlucky.entity.Player;
import com.unlucky.entity.enemy.Enemy;
import com.unlucky.event.Battle;
import com.unlucky.event.EventState;
import com.unlucky.main.Unlucky;
import com.unlucky.map.GameMap;
import com.unlucky.parallax.Background;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.game.DialogScreen;
import com.unlucky.screen.game.LevelUpScreen;
import com.unlucky.screen.game.TransitionScreen;
import com.unlucky.ui.Hud;
import com.unlucky.ui.battleui.BattleUIHandler;

// NEW: Thêm thư viện âm thanh
import com.badlogic.gdx.audio.Sound;

public class GameScreen extends AbstractScreen {

    public EventState currentEvent;

    private Player player;
    public GameMap gameMap;
    public Hud hud;
    public BattleUIHandler battleUIHandler;
    public Battle battle;
    public TransitionScreen transition;
    public LevelUpScreen levelUp;
    public DialogScreen dialog;

    // input
    public InputMultiplexer multiplexer;

    // battle background
    private Background[] bg;

    // key
    private int worldIndex;
    private int levelIndex;

    // whether or not to reset the game map on show
    public boolean resetGame = true;

    // Camera and batch variables (khôi phục cách dùng camera ban đầu)
    private OrthographicCamera cam;

    // === Bullet System ===
    private Array<Bullet> bullets;

    // NEW: === Raindrop System (replaces Cloud system) ===
    private Array<Raindrop> raindrops;

    // NEW: === Explosion System ===
    private Array<Explosion> explosions;

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

    // --- Inner Bullet Class ---
    private class Bullet {
        float x, y;
        // COMMENTED: Directional bullet system for teacher requirement - one direction only
        /*
        float velocityX, velocityY; // Direction vector
        float speed = 180f; // Increased speed for better visibility
        float size = 12f; // Slightly larger for sword

        Bullet(float startX, float startY, float targetX, float targetY) {
            this.x = startX;
            this.y = startY;
            
            // Calculate direction vector to target
            float deltaX = targetX - startX;
            float deltaY = targetY - startY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            
            // Normalize and apply speed
            if (distance > 0) {
                this.velocityX = (deltaX / distance) * speed;
                this.velocityY = (deltaY / distance) * speed;
            } else {
                // Default upward if no direction
                this.velocityX = 0;
                this.velocityY = speed;
            }
        }

        void update(float dt) {
            x += velocityX * dt;
            y += velocityY * dt;
        }
        */

        // NEW: Simple one-direction bullet system (upward only) for teacher requirement
        float speed = 180f;
        float size = 12f; // Size for sword item

        Bullet(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void update(float dt) {
            y += speed * dt; // Only move upward
        }

        void render(SpriteBatch batch) {
            // Use sword/weapon item from atlas instead of red arrow - much cooler!
            batch.draw(rm.items20x20[1][0], x - size / 2, y - size / 2, size, size);
        }

        // NEW: Check if bullet touches screen borders (not outside them) - ready for explosion effect!
        boolean touchesBorder(float camX, float camY, float viewWidth, float viewHeight) {
            float leftBorder = camX - viewWidth/2;
            float rightBorder = camX + viewWidth/2;
            float topBorder = camY + viewHeight/2;
            float bottomBorder = camY - viewHeight/2;

            // Check if bullet position is at or beyond any border
            return x <= leftBorder || x >= rightBorder ||
                   y <= bottomBorder || y >= topBorder;
        }

        // COMMENTED: Old out-of-screen method
        /*
        boolean isOutOfScreen(float camX, float camY, float viewWidth, float viewHeight) {
            // Check if bullet is outside camera bounds with some margin
            float margin = 32f;
            return x < camX - viewWidth/2 - margin || 
                   x > camX + viewWidth/2 + margin ||
                   y < camY - viewHeight/2 - margin || 
                   y > camY + viewHeight/2 + margin;
        }
        */
    }

    // NEW: --- Inner Explosion Class ---
    private class Explosion {
        float x, y;
        float animationTime = 0f;
        float frameDuration = 0.15f; // Duration per frame (150ms)
        float totalDuration = frameDuration * 3; // 3 frames total
        float size = 24f; // Larger than bullet for dramatic effect
        boolean isFinished = false;

        Explosion(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void update(float dt) {
            animationTime += dt;
            if (animationTime >= totalDuration) {
                isFinished = true;
            }
        }

        void render(SpriteBatch batch) {
            if (!isFinished) {
                // Calculate which frame to show based on animation time
                int frameIndex = (int) (animationTime / frameDuration);
                if (frameIndex >= 3) frameIndex = 2; // Clamp to last frame

                // Use battleSprites96x96[0][0] to [0][2] for explosion animation
                TextureRegion explosionFrame = rm.battleAttacks64x64[2][frameIndex];
                batch.draw(explosionFrame, x - size / 2, y - size, size, size);

                // Optional: Add some screen shake effect or particle sparkles here later!
            }
        }

        boolean isFinished() {
            return isFinished;
        }
    }

    // NEW: --- Inner Raindrop Class (renamed from Cloud) ---
    private class Raindrop {
        float x, y;
        float speed = -50f; // Moving downward
        float size = 16f;

        Raindrop(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void update(float dt) {
            y += speed * dt; // Move downward
        }

        void render(SpriteBatch batch) {
            // Use raindrop from atlas - perfect for falling weather effect
            batch.draw(rm.raindrop, x - size / 2, y - size / 2, size, size);
        }

        boolean isOutOfScreen(float screenBottom) {
            return y + size / 2 < screenBottom; // Off bottom of screen
        }

        void resetPosition(float screenTop) {
            y = screenTop + size / 2; // Reset to top
        }
    }

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

        currentEvent = EventState.MOVING;

        gameMap = new GameMap(this, game.player, rm);
        battle = new Battle(this, gameMap.tileMap, gameMap.player);
        hud = new Hud(this, gameMap.tileMap, gameMap.player, rm);
        battleUIHandler = new BattleUIHandler(this, gameMap.tileMap, gameMap.player, battle, rm);
        transition = new TransitionScreen(this, battle, battleUIHandler, hud, gameMap.player, rm);
        levelUp = new LevelUpScreen(this, gameMap.tileMap, gameMap.player, rm);
        dialog = new DialogScreen(this, gameMap.tileMap, gameMap.player, rm);

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

        // create bg
        bg = new Background[2];
        bg[0] = new Background(cam, new Vector2(0.3f, 0));
        bg[1] = new Background(cam, new Vector2(0, 0));

        // === Bullet init ===
        bullets = new Array<>();

        // NEW: === Raindrop init (replaces clouds) ===
        raindrops = new Array<>();

        // NEW: === Explosion init ===
        explosions = new Array<>();

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
                } else if (currentEvent == EventState.INVENTORY
                        && game.inventoryUI.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                }

                if (!hitUI && (currentEvent == EventState.MOVING || currentEvent == EventState.BATTLING)) {
                    float playerX = gameMap.player.getPosition().x;
                    float playerY = gameMap.player.getPosition().y;

                    // COMMENTED: Directional bullet code - kept for reference
                    /*
                    // Convert screen coordinates to world coordinates for target
                    Vector2 targetCoords = new Vector2(screenX, screenY);
                    Vector2 targetStageCoords = hud.getStage().screenToStageCoordinates(targetCoords);
                    
                    // Convert stage coordinates to world coordinates
                    float targetX = cam.position.x + (targetStageCoords.x - cam.viewportWidth / 2);
                    float targetY = cam.position.y + (targetStageCoords.y - cam.viewportHeight / 2);

                    // Create bullet that flies toward the tapped location
                    Bullet bullet = new Bullet(playerX, playerY + 8, targetX, targetY);
                    */

                    // NEW: Simple one-direction bullet for teacher requirement
                    Bullet bullet = new Bullet(playerX, playerY + 8);
                    bullets.add(bullet);

                    Gdx.app.log("Bullet",
                            "Sword bullet fired upward from: " + playerX + ", " + playerY +
                            " | total bullets: " + bullets.size);
                    return true;
                }

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
        float buttonSize = 24f;
        float padding = 10f; // Distance from edges

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

            // NEW: Initialize 2 raindrops at the top of screen
            raindrops.clear();
            float centerX = cam.position.x;
            float topY = cam.position.y + cam.viewportHeight / 2 + 16f / 2; // Top of screen
            raindrops.add(new Raindrop(centerX - 40f, topY)); // Raindrop 1, left
            raindrops.add(new Raindrop(centerX + 40f, topY)); // Raindrop 2, right

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
        if (currentEvent == EventState.INVENTORY)
            game.inventoryUI.update(dt);

        // update bullets
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(dt);

            // Check collision with slimes first
            if (checkBulletSlimeCollision(b)) {
                // Create explosion at bullet position
                Explosion explosion = new Explosion(b.x, b.y);
                explosions.add(explosion);

                bullets.removeIndex(i);

                // Play explosion sound with volume setting (following game pattern)
                if (bulletEndSound != null && !game.player.settings.muteSfx) {
                    bulletEndSound.play(game.player.settings.sfxVolume);
                }

                Gdx.app.log("Explosion", "💥 BOOM! Bullet hit slime at: " + b.x + ", " + b.y);
                continue; // Skip border check since bullet already exploded
            }

            // NEW: Create explosion when bullet TOUCHES border!
            if (b.touchesBorder(cam.position.x, cam.position.y, cam.viewportWidth, cam.viewportHeight)) {
                // Create explosion at bullet position
                Explosion explosion = new Explosion(b.x, b.y);
                explosions.add(explosion);

                bullets.removeIndex(i);

                // Play explosion sound with volume setting (following game pattern)
                if (bulletEndSound != null && !game.player.settings.muteSfx) {
                    bulletEndSound.play(game.player.settings.sfxVolume);
                }

                Gdx.app.log("Explosion", "💥 BOOM! Explosion created at: " + b.x + ", " + b.y);
            }
        }

        // NEW: update explosions
        for (int i = explosions.size - 1; i >= 0; i--) {
            Explosion e = explosions.get(i);
            e.update(dt);
            if (e.isFinished()) {
                explosions.removeIndex(i);
                Gdx.app.log("Explosion", "Explosion animation finished and removed");
            }
        }

        // NEW: update raindrops - falling weather effect
        float screenBottom = cam.position.y - cam.viewportHeight / 2;
        float screenTop = cam.position.y + cam.viewportHeight / 2;
        for (Raindrop r : raindrops) {
            r.update(dt);
            if (r.isOutOfScreen(screenBottom)) {
                r.resetPosition(screenTop);
                Gdx.app.log("Raindrop", "Raindrop reset to top: " + r.y);
            }
        }

        // NEW: === Moving Objects System Update for Teacher Requirements ===
        updateMovingObjects(dt);
    }

    // NEW: Update method for moving objects system
    private void updateMovingObjects(float dt) {
        Gdx.app.log("MovingObjects", "=== UPDATE MOVING OBJECTS CALLED ===");
        try {
            // Slow motion factor: 1 = normal, 0.1 = slow
            float factor = hud.slowMotion ? 0.1f : 1f; // <-- sửa ở đây

            // Ensure we always have exactly 3 objects - spawn immediately if needed
            while (movingObjects.size < MAX_OBJECTS) {
                spawnRandomObject();
            }
            
            // Debug log để kiểm tra moving objects
            Gdx.app.log("MovingObjects", "=== UPDATE MOVING OBJECTS ===");
            Gdx.app.log("MovingObjects", "Count: " + movingObjects.size);
            
            // Safe player access
            if (gameMap != null && gameMap.player != null) {
                Gdx.app.log("MovingObjects", "Player position: " + gameMap.player.getPosition().x + ", " + gameMap.player.getPosition().y);
                Gdx.app.log("MovingObjects", "Player shield active: " + gameMap.player.isShieldActive());
            } else {
                Gdx.app.log("MovingObjects", "ERROR: gameMap or player is null!");
            }

            // Update all moving objects with safe iteration
            for (int i = movingObjects.size - 1; i >= 0; i--) {
                if (i >= movingObjects.size) continue; // Safety check

                MovingObject obj = movingObjects.get(i);
                if (obj == null) continue;

                obj.update(dt * factor);  // apply slow motion
                
                // Remove objects that finished sliding animation
                if (obj.isSliding && obj.slideTimer >= obj.slideDuration) {
                    Gdx.app.log("MovingObjects", "Removing object after slide animation");
                    movingObjects.removeIndex(i);
                    continue;
                }

                // Check collision with player
                if (obj.collidesWithPlayer()) {
                    Gdx.app.log("Collision", "=== OBJECT COLLIDED WITH PLAYER! ===");
                    playRandomCollisionSound();

                    // Xử lý va chạm với khiên (nếu có)
                    if (gameMap.player.isShieldActive()) {
                        Gdx.app.log("Collision", "Player has shield - calling handleShieldCollision()");
                        gameMap.player.handleShieldCollision();
                    } else {
                        Gdx.app.log("Collision", "Player has no shield - calling deactivateShield()");
                        // Không có khiên thì tắt shield
                        gameMap.player.deactivateShield();
                    }

                    movingObjects.removeIndex(i);
                    Gdx.app.log("Collision", "Object removed! Objects remaining: " + movingObjects.size);
                    continue;
                }

                // Check collision with other objects
                boolean objRemoved = false;
                for (int j = i - 1; j >= 0; j--) {
                    if (j >= movingObjects.size || i >= movingObjects.size) break;

                    MovingObject other = movingObjects.get(j);
                    if (other == null) continue;

                    if (obj.collidesWith(other)) {
                        // Xử lý va chạm với khiên (nếu có)
                        if (gameMap.player.isShieldActive()) {
                            gameMap.player.handleShieldCollision();
                        } else {
                            gameMap.player.deactivateShield();
                        }
                        playRandomCollisionSound();
                        if (i < movingObjects.size) movingObjects.removeIndex(i);
                        if (j < movingObjects.size) movingObjects.removeIndex(j);
                        Gdx.app.log("Collision", "Two objects collided! Objects remaining: " + movingObjects.size);
                        objRemoved = true;
                        break;
                    }
                }
                if (objRemoved) continue;
            }
        } catch (Exception e) {
            Gdx.app.log("GameScreen", "Error in updateMovingObjects: " + e.getMessage());
            movingObjects.clear();
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

    /**
     * Check if bullet collides with any slime and explode it
     */
    private boolean checkBulletSlimeCollision(Bullet bullet) {
        try {
            if (gameMap == null || gameMap.tileMap == null) {
                return false;
            }
            
            // Convert bullet world position to tile coordinates
            int bulletTileX = (int) (bullet.x / gameMap.tileMap.tileSize);
            int bulletTileY = (int) (bullet.y / gameMap.tileMap.tileSize);
            
            // Check bounds
            if (bulletTileX < 0 || bulletTileX >= gameMap.tileMap.mapWidth || 
                bulletTileY < 0 || bulletTileY >= gameMap.tileMap.mapHeight) {
                return false;
            }
            
            // Check if there's an enemy (slime) at this position
            if (gameMap.tileMap.containsEntity(bulletTileX, bulletTileY)) {
                Entity entity = gameMap.tileMap.getEntity(bulletTileX, bulletTileY);
                if (entity != null && entity instanceof Enemy) {
                    Enemy enemy = (Enemy) entity;
                    if (!enemy.isDead()) {
                        // Remove slime from map
                        gameMap.tileMap.removeEntity(bulletTileX, bulletTileY);
                        
                        Gdx.app.log("BulletCollision", "Slime exploded by bullet at (" + bulletTileX + "," + bulletTileY + ")");
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            Gdx.app.log("BulletCollision", "Error checking bullet-slime collision: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shoot a bullet from the specified position
     */
    public void shootBullet(float x, float y) {
        Bullet bullet = new Bullet(x, y);
        bullets.add(bullet);
        Gdx.app.log("GameScreen", "Bullet fired at: " + x + ", " + y);
    }
    
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

            // render bullets
            for (Bullet b : bullets)
                b.render(game.batch);

            // NEW: render raindrops - beautiful falling weather effect
            for (Raindrop r : raindrops)
                r.render(game.batch);

            // NEW: render explosions - spectacular blast effects! 💥
            for (Explosion e : explosions)
                e.render(game.batch);

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
        else if (currentEvent == EventState.INVENTORY)
            game.inventoryUI.render(dt);
        else if (currentEvent == EventState.PAUSE)
            hud.render(dt);  // Render HUD when paused to show settings dialog
    }

    public void setCurrentEvent(EventState event) {
        currentEvent = event;
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
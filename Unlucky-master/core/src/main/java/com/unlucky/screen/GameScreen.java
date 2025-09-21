package com.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
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

    // NEW: Sound for bullet end
    private Sound bulletEndSound;

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

    public void init(int worldIndex, int levelIndex) {
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

        cam.position.x = playerX;
        cam.position.y = playerY;

        if (cam.position.y < halfHeight)
            cam.position.y = halfHeight;
        if (cam.position.y > gameMap.tileMap.mapHeight * 16 - halfHeight)
            cam.position.y = gameMap.tileMap.mapHeight * 16 - halfHeight;

        if (cam.position.x < 0)
            cam.position.x = 0;
        if (cam.position.x > gameMap.tileMap.mapWidth * 16 - cam.viewportWidth)
            cam.position.x = gameMap.tileMap.mapWidth * 16 - cam.viewportWidth;

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
            // NEW: Create explosion when bullet TOUCHES border!
            if (b.touchesBorder(cam.position.x, cam.position.y, cam.viewportWidth, cam.viewportHeight)) {
                // Create explosion at bullet position
                Explosion explosion = new Explosion(b.x, b.y);
                explosions.add(explosion);
                
                bullets.removeIndex(i);
                
                // Play explosion sound
                if (bulletEndSound != null) {
                    bulletEndSound.play();
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
    }

    public void render(float dt) {
        update(dt);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
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
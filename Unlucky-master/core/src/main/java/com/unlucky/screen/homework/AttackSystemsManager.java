package com.unlucky.screen.homework;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.unlucky.main.Unlucky;
import com.unlucky.map.GameMap;
import com.unlucky.resource.ResourceManager;

/**
 * Manages all attack systems for homework requirements:
 * - Bullet System (upward shooting)
 * - Explosion Effects (on bullet collision)
 * - Moving Objects (pursuing player)
 * 
 * @author Homework Implementation
 */
public class AttackSystemsManager {
    
    private final Unlucky game;
    private final ResourceManager rm;
    private final GameMap gameMap;
    private final OrthographicCamera cam;
    
    // Attack Systems
    private Array<Bullet> bullets;
    private Array<Explosion> explosions;
    private Array<MovingObject> movingObjects;
    
    // Configuration
    private final int MAX_OBJECTS = 3;
    private Sound bulletEndSound;
    private Sound[] collisionSounds;
    
    public AttackSystemsManager(Unlucky game, ResourceManager rm, GameMap gameMap, OrthographicCamera cam) {
        this.game = game;
        this.rm = rm;
        this.gameMap = gameMap;
        this.cam = cam;
        
        initializeSystems();
    }
    
    private void initializeSystems() {
        bullets = new Array<>();
        explosions = new Array<>();
        movingObjects = new Array<>();
        
        // Initialize collision sounds
        collisionSounds = new Sound[]{rm.hit};
        
        // Load bullet end sound
        try {
            bulletEndSound = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.ogg"));
        } catch (Exception e) {
            Gdx.app.log("AttackSystems", "Error loading bullet sound: " + e.getMessage());
            bulletEndSound = null;
        }
        
        Gdx.app.log("AttackSystems", "Attack systems initialized");
    }
    
    /**
     * Fire a bullet from player position
     */
    public void fireBullet(float playerX, float playerY) {
        Bullet bullet = new Bullet(playerX, playerY + 8);
        bullets.add(bullet);
        Gdx.app.log("AttackSystems", "Bullet fired from: " + playerX + ", " + playerY);
    }
    
    /**
     * Update all attack systems
     */
    public void update(float dt) {
        updateBullets(dt);
        updateExplosions(dt);
        updateMovingObjects(dt);
    }
    
    private void updateBullets(float dt) {
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(dt);
            
            if (b.touchesBorder(cam.position.x, cam.position.y, cam.viewportWidth, cam.viewportHeight)) {
                // Create explosion at bullet position
                Explosion explosion = new Explosion(b.x, b.y);
                explosions.add(explosion);
                
                bullets.removeIndex(i);
                
                // Play explosion sound
                if (bulletEndSound != null && !game.player.settings.muteSfx) {
                    bulletEndSound.play(game.player.settings.sfxVolume);
                }
                
                Gdx.app.log("AttackSystems", "💥 Explosion created at: " + b.x + ", " + b.y);
            }
        }
    }
    
    private void updateExplosions(float dt) {
        for (int i = explosions.size - 1; i >= 0; i--) {
            Explosion e = explosions.get(i);
            e.update(dt);
            if (e.isFinished()) {
                explosions.removeIndex(i);
            }
        }
    }
    
    private void updateMovingObjects(float dt) {
        try {
            // Ensure we always have exactly 3 objects
            while (movingObjects.size < MAX_OBJECTS) {
                spawnRandomObject();
            }

            // Update all moving objects with safe iteration
            for (int i = movingObjects.size - 1; i >= 0; i--) {
                if (i >= movingObjects.size) continue;
                
                MovingObject obj = movingObjects.get(i);
                if (obj == null) continue;
                
                obj.update(dt);

                // Check collision with player
                if (obj.collidesWithPlayer()) {
                    playRandomCollisionSound();
                    movingObjects.removeIndex(i);
                    Gdx.app.log("AttackSystems", "Object collided with player! Objects remaining: " + movingObjects.size);
                    continue;
                }

                // Check collision with other objects
                boolean objRemoved = false;
                for (int j = i - 1; j >= 0; j--) {
                    if (j >= movingObjects.size || i >= movingObjects.size) break;
                    
                    MovingObject other = movingObjects.get(j);
                    if (other == null) continue;
                    
                    if (obj.collidesWith(other)) {
                        playRandomCollisionSound();
                        // Remove objects safely
                        if (i < movingObjects.size) movingObjects.removeIndex(i);
                        if (j < movingObjects.size) movingObjects.removeIndex(j);
                        Gdx.app.log("AttackSystems", "Two objects collided! Objects remaining: " + movingObjects.size);
                        objRemoved = true;
                        break;
                    }
                }
                if (objRemoved) continue;
            }
        } catch (Exception e) {
            Gdx.app.log("AttackSystems", "Error in updateMovingObjects: " + e.getMessage());
            movingObjects.clear();
        }
    }
    
    private void spawnRandomObject() {
        try {
            // Spawn objects around the camera view, but not too close to player
            float camX = cam.position.x;
            float camY = cam.position.y;
            float spawnRadius = 80f;
            float minDistFromPlayer = 30f;

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
            Gdx.app.log("AttackSystems", "New object spawned at: " + spawnX + ", " + spawnY);
        } catch (Exception e) {
            Gdx.app.log("AttackSystems", "Error spawning object: " + e.getMessage());
        }
    }
    
    private boolean tooCloseToPlayer(float x, float y, float minDistance) {
        try {
            if (gameMap == null || gameMap.player == null || gameMap.player.getPosition() == null) {
                return false;
            }
            
            float playerX = gameMap.player.getPosition().x;
            float playerY = gameMap.player.getPosition().y;
            float distance = (float) Math.sqrt((x - playerX) * (x - playerX) + (y - playerY) * (y - playerY));
            return distance < minDistance;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean tooCloseToOtherObjects(float x, float y, float minDistance) {
        for (MovingObject obj : movingObjects) {
            float distance = (float) Math.sqrt((x - obj.x) * (x - obj.x) + (y - obj.y) * (y - obj.y));
            if (distance < minDistance) {
                return true;
            }
        }
        return false;
    }
    
    private void playRandomCollisionSound() {
        try {
            if (collisionSounds != null && collisionSounds.length > 0 && !game.player.settings.muteSfx) {
                Sound randomSound = collisionSounds[0];
                if (randomSound != null) {
                    randomSound.play(game.player.settings.sfxVolume);
                }
            }
        } catch (Exception e) {
            Gdx.app.log("AttackSystems", "Error playing collision sound: " + e.getMessage());
        }
    }
    
    /**
     * Render all attack systems
     */
    public void render(SpriteBatch batch) {
        try {
            // Render bullets
            for (Bullet b : bullets) {
                if (b != null) b.render(batch);
            }
            
            // Render explosions
            for (Explosion e : explosions) {
                if (e != null) e.render(batch);
            }
            
            // Render moving objects
            for (MovingObject obj : movingObjects) {
                if (obj != null && obj.sprite != null) {
                    obj.render(batch);
                }
            }
        } catch (Exception e) {
            Gdx.app.log("AttackSystems", "Error rendering: " + e.getMessage());
        }
    }
    
    /**
     * Initialize moving objects when level starts
     */
    public void initializeLevel() {
        movingObjects.clear();
        
        // Spawn 3 objects immediately
        for (int i = 0; i < MAX_OBJECTS; i++) {
            spawnRandomObject();
        }
        Gdx.app.log("AttackSystems", "Level initialized with " + MAX_OBJECTS + " objects");
    }
    
    /**
     * Get moving objects array for external access (e.g., HUD)
     */
    public Array<MovingObject> getMovingObjects() {
        return movingObjects;
    }
    
    public void dispose() {
        if (bulletEndSound != null) {
            bulletEndSound.dispose();
        }
    }
    
    // Inner Classes
    public class Bullet {
        float x, y;
        float speed = 180f;
        float size = 12f;

        Bullet(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void update(float dt) {
            y += speed * dt;
        }

        void render(SpriteBatch batch) {
            batch.draw(rm.items20x20[1][0], x - size / 2, y - size / 2, size, size);
        }

        boolean touchesBorder(float camX, float camY, float viewWidth, float viewHeight) {
            float leftBorder = camX - viewWidth/2;
            float rightBorder = camX + viewWidth/2;
            float topBorder = camY + viewHeight/2;
            float bottomBorder = camY - viewHeight/2;
            
            return x <= leftBorder || x >= rightBorder || 
                   y <= bottomBorder || y >= topBorder;
        }
    }
    
    public class Explosion {
        float x, y;
        float animationTime = 0f;
        float frameDuration = 0.15f;
        float totalDuration = frameDuration * 3;
        float size = 24f;
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
                int frameIndex = (int) (animationTime / frameDuration);
                if (frameIndex >= 3) frameIndex = 2;
                
                TextureRegion explosionFrame = rm.battleAttacks64x64[2][frameIndex];
                batch.draw(explosionFrame, x - size / 2, y - size, size, size);
            }
        }

        boolean isFinished() {
            return isFinished;
        }
    }
    
    public class MovingObject {
        public float x, y;
        float speed = 60f;
        float size = 20f;
        public TextureRegion sprite;
        
        MovingObject(float x, float y) {
            this.x = x;
            this.y = y;
            
            // Safely select a random sprite from [0][1] to [0][3]
            try {
                if (rm.items20x20 != null && rm.items20x20.length > 0 && rm.items20x20[0] != null && rm.items20x20[0].length > 3) {
                    int randomIndex = 1 + (int)(Math.random() * 3);
                    this.sprite = rm.items20x20[0][randomIndex];
                } else {
                    this.sprite = rm.raindrop;
                }
            } catch (Exception e) {
                this.sprite = rm.raindrop;
                Gdx.app.log("AttackSystems", "Error accessing items20x20, using fallback: " + e.getMessage());
            }
        }
        
        void update(float dt) {
            try {
                if (gameMap != null && gameMap.player != null && gameMap.player.getPosition() != null) {
                    float playerX = gameMap.player.getPosition().x;
                    float playerY = gameMap.player.getPosition().y;
                    
                    float deltaX = playerX - x;
                    float deltaY = playerY - y;
                    float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    
                    if (distance > speed * dt) {
                        x += (deltaX / distance) * speed * dt;
                        y += (deltaY / distance) * speed * dt;
                    }
                }
            } catch (Exception e) {
                Gdx.app.log("AttackSystems", "Error in MovingObject update: " + e.getMessage());
            }
        }
        
        void render(SpriteBatch batch) {
            try {
                if (sprite != null && batch != null) {
                    batch.draw(sprite, x - size / 2, y - size / 2, size, size);
                }
            } catch (Exception e) {
                Gdx.app.log("AttackSystems", "Error rendering MovingObject: " + e.getMessage());
            }
        }
        
        Rectangle getRectangle() {
            return new Rectangle(x - size / 2, y - size / 2, size, size);
        }
        
        boolean collidesWith(MovingObject other) {
            return this.getRectangle().overlaps(other.getRectangle());
        }
        
        boolean collidesWithPlayer() {
            try {
                if (gameMap == null || gameMap.player == null || gameMap.player.getPosition() == null) {
                    return false;
                }
                
                float playerX = gameMap.player.getPosition().x;
                float playerY = gameMap.player.getPosition().y;
                float playerSize = 16f;
                
                Rectangle playerRect = new Rectangle(
                    playerX - playerSize / 2, playerY - playerSize / 2, playerSize, playerSize);
                
                return this.getRectangle().overlaps(playerRect);
            } catch (Exception e) {
                return false;
            }
        }
    }
}
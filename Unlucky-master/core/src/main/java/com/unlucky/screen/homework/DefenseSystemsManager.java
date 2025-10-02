package com.unlucky.screen.homework;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.unlucky.entity.Player;
import com.unlucky.main.Unlucky;
import com.unlucky.resource.ResourceManager;

/**
 * Manages all defense systems for homework requirements:
 * - Shield System (blocks moving objects)
 * - Slow Motion (slows down game time)
 * - Wind Wall (creates barriers)
 * 
 * @author Homework Implementation
 */
public class DefenseSystemsManager {
    
    private final Unlucky game;
    private final ResourceManager rm;
    private final Player player;
    private com.unlucky.ui.Hud hud; // Reference to HUD for mana checking
    
    // Defense States
    public boolean slowMotion = false;
    private boolean windWallActive = false;
    private Image windWallImage;
    
    // Shield System - full control in DefenseSystemsManager
    private boolean shieldActive = false;
    private int shieldHitCount = 0;
    private final int MAX_SHIELD_HITS = 10; // Max hits before shield is destroyed
    private boolean shieldFlashing = false;
    private float shieldFlashTimer = 0f;
    private final float SHIELD_FLASH_DURATION = 1.0f; // 1 second flashing after hit
    
    // Wind Wall Barrier System
    private Array<WindWallBarrier> windWallBarriers;
    private int windWallBarriersRemaining = 0;
    private final int MAX_WIND_WALL_BARRIERS = 20; // Can block 20 objects total
    
    // Timing
    private float slowMotionTimer = 0f;
    private float windWallTimer = 0f;
    private final float SLOW_MOTION_DURATION = 5f; // 5 seconds
    
    // Mana Costs
    private final int SHIELD_MANA_COST = 20;
    private final int SLOW_MOTION_MANA_COST = 30;
    private final int WIND_WALL_MANA_COST = 25;
    private final float WIND_WALL_DURATION = 15f; // 15 seconds for barrier duration
    
    public DefenseSystemsManager(Unlucky game, ResourceManager rm, Player player) {
        this.game = game;
        this.rm = rm;
        this.player = player;
        
        initializeDefenseSystems();
    }
    
    private void initializeDefenseSystems() {
        // Initialize wind wall image
        windWallImage = new Image(rm.shopitems[9][0]);
        windWallImage.setSize(32, 32);
        windWallImage.setPosition(100, 150);
        windWallImage.setVisible(false);
        
        // Initialize wind wall barriers array
        windWallBarriers = new Array<>();
        
        // Add blinking animation
        windWallImage.addAction(Actions.forever(Actions.sequence(
            Actions.fadeOut(0.5f),
            Actions.fadeIn(0.5f)
        )));
        
        Gdx.app.log("DefenseSystems", "Defense systems initialized");
    }
    
    /**
     * Add wind wall to a stage (usually Hud stage)
     */
    public void addWindWallToStage(Stage stage) {
        if (windWallImage != null && stage != null) {
            stage.addActor(windWallImage);
            Gdx.app.log("DefenseSystems", "Wind wall added to stage");
        }
    }
    
    /**
     * Sets the HUD reference for mana checking
     */
    public void setHud(com.unlucky.ui.Hud hud) {
        this.hud = hud;
    }
    
    /**
     * Activate shield defense - checks mana before activation
     */
    public void activateShield() {
        if (!shieldActive) {
            // Check mana before activation
            if (hud != null && !hud.hasEnoughMana(SHIELD_MANA_COST)) {
                Gdx.app.log("DefenseSystems", "❌ Not enough mana for shield! Need " + SHIELD_MANA_COST);
                return;
            }
            
            // Consume mana
            if (hud != null) {
                hud.consumeMana(SHIELD_MANA_COST);
            }
            
            shieldActive = true;
            shieldHitCount = 0;
            shieldFlashing = false;
            shieldFlashTimer = 0f;
            
            Gdx.app.log("DefenseSystems", "🛡️ Shield activated! Can block " + MAX_SHIELD_HITS + " hits (Cost: " + SHIELD_MANA_COST + " mana)");
        } else {
            // Deactivate shield
            deactivateShield();
            Gdx.app.log("DefenseSystems", "🛡️ Shield deactivated!");
        }
    }
    
    /**
     * Deactivate shield system
     */
    private void deactivateShield() {
        shieldActive = false;
        shieldHitCount = 0;
        shieldFlashing = false;
        shieldFlashTimer = 0f;
        
        // Shield is now completely managed by DefenseSystemsManager
        // No need to deactivate anything in Player class
    }
    
    /**
     * Activate slow motion defense - checks mana before activation
     */
    public void activateSlowMotion() {
        if (!slowMotion) {
            // Check mana before activation
            if (hud != null && !hud.hasEnoughMana(SLOW_MOTION_MANA_COST)) {
                Gdx.app.log("DefenseSystems", "❌ Not enough mana for slow motion! Need " + SLOW_MOTION_MANA_COST);
                return;
            }
            
            // Consume mana
            if (hud != null) {
                hud.consumeMana(SLOW_MOTION_MANA_COST);
            }
            
            slowMotion = true;
            slowMotionTimer = 0f;
            Gdx.app.log("DefenseSystems", "🐌 SLOW MOTION ACTIVATED for " + SLOW_MOTION_DURATION + " seconds! (Cost: " + SLOW_MOTION_MANA_COST + " mana)");
        } else {
            slowMotion = false;
            Gdx.app.log("DefenseSystems", "⚡ SLOW MOTION DEACTIVATED!");
        }
    }
    
    /**
     * Activate wind wall defense - checks mana before activation
     */
    public void activateWindWall() {
        if (!windWallActive) {
            // Check mana before activation
            if (hud != null && !hud.hasEnoughMana(WIND_WALL_MANA_COST)) {
                Gdx.app.log("DefenseSystems", "❌ Not enough mana for wind wall! Need " + WIND_WALL_MANA_COST);
                return;
            }
            
            // Consume mana
            if (hud != null) {
                hud.consumeMana(WIND_WALL_MANA_COST);
            }
            
            windWallActive = true;
            windWallTimer = 0f;
            windWallBarriersRemaining = MAX_WIND_WALL_BARRIERS;
            
            // Spawn 4 barrier objects in front of player
            spawnWindWallBarriers();
            
            Gdx.app.log("DefenseSystems", "🌪️ WIND WALL ACTIVATED! Spawned barriers, can block " + MAX_WIND_WALL_BARRIERS + " objects");
        } else {
            // Deactivate and clear barriers
            windWallActive = false;
            windWallBarriers.clear();
            windWallBarriersRemaining = 0;
            Gdx.app.log("DefenseSystems", "🌪️ WIND WALL DEACTIVATED!");
        }
    }
    
    /**
     * Spawn wind wall barriers in front of player
     */
    private void spawnWindWallBarriers() {
        windWallBarriers.clear();
        
        float playerX = player.getPosition().x;
        float playerY = player.getPosition().y;
        
        // Spawn 4 barriers in a row in front of player (assume player faces up)
        float startX = playerX - 36f; // Start left of player
        float barrierY = playerY + 40f; // In front of player
        float spacing = 24f;
        
        for (int i = 0; i < 4; i++) {
            float barrierX = startX + (i * spacing);
            
            // Use rock/stone texture from items or shopitems
            TextureRegion rockSprite = rm.shopitems[2][0]; // Using a solid item as barrier
            WindWallBarrier barrier = new WindWallBarrier(barrierX, barrierY, rockSprite);
            windWallBarriers.add(barrier);
            
            Gdx.app.log("DefenseSystems", "Spawned barrier at: " + barrierX + ", " + barrierY);
        }
    }
    
    /**
     * Update all defense systems
     */
    public void update(float dt) {
        updateSlowMotion(dt);
        updateWindWall(dt);
        updateShield(dt);
    }
    
    private void updateSlowMotion(float dt) {
        if (slowMotion) {
            slowMotionTimer += dt;
            
            // Auto-deactivate after duration
            if (slowMotionTimer >= SLOW_MOTION_DURATION) {
                slowMotion = false;
                slowMotionTimer = 0f;
                Gdx.app.log("DefenseSystems", "⚡ Slow motion auto-deactivated after " + SLOW_MOTION_DURATION + " seconds");
            }
        }
    }
    
    private void updateWindWall(float dt) {
        if (windWallActive) {
            windWallTimer += dt;
            
            // Auto-deactivate after duration
            if (windWallTimer >= WIND_WALL_DURATION) {
                windWallActive = false;
                windWallTimer = 0f;
                windWallImage.setVisible(false);
                Gdx.app.log("DefenseSystems", "🌪️ Wind wall auto-deactivated after " + WIND_WALL_DURATION + " seconds");
            }
        }
    }
    
    private void updateShield(float dt) {
        // Update shield flashing effect
        if (shieldFlashing) {
            shieldFlashTimer += dt;
            
            // Stop flashing after duration
            if (shieldFlashTimer >= SHIELD_FLASH_DURATION) {
                shieldFlashing = false;
                shieldFlashTimer = 0f;
                Gdx.app.log("DefenseSystems", "🛡️ Shield flashing stopped");
            }
        }
        
        // Shield is now completely managed by DefenseSystemsManager
        // No Player shield update needed
    }
    
    /**
     * Handle collision with moving objects when shield is active
     * Full control of shield logic in DefenseSystemsManager
     */
    public boolean handleShieldCollision(AttackSystemsManager.MovingObject obj) {
        if (shieldActive) {
            // Increase hit counter
            shieldHitCount++;
            
            // Start flashing effect
            shieldFlashing = true;
            shieldFlashTimer = 0f;
            
            Gdx.app.log("DefenseSystems", "🛡️ SHIELD HIT! Count: " + shieldHitCount + "/" + MAX_SHIELD_HITS);
            Gdx.app.log("DefenseSystems", "🛡️ Shield flashing activated");
            
            // Object will be removed by GameScreen - no need to push it away
            // Shield collision destroys the object completely
            
            // Check if shield should be destroyed
            if (shieldHitCount >= MAX_SHIELD_HITS) {
                deactivateShield();
                Gdx.app.log("DefenseSystems", "🛡️ SHIELD DESTROYED after " + MAX_SHIELD_HITS + " hits!");
            }
            
            return true; // Collision was handled by shield
        }
        return false; // No shield protection
    }
    
    /**
     * Check if wind wall barriers block object movement
     */
    public boolean checkWindWallBarrierCollision(AttackSystemsManager.MovingObject obj) {
        if (!windWallActive || windWallBarriersRemaining <= 0) return false;
        
        for (int i = windWallBarriers.size - 1; i >= 0; i--) {
            WindWallBarrier barrier = windWallBarriers.get(i);
            if (!barrier.isDestroyed && obj.getRectangle().overlaps(barrier.getRectangle())) {
                // Barrier blocks the object
                barrier.destroy();
                windWallBarriers.removeIndex(i);
                windWallBarriersRemaining--;
                
                Gdx.app.log("DefenseSystems", "🌪️ Barrier destroyed object! Barriers remaining: " + windWallBarriersRemaining);
                
                // Deactivate wind wall if no barriers left
                if (windWallBarriersRemaining <= 0) {
                    windWallActive = false;
                    Gdx.app.log("DefenseSystems", "🌪️ All barriers used up! Wind wall deactivated");
                }
                
                return true; // Object was blocked
            }
        }
        return false; // No collision with barriers
    }
    
    /**
     * Check if wind wall blocks object movement
     */
    public boolean isWindWallBlocking(float x, float y) {
        if (!windWallActive) return false;
        
        // Enhanced collision detection with wind wall
        float wallX = windWallImage.getX();
        float wallY = windWallImage.getY();
        float wallWidth = windWallImage.getWidth();
        float wallHeight = windWallImage.getHeight();
        
        // Add some buffer around the wind wall for better collision detection
        float buffer = 5f;
        return x >= wallX - buffer && x <= wallX + wallWidth + buffer && 
               y >= wallY - buffer && y <= wallY + wallHeight + buffer;
    }
    
    /**
     * Get time multiplier for slow motion effect
     */
    public float getTimeMultiplier() {
        return slowMotion ? 0.3f : 1.0f; // 30% speed when slow motion is active
    }
    
    /**
     * Render defense effects
     */
    public void render(SpriteBatch batch) {
        // Render shield around player if active
        if (shieldActive && shouldShowShield()) {
            float playerX = player.getPosition().x;
            float playerY = player.getPosition().y;
            
            // Draw red shield around player
            float shieldSize = 28f;
            batch.setColor(1.0f, 0.0f, 0.0f, 0.8f); // Red color
            batch.draw(rm.shopitems[7][0], playerX - 6, playerY - 6, shieldSize, shieldSize);
            batch.setColor(1, 1, 1, 1); // Reset color to white
            
            Gdx.app.log("DefenseSystems", "🛡️ Shield rendered at (" + playerX + "," + playerY + ")");
        }
        
        // Slow motion is a time effect, no visual rendering needed
        
        // Render wind wall barriers
        if (windWallActive) {
            for (WindWallBarrier barrier : windWallBarriers) {
                barrier.render(batch);
            }
        }
    }
    
    /**
     * Get wind wall image for UI integration
     */
    public Image getWindWallImage() {
        return windWallImage;
    }
    
    /**
     * Get current defense states for UI display
     */
    public boolean isShieldActive() {
        return shieldActive;
    }
    
    public boolean isSlowMotionActive() {
        return slowMotion;
    }
    
    public boolean isWindWallActive() {
        return windWallActive;
    }
    
    /**
     * Get remaining time for active defenses
     */
    public float getSlowMotionTimeRemaining() {
        return slowMotion ? Math.max(0, SLOW_MOTION_DURATION - slowMotionTimer) : 0;
    }
    
    public float getWindWallTimeRemaining() {
        return windWallActive ? Math.max(0, WIND_WALL_DURATION - windWallTimer) : 0;
    }
    
    /**
     * Get shield information for Player class rendering
     */
    public boolean isShieldFlashing() {
        return shieldFlashing;
    }
    
    public boolean shouldShowShield() {
        if (!shieldFlashing) return true; // Always show when not flashing
        
        // Create flashing effect - show/hide every 0.1 seconds
        return (int)(shieldFlashTimer * 10) % 2 == 0;
    }
    
    public int getShieldHitCount() {
        return shieldHitCount;
    }
    
    public int getMaxShieldHits() {
        return MAX_SHIELD_HITS;
    }
    
    /**
     * Reset all defense systems
     */
    public void resetDefenses() {
        slowMotion = false;
        windWallActive = false;
        slowMotionTimer = 0f;
        windWallTimer = 0f;
        windWallImage.setVisible(false);
        
        // Reset shield - full control here
        if (shieldActive) {
            deactivateShield();
        }
        
        // Clear wind wall barriers
        windWallBarriers.clear();
        windWallBarriersRemaining = 0;
        
        Gdx.app.log("DefenseSystems", "All defense systems reset");
    }
    
    public void dispose() {
        // Clean up resources if needed
    }
    
    /**
     * Wind Wall Barrier - physical objects that block incoming attacks
     */
    public static class WindWallBarrier {
        public float x, y;
        public float size = 24f;
        public TextureRegion sprite;
        public boolean isDestroyed = false;
        
        public WindWallBarrier(float x, float y, TextureRegion sprite) {
            this.x = x;
            this.y = y;
            this.sprite = sprite;
        }
        
        public Rectangle getRectangle() {
            return new Rectangle(x - size / 2, y - size / 2, size, size);
        }
        
        public void render(SpriteBatch batch) {
            if (!isDestroyed && sprite != null) {
                batch.draw(sprite, x - size / 2, y - size / 2, size, size);
            }
        }
        
        public void destroy() {
            isDestroyed = true;
        }
    }
}
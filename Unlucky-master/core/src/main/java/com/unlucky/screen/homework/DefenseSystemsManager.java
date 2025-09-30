package com.unlucky.screen.homework;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    
    // Defense States
    public boolean slowMotion = false;
    private boolean windWallActive = false;
    private Image windWallImage;
    
    // Timing
    private float slowMotionTimer = 0f;
    private float windWallTimer = 0f;
    private final float SLOW_MOTION_DURATION = 5f; // 5 seconds
    private final float WIND_WALL_DURATION = 3f; // 3 seconds
    
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
        
        // Add blinking animation
        windWallImage.addAction(Actions.forever(Actions.sequence(
            Actions.fadeOut(0.5f),
            Actions.fadeIn(0.5f)
        )));
        
        Gdx.app.log("DefenseSystems", "Defense systems initialized");
    }
    
    /**
     * Activate shield defense
     */
    public void activateShield() {
        player.toggleShield();
        Gdx.app.log("DefenseSystems", "Shield toggled: " + player.isShieldActive());
    }
    
    /**
     * Activate slow motion defense
     */
    public void activateSlowMotion() {
        slowMotion = !slowMotion;
        slowMotionTimer = 0f;
        
        if (slowMotion) {
            Gdx.app.log("DefenseSystems", "🐌 SLOW MOTION ACTIVATED for " + SLOW_MOTION_DURATION + " seconds!");
        } else {
            Gdx.app.log("DefenseSystems", "⚡ SLOW MOTION DEACTIVATED!");
        }
    }
    
    /**
     * Activate wind wall defense
     */
    public void activateWindWall() {
        windWallActive = !windWallActive;
        windWallTimer = 0f;
        windWallImage.setVisible(windWallActive);
        
        if (windWallActive) {
            Gdx.app.log("DefenseSystems", "🌪️ WIND WALL ACTIVATED for " + WIND_WALL_DURATION + " seconds!");
        } else {
            Gdx.app.log("DefenseSystems", "🌪️ WIND WALL DEACTIVATED!");
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
        // Shield is handled by Player class
        player.updateShield(dt);
    }
    
    /**
     * Handle collision with moving objects when shield is active
     */
    public boolean handleShieldCollision(AttackSystemsManager.MovingObject obj) {
        if (player.isShieldActive()) {
            // Trigger shield collision handling
            player.handleShieldCollision();
            
            // Make object slide away from player
            if (obj != null) {
                float playerX = player.getPosition().x;
                float playerY = player.getPosition().y;
                
                // Calculate direction away from player
                float deltaX = obj.x - playerX;
                float deltaY = obj.y - playerY;
                float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                
                if (distance > 0) {
                    // Push object away
                    float pushDistance = 50f;
                    obj.x += (deltaX / distance) * pushDistance;
                    obj.y += (deltaY / distance) * pushDistance;
                }
                
                Gdx.app.log("DefenseSystems", "🛡️ Shield blocked moving object!");
                return true; // Collision was handled by shield
            }
        }
        return false; // No shield protection
    }
    
    /**
     * Check if wind wall blocks object movement
     */
    public boolean isWindWallBlocking(float x, float y) {
        if (!windWallActive) return false;
        
        // Simple rectangular collision with wind wall
        float wallX = windWallImage.getX();
        float wallY = windWallImage.getY();
        float wallWidth = windWallImage.getWidth();
        float wallHeight = windWallImage.getHeight();
        
        return x >= wallX && x <= wallX + wallWidth && 
               y >= wallY && y <= wallY + wallHeight;
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
        // Shield rendering is handled by Player class
        // Wind wall rendering is handled by UI stage
        // Slow motion is a time effect, no visual rendering needed
        
        // Could add visual indicators here if needed
        if (slowMotion) {
            // Could add screen tint or particle effects
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
        return player.isShieldActive();
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
     * Reset all defense systems
     */
    public void resetDefenses() {
        slowMotion = false;
        windWallActive = false;
        slowMotionTimer = 0f;
        windWallTimer = 0f;
        windWallImage.setVisible(false);
        
        if (player.isShieldActive()) {
            player.deactivateShield();
        }
        
        Gdx.app.log("DefenseSystems", "All defense systems reset");
    }
    
    public void dispose() {
        // Clean up resources if needed
    }
}
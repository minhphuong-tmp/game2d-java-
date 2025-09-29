package com.unlucky.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.unlucky.animation.AnimationManager;
import com.unlucky.battle.Moveset;
import com.unlucky.battle.StatusSet;
import com.unlucky.entity.Entity;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;

/**
 * An Entity that the Player can battle if encountered
 * A Tile will hold an Enemy
 * An Enemy will not be able to move
 *
 * @author Ming Li
 */
public abstract class Enemy extends Entity {

    // battle status effects
    public StatusSet statusEffects;

    // battle sprite size
    // (used for making sprites bigger or smaller for effect)
    public int battleSize;
    // num of times the enemy respawned
    // (used for enemies that have special respawn capabilities)
    public int numRespawn;
    
    // sword flash effect
    public boolean isSwordFlashing = false;
    private float swordFlashTimer = 0f;
    private final float FLASH_INTERVAL = 0.05f; // Flash every 0.05 seconds (faster)
    private int flashCount = 0;
    private final int MAX_FLASHES = 20; // Flash 20 times (longer)
    private boolean isVisible = true; // For flashing effect
    
    // sliding effect for kick
    public boolean isSliding = false;
    private float slideTimer = 0f;
    private final float SLIDE_DURATION = 2.0f; // Slide for 2 seconds (slower)
    private int slideDirection = 0; // 0=down, 1=up, 2=right, 3=left
    private float slideSpeed = 100f; // Pixels per second (slower)
    private float slideRotation = 0f; // Rotation angle for sliding effect
    private int originalSlideTileX = -1; // Store original tile position for removal
    private int originalSlideTileY = -1;

    public Enemy(String id, Vector2 position, TileMap tileMap, ResourceManager rm) {
        super(id, position, tileMap, rm);
        moveset = new Moveset(rm);
        statusEffects = new StatusSet(false, rm);
        battleSize = 48;
        numRespawn = 0;
    }

    public abstract boolean isElite();

    public abstract boolean isBoss();

    /**
     * Sets and scales the stats of the enemy based on its type and level
     */
    public abstract void setStats();

    @Override
    public void setMaxHp(int maxHp) {
        this.maxHp = this.hp = this.previousHp = maxHp;
    }

    public void setOnlyMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }
    
    /**
     * Start sword flash effect - enemy will flash 3 times then die
     */
    public void startSwordFlash() {
        if (!isSwordFlashing && !isDead()) {
            isSwordFlashing = true;
            swordFlashTimer = 0f;
            flashCount = 0;
            Gdx.app.log("Enemy", "Sword flash started on " + id);
        }
    }
    
    /**
     * Start sliding effect - enemy will slide off screen when kicked
     */
    public void startSlidingEffect(int direction) {
        if (!isSliding && !isDead()) {
            isSliding = true;
            slideTimer = 0f;
            slideDirection = direction;
            slideRotation = 0f;
            
            // Store original tile position for later removal
            if (tileMap != null) {
                originalSlideTileX = (int) (position.x / tileMap.tileSize);
                originalSlideTileY = (int) (position.y / tileMap.tileSize);
            }
            
            Gdx.app.log("Enemy", "Sliding effect started on " + id + " direction: " + direction + " at (" + originalSlideTileX + "," + originalSlideTileY + ")");
        }
    }
    
    @Override
    public void update(float dt) {
        super.update(dt);
        
        // Update sword flash effect - rapid flashing like burning
        if (isSwordFlashing) {
            swordFlashTimer += dt;
            
            // Rapid flashing effect - toggle visibility every 0.2 seconds
            if (swordFlashTimer >= FLASH_INTERVAL) {
                isVisible = !isVisible;
                swordFlashTimer = 0f;
                flashCount++;
                
                Gdx.app.log("Enemy", "Flash " + flashCount + "/" + MAX_FLASHES + " for " + id + " (visible: " + isVisible + ")");
                
                // After 20 flashes, kill the enemy
                if (flashCount >= MAX_FLASHES) {
                    isSwordFlashing = false;
                    isVisible = true; // Make sure enemy is visible when dying
                    setDead(true);
                    
                    // Remove from map so it can respawn
                    if (tileMap != null) {
                        int tileX = (int) (position.x / tileMap.tileSize);
                        int tileY = (int) (position.y / tileMap.tileSize);
                        tileMap.removeEntity(tileX, tileY);
                        Gdx.app.log("Enemy", id + " removed from map after sword flash!");
                    }
                    
                    Gdx.app.log("Enemy", id + " died from sword flash!");
                }
            }
        }
        
        // Update sliding effect - move enemy off screen
        if (isSliding) {
            slideTimer += dt;
            
            // Calculate slide movement based on direction
            float slideX = 0f;
            float slideY = 0f;
            
            if (slideDirection == 0) slideY = -slideSpeed * dt; // down
            else if (slideDirection == 1) slideY = slideSpeed * dt; // up
            else if (slideDirection == 2) slideX = slideSpeed * dt; // right
            else if (slideDirection == 3) slideX = -slideSpeed * dt; // left
            
            // Move enemy
            position.x += slideX;
            position.y += slideY;
            
            // Add rotation effect while sliding
            slideRotation += 360f * dt; // Rotate 360 degrees per second
            
            // Check if slide duration is over
            if (slideTimer >= SLIDE_DURATION) {
                isSliding = false;
                setDead(true);
                
                // Remove from map after sliding is complete
                if (tileMap != null && originalSlideTileX >= 0 && originalSlideTileY >= 0) {
                    // Remove from original position
                    if (tileMap.containsEntity(originalSlideTileX, originalSlideTileY)) {
                        tileMap.removeEntity(originalSlideTileX, originalSlideTileY);
                        Gdx.app.log("Enemy", id + " removed from original position (" + originalSlideTileX + "," + originalSlideTileY + ")");
                    } else {
                        Gdx.app.log("Enemy", "Could not find entity at original position (" + originalSlideTileX + "," + originalSlideTileY + ")");
                    }
                }
                
                // Remove from sliding slimes list
                // Note: We can't access gameScreen directly from Enemy, so we'll handle this differently
                Gdx.app.log("Enemy", "Sliding completed for " + id);
                
                Gdx.app.log("Enemy", id + " finished sliding and died!");
            }
        }
    }
    
    @Override
    public void render(SpriteBatch batch, boolean looping) {
        // Only render if visible (for flashing effect)
        if (isVisible || !isSwordFlashing) {
            if (isSliding) {
                // Apply rotation effect while sliding
                float centerX = position.x + 8; // Center of sprite
                float centerY = position.y + 8;
                
                // Save current transform
                batch.flush();
                
                // Apply rotation
                batch.setTransformMatrix(batch.getTransformMatrix().translate(centerX, centerY, 0)
                    .rotate(0, 0, 1, slideRotation)
                    .translate(-centerX, -centerY, 0));
                
                // Render with rotation
                super.render(batch, looping);
                
                // Reset transform
                batch.setTransformMatrix(batch.getTransformMatrix().idt());
            } else {
                // Normal rendering
                super.render(batch, looping);
            }
        }
    }

}

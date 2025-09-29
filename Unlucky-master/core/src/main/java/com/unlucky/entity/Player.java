package com.unlucky.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.unlucky.animation.AnimationManager;
import com.unlucky.battle.Moveset;
import com.unlucky.battle.SpecialMoveset;
import com.unlucky.battle.StatusSet;
import com.unlucky.entity.enemy.Enemy;
import com.unlucky.inventory.Equipment;
import com.unlucky.inventory.Inventory;
import com.unlucky.inventory.Item;
import com.unlucky.map.GameMap;
import com.unlucky.map.Tile;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Statistics;
import com.unlucky.resource.Util;
import com.unlucky.save.Settings;

/**
 * The protagonist of the game.
 *
 * @author Ming Li
 */
public class Player extends Entity {


    /**
     * -1 - stop
     * 0 - down
     * 1 - up
     * 2 - right
     * 3 - left
     */
    public int moving = -1;
    // entity is in a continuous movement
    private float speed;
    // the Entity's current tile coordinates
    private int currentTileX;
    private int currentTileY;
    private int prevDir = -1;
    // tile causing a dialog event
    private boolean tileInteraction = false;
    // teleportation tiles
    private boolean shieldActive = false;
    private boolean teleporting = false;
    // end tiles
    private float shieldTimer = 0f;

    public boolean completedMap = false;

    public boolean isShieldActive() {
        return shieldActive;
    }
    // Statistics
    public Statistics stats = new Statistics();

    // Battle
    private Enemy opponent;
    private boolean battling = false;

    // exp and level up
    public void deactivateShield() {
        // Tắt trạng thái shield
        this.shieldActive = false;

        // Nếu
    }
    private int exp;
    private int maxExp;

    private boolean shieldVisible = true;    // để tạm ẩn khiên khi va chạm
    private float shieldCooldown = 0f;       // thời gian tạm ẩn khiên
    private final float SHIELD_HIDE_TIME = 0.5f; // 0.5 giây
    private int hpIncrease = 0;
    private int minDmgIncrease = 0;
    private int maxDmgIncrease = 0;
    private int accuracyIncrease = 0;
    private int maxExpIncrease = 0;

    // gold
    private int gold = 0;

    // inventory and equips
    public Inventory inventory;
    public Equipment equips;

    // battle status effects
    public StatusSet statusEffects;
    // special moveset
    public SpecialMoveset smoveset;

    // special move cooldown
    // starts at 4 turns then every 10 levels it is reduced by 1 with a min of 1
    public int smoveCd = 4;

    // whether or not the player is currently in a map
    public boolean inMap = false;

    // kick animation state
    public boolean isKicking = false;
    private float kickTimer = 0f;
    private final float KICK_DURATION = 0.8f; // Duration of kick animation
    private float kickPower = 0f; // For kick effect animation
    
    // sword flash effect
    public boolean isSwordFlashing = false;
    private float swordFlashTimer = 0f;
    private final float SWORD_FLASH_DURATION = 1.0f; // Duration of flash effect
    
    // slime respawn system
    private float respawnTimer = 0f;
    private final float RESPAWN_INTERVAL = 3.0f; // Respawn every 3 seconds
    private java.util.List<int[]> originalSlimePositions = new java.util.ArrayList<>();

    // player's level progress stored as a (world, level) key
    public int maxWorld = 0;
    public int maxLevel = 0;

    // the player's custom game settings
    public Settings settings = new Settings();

    public void activateShield(float duration) {
        shieldActive = true;
        shieldTimer = duration;
    }

    public void updateShield(float dt) {
        if (!shieldVisible) {
            shieldCooldown -= dt;
            if (shieldCooldown <= 0f) {
                shieldVisible = true;
            }
        }
    }

    public Player(String id, ResourceManager rm) {
        super(id, rm);

        inventory = new Inventory();
        equips = new Equipment();

        // attributes
        hp = maxHp = previousHp = Util.PLAYER_INIT_MAX_HP;
        accuracy = Util.PLAYER_ACCURACY;
        minDamage = Util.PLAYER_INIT_MIN_DMG;
        maxDamage = Util.PLAYER_INIT_MAX_DMG;

        level = 1;
        speed = 50.f;

        exp = 0;
        // offset between 3 and 5
        maxExp = Util.calculateMaxExp(1, MathUtils.random(3, 5));

        // create tilemap animation
        am = new AnimationManager(rm.sprites16x16, Util.PLAYER_WALKING, Util.PLAYER_WALKING_DELAY);
        // create battle scene animation
        bam = new AnimationManager(rm.battleSprites96x96, 2, Util.PLAYER_WALKING, 2 / 5f);

        moveset = new Moveset(rm);
        // damage seed is a random number between the damage range
        moveset.reset(minDamage, maxDamage, maxHp);

        statusEffects = new StatusSet(true, rm);
        smoveset = new SpecialMoveset();
    }
    public void toggleShield() {
        shieldActive = !shieldActive;
        Gdx.app.log("Player", "🛡️ Shield toggled to: " + shieldActive);

        if (shieldActive) {
            showDebugText("SHIELD ACTIVATED!");
            Gdx.app.log("Player", "🛡️ Shield ON - will stay active until toggled off");
        } else {
            showDebugText("SHIELD DEACTIVATED!");
            Gdx.app.log("Player", "🛡️ Shield OFF");
        }
    }    public void update(float dt) {
        super.update(dt);

        // Update kick animation
        if (isKicking) {
            kickTimer += dt;
            
            // Create kick power effect (0 to 1 and back)
            kickPower = (float) Math.sin(kickTimer * 8) * 0.5f + 0.5f;
            
            if (kickTimer >= KICK_DURATION) {
                isKicking = false;
                kickTimer = 0f;
                kickPower = 0f;
            }
        }
        
        // Update sword flash animation
        if (isSwordFlashing) {
            swordFlashTimer += dt;
            if (swordFlashTimer >= SWORD_FLASH_DURATION) {
                isSwordFlashing = false;
                swordFlashTimer = 0f;
            }
        }
        
        // Update slime respawn timer
        respawnTimer += dt;
        if (respawnTimer >= RESPAWN_INTERVAL) {
            respawnSlimes();
            respawnTimer = 0f;
        }
        
        // Update debug text timer
        if (debugTextTimer > 0) {
            debugTextTimer -= dt;
            if (debugTextTimer <= 0) {
                debugText = "";
            }
        }

        // movement
        handleMovement(dt);
        // special tile handling
        handleSpecialTiles();

        // check for Entity interaction
        if (tileMap.containsEntity(tileMap.toTileCoords(position)) && canMove()) {
            opponent = (com.unlucky.entity.enemy.Enemy) tileMap.getEntity(tileMap.toTileCoords(position));
            battling = true;
        }
//        updateShield(dt);
    }

    public void render(SpriteBatch batch) {
        Gdx.app.log("PlayerRender", "🎬 RENDER METHOD CALLED - shieldActive: " + shieldActive);
        // draw shadow
        batch.draw(rm.shadow11x6, position.x + 3, position.y - 3);

        // VẼ NHÂN VẬT TRƯỚC
        batch.draw(am.getKeyFrame(true), position.x + 1, position.y);


        // === HIỆU ỨNG KHIEN - VẼ SAU NHÂN VẬT ===
        if (shieldActive) {
            Gdx.app.log("PlayerRender", "🔴 🛡️ DRAWING RED SHIELD - Active: " + shieldActive);

            // Vẽ khiên màu đỏ lớn để dễ nhìn
            float shieldSize = 28f;
            batch.setColor(1.0f, 0.0f, 0.0f, 0.8f); // Màu đỏ
            batch.draw(rm.shopitems[7][0], position.x - 6, position.y - 6, shieldSize, shieldSize);
            batch.setColor(Color.WHITE); // Reset màu
        }

        // Draw kick effect if kicking
        if (isKicking) {
            // Draw the boot icon at the front of the player with power effect
            float kickX = position.x + 1; // Same as sword position
            float kickY = position.y; // Same as sword position

            // Adjust position based on direction - make it more visible
            if (prevDir == 0) kickY -= 20; // down
            else if (prevDir == 1) kickY += 20; // up
            else if (prevDir == 2) kickX += 20; // right
            else if (prevDir == 3) kickX -= 20; // left

            // Scale based on kick power for dramatic effect
            float scale = 1.0f + kickPower * 0.5f; // Scale from 1.0 to 1.5
            float size = 20 * scale;

            // Draw boot icon (shopitems[5][6] - Inferno Greaves) with scaling
            batch.draw(rm.shopitems[5][6], kickX - size/2, kickY - size/2, size, size);

            // Add kick trail effect
            if (kickPower > 0.7f) {
                // Draw additional boot icons for trail effect
                float trailOffset = kickPower * 10f;
                if (prevDir == 0) kickY -= trailOffset; // down
                else if (prevDir == 1) kickY += trailOffset; // up
                else if (prevDir == 2) kickX += trailOffset; // right
                else if (prevDir == 3) kickX -= trailOffset; // left

                batch.setColor(1, 1, 1, kickPower * 0.5f); // Semi-transparent trail
                batch.draw(rm.shopitems[5][6], kickX - size/2, kickY - size/2, size * 0.7f, size * 0.7f);
                batch.setColor(1, 1, 1, 1); // Reset color
            }
        }

        // Draw sword flash effect if flashing
        if (isSwordFlashing) {
            // Calculate flash position (in front of player)
            float flashX = position.x + 1;
            float flashY = position.y;

            if (prevDir == 0) flashY -= 20; // down
            else if (prevDir == 1) flashY += 20; // up
            else if (prevDir == 2) flashX += 20; // right
            else if (prevDir == 3) flashX -= 20; // left

            // Create flashing effect with color change
            float flashIntensity = (float) Math.sin(swordFlashTimer * 20) * 0.5f + 0.5f;
            batch.setColor(1, 1, 1, flashIntensity);

            // Draw sword icon (shopitems[3][8] - Inferno Chestplate) as flash
            batch.draw(rm.shopitems[3][8], flashX, flashY, 20, 20);

            // Reset color
            batch.setColor(1, 1, 1, 1);
        }

        // Draw debug text if active
        if (debugTextTimer > 0) {
            // Simple text rendering - you can enhance this later
            // For now, just log it so you can see in console
            Gdx.app.log("DebugText", debugText);
        }
    }
    /**
     * Moves an entity to a target position with a given magnitude.
     * Player movement triggered by input
     *
     * @param dir
     */
    public void move(int dir) {
        currentTileX = (int) (position.x / tileMap.tileSize);
        currentTileY = (int) (position.y / tileMap.tileSize);
        prevDir = dir;
        moving = dir;
        stats.numSteps++;
    }

    public boolean canMove() {
        return moving == -1;
    }

    /**
     * This method is to fix a problem where the player can reset their
     * movement magnitudes continuously on a blocked tile
     *
     * @param dir
     * @return
     */
    public boolean nextTileBlocked(int dir) {
        currentTileX = (int) (position.x / tileMap.tileSize);
        currentTileY = (int) (position.y / tileMap.tileSize);
        switch (dir) {
            case 0: // down
                return tileMap.getTile(currentTileX, currentTileY - 1).isBlocked();
            case 1: // up
                return tileMap.getTile(currentTileX, currentTileY + 1).isBlocked();
            case 2: // right
                return tileMap.getTile(currentTileX + 1, currentTileY).isBlocked();
            case 3: // left
                return tileMap.getTile(currentTileX - 1, currentTileY).isBlocked();
        }
        return false;
    }

    /**
     * Returns the next tile coordinate to move to either
     * currentPos +/- 1 or currentPos if the next tile is blocked
     *
     * @param dir
     * @return
     */
    public int nextPosition(int dir) {
        switch (dir) {
            case 0: // down
                Tile d = tileMap.getTile(currentTileX, currentTileY - 1);
                if (d.isBlocked() || currentTileY - 1 <= 0) {
                    return currentTileY;
                }
                return currentTileY - 1;
            case 1: // up
                Tile u = tileMap.getTile(currentTileX, currentTileY + 1);
                if (u.isBlocked() || currentTileY + 1 >= tileMap.mapHeight - 1) {
                    return currentTileY;
                }
                return currentTileY + 1;
            case 2: // right
                Tile r = tileMap.getTile(currentTileX + 1, currentTileY);
                if (r.isBlocked() || currentTileX + 1 >= tileMap.mapWidth - 1) {
                    return currentTileX;
                }
                return currentTileX + 1;
            case 3: // left
                Tile l = tileMap.getTile(currentTileX - 1, currentTileY);
                if (l.isBlocked() || currentTileX - 1 <= 0) {
                    return currentTileX;
                }
                return currentTileX - 1;
        }
        return 0;
    }

    /**
     * Handles the player's next movements when standing on a special tile
     */
    public void handleSpecialTiles() {
        int cx = (int) (position.x / tileMap.tileSize);
        int cy = (int) (position.y / tileMap.tileSize);
        Tile currentTile = tileMap.getTile(cx, cy);

        if (currentTile.isSpecial()) am.currentAnimation.stop();

        if (canMove()) {
            // Player goes forwards or backwards from the tile in the direction they entered
            if (currentTile.isChange()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                boolean k = MathUtils.randomBoolean();
                switch (prevDir) {
                    case 0: // down
                        if (k) changeDirection(1);
                        else changeDirection(0);
                        break;
                    case 1: // up
                        if (k) changeDirection(0);
                        else changeDirection(1);
                        break;
                    case 2: // right
                        if (k) changeDirection(3);
                        else changeDirection(2);
                        break;
                    case 3: // left
                        if (k) changeDirection(2);
                        else changeDirection(3);
                        break;
                }
            }
            // Player goes 1 tile in a random direction not the direction they entered the tile on
            else if (currentTile.isInAndOut()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                // output direction (all other directions other than input direction)
                int odir = MathUtils.random(2);
                switch (prevDir) {
                    case 0: // down
                        if (odir == 0) changeDirection(3);
                        else if (odir == 1) changeDirection(2);
                        else changeDirection(0);
                        break;
                    case 1: // up
                        if (odir == 0) changeDirection(3);
                        else if (odir == 1) changeDirection(2);
                        else changeDirection(1);
                        break;
                    case 2: // right
                        if (odir == 0) changeDirection(0);
                        else if (odir == 1) changeDirection(1);
                        else changeDirection(2);
                        break;
                    case 3: // left
                        if (odir == 0) changeDirection(0);
                        else if (odir == 1) changeDirection(1);
                        else changeDirection(3);
                        break;
                }
            }
            else if (currentTile.isDown()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(0);
            }
            else if (currentTile.isUp()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(1);
            }
            else if (currentTile.isRight()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(2);
            }
            else if (currentTile.isLeft()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(3);
            }
            // trigger dialog event
            else if (currentTile.isQuestionMark() || currentTile.isExclamationMark()) tileInteraction = true;
            // trigger teleport event
            else if (currentTile.isTeleport()) teleporting = true;
            // ice sliding
            else if (currentTile.isIce()) {
                if (!nextTileBlocked(prevDir)) {
                    move(prevDir);
                    am.setAnimation(prevDir);
                    am.stopAnimation();
                    pauseAnim = true;
                }
            }
            // map completed
            else if (currentTile.isEnd()) completedMap = true;
            else pauseAnim = false;
        }
    }

    public void changeDirection(int dir) {
        move(dir);
        prevDir = dir;
        am.setAnimation(dir);
    }

    /**
     * Performs a kick attack
     */
    public void kick() {
        Gdx.app.log("TEST", "=== KICK METHOD CALLED ===");
        Gdx.app.log("Kick", "Kick method called! isKicking: " + isKicking + ", canMove: " + canMove());
        Gdx.app.log("Kick", "Player position: (" + currentTileX + "," + currentTileY + "), prevDir: " + prevDir);
        
        if (!isKicking && canMove()) {
            isKicking = true;
            kickTimer = 0f;
            
            Gdx.app.log("Kick", "Starting kick animation");
            
            // Play kick sound effect
            if (!settings.muteSfx) rm.hit.play(settings.sfxVolume);
            
            // Check for enemies in front of player and deal damage
            checkKickDamage();
        } else {
            Gdx.app.log("Kick", "Cannot kick - isKicking: " + isKicking + ", canMove: " + canMove());
        }
    }

    /**
     * Checks for enemies in front of player and pushes them away
     */
    private void checkKickDamage() {
        Gdx.app.log("Kick", "=== CHECKING KICK DAMAGE ===");
        
        // Get player's current tile position
        int playerX = currentTileX;
        int playerY = currentTileY;
        
        Gdx.app.log("Kick", "Player at: (" + playerX + "," + playerY + "), prevDir: " + prevDir);
        
        // Calculate front position based on current direction
        int frontX = playerX;
        int frontY = playerY;
        
        if (prevDir == 0) frontY--; // down
        else if (prevDir == 1) frontY++; // up
        else if (prevDir == 2) frontX++; // right
        else if (prevDir == 3) frontX--; // left
        
        Gdx.app.log("Kick", "Checking front position: (" + frontX + "," + frontY + ")");
        
        // Check if there's an enemy at the front position
        if (tileMap.containsEntity(frontX, frontY)) {
            Enemy enemy = (Enemy) tileMap.getEntity(frontX, frontY);
            if (enemy != null && !enemy.isDead()) {
                Gdx.app.log("Kick", "Found enemy at (" + frontX + "," + frontY + ")");
                
                // Simple push: move enemy 1 tile in the same direction
                int pushX = frontX;
                int pushY = frontY;
                
                if (prevDir == 0) pushY--; // down
                else if (prevDir == 1) pushY++; // up
                else if (prevDir == 2) pushX++; // right
                else if (prevDir == 3) pushX--; // left
                
                Gdx.app.log("Kick", "Trying to push enemy to (" + pushX + "," + pushY + ")");
                
                // Check bounds
                if (pushX >= 0 && pushX < tileMap.mapWidth && 
                    pushY >= 0 && pushY < tileMap.mapHeight) {
                    
                    // Check if push position is empty
                    if (!tileMap.containsEntity(pushX, pushY)) {
                        // Move enemy
                        tileMap.removeEntity(frontX, frontY);
                        tileMap.addEntity(enemy, pushX, pushY);
                        enemy.getPosition().set(pushX * tileMap.tileSize, pushY * tileMap.tileSize);
                        
                        Gdx.app.log("Kick", "SUCCESS: Enemy pushed!");
                    } else {
                        Gdx.app.log("Kick", "Push position blocked, dealing damage instead");
                        // Deal damage if can't push
                        int damage = MathUtils.random(minDamage, maxDamage);
                        enemy.hit(damage);
                        enemy.applyDamage();
                    }
                } else {
                    Gdx.app.log("Kick", "Push position out of bounds, dealing damage instead");
                    // Deal damage if out of bounds
                    int damage = MathUtils.random(minDamage, maxDamage);
                    enemy.hit(damage);
                    enemy.applyDamage();
                }
            } else {
                Gdx.app.log("Kick", "Enemy is null or dead");
            }
        } else {
            Gdx.app.log("Kick", "No enemy found at front position");
        }
    }

    /**
     * Kicks enemy in front of player - kicks them off screen (kills them)
     */
    public void kickEnemy() {
        Gdx.app.log("Kick", "=== KICK STARTED ===");
        Gdx.app.log("Kick", "isKicking: " + isKicking + ", canMove: " + canMove());
        Gdx.app.log("Kick", "tileMap: " + (tileMap != null ? "OK" : "NULL"));
        
        // Always try to kick (remove isKicking check for testing)
        isKicking = true;
        kickTimer = 0f;
        
        Gdx.app.log("Kick", "Kicking enemy off screen");
            
            // Use bullet collision logic - convert world position to tile coordinates
            float playerWorldX = position.x;
            float playerWorldY = position.y;
            int playerTileX = (int) (playerWorldX / tileMap.tileSize);
            int playerTileY = (int) (playerWorldY / tileMap.tileSize);
            
            Gdx.app.log("Kick", "Player world pos: (" + playerWorldX + "," + playerWorldY + ")");
            Gdx.app.log("Kick", "Player tile pos: (" + playerTileX + "," + playerTileY + "), prevDir: " + prevDir);
            
            // Debug: Check all entities on map
            Gdx.app.log("Kick", "=== SCANNING ALL ENTITIES ===");
            for (int y = 0; y < tileMap.mapHeight; y++) {
                for (int x = 0; x < tileMap.mapWidth; x++) {
                    if (tileMap.containsEntity(x, y)) {
                        Entity entity = tileMap.getEntity(x, y);
                        if (entity instanceof Enemy) {
                            Enemy enemy = (Enemy) entity;
                            Gdx.app.log("Kick", "Enemy at (" + x + "," + y + "): " + entity.getClass().getSimpleName() + 
                                      " - HP: " + enemy.hp + "/" + enemy.maxHp + " - Dead: " + enemy.isDead());
                        } else {
                            Gdx.app.log("Kick", "Entity at (" + x + "," + y + "): " + entity.getClass().getSimpleName());
                        }
                    }
                }
            }
            Gdx.app.log("Kick", "=== END SCAN ===");
            
            // Check only the tile in front of player based on direction
            int frontTileX = playerTileX;
            int frontTileY = playerTileY;
            
            // Calculate front position based on direction
            if (prevDir == 0) frontTileY--; // down
            else if (prevDir == 1) frontTileY++; // up
            else if (prevDir == 2) frontTileX++; // right
            else if (prevDir == 3) frontTileX--; // left
            
            Gdx.app.log("Kick", "Front tile pos: (" + frontTileX + "," + frontTileY + ")");
            
            // Check bounds
            if (frontTileX >= 0 && frontTileX < tileMap.mapWidth && 
                frontTileY >= 0 && frontTileY < tileMap.mapHeight) {
                
                // Check if there's an enemy at the front position
                if (tileMap.containsEntity(frontTileX, frontTileY)) {
                    Entity entity = tileMap.getEntity(frontTileX, frontTileY);
                    if (entity != null && entity instanceof Enemy) {
                        Enemy enemy = (Enemy) entity;
                        Gdx.app.log("Kick", "Found enemy at front position (" + frontTileX + "," + frontTileY + ")");
                        Gdx.app.log("Kick", "Enemy HP: " + enemy.hp + "/" + enemy.maxHp + ", Dead: " + enemy.isDead());
                        
                        // Create sliding effect - move enemy off screen instead of removing immediately
                        Gdx.app.log("Kick", "Creating sliding effect for enemy at (" + frontTileX + "," + frontTileY + ")");
                        
                        // Start sliding animation first
                        enemy.startSlidingEffect(prevDir);
                        
                        // Add to sliding slimes list for high-priority rendering
                        // Note: We'll handle this in GameScreen update loop instead
                        
                        // Don't remove from map immediately - let it slide first
                        // tileMap.removeEntity(frontTileX, frontTileY);
                        Gdx.app.log("Kick", "Enemy will slide before being removed");
                        
                        Gdx.app.log("Kick", "SUCCESS: Enemy kicked off screen!");
                        
                        // Visual feedback - show text on screen
                        showDebugText("KICK SUCCESS!");
                        
                        // Play sound effect
                        if (!settings.muteSfx) rm.hit.play(settings.sfxVolume);
                    } else {
                        Gdx.app.log("Kick", "Entity is not an enemy: " + entity.getClass().getSimpleName());
                    }
                } else {
                    Gdx.app.log("Kick", "No entity at front position (" + frontTileX + "," + frontTileY + ")");
                }
            } else {
                Gdx.app.log("Kick", "Front position out of bounds");
            }
        
        Gdx.app.log("Kick", "=== KICK COMPLETED ===");
    }

    /**
     * Creates sword flash effect on slime in front of player
     * Slime will flash 3 times then die
     */
    public void createSwordFlash() {
        Gdx.app.log("SwordFlash", "=== SWORD FLASH STARTED ===");
        Gdx.app.log("SwordFlash", "isSwordFlashing: " + isSwordFlashing + ", canMove: " + canMove());
        Gdx.app.log("SwordFlash", "tileMap: " + (tileMap != null ? "OK" : "NULL"));
        
        // Always try to flash (remove isSwordFlashing check for testing)
        isSwordFlashing = true;
        swordFlashTimer = 0f;
        
        Gdx.app.log("SwordFlash", "Creating sword flash effect");
            
            // Use bullet collision logic - convert world position to tile coordinates
            float playerWorldX = position.x;
            float playerWorldY = position.y;
            int playerTileX = (int) (playerWorldX / tileMap.tileSize);
            int playerTileY = (int) (playerWorldY / tileMap.tileSize);
            
            Gdx.app.log("SwordFlash", "Player world pos: (" + playerWorldX + "," + playerWorldY + ")");
            Gdx.app.log("SwordFlash", "Player tile pos: (" + playerTileX + "," + playerTileY + "), prevDir: " + prevDir);
            
            // Check only the tile in front of player based on direction
            int frontTileX = playerTileX;
            int frontTileY = playerTileY;
            
            // Calculate front position based on direction
            if (prevDir == 0) frontTileY--; // down
            else if (prevDir == 1) frontTileY++; // up
            else if (prevDir == 2) frontTileX++; // right
            else if (prevDir == 3) frontTileX--; // left
            
            Gdx.app.log("SwordFlash", "Front tile pos: (" + frontTileX + "," + frontTileY + ")");
            
            // Check bounds
            if (frontTileX >= 0 && frontTileX < tileMap.mapWidth && 
                frontTileY >= 0 && frontTileY < tileMap.mapHeight) {
                
                // Check if there's an enemy at the front position
                if (tileMap.containsEntity(frontTileX, frontTileY)) {
                    Entity entity = tileMap.getEntity(frontTileX, frontTileY);
                    if (entity != null && entity instanceof Enemy) {
                        Enemy enemy = (Enemy) entity;
                        Gdx.app.log("SwordFlash", "Found enemy at front position (" + frontTileX + "," + frontTileY + ")");
                        Gdx.app.log("SwordFlash", "Enemy HP: " + enemy.hp + "/" + enemy.maxHp + ", Dead: " + enemy.isDead());
                        
                        // Always flash regardless of dead status (for testing)
                        Gdx.app.log("SwordFlash", "Starting sword flash effect on enemy");
                        enemy.startSwordFlash();
                        
                        Gdx.app.log("SwordFlash", "SUCCESS: Sword flash started on slime!");
                        
                        // Visual feedback - show text on screen
                        showDebugText("SWORD FLASH!");
                    } else {
                        Gdx.app.log("SwordFlash", "Entity is not an enemy: " + entity.getClass().getSimpleName());
                    }
                } else {
                    Gdx.app.log("SwordFlash", "No entity at front position (" + frontTileX + "," + frontTileY + ")");
                }
            } else {
                Gdx.app.log("SwordFlash", "Front position out of bounds");
            }
            
            // Play sound effect
            if (!settings.muteSfx) rm.hit.play(settings.sfxVolume);
        
        Gdx.app.log("SwordFlash", "=== SWORD FLASH COMPLETED ===");
    }

    // Debug text variables
    private String debugText = "";
    private float debugTextTimer = 0f;
    private static final float DEBUG_TEXT_DURATION = 2f;
    
    // Show debug text on screen
    private void showDebugText(String text) {
        debugText = text;
        debugTextTimer = DEBUG_TEXT_DURATION;
        Gdx.app.log("Debug", text);
    }
    
    /**
     * Explodes slime in front of player
     */
    public void explodeSlime() {
        try {
            Gdx.app.log("Explode", "Exploding slime in front of player");
            
            // Check if tileMap is available
            if (tileMap == null) {
                Gdx.app.log("Explode", "tileMap is null, cannot explode slime");
                return;
            }
            
            // Get the tile in front of the player
            int frontX = currentTileX;
            int frontY = currentTileY;
            
            if (prevDir == 0) frontY--; // down
            else if (prevDir == 1) frontY++; // up
            else if (prevDir == 2) frontX++; // right
            else if (prevDir == 3) frontX--; // left
            
            // Check bounds
            if (frontX < 0 || frontX >= tileMap.mapWidth || frontY < 0 || frontY >= tileMap.mapHeight) {
                Gdx.app.log("Explode", "Front position out of bounds: (" + frontX + "," + frontY + ")");
                return;
            }
            
            // Check if there's an enemy at the front position
            if (tileMap.containsEntity(frontX, frontY)) {
                Entity entity = tileMap.getEntity(frontX, frontY);
                if (entity != null && entity instanceof Enemy) {
                    Enemy enemy = (Enemy) entity;
                    if (!enemy.isDead()) {
                        // Remove enemy from map
                        tileMap.removeEntity(frontX, frontY);
                        
                        Gdx.app.log("Explode", "Slime exploded and removed from (" + frontX + "," + frontY + ")");
                        
                        // Play explosion sound
                        if (!settings.muteSfx) rm.hit.play(settings.sfxVolume);
                    }
                }
            } else {
                Gdx.app.log("Explode", "No entity found at front position (" + frontX + "," + frontY + ")");
            }
        } catch (Exception e) {
            Gdx.app.log("Explode", "Error exploding slime: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMap(TileMap map) {
        this.tileMap = map;
        this.position.set(map.toMapCoords(map.playerSpawn));
        
        // Save original slime positions for respawn
        saveOriginalSlimePositions();
    }

    /**
     * Save original slime positions when map is loaded
     */
    public void saveOriginalSlimePositions() {
        originalSlimePositions.clear();
        
        if (tileMap == null) return;
        
        Gdx.app.log("Respawn", "Saving original slime positions...");
        
        // Scan entire map for slimes
        for (int y = 0; y < tileMap.mapHeight; y++) {
            for (int x = 0; x < tileMap.mapWidth; x++) {
                if (tileMap.containsEntity(x, y)) {
                    Entity entity = tileMap.getEntity(x, y);
                    if (entity instanceof Enemy) {
                        Enemy enemy = (Enemy) entity;
                        if (!enemy.isDead()) {
                            originalSlimePositions.add(new int[]{x, y});
                            Gdx.app.log("Respawn", "Saved slime position: (" + x + "," + y + ")");
                        }
                    }
                }
            }
        }
        
        Gdx.app.log("Respawn", "Total original slime positions saved: " + originalSlimePositions.size());
    }

    /**
     * Respawns slimes that were removed at their original positions
     */
    private void respawnSlimes() {
        try {
            // Check if tileMap is available
            if (tileMap == null) {
                Gdx.app.log("Respawn", "tileMap is null, cannot respawn slimes");
                return;
            }
            
            Gdx.app.log("Respawn", "=== RESPAWN CHECK STARTED ===");
            Gdx.app.log("Respawn", "Original positions saved: " + originalSlimePositions.size());
            
            int respawnedCount = 0;
            
            // Respawn slimes at their original positions if empty
            for (int[] pos : originalSlimePositions) {
                int x = pos[0];
                int y = pos[1];
                
                // Check if position is empty
                if (!tileMap.containsEntity(x, y)) {
                    try {
                        // Create slime using the same method as original map loading
                        Enemy slime = (Enemy) Util.getEntity(2, tileMap.toMapCoords(x, y), tileMap, rm);
                        if (slime != null) {
                            tileMap.addEntity(slime, x, y);
                            respawnedCount++;
                            Gdx.app.log("Respawn", "SUCCESS: Slime respawned at original position (" + x + "," + y + ")");
                        }
                    } catch (Exception e) {
                        Gdx.app.log("Respawn", "Error creating slime at (" + x + "," + y + "): " + e.getMessage());
                    }
                }
            }
            
            Gdx.app.log("Respawn", "=== RESPAWN CHECK COMPLETED ===");
            Gdx.app.log("Respawn", "Total slimes respawned: " + respawnedCount);
        } catch (Exception e) {
            Gdx.app.log("Respawn", "Error respawning slimes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates every tick and moves an Entity if not on the tile map grid
     */
    public void handleMovement(float dt) {
        // down
        if (moving == 0) {
            int targetY = nextPosition(0);
            if (targetY == currentTileY) {
                moving = -1;
            } else {
                position.y -= speed * dt;
                if (Math.abs(position.y - targetY * tileMap.tileSize) <= speed * dt) {
                    position.y = targetY * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // up
        if (moving == 1) {
            int targetY = nextPosition(1);
            if (targetY == currentTileY) {
                moving = -1;
            } else {
                position.y += speed * dt;
                if (Math.abs(position.y - targetY * tileMap.tileSize) <= speed * dt) {
                    position.y = targetY * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // right
        if (moving == 2) {
            int targetX = nextPosition(2);
            if (targetX == currentTileX) {
                moving = -1;
            } else {
                position.x += speed * dt;
                if (Math.abs(position.x - targetX * tileMap.tileSize) <= speed * dt) {
                    position.x = targetX * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // left
        if (moving == 3) {
            int targetX = nextPosition(3);
            if (targetX == currentTileX) {
                moving = -1;
            } else {
                position.x -= speed * dt;
                if (Math.abs(position.x - targetX * tileMap.tileSize) <= speed * dt) {
                    position.x = targetX * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
    }

    /**
     * Increments level and recalculates max exp
     * Sets increase variables to display on screen
     * Recursively accounts for n consecutive level ups from remaining exp
     *
     * @param remainder the amount of exp left after a level up
     */
    public void levelUp(int remainder) {
        level++;

        hpIncrease += MathUtils.random(Util.PLAYER_MIN_HP_INCREASE, Util.PLAYER_MAX_HP_INCREASE);
        int dmgMean = MathUtils.random(Util.PLAYER_MIN_DMG_INCREASE, Util.PLAYER_MAX_DMG_INCREASE);

        // deviates from mean by 0 to 2
        minDmgIncrease += (dmgMean - MathUtils.random(1));
        maxDmgIncrease += (dmgMean + MathUtils.random(1));
        // accuracy increases by 1% every 10 levels
        accuracyIncrease += level % 10 == 0 ? 1 : 0;
        // smoveCd reduces every 10 levels
        if (smoveCd > 1) smoveCd -= level % 10 == 0 ? 1 : 0;

        int prevMaxExp = maxExp;
        maxExp = Util.calculateMaxExp(level, MathUtils.random(3, 5));
        maxExpIncrease += (maxExp - prevMaxExp);

        // another level up
        if (remainder >= maxExp) {
            levelUp(remainder - maxExp);
        } else {
            exp = remainder;
        }
    }

    /**
     * Increases the actual stats by their level up amounts
     */
    public void applyLevelUp() {
        maxHp += hpIncrease;
        hp = maxHp;
        minDamage += minDmgIncrease;
        maxDamage += maxDmgIncrease;
        accuracy += accuracyIncrease;

        // reset variables
        hpIncrease = 0;
        minDmgIncrease = 0;
        maxDmgIncrease = 0;
        accuracyIncrease = 0;
        maxExpIncrease = 0;
    }

    /**
     * Applies the stats of an equipable item
     *
     * @param item
     */
    public void equip(Item item) {
        maxHp += item.mhp;
        hp = maxHp;
        minDamage += item.dmg;
        maxDamage += item.dmg;
        accuracy += item.acc;
    }

    /**
     * Removes the stats of an equipable item
     *
     * @param item
     */
    public void unequip(Item item) {
        maxHp -= item.mhp;
        hp = maxHp;
        minDamage -= item.dmg;
        maxDamage -= item.dmg;
        accuracy -= item.acc;
    }

    public Enemy getOpponent() {
        return opponent;
    }

    public void finishBattling() {
        battling = false;
        opponent = null;
        moving = -1;
    }

    public void finishTileInteraction() {
        tileInteraction = false;
        moving = -1;
    }

    /**
     * After teleportation is done the player is moved out of the tile in a random direction
     */
    public void finishTeleporting() {
        teleporting = false;
        changeDirection(MathUtils.random(3));
    }

    public void potion(int heal) {
        hp += heal;
        if (hp > maxHp) hp = maxHp;
    }

    /**
     * Applies a percentage health potion
     * @param php
     */
    public void percentagePotion(int php) {
        hp += (int) ((php / 100f) * maxHp);
        if (hp > maxHp) hp = maxHp;
    }

    /**
     * Green question mark tiles can drop 70% of the time
     * if does drop:
     * - gold (50% of the time) (based on map level)
     * - heals based on map level (45% of the time)
     * - items (5% of the time)
     *
     * @return
     */
    public String[] getQuestionMarkDialog(int mapLevel, GameMap gameMap) {
        String[] ret = null;

        if (Util.isSuccess(Util.TILE_INTERATION)) {
            int k = MathUtils.random(99);
            // gold
            if (k < 50) {
                // gold per level scaled off map's average level
                int gold = 0;
                for (int i = 0; i < mapLevel; i++) {
                    gold += MathUtils.random(7, 13);
                }
                this.gold += gold;
                gameMap.goldObtained += gold;
                ret = new String[] {
                    "The random tile gave something!",
                    "You obtained " + gold + " gold!"
                };
            }
            // heal
            else if (k < 95) {
                int heal = 0;
                for (int i = 0; i < mapLevel; i++) {
                    heal += MathUtils.random(2, 5);
                }
                this.hp += heal;
                if (hp > maxHp) hp = maxHp;
                ret = new String[] {
                    "The random tile gave something!",
                    "It healed you for " + heal + " hp!"
                };
            }
            // item
            else if (k < 100) {
                Item item = rm.getRandomItem();
                if (inventory.isFull()) {
                    ret = new String[] {
                        "The random tile gave something!",
                        "It dropped a " + item.getDialogName() + "!",
                        "Oh no, too bad your inventory was full."
                    };
                }
                else {
                    ret = new String[]{
                        "The random tile gave something!",
                        "It dropped a " + item.getDialogName() + "!",
                        "The item was added to your inventory."
                    };
                    item.adjust(mapLevel);
                    inventory.addItem(item);
                    gameMap.itemsObtained.add(item);
                }
            }
        }
        else {
            ret = new String[] {
                "The random tile did not give anything."
            };
        }

        return ret;
    }

    /**
     * The purple exclamation mark tile is a destructive tile
     * that has a 60% chance to do damage to the player and
     * 40% chance to steal gold.
     *
     * @param mapLevel
     * @return
     */
    public String[] getExclamDialog(int mapLevel, GameMap gameMap) {
        String[] ret = null;

        if (Util.isSuccess(Util.TILE_INTERATION)) {
            if (Util.isSuccess(60)) {
                int dmg = 0;
                for (int i = 0; i < mapLevel; i++) {
                    dmg += MathUtils.random(1, 4);
                }
                hp -= dmg;
                // player dies from tile
                if (hp <= 0) {
                    ret = new String[] { "" +
                        "The random tile cursed you!",
                        "It damaged you for " + dmg + " damage!",
                        "Oh no, you took fatal damage and died!",
                        "You will lose " + Util.DEATH_PENALTY +
                            "% of your exp and gold and all the items obtained in this level as a penalty."
                    };
                }
                else {
                    ret = new String[] {
                        "The random tile cursed you!",
                        "It damaged you for " + dmg + " damage!"
                    };
                }
            }
            else {
                int steal = 0;
                for (int i = 0; i < mapLevel; i++) {
                    steal += MathUtils.random(4, 9);
                }
                gold -= steal;
                if (gold < 0) gold = 0;
                ret = new String[] {
                    "The random tile cursed you!",
                    "It caused you to lose " + steal + " gold!"
                };
            }
        }
        else {
            ret = new String[] {
                "The random tile did not affect you."
            };
        }

        return ret;
    }

    /**
     * Sets the player's position to another teleportation tile anywhere on the map
     */
    public void teleport() {
        Tile currentTile = tileMap.getTile(tileMap.toTileCoords(position));
        Array<Tile> candidates = tileMap.getTeleportationTiles(currentTile);
        Tile choose = candidates.get(MathUtils.random(candidates.size - 1));
        position.set(tileMap.toMapCoords(choose.tilePosition));
    }

    /**
     * Adds a given amount of exp to the player's current exp and checks for level up
     */
    public void addExp(int exp) {
        // level up with no screen
        if (this.exp + exp >= maxExp) {
            int remainder = (this.exp + exp) - maxExp;
            levelUp(remainder);
            applyLevelUp();
        }
        else if (this.exp + exp < 0) {
            this.exp = 0;
        }
        else {
            this.exp += exp;
        }
    }

    public boolean isBattling() {
        return battling;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setMaxExp(int maxExp) {
        this.maxExp = maxExp;
    }

    public int getExp() {
        return exp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    public int getHpIncrease() {
        return hpIncrease;
    }

    public void setHpIncrease(int hpIncrease) {
        this.hpIncrease = hpIncrease;
    }

    public int getMinDmgIncrease() {
        return minDmgIncrease;
    }

    public void setMinDmgIncrease(int minDmgIncrease) {
        this.minDmgIncrease = minDmgIncrease;
    }

    public int getMaxDmgIncrease() {
        return maxDmgIncrease;
    }

    public void setMaxDmgIncrease(int maxDmgIncrease) {
        this.maxDmgIncrease = maxDmgIncrease;
    }

    public int getAccuracyIncrease() {
        return accuracyIncrease;
    }

    public void setAccuracyIncrease(int accuracyIncrease) {
        this.accuracyIncrease = accuracyIncrease;
    }

    public int getMaxExpIncrease() { return maxExpIncrease; }

    public void addGold(int g) {
        if (this.gold + g < 0) this.gold = 0;
        else this.gold += g;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getGold() { return gold; }

    public int getCurrentTileX() {
        return currentTileX;
    }

    public int getCurrentTileY() {
        return currentTileY;
    }

    public boolean isMoving() {
        return moving != -1;
    }

    public boolean isTileInteraction() { return tileInteraction; }

    public boolean isTeleporting() { return teleporting; }

}
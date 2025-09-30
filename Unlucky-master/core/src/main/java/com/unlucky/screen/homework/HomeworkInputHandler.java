package com.unlucky.screen.homework;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.unlucky.event.EventState;
import com.unlucky.screen.GameScreen;
import com.unlucky.ui.Hud;

/**
 * Handles input events specifically for homework functionality:
 * - Bullet firing (tap to shoot)
 * - Defense system activation
 * - UI interaction for homework buttons
 * 
 * @author Homework Implementation
 */
public class HomeworkInputHandler extends InputAdapter {
    
    private final GameScreen gameScreen;
    private final Hud hud;
    private final AttackSystemsManager attackSystems;
    private final DefenseSystemsManager defenseSystems;
    
    public HomeworkInputHandler(GameScreen gameScreen, Hud hud, 
                               AttackSystemsManager attackSystems, 
                               DefenseSystemsManager defenseSystems) {
        this.gameScreen = gameScreen;
        this.hud = hud;
        this.attackSystems = attackSystems;
        this.defenseSystems = defenseSystems;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer > 0) return false;

        Vector2 stageCoords = hud.getStage().screenToStageCoordinates(new Vector2(screenX, screenY));

        // Check if touch hit any UI elements first
        boolean hitUI = checkUIHit(stageCoords);

        // If didn't hit UI and game is in appropriate state, fire bullet
        if (!hitUI && canFireBullet()) {
            float playerX = gameScreen.gameMap.player.getPosition().x;
            float playerY = gameScreen.gameMap.player.getPosition().y;

            // Fire bullet from player position
            attackSystems.fireBullet(playerX, playerY);
            
            return true;
        }

        return false;
    }
    
    private boolean checkUIHit(Vector2 stageCoords) {
        EventState currentEvent = gameScreen.currentEvent;
        
        // Check different UI stages based on current game state
        if (currentEvent == EventState.MOVING && 
            hud.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
            return true;
        } else if (currentEvent == EventState.BATTLING && 
                   gameScreen.battleUIHandler.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
            return true;
        } else if (currentEvent == EventState.LEVEL_UP && 
                   gameScreen.levelUp.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
            return true;
        } else if (currentEvent == EventState.TILE_EVENT && 
                   gameScreen.dialog.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
            return true;
        } else if (currentEvent == EventState.INVENTORY && 
                   gameScreen.getGame().inventoryUI.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
            return true;
        }
        
        return false;
    }
    
    private boolean canFireBullet() {
        EventState currentEvent = gameScreen.currentEvent;
        return currentEvent == EventState.MOVING || currentEvent == EventState.BATTLING;
    }
    
    /**
     * Handle defense system key presses (could be extended for keyboard shortcuts)
     */
    @Override
    public boolean keyDown(int keycode) {
        // Could add keyboard shortcuts for defense systems here
        // For example:
        // if (keycode == Input.Keys.S) defenseSystems.activateShield();
        // if (keycode == Input.Keys.SPACE) defenseSystems.activateSlowMotion();
        // if (keycode == Input.Keys.W) defenseSystems.activateWindWall();
        
        return false;
    }
    
    /**
     * Handle mouse/touch movement for advanced interactions
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Could implement advanced touch gestures here
        return false;
    }
    
    /**
     * Handle touch up events
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Could implement touch-and-hold mechanics here
        return false;
    }
}
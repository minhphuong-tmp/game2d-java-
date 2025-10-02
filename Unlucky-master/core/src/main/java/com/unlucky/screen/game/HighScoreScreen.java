package com.unlucky.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.unlucky.entity.Player;
import com.unlucky.event.EventState;
import com.unlucky.map.TileMap;
import com.unlucky.map.WeatherType;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.GameScreen;
import com.unlucky.ui.UI;

/**
 * High Score screen that appears after death screen
 * Shows top 6 survival times with scores
 *
 * @author Ming Li
 */
public class HighScoreScreen extends UI {

    // banner
    private Image bannerBg;
    private Label bannerText;

    // back to menu button
    private ImageButton backButton;
    private Label backLabel;
    
    // retry button
    private ImageButton retryButton;
    private Label retryLabel;
    
    // home button
    private ImageButton homeButton;
    private Label homeLabel;

    // information panel
    private Image infoBg;
    private Label highScoresList;
    private Label rightColumnLabel;
    private Label currentStatsLabel;

    private java.util.List<com.unlucky.save.HighScoreEntry> highScores;
    private com.unlucky.save.HighScoreEntry currentEntry;

    public HighScoreScreen(GameScreen gameScreen, TileMap tileMap, Player player, ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);

        // Create banner background
        bannerBg = new Image(rm.skin, "default-slider");
        bannerBg.setSize(140, 18);
        bannerBg.setPosition(30, 96);
        stage.addActor(bannerBg);

        // Create banner text
        bannerText = new Label("TOP 6 SURVIVAL", new Label.LabelStyle(rm.pixel10, new Color(1, 215 / 255.f, 0, 1)));
        bannerText.setFontScale(1.0f);
        bannerText.setSize(140, 18);
        bannerText.setPosition(30, 96);
        bannerText.setAlignment(Align.center);
        stage.addActor(bannerText);

        // Create info background
        infoBg = new Image(rm.skin, "default-slider");
        infoBg.setSize(140, 85);
        infoBg.setPosition(30, 8);
        stage.addActor(infoBg);

        // Current game stats
        currentStatsLabel = new Label("", new Label.LabelStyle(rm.pixel10, Color.YELLOW));
        currentStatsLabel.setFontScale(0.6f);
        currentStatsLabel.setAlignment(Align.center);
        currentStatsLabel.setSize(130, 20);
        currentStatsLabel.setPosition(35, 70);
        stage.addActor(currentStatsLabel);

        // High scores list - left column (1-4)
        highScoresList = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        highScoresList.setFontScale(0.45f);
        highScoresList.setWrap(true);
        highScoresList.setAlignment(Align.topLeft);
        highScoresList.setSize(65, 55);
        highScoresList.setPosition(35, 12);
        stage.addActor(highScoresList);
        
        // High scores list - right column (5-6)
        rightColumnLabel = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        rightColumnLabel.setFontScale(0.45f);
        rightColumnLabel.setWrap(true);
        rightColumnLabel.setAlignment(Align.topLeft);
        rightColumnLabel.setSize(65, 55);
        rightColumnLabel.setPosition(105, 12);
        stage.addActor(rightColumnLabel);

        // Create back button
        ImageButton.ImageButtonStyle backStyle = new ImageButton.ImageButtonStyle();
        backStyle.imageUp = new TextureRegionDrawable(rm.menuExitButton[0][0]);
        backStyle.imageDown = new TextureRegionDrawable(rm.menuExitButton[1][0]);

        backButton = new ImageButton(backStyle);
        backButton.setSize(18, 18);
        backButton.setPosition(177, 96);
        stage.addActor(backButton);

        backLabel = new Label("MENU", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        backLabel.setFontScale(0.4f);
        backLabel.setTouchable(Touchable.disabled);
        backLabel.setSize(18, 8);
        backLabel.setAlignment(Align.center);
        backLabel.setPosition(177, 88);
        stage.addActor(backLabel);

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Return to main menu
                hide();
                backToMenu();
                Gdx.app.log("HighScoreScreen", "Back to menu clicked");
            }
        });

        // Create retry button
        ImageButton.ImageButtonStyle retryStyle = new ImageButton.ImageButtonStyle();
        retryStyle.imageUp = new TextureRegionDrawable(rm.smoveButtons[0][0]);
        retryStyle.imageDown = new TextureRegionDrawable(rm.smoveButtons[1][0]);

        retryButton = new ImageButton(retryStyle);
        retryButton.setPosition(157, 45);
        stage.addActor(retryButton);

        retryLabel = new Label("RETRY", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        retryLabel.setFontScale(0.45f);
        retryLabel.setTouchable(Touchable.disabled);
        retryLabel.setSize(38, 18);
        retryLabel.setAlignment(Align.center);
        retryLabel.setPosition(154, 45);
        stage.addActor(retryLabel);

        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!player.settings.muteSfx) rm.buttonclick0.play(player.settings.sfxVolume);
                // Restart current level
                hide();
                restartLevel();
                Gdx.app.log("HighScoreScreen", "Retry clicked - restarting level");
            }
        });

        // Create home button
        ImageButton.ImageButtonStyle homeStyle = new ImageButton.ImageButtonStyle();
        homeStyle.imageUp = new TextureRegionDrawable(rm.smoveButtons[0][0]);
        homeStyle.imageDown = new TextureRegionDrawable(rm.smoveButtons[1][0]);

        homeButton = new ImageButton(homeStyle);
        homeButton.setPosition(157, 25);
        stage.addActor(homeButton);

        homeLabel = new Label("HOME", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        homeLabel.setFontScale(0.45f);
        homeLabel.setTouchable(Touchable.disabled);
        homeLabel.setSize(38, 18);
        homeLabel.setAlignment(Align.center);
        homeLabel.setPosition(154, 25);
        stage.addActor(homeLabel);

        homeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!player.settings.muteSfx) rm.buttonclick0.play(player.settings.sfxVolume);
                // Go to main menu
                hide();
                backToMenu();
                Gdx.app.log("HighScoreScreen", "Home clicked - returning to main menu");
            }
        });

        // Initially hide all elements
        hide();
    }

    public void show(java.util.List<com.unlucky.save.HighScoreEntry> scores, com.unlucky.save.HighScoreEntry current) {
        this.highScores = scores;
        this.currentEntry = current;

        // Update current stats
        String currentStats = String.format("Your Game:\nSCORE: %d\nSURVIVED: %s", 
            current.score, current.getFormattedTime());
        
        // Check if this is a new record
        boolean isNewRecord = !highScores.isEmpty() && highScores.get(0) == current;
        if (isNewRecord) {
            currentStats += "\nNEW SURVIVAL RECORD!";
        }
        currentStatsLabel.setText(currentStats);

        // Build left column (1-4)
        StringBuilder leftColumn = new StringBuilder();
        for (int i = 0; i < Math.min(highScores.size(), 4); i++) {
            com.unlucky.save.HighScoreEntry entry = highScores.get(i);
            String prefix = (i + 1) + ". ";
            leftColumn.append(String.format("%sSCORE: %d\n", prefix, entry.score));
            leftColumn.append(String.format("   SURVIVED: %s\n", entry.getFormattedTime()));
            if (i < Math.min(highScores.size(), 4) - 1) {
                leftColumn.append("\n");
            }
        }
        
        // Build right column (5-6)
        StringBuilder rightColumn = new StringBuilder();
        for (int i = 4; i < Math.min(highScores.size(), 6); i++) {
            com.unlucky.save.HighScoreEntry entry = highScores.get(i);
            String prefix = (i + 1) + ". ";
            rightColumn.append(String.format("%sSCORE: %d\n", prefix, entry.score));
            rightColumn.append(String.format("   SURVIVED: %s\n", entry.getFormattedTime()));
            if (i < Math.min(highScores.size(), 6) - 1) {
                rightColumn.append("\n");
            }
        }
        
        if (highScores.isEmpty()) {
            leftColumn.append("No records yet!\nStart playing to\nset records!");
        }
        
        highScoresList.setText(leftColumn.toString());
        rightColumnLabel.setText(rightColumn.toString());

        // Show all elements
        bannerBg.setVisible(true);
        bannerText.setVisible(true);
        infoBg.setVisible(true);
        currentStatsLabel.setVisible(true);
        highScoresList.setVisible(true);
        rightColumnLabel.setVisible(true);
        backButton.setVisible(true);
        backButton.setTouchable(Touchable.enabled);
        backLabel.setVisible(true);
        retryButton.setVisible(true);
        retryButton.setTouchable(Touchable.enabled);
        retryLabel.setVisible(true);
        homeButton.setVisible(true);
        homeButton.setTouchable(Touchable.enabled);
        homeLabel.setVisible(true);

        Gdx.app.log("HighScoreScreen", "High score screen shown with " + highScores.size() + " entries");
    }
    
    /**
     * Show high score screen with "YOU WIN" message
     */
    public void showWin(java.util.List<com.unlucky.save.HighScoreEntry> scores, com.unlucky.save.HighScoreEntry current) {
        this.highScores = scores;
        this.currentEntry = current;

        // Update banner text to "YOU WIN"
        bannerText.setText("YOU WIN!");
        bannerText.setColor(new Color(0, 1, 0, 1)); // Green color for win

        // Update current stats with win message
        String currentStats = String.format("Your Game:\nSCORE: %d\nSURVIVED: %s\nCONGRATULATIONS!",
            current.score, current.getFormattedTime());

        // Check if this is a new record
        boolean isNewRecord = !highScores.isEmpty() && highScores.get(0) == current;
        if (isNewRecord) {
            currentStats += "\nNEW SURVIVAL RECORD!";
        }
        currentStatsLabel.setText(currentStats);

        // Build left column (1-4)
        StringBuilder leftColumn = new StringBuilder();
        for (int i = 0; i < Math.min(highScores.size(), 4); i++) {
            com.unlucky.save.HighScoreEntry entry = highScores.get(i);
            String prefix = (i + 1) + ". ";
            leftColumn.append(String.format("%sSCORE: %d\n", prefix, entry.score));
            leftColumn.append(String.format("   SURVIVED: %s\n", entry.getFormattedTime()));
            if (i < Math.min(highScores.size(), 4) - 1) {
                leftColumn.append("\n");
            }
        }

        // Build right column (5-6)
        StringBuilder rightColumn = new StringBuilder();
        for (int i = 4; i < Math.min(highScores.size(), 6); i++) {
            com.unlucky.save.HighScoreEntry entry = highScores.get(i);
            String prefix = (i + 1) + ". ";
            rightColumn.append(String.format("%sSCORE: %d\n", prefix, entry.score));
            rightColumn.append(String.format("   SURVIVED: %s\n", entry.getFormattedTime()));
            if (i < Math.min(highScores.size(), 6) - 1) {
                rightColumn.append("\n");
            }
        }

        if (highScores.isEmpty()) {
            leftColumn.append("No records yet!\nStart playing to\nset records!");
        }

        highScoresList.setText(leftColumn.toString());
        rightColumnLabel.setText(rightColumn.toString());

        // Show all elements
        bannerBg.setVisible(true);
        bannerText.setVisible(true);
        infoBg.setVisible(true);
        currentStatsLabel.setVisible(true);
        highScoresList.setVisible(true);
        rightColumnLabel.setVisible(true);
        backButton.setVisible(true);
        backButton.setTouchable(Touchable.enabled);
        backLabel.setVisible(true);
        retryButton.setVisible(true);
        retryButton.setTouchable(Touchable.enabled);
        retryLabel.setVisible(true);
        homeButton.setVisible(true);
        homeButton.setTouchable(Touchable.enabled);
        homeLabel.setVisible(true);

        Gdx.app.log("HighScoreScreen", "YOU WIN screen shown with " + highScores.size() + " entries");
    }

    public void hide() {
        bannerBg.setVisible(false);
        bannerText.setVisible(false);
        infoBg.setVisible(false);
        currentStatsLabel.setVisible(false);
        highScoresList.setVisible(false);
        rightColumnLabel.setVisible(false);
        backButton.setVisible(false);
        backButton.setTouchable(Touchable.disabled);
        backLabel.setVisible(false);
        retryButton.setVisible(false);
        retryButton.setTouchable(Touchable.disabled);
        retryLabel.setVisible(false);
        homeButton.setVisible(false);
        homeButton.setTouchable(Touchable.disabled);
        homeLabel.setVisible(false);
    }

    private void backToMenu() {
        // Return to main menu - use public method
        if (gameScreen != null && gameScreen.hud != null) {
            gameScreen.hud.backToMenu();
            Gdx.app.log("HighScoreScreen", "Successfully returned to main menu");
        } else {
            Gdx.app.log("HighScoreScreen", "ERROR: Cannot return to menu - missing gameScreen or hud");
        }
    }
    
    private void restartLevel() {
        // Restart current level - reset game state and start over
        if (gameScreen != null && gameScreen.hud != null) {
            // Hide high score screen first
            hide();
            
            // Reset game state using public method
            gameScreen.hud.resetForNewGame();
            
            // Reset player position and stats
            if (gameScreen.gameMap != null && gameScreen.gameMap.player != null) {
                // Reset player HP and Mana to full
                gameScreen.gameMap.player.setHp(gameScreen.gameMap.player.getMaxHp());
                gameScreen.gameMap.player.setMana(gameScreen.gameMap.player.getMaxMana());
                
                // Reset player position to spawn point
                if (gameScreen.gameMap.tileMap != null && gameScreen.gameMap.tileMap.playerSpawn != null) {
                    gameScreen.gameMap.player.setPosition(gameScreen.gameMap.tileMap.toMapCoords(gameScreen.gameMap.tileMap.playerSpawn));
                    Gdx.app.log("RestartLevel", "Player position reset to spawn point");
                }
                
                // Reset player state
                gameScreen.gameMap.player.setDead(false);
            }
            
            // Reset any battle state - ensure we're not in battle
            if (gameScreen.currentEvent == EventState.BATTLING) {
                gameScreen.setCurrentEvent(EventState.MOVING);
            }
            
            // Return to moving state and show HUD
            gameScreen.setCurrentEvent(EventState.MOVING);
            gameScreen.hud.toggle(true);
            
            // Reset banner text color back to normal for next time
            bannerText.setColor(new Color(1, 215 / 255.f, 0, 1)); // Original yellow color
            bannerText.setText("TOP 6 SURVIVAL");
            
            Gdx.app.log("HighScoreScreen", "Level restarted successfully - All states reset");
        }
    }

    public void update(float dt) {
        // No animation needed for now
    }

    public void render(float dt) {
        stage.act(dt);
        stage.draw();
    }
}

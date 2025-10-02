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
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.GameScreen;
import com.unlucky.ui.UI;

/**
 * Win screen that appears when player reaches score of 5
 * Shows congratulations message, final score, and completion time
 *
 * @author Ming Li
 */
public class WinScreen extends UI {

    // banner
    private Image bannerBg;
    private Label bannerText;

    // continue button
    private ImageButton continueButton;
    private Label continueLabel;

    // information panel
    private Image infoBg;
    private Label info;

    private int finalScore;
    private float finalTime;

    public WinScreen(GameScreen gameScreen, TileMap tileMap, Player player, ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);

        // Create banner background
        bannerBg = new Image(rm.skin, "default-slider");
        bannerBg.setSize(120, 18);
        bannerBg.setPosition(40, 96);
        stage.addActor(bannerBg);

        // Create banner text
        bannerText = new Label("CHUC MUNG!", new Label.LabelStyle(rm.pixel10, new Color(0, 215 / 255.f, 0, 1)));
        bannerText.setFontScale(1.2f);
        bannerText.setSize(120, 18);
        bannerText.setPosition(40, 96);
        bannerText.setAlignment(Align.center);
        stage.addActor(bannerText);

        // Create info background
        infoBg = new Image(rm.skin, "default-slider");
        infoBg.setSize(120, 70);
        infoBg.setPosition(40, 20);
        stage.addActor(infoBg);

        // Create info text
        info = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        info.setFontScale(0.6f);
        info.setWrap(true);
        info.setAlignment(Align.topLeft);
        info.setSize(112, 62);
        info.setPosition(44, 24);
        stage.addActor(info);

        // Create continue button
        ImageButton.ImageButtonStyle continueStyle = new ImageButton.ImageButtonStyle();
        continueStyle.imageUp = new TextureRegionDrawable(rm.smoveButtons[0][0]);
        continueStyle.imageDown = new TextureRegionDrawable(rm.smoveButtons[1][0]);

        continueButton = new ImageButton(continueStyle);
        continueButton.setPosition(157, 8);
        stage.addActor(continueButton);

        continueLabel = new Label("TIEP TUC", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        continueLabel.setFontScale(0.45f);
        continueLabel.setTouchable(Touchable.disabled);
        continueLabel.setSize(38, 18);
        continueLabel.setAlignment(Align.center);
        continueLabel.setPosition(154, 8);
        stage.addActor(continueLabel);

        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Close win screen and return to game
                hide();
                gameScreen.setCurrentEvent(EventState.MOVING);
                gameScreen.hud.toggle(true);
                Gdx.app.log("WinScreen", "Continue clicked - returning to game");
            }
        });

        // Initially hide all elements
        hide();
    }

    public void show(int score, float gameTime) {
        this.finalScore = score;
        this.finalTime = gameTime;

        // Calculate time display
        int minutes = (int) (gameTime / 60);
        int seconds = (int) (gameTime % 60);
        String timeText = String.format("%02d:%02d", minutes, seconds);

        // Set info text
        String infoText = "Ban da hoan thanh thach thuc!\n\n" +
                         "Diem so cuoi cung: " + score + "\n" +
                         "Thoi gian hoan thanh: " + timeText + "\n\n" +
                         "Ban that gioi! Hay tiep tuc\n" +
                         "cuoc hanh trinh cua minh!";
        info.setText(infoText);

        // Show all elements
        bannerBg.setVisible(true);
        bannerText.setVisible(true);
        infoBg.setVisible(true);
        info.setVisible(true);
        continueButton.setVisible(true);
        continueButton.setTouchable(Touchable.enabled);
        continueLabel.setVisible(true);

        Gdx.app.log("WinScreen", "Win screen shown with score: " + score + ", time: " + timeText);
    }

    public void hide() {
        bannerBg.setVisible(false);
        bannerText.setVisible(false);
        infoBg.setVisible(false);
        info.setVisible(false);
        continueButton.setVisible(false);
        continueButton.setTouchable(Touchable.disabled);
        continueLabel.setVisible(false);
    }

    public void update(float dt) {
        // No animation needed for now
    }

    public void render(float dt) {
        stage.act(dt);
        stage.draw();
    }
}

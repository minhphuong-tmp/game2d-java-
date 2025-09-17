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

    // === Bullet System ===
    private Array<Bullet> bullets;
    private TextureRegion bulletTexture;

    // --- Inner Bullet Class ---
    private class Bullet {
        float x, y;
        float speed = 150f; // bay chậm hơn
        float size = 8f;

        Bullet(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void update(float dt) {
            y += speed * dt;
        }

        void render(SpriteBatch batch) {
            if (bulletTexture != null) {
                batch.draw(bulletTexture, x - size / 2, y - size / 2, size, size);
            } else {
                // fallback: vẽ hình vuông đỏ
                batch.setColor(Color.RED);
                batch.draw(rm.redarrow10x9, x - size / 2, y - size / 2, size, size);
                batch.setColor(Color.WHITE);
            }
        }

        boolean isOutOfScreen(float screenHeight) {
            return y > screenHeight;
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

        // load bullet texture ngay khi tạo screen
        bulletTexture = rm.bullet;
        if (bulletTexture == null) {
            Gdx.app.log("GameScreen", "Bullet texture is NULL, will use red fallback!");
        } else {
            Gdx.app.log("GameScreen", "Bullet texture loaded: " + bulletTexture);
        }

        // create bg
        bg = new Background[2];
        bg[0] = new Background((OrthographicCamera) battleUIHandler.getStage().getCamera(), new Vector2(0.3f, 0));
        bg[1] = new Background((OrthographicCamera) battleUIHandler.getStage().getCamera(), new Vector2(0, 0));

        // === Bullet init ===
        bullets = new Array<>();

        // input multiplexer
        multiplexer = new InputMultiplexer();

        // Bullet input: đặt lên đầu để nhận sự kiện trước
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (pointer > 0) return false; // chỉ nhận 1 ngón

                // Chuyển đổi tọa độ màn hình thành tọa độ thế giới của Stage
                Vector2 stageCoords = hud.getStage().screenToStageCoordinates(new Vector2(screenX, screenY));

                // Kiểm tra xem vị trí chạm có nằm trên UI hay không
                boolean hitUI = false;
                if (currentEvent == EventState.MOVING && hud.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true; // Chạm vào HUD
                } else if (currentEvent == EventState.BATTLING && battleUIHandler.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true; // Chạm vào BattleUI
                } else if (currentEvent == EventState.LEVEL_UP && levelUp.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true; // Chạm vào LevelUp UI
                } else if (currentEvent == EventState.TILE_EVENT && dialog.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true; // Chạm vào Dialog UI
                } else if (currentEvent == EventState.INVENTORY && game.inventoryUI.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true; // Chạm vào Inventory UI
                }

                // Chỉ bắn đạn nếu không chạm vào UI và đang ở trạng thái MOVING hoặc BATTLING
                if (!hitUI && (currentEvent == EventState.MOVING || currentEvent == EventState.BATTLING)) {
                    float playerX = gameMap.player.getPosition().x;
                    float playerY = gameMap.player.getPosition().y;

                    Bullet bullet = new Bullet(playerX, playerY + 8);
                    bullets.add(bullet);

                    Gdx.app.log("Bullet", "Bullet fired at: " + playerX + ", " + playerY + " | total bullets: " + bullets.size);
                    return true; // Xử lý sự kiện bắn đạn
                }

                return false; // Không xử lý sự kiện nếu chạm vào UI
            }
        });

        // Stage UI khác
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
        batchFade = renderBatch = true;

        hud.getStage().addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.5f)));

        // bullet fallback check
        if (bulletTexture == null) {
            Gdx.app.log("GameScreen", "Bullet texture still null, will use red fallback");
        }

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

            createBackground(gameMap.worldIndex);

            hud.toggle(true);
            hud.touchDown = false;
            hud.shade.setVisible(false);
            hud.startLevelDescriptor();
        }
    }

    private void createBackground(int bgIndex) {
        TextureRegion[] images = rm.battleBackgrounds400x240[bgIndex];
        for (int i = 0; i < 2; i++) bg[i].setImage(images[i]);
        if (bgIndex == 0) bg[0].setVector(160, 0);
        else if (bgIndex == 1) bg[0].setVector(0, 0);
        else if (bgIndex == 2) bg[0].setVector(40, 0);
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

        if (cam.position.y < halfHeight) cam.position.y = halfHeight;
        if (cam.position.y > gameMap.tileMap.mapHeight * 16 - halfHeight)
            cam.position.y = gameMap.tileMap.mapHeight * 16 - halfHeight;

        if (cam.position.x < 0) cam.position.x = 0;
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

        if (currentEvent == EventState.BATTLING) battleUIHandler.update(dt);
        if (currentEvent == EventState.TRANSITION) transition.update(dt);
        if (currentEvent == EventState.LEVEL_UP) levelUp.update(dt);
        if (currentEvent == EventState.TILE_EVENT) dialog.update(dt);
        if (currentEvent == EventState.INVENTORY) game.inventoryUI.update(dt);

        // update bullets
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(dt);
            if (b.isOutOfScreen(cam.viewportHeight)) bullets.removeIndex(i);
        }
    }

    public void render(float dt) {
        update(dt);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderBatch) {
            game.batch.begin();

            if (batchFade) game.batch.setColor(Color.WHITE);

            game.batch.setProjectionMatrix(cam.combined);
            for (Background bgi : bg) bgi.render(game.batch);

            if (currentEvent == EventState.MOVING || currentEvent == EventState.INVENTORY ||
                    transition.renderMap || currentEvent == EventState.TILE_EVENT ||
                    currentEvent == EventState.DEATH || currentEvent == EventState.PAUSE) {
                game.batch.setProjectionMatrix(cam.combined);
                gameMap.render(dt, game.batch, cam);
            }

            // render bullets
            for (Bullet b : bullets) b.render(game.batch);

            game.batch.end();
        }

        // render HUD / UI
        if (currentEvent == EventState.MOVING) hud.render(dt);
        else if (currentEvent == EventState.BATTLING) battleUIHandler.render(dt);
        else if (currentEvent == EventState.TRANSITION) transition.render(dt);
        else if (currentEvent == EventState.LEVEL_UP) levelUp.render(dt);
        else if (currentEvent == EventState.TILE_EVENT) dialog.render(dt);
        else if (currentEvent == EventState.INVENTORY) game.inventoryUI.render(dt);
    }

    public void setCurrentEvent(EventState event) {
        currentEvent = event;
    }
}
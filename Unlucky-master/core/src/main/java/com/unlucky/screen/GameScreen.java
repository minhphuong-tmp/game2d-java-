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
    private TextureRegion bulletTexture;

    // NEW: === Cloud System ===
    private Array<Cloud> clouds;
    private TextureRegion cloudTexture;

    // NEW: Sound for bullet end
    private Sound bulletEndSound;

    // --- Inner Bullet Class ---
    private class Bullet {
        float x, y;
        float speed = 150f;
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
                batch.setColor(Color.RED);
                batch.draw(rm.redarrow10x9, x - size / 2, y - size / 2, size, size);
                batch.setColor(Color.WHITE);
            }
        }

        boolean isOutOfScreen(float screenHeight) {
            return y > screenHeight;
        }
    }

    // NEW: --- Inner Cloud Class ---
    private class Cloud {
        float x, y;
        float speed = -50f; // Di chuyển từ trên xuống dưới (tốc độ âm cho trục y)
        float size = 16f; // Kích thước giống nhau

        Cloud(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void update(float dt) {
            y += speed * dt; // Cập nhật vị trí theo trục y (xuống dưới)
        }

        void render(SpriteBatch batch) {
            if (cloudTexture != null) {
                batch.draw(cloudTexture, x - size / 2, y - size / 2, size, size);
            } else {
                // Fallback: Vẽ hình dạng giống đám mây đơn giản
                batch.setColor(Color.BLUE);
                batch.draw(rm.redarrow10x9, x - size / 2, y - size / 2, size, size);
                batch.setColor(Color.BLUE);
            }
        }

        boolean isOutOfScreen(float screenBottom) {
            return y + size / 2 < screenBottom; // Ra khỏi dưới màn hình
        }

        void resetPosition(float screenTop) {
            y = screenTop + size / 2; // Reset lên trên cùng
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

        // load bullet texture
        bulletTexture = rm.bullet;
        if (bulletTexture == null) {
            Gdx.app.log("GameScreen", "Bullet texture is NULL, will use red fallback!");
        } else {
            Gdx.app.log("GameScreen", "Bullet texture loaded: " + bulletTexture);
        }

        // NEW: load cloud texture
        cloudTexture = rm.cloudTexture;
        if (cloudTexture == null) {
            Gdx.app.log("GameScreen", "Cloud texture is NULL, will use white cloud-like fallback!");
        } else {
            Gdx.app.log("GameScreen", "Cloud texture loaded: " + cloudTexture);
        }

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

        // NEW: === Cloud init ===
        clouds = new Array<>();

        // input multiplexer
        multiplexer = new InputMultiplexer();

        // Bullet input
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (pointer > 0) return false;

                Vector2 stageCoords = hud.getStage().screenToStageCoordinates(new Vector2(screenX, screenY));

                boolean hitUI = false;
                if (currentEvent == EventState.MOVING && hud.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.BATTLING && battleUIHandler.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.LEVEL_UP && levelUp.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.TILE_EVENT && dialog.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                } else if (currentEvent == EventState.INVENTORY && game.inventoryUI.getStage().hit(stageCoords.x, stageCoords.y, true) != null) {
                    hitUI = true;
                }

                if (!hitUI && (currentEvent == EventState.MOVING || currentEvent == EventState.BATTLING)) {
                    float playerX = gameMap.player.getPosition().x;
                    float playerY = gameMap.player.getPosition().y;

                    Bullet bullet = new Bullet(playerX, playerY + 8);
                    bullets.add(bullet);

                    Gdx.app.log("Bullet", "Bullet fired at: " + playerX + ", " + playerY + " | total bullets: " + bullets.size);
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

        // bullet fallback check
        if (bulletTexture == null) {
            Gdx.app.log("GameScreen", "Bullet texture still null, will use red fallback");
        }

        // NEW: cloud fallback check
        if (cloudTexture == null) {
            Gdx.app.log("GameScreen", "Cloud texture still null, will use white cloud-like fallback");
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

            // Cập nhật camera theo player (dựa trên cách ban đầu)
            updateCamera();

            createBackground(gameMap.worldIndex);

            hud.toggle(true);
            hud.touchDown = false;
            hud.shade.setVisible(false);
            hud.startLevelDescriptor();

            // NEW: Khởi tạo 2 đám mây ban đầu, cạnh nhau (x khác nhau), ở trên cùng màn hình
            clouds.clear();
            float centerX = cam.position.x;
            float topY = cam.position.y + cam.viewportHeight / 2 + 16f / 2; // Trên cùng, ngoài màn hình một chút
            clouds.add(new Cloud(centerX - 40f, topY)); // Đám mây 1, bên trái
            clouds.add(new Cloud(centerX - 70f, topY)); // Đám mây 2, bên phải
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
            if (b.isOutOfScreen(cam.viewportHeight)) {
                bullets.removeIndex(i);
                // NEW: Phát âm thanh khi đạn hết đường
                if (bulletEndSound != null) {
                    bulletEndSound.play();
                }
            }
        }

        // NEW: update clouds - Chỉ có 2 đám mây, di chuyển cùng lúc, reset khi ra khỏi dưới
        float screenBottom = cam.position.y - cam.viewportHeight / 2;
        float screenTop = cam.position.y + cam.viewportHeight / 2;
        for (Cloud c : clouds) {
            c.update(dt);
            if (c.isOutOfScreen(screenBottom)) {
                c.resetPosition(screenTop);
                Gdx.app.log("Cloud", "Cloud reset to top: " + c.y);
            }
        }
    }

    public void render(float dt) {
        update(dt);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.batch != null) {
            game.batch.begin();

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

            // NEW: render clouds
            for (Cloud c : clouds) c.render(game.batch);

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

    @Override
    public void dispose() {
        // NEW: Giải phóng âm thanh khi dispose
        if (bulletEndSound != null) {
            bulletEndSound.dispose();
        }
        super.dispose();
    }
}
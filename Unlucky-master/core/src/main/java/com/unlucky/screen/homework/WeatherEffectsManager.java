package com.unlucky.screen.homework;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.unlucky.resource.ResourceManager;

/**
 * Manages weather effects and environmental systems:
 * - Raindrop System (falling weather effects)
 * - Additional weather effects can be added here
 * 
 * @author Homework Implementation
 */
public class WeatherEffectsManager {
    
    private final ResourceManager rm;
    private final OrthographicCamera cam;
    
    // Weather Systems
    private Array<Raindrop> raindrops;
    
    public WeatherEffectsManager(ResourceManager rm, OrthographicCamera cam) {
        this.rm = rm;
        this.cam = cam;
        
        initializeWeatherSystems();
    }
    
    private void initializeWeatherSystems() {
        raindrops = new Array<>();
        
        Gdx.app.log("WeatherEffects", "Weather effects initialized");
    }
    
    /**
     * Initialize weather when level starts
     */
    public void initializeLevel() {
        raindrops.clear();
        
        // Initialize 2 raindrops at the top of screen
        float centerX = cam.position.x;
        float topY = cam.position.y + cam.viewportHeight / 2 + 16f / 2;
        raindrops.add(new Raindrop(centerX - 40f, topY));
        raindrops.add(new Raindrop(centerX + 40f, topY));
        
        Gdx.app.log("WeatherEffects", "Level weather initialized with " + raindrops.size + " raindrops");
    }
    
    /**
     * Update all weather effects
     */
    public void update(float dt) {
        updateRaindrops(dt);
    }
    
    private void updateRaindrops(float dt) {
        float screenBottom = cam.position.y - cam.viewportHeight / 2;
        float screenTop = cam.position.y + cam.viewportHeight / 2;
        
        for (Raindrop r : raindrops) {
            r.update(dt);
            if (r.isOutOfScreen(screenBottom)) {
                r.resetPosition(screenTop);
            }
        }
    }
    
    /**
     * Render all weather effects
     */
    public void render(SpriteBatch batch) {
        try {
            // Render raindrops
            for (Raindrop r : raindrops) {
                if (r != null) {
                    r.render(batch);
                }
            }
        } catch (Exception e) {
            Gdx.app.log("WeatherEffects", "Error rendering weather: " + e.getMessage());
        }
    }
    
    /**
     * Add more raindrops dynamically
     */
    public void addRaindrop(float x, float y) {
        raindrops.add(new Raindrop(x, y));
    }
    
    /**
     * Clear all weather effects
     */
    public void clearWeather() {
        raindrops.clear();
        Gdx.app.log("WeatherEffects", "All weather effects cleared");
    }
    
    public void dispose() {
        // Clean up resources if needed
    }
    
    // Inner Classes
    public class Raindrop {
        float x, y;
        float speed = -50f; // Moving downward
        float size = 16f;

        Raindrop(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void update(float dt) {
            y += speed * dt; // Move downward
        }

        void render(SpriteBatch batch) {
            // Use raindrop from atlas - perfect for falling weather effect
            batch.draw(rm.raindrop, x - size / 2, y - size / 2, size, size);
        }

        boolean isOutOfScreen(float screenBottom) {
            return y + size / 2 < screenBottom; // Off bottom of screen
        }

        void resetPosition(float screenTop) {
            y = screenTop + size / 2; // Reset to top
        }
    }
}
package com.unlucky.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Extension of the LibGDX Animation class which replaces the stateTime argument with various methods.
 *
 * @author Ivan Vinski
 * @since 1.0
 */
public class CustomAnimation extends Animation<TextureRegion> {

    private float stateTime;
    private boolean playing;

    public CustomAnimation(float frameDuration, Array<? extends TextureRegion> keyFrames) {
        super(frameDuration, keyFrames);
    }

    public CustomAnimation(float frameDuration, Array<? extends TextureRegion> keyFrames, PlayMode playMode) {
        super(frameDuration, keyFrames, playMode);
    }

    public CustomAnimation(float frameDuration, TextureRegion... keyFrames) {
        super(frameDuration, keyFrames);
    }

    public void play() {
        playing = true;
    }

    public void pause() {
        playing = false;
    }

    public void stop() {
        stateTime = 0f;
        playing = false;
    }

    public void update(float delta) {
        if (playing) {
            stateTime += delta;
        }
    }

    public void reset() {
        stateTime = 0f;
    }

    public TextureRegion getKeyFrame(boolean looping) {
        return super.getKeyFrame(stateTime, looping);
    }

    public TextureRegion getKeyFrame() {
        return super.getKeyFrame(stateTime);
    }

    public int getKeyFrameIndex() {
        return super.getKeyFrameIndex(stateTime);
    }

    public boolean isAnimationFinished() {
        return super.isAnimationFinished(stateTime);
    }

    public boolean isPlaying() {
        return playing;
    }
}

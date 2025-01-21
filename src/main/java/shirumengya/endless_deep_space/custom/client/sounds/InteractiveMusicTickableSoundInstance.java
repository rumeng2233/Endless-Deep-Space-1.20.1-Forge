package shirumengya.endless_deep_space.custom.client.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import shirumengya.endless_deep_space.custom.init.ModSounds;

public class InteractiveMusicTickableSoundInstance extends LoopTickableSoundInstance {
	public float fade;
    public boolean stopping;
    public boolean starting = true;
    public float dampen;
    public boolean isStopped;
	
	public InteractiveMusicTickableSoundInstance(SoundEvent event, SoundSource source, boolean isLoop) {
		super(event, source, isLoop);
		this.relative = true;
		this.delay = 0;
		this.fade = 0.0F;
		this.looping = isLoop;
      this.volume = 0.001F;
	}

	public void tick() {
    	if (this.stopping) {
        	if (this.fade > 0.0F) {
				--this.fade;
			} else {
            this.volume = 0.001F;
            if (this.isStopped) {
                this.stop();
            }
			}
		} else if (this.starting) {
			if (this.fade < (float)this.getFadeTime()) {
				++this.fade;
			} else {
				this.starting = false;
			}
		}

		float volume = Math.max(0.001F, Math.min(this.fade / (float)this.getFadeTime(), 1.0F)) * this.maximumVolume();
		this.volume = volume - Math.max(0.001F, this.dampen * 0.02F);
	}

	public boolean shouldFadeOut() {
        return this.stopping;
    }

    public boolean isStopping() {
        return this.stopping;
    }

    public boolean isStarting() {
        return this.starting;
    }

    public void stopSound() {
        if (!this.stopping) {
            this.stopping = true;
            this.fade = (float)this.getFadeTime();
        }

    }

    public void stopMusic() {
        this.stopSound();
        this.isStopped = true;
    }

    public void continueSound() {
        if (this.stopping) {
            this.stopping = false;
            this.starting = true;
        }

    }

    protected int getFadeTime() {
        return 40;
    }

    public boolean isLooping() {
    	return this.looping;
    }
}

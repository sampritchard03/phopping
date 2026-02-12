package slaals.phopping.mixin;

import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.achievement.stat.Stat;
import net.minecraft.core.achievement.stat.StatList;
import net.minecraft.core.block.BlockLogicIce;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.Difficulty;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin extends Mob {

	@Shadow
	public ContainerInventory inventory;

	@Shadow
	public float cameraVelocityOld;

	@Shadow
	public float cameraVelocity;

	@Shadow
	protected float baseSpeed;

	@Shadow
	protected float baseFlySpeed;

	@Shadow
	private ChunkCoordinates lastDeathCoordinate;

	@Shadow
	protected abstract void collideWithPlayer(Entity entity);

	@Shadow
	public abstract void addStat(Stat statbase, int i);

	public PlayerMixin(@Nullable World world) {
		super(world);
	}

	@Unique
	private int chainedJumps = 0;
	@Unique
	private int ticksOnGround = 0;

	@Unique
	public void customSpeed() {
		this.speed = this.baseSpeed;
		if (this.isSprinting()) {
			if (ticksOnGround > 0 && ticksOnGround <= 5 && isJumping) {
				ticksOnGround = 0;
				chainedJumps++;
			}
			if (ticksOnGround > 5) chainedJumps = 0;
			this.speed = (float)(((double)this.speed + (double)this.baseSpeed * 0.3) * (Math.log(chainedJumps+1)+1));
			if (this.onGround) ticksOnGround++;
		} else {
			chainedJumps = 0;
		}

	}

	@Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true)
	public void onLivingUpdate(CallbackInfo ci) {
		if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.getHealth() < this.getMaxHealth() && this.tickCount % 20 * 12 == 0) {
			this.heal(1);
		}

		this.inventory.decrementAnimations();
		this.cameraVelocityOld = this.cameraVelocity;
		super.onLivingUpdate();
		this.flySpeed = this.baseFlySpeed;
		if (this.isSprinting()) {
			this.flySpeed = (float)((double)this.flySpeed + (double)this.baseFlySpeed * 0.3);
		}
		customSpeed();

		double velocity = (double) MathHelper.sqrt(this.xd * this.xd + this.zd * this.zd);
		double pitch = (double)((float)Math.atan(-this.yd * 0.2) * 15.0F);
		if (velocity > (double)0.1F) {
			velocity = (double)0.1F;
		}

		if (!this.onGround || this.getHealth() <= 0) {
			velocity = (double)0.0F;
		}

		if (this.onGround || this.getHealth() <= 0) {
			pitch = (double)0.0F;
		}

		this.cameraVelocity += (float)((velocity - (double)this.cameraVelocity) * (double)0.4F);
		this.cameraPitch += (float)((pitch - (double)this.cameraPitch) * (double)0.8F);
		if (!this.dead && this.lastDeathCoordinate != null && this.distanceTo((double)this.lastDeathCoordinate.x, (double)this.lastDeathCoordinate.y, (double)this.lastDeathCoordinate.z) < (double)8.0F) {
			this.lastDeathCoordinate = null;
		}

		if (this.getHealth() > 0) {
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.bb.grow((double)1.0F, (double)0.5F, (double)1.0F));
			if (list != null) {
				for(Entity entity : list) {
					if (!entity.removed) {
						this.collideWithPlayer(entity);
					}
				}
			}
		}
		ci.cancel();
	}
}

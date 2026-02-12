package slaals.phopping.mixin;

import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerLocal.class)
public abstract class PlayerLocalMixin extends Player {

	public PlayerLocalMixin(@Nullable World world) {
		super(world);
	}

	@Inject(method = "getFovModifier", at = @At("HEAD"), cancellable = true)
	public void getFovModifier(CallbackInfoReturnable<Float> cir) {
		float f = 1.0F;
		double speed = this.baseSpeed;
		if (this.isSprinting()) speed = (speed + this.baseSpeed * 0.3);
		f *= ((float)speed / this.baseSpeed + 1.0F) / 2.0F;
		f *= this.heldObject == null ? 1.0F : 0.8F;
		cir.setReturnValue(f);
		cir.cancel();
	}
}

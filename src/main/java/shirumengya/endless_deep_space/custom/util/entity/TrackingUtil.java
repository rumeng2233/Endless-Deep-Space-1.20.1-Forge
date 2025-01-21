package shirumengya.endless_deep_space.custom.util.entity;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLordPart;

import java.util.List;
import java.util.Random;
import java.util.Vector;

public class TrackingUtil {

    private static final Random random = new Random();

    public static double vec2AngleCos(double x1, double y1, double x2, double y2) {
        return Mth.fastInvSqrt(modSqr(x1, y1)) * Mth.fastInvSqrt(modSqr(x2, y2)) * (x1 * x2 + y1 * y2);
    }

    public static double vec3AngleCos(Vec3 a, Vec3 b) {
        return Mth.fastInvSqrt(a.lengthSqr()) * Mth.fastInvSqrt(b.lengthSqr()) * a.dot(b);
    }

    public static double modSqr(double x, double y) {
        return x * x + y * y;
    }

    public static double modSqr(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    public static <T extends Entity> List<T> getTargetClassList(Entity entity, Class<T> t, double range) {
        Level level = entity.level();
        List<T> list = level.getEntitiesOfClass(t, entity.getBoundingBox().inflate(range, range, range), (p_186450_) -> true);
        list.remove(entity);
        if (entity instanceof Projectile) {
            Projectile entity1 = (Projectile)entity;
            if (entity1.getOwner() != null) {
                list.remove(entity1.getOwner());
            }
        }
        return list;
    }

    public static <T extends Entity> Entity getTargetClass(Projectile projectile, Class<T> t, boolean angleRestriction, double trackingRange) {
        Level level = projectile.level();
        List<T> list = level.getEntitiesOfClass(t, projectile.getBoundingBox().inflate(trackingRange, trackingRange, trackingRange), (p_186450_) -> true);
        Entity owner = projectile.getOwner();
        list.remove(projectile);
        list.remove(owner);
        if (angleRestriction) {
            Vector<Entity> vector = new Vector<>();
            Vec3 velocity = projectile.getDeltaMovement();
            for (T entity : list) {
                Vec3 vec3 = new Vec3(entity.getX() - projectile.getX(), entity.getY() - projectile.getY(), entity.getZ() - projectile.getZ());
                if (vec3AngleCos(vec3, velocity) < 0.5) {
                    vector.add(entity);
                }
            }
            for (Entity entity : vector) {
                list.remove(entity);
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        Entity entity1 = list.get(0);
        for (T entity : list) {
            if (projectile.distanceToSqr(entity) < projectile.distanceToSqr(entity1)) {
                entity1 = entity;
            }

            if (entity instanceof EnderLordPart part) {
                if (part.parentMob.head != null) {
                    return part.parentMob.head;
                }
            } else if (entity instanceof EnderLord enderLord) {
                if (enderLord.head != null) {
                    return enderLord.head;
                }
            }

            if (entity instanceof EnderDragonPart part) {
                return part.parentMob.head;
            } else if (entity instanceof EnderDragon enderDragon) {
                return enderDragon.head;
            }
        }
        return entity1;
    }

    public static <T extends Entity> void TrackingEntityClass(Projectile projectile, Class<T> targetClass, double trackingRange, boolean angleRestriction, double maxTurningAngleCos, double maxTurningAngleSin, boolean lockFeet) {
        Level level = projectile.level();
        Entity target = getTargetClass(projectile, targetClass, angleRestriction, trackingRange);
        Vec3 velocity = projectile.getDeltaMovement();
        if (target == null || !target.isAlive() || velocity.lengthSqr() < 0.25) {
            projectile.setNoGravity(false);
        } else if (!level.isClientSide) {
            projectile.setNoGravity(true);
            Vec3 delta;
            if (lockFeet) {
                delta = new Vec3(target.getX() - projectile.getX(), target.getY() - projectile.getY(), target.getZ() - projectile.getZ());
            } else {
                delta = new Vec3(target.getX() - projectile.getX(), target.getEyeY() - projectile.getY(), target.getZ() - projectile.getZ());
            }
            double cosTheta = vec2AngleCos(delta.x, delta.z, velocity.x, velocity.z);
            double sinTheta;
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            double vx, vz, vy;
            double d0 = velocity.x * cosTheta - velocity.z * sinTheta;
            double d1 = velocity.x * sinTheta + velocity.z * cosTheta;
            double d2 = velocity.x * cosTheta + velocity.z * sinTheta;
            double d3 = -velocity.x * sinTheta + velocity.z * cosTheta;
            if (d0 * delta.x + d1 * delta.z > d2 * delta.x + d3 * delta.z) {
                vx = d0;
                vz = d1;
            } else {
                vx = d2;
                vz = d3;
            }
            double vNewX = Math.sqrt(modSqr(vx, vz));
            double deltaNewX = Math.sqrt(modSqr(delta.x, delta.z));
            cosTheta = vec2AngleCos(vNewX, velocity.y, deltaNewX, delta.y);
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            d0 = vNewX * cosTheta - velocity.y * sinTheta;
            d1 = vNewX * sinTheta + velocity.y * cosTheta;
            d2 = vNewX * cosTheta + velocity.y * sinTheta;
            d3 = -vNewX * sinTheta + velocity.y * cosTheta;
            double adjusted;
            if (d0 * deltaNewX + d1 * delta.y > d2 * deltaNewX + d3 * delta.y) {
                adjusted = d0;
                vy = d1;
            } else {
                adjusted = d2;
                vy = d3;
            }
            vx *= adjusted / vNewX;
            vz *= adjusted / vNewX;
            projectile.push(vx - velocity.x, vy - velocity.y, vz - velocity.z);
        }
    }

    public static <T extends Entity> void forceEffect(Entity centerEntity, Class<T> targetClass, boolean bypassPlayer, double range, double GM, double boundaryR2) {
        List<T> list = getTargetClassList(centerEntity, targetClass, range);
        for (T entity : list) {
            Vec3 rVec = new Vec3(centerEntity.getX() - entity.getX(), centerEntity.getY() - entity.getEyeY(), centerEntity.getZ() - entity.getZ());
            double r2 = rVec.lengthSqr();
            double ir2 = Mth.fastInvSqrt(r2);
            double a;
            if (r2 > boundaryR2) {
                a = GM / r2;
            } else if (r2 > 0.25) {
                a = GM / boundaryR2;
            } else {
                a = 0;
            }
            entity.push(a * rVec.x * ir2, a * rVec.y * ir2, a * rVec.z * ir2);
            if (entity instanceof ServerPlayer player && !player.getAbilities().instabuild && !bypassPlayer) {
                player.connection.send(new ClientboundSetEntityMotionPacket(entity));
            }
        }
    }

    public static void TrackingEntity(Projectile projectile, double maxTurningAngleCos, double maxTurningAngleSin, boolean lockFeet) {
        Level level = projectile.level();
        Mob _mob = (projectile.getOwner() instanceof Mob) ? (Mob)projectile.getOwner() : null;
        Entity target = null;
        if (_mob != null) {
            target = _mob.getTarget();
        }
        if (projectile.getOwner() instanceof EnderLord && projectile.getOwner() != null) {
            target = ((EnderLord)projectile.getOwner()).attackTarget;
        }
        Vec3 velocity = projectile.getDeltaMovement();
        if (target == null || !target.isAlive() || velocity.lengthSqr() < 0.25) {
            projectile.setNoGravity(false);
        } else if (!level.isClientSide) {
            projectile.setNoGravity(true);
            Vec3 delta;
            if (lockFeet) {
                delta = new Vec3(target.getX() - projectile.getX(), target.getY() - projectile.getY(), target.getZ() - projectile.getZ());
            } else {
                delta = new Vec3(target.getX() - projectile.getX(), target.getEyeY() - projectile.getY(), target.getZ() - projectile.getZ());
            }
            double cosTheta = vec2AngleCos(delta.x, delta.z, velocity.x, velocity.z);
            double sinTheta;
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            double vx, vz, vy;
            double d0 = velocity.x * cosTheta - velocity.z * sinTheta;
            double d1 = velocity.x * sinTheta + velocity.z * cosTheta;
            double d2 = velocity.x * cosTheta + velocity.z * sinTheta;
            double d3 = -velocity.x * sinTheta + velocity.z * cosTheta;
            if (d0 * delta.x + d1 * delta.z > d2 * delta.x + d3 * delta.z) {
                vx = d0;
                vz = d1;
            } else {
                vx = d2;
                vz = d3;
            }
            double vNewX = Math.sqrt(modSqr(vx, vz));
            double deltaNewX = Math.sqrt(modSqr(delta.x, delta.z));
            cosTheta = vec2AngleCos(vNewX, velocity.y, deltaNewX, delta.y);
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            d0 = vNewX * cosTheta - velocity.y * sinTheta;
            d1 = vNewX * sinTheta + velocity.y * cosTheta;
            d2 = vNewX * cosTheta + velocity.y * sinTheta;
            d3 = -vNewX * sinTheta + velocity.y * cosTheta;
            double adjusted;
            if (d0 * deltaNewX + d1 * delta.y > d2 * deltaNewX + d3 * delta.y) {
                adjusted = d0;
                vy = d1;
            } else {
                adjusted = d2;
                vy = d3;
            }
            vx *= adjusted / vNewX;
            vz *= adjusted / vNewX;
            projectile.push(vx - velocity.x, vy - velocity.y, vz - velocity.z);
        }
    }

    public static void TrackingEntityOnAbstractHurtingProjectile(AbstractHurtingProjectile projectile, double maxTurningAngleCos, double maxTurningAngleSin, boolean lockFeet) {
        Level level = projectile.level();
        Mob _mob = (projectile.getOwner() instanceof Mob) ? (Mob)projectile.getOwner() : null;
        Entity target = null;
        if (_mob != null) {
            target = _mob.getTarget();
        } else {
            target = null;
        }
        if (projectile.getOwner() instanceof EnderLord && projectile.getOwner() != null) {
            target = ((EnderLord)projectile.getOwner()).attackTarget;
        }
        Vec3 velocity = projectile.getDeltaMovement();
        if (target == null || !target.isAlive() || velocity.lengthSqr() < 0.25) {
            projectile.setNoGravity(false);
        } else if (!level.isClientSide) {
            projectile.setNoGravity(true);
            Vec3 delta;
            if (lockFeet) {
                delta = new Vec3(target.getX() - projectile.getX(), target.getY() - projectile.getY(), target.getZ() - projectile.getZ());
            } else {
                delta = new Vec3(target.getX() - projectile.getX(), target.getEyeY() - projectile.getY(), target.getZ() - projectile.getZ());
            }
            double cosTheta = vec2AngleCos(delta.x, delta.z, velocity.x, velocity.z);
            double sinTheta;
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            double vx, vz, vy;
            double d0 = velocity.x * cosTheta - velocity.z * sinTheta;
            double d1 = velocity.x * sinTheta + velocity.z * cosTheta;
            double d2 = velocity.x * cosTheta + velocity.z * sinTheta;
            double d3 = -velocity.x * sinTheta + velocity.z * cosTheta;
            if (d0 * delta.x + d1 * delta.z > d2 * delta.x + d3 * delta.z) {
                vx = d0;
                vz = d1;
            } else {
                vx = d2;
                vz = d3;
            }
            double vNewX = Math.sqrt(modSqr(vx, vz));
            double deltaNewX = Math.sqrt(modSqr(delta.x, delta.z));
            cosTheta = vec2AngleCos(vNewX, velocity.y, deltaNewX, delta.y);
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            d0 = vNewX * cosTheta - velocity.y * sinTheta;
            d1 = vNewX * sinTheta + velocity.y * cosTheta;
            d2 = vNewX * cosTheta + velocity.y * sinTheta;
            d3 = -vNewX * sinTheta + velocity.y * cosTheta;
            double adjusted;
            if (d0 * deltaNewX + d1 * delta.y > d2 * deltaNewX + d3 * delta.y) {
                adjusted = d0;
                vy = d1;
            } else {
                adjusted = d2;
                vy = d3;
            }
            vx *= adjusted / vNewX;
            vz *= adjusted / vNewX;

            projectile.xPower = vx - velocity.x;
            projectile.yPower = vy - velocity.y;
            projectile.zPower = vz - velocity.z;
        }
    }

    public static void missilesTracking(Projectile projectile, boolean isLockFeet) {
        Level level = projectile.level();
        Mob _mob = (projectile.getOwner() instanceof Mob) ? (Mob)projectile.getOwner() : null;
        Entity target = null;
        if (_mob != null) {
            target = _mob.getTarget();
        } else {
            target = null;
        }
        if (projectile.getOwner() instanceof EnderLord && projectile.getOwner() != null) {
            target = ((EnderLord)projectile.getOwner()).attackTarget;
        }
        Vec3 velocity = projectile.getDeltaMovement();
        if (velocity.lengthSqr() < 0.25) {
            projectile.setNoGravity(false);
        } else if (target == null || !target.isAlive()) {
            projectile.setNoGravity(false);
        } else {
            projectile.setNoGravity(true);
            Vec3 delta;
            if (isLockFeet) {
                delta = target.getPosition(1).subtract(projectile.getPosition(1));
            } else {
                delta = target.getBoundingBox().getCenter().subtract(projectile.getPosition(1));
            }
            double cosTheta = newVec2AngleCos(delta.x, delta.z, velocity.x, velocity.z);
            if (cosTheta > 1) {
                cosTheta = 1;
            }
            double sinTheta;
            float maxTurningAngleCos = Mth.cos(7.1619724F * (float) velocity.length() * Mth.DEG_TO_RAD);
            float maxTurningAngleSin = Mth.sin(7.1619724F * (float) velocity.length() * Mth.DEG_TO_RAD);
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            double vx, vz, vy;
            double d0 = velocity.x * cosTheta - velocity.z * sinTheta;
            double d1 = velocity.x * sinTheta + velocity.z * cosTheta;
            double d2 = velocity.x * cosTheta + velocity.z * sinTheta;
            double d3 = -velocity.x * sinTheta + velocity.z * cosTheta;
            if (d0 * delta.x + d1 * delta.z > d2 * delta.x + d3 * delta.z) {
                vx = d0;
                vz = d1;
            } else {
                vx = d2;
                vz = d3;
            }
            double vNewX = Math.sqrt(lengthSqr(vx, vz));
            double deltaNewX = Math.sqrt(lengthSqr(delta.x, delta.z));
            cosTheta = newVec2AngleCos(vNewX, velocity.y, deltaNewX, delta.y);
            if (cosTheta > 1) {
                cosTheta = 1;
            }
            if (cosTheta < maxTurningAngleCos) {
                cosTheta = maxTurningAngleCos;
                sinTheta = maxTurningAngleSin;
            } else {
                sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            }
            d0 = vNewX * cosTheta - velocity.y * sinTheta;
            d1 = vNewX * sinTheta + velocity.y * cosTheta;
            d2 = vNewX * cosTheta + velocity.y * sinTheta;
            d3 = -vNewX * sinTheta + velocity.y * cosTheta;
            double adjusted;
            if (d0 * deltaNewX + d1 * delta.y > d2 * deltaNewX + d3 * delta.y) {
                adjusted = d0;
                vy = d1;
            } else {
                adjusted = d2;
                vy = d3;
            }
            vx *= adjusted / vNewX;
            vz *= adjusted / vNewX;
            //Do not directly use "setDeltaMovement" because it will cause the lagging of texture. Use "push" may avoid this.
            projectile.push(vx - velocity.x, vy - velocity.y, vz - velocity.z);
        }
    }

    public static double newVec2AngleCos(double x1, double y1, double x2, double y2) {
        return Mth.invSqrt(lengthSqr(x1, y1)) * Mth.invSqrt(lengthSqr(x2, y2)) * (x1 * x2 + y1 * y2);
    }

    public static double newVec3AngleCos(Vec3 a, Vec3 b) {
        return Mth.invSqrt(a.lengthSqr()) * Mth.invSqrt(b.lengthSqr()) * a.dot(b);
    }

    public static double lengthSqr(double x, double y) {
        return x * x + y * y;
    }

    public static double lengthSqr(double x, double y, double z) {
        return x * x + y * y + z * z;
    }
}
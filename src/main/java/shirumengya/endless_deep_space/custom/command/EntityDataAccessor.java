package shirumengya.endless_deep_space.custom.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

public class EntityDataAccessor implements DataAccessor {
   public static final Function<String, EndlessDeepSpaceCommand.DataProvider> PROVIDER = (p_139517_) -> {
      return new EndlessDeepSpaceCommand.DataProvider() {
         public DataAccessor access(CommandContext<CommandSourceStack> p_139530_) throws CommandSyntaxException {
            return new EntityDataAccessor(EntityArgument.getEntity(p_139530_, p_139517_));
         }

         public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> p_139527_, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> p_139528_) {
            return p_139527_.then(Commands.literal("entity").then(p_139528_.apply(Commands.argument(p_139517_, EntityArgument.entity()))));
         }
      };
   };
   private final Entity entity;

   public EntityDataAccessor(Entity p_139510_) {
      this.entity = p_139510_;
   }

   public void setData(CompoundTag p_139519_) throws CommandSyntaxException {
       UUID uuid = this.entity.getUUID();
       this.entity.load(p_139519_);
       this.entity.setUUID(uuid);
   }

   public CompoundTag getData() {
      return NbtPredicate.getEntityTagToCompare(this.entity);
   }

   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.entity.modified", this.entity.getDisplayName());
   }

   public Component getPrintSuccess(Tag p_139521_) {
      return Component.translatable("commands.data.entity.query", this.entity.getDisplayName(), NbtUtils.toPrettyComponent(p_139521_));
   }

   public Component getPrintSuccess(NbtPathArgument.NbtPath p_139513_, double p_139514_, int p_139515_) {
      return Component.translatable("commands.data.entity.get", p_139513_, this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", p_139514_), p_139515_);
   }
}
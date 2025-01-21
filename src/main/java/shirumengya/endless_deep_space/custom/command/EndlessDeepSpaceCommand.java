package shirumengya.endless_deep_space.custom.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.CrashGameS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.DeleteEntityS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.ForceTPS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateAttritionS2CPacket;
import shirumengya.endless_deep_space.custom.world.data.ModWorldData;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;
import shirumengya.endless_deep_space.mixins.EntityAccessor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class EndlessDeepSpaceCommand {
   public static final SimpleCommandExceptionType ERROR_INVULNERABLE = new SimpleCommandExceptionType(Component.translatable("commands.damage.invulnerable"));
   private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType((p_137029_) -> {
      return Component.translatable("commands.enchant.failed.entity", p_137029_);
   });
   private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType((p_137027_) -> {
      return Component.translatable("commands.enchant.failed.itemless", p_137027_);
   });
   private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(Component.translatable("commands.enchant.failed"));
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
   private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
   private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));
   public static final SuggestionProvider<CommandSourceStack> ENTITIES = SuggestionProviders.register(new ResourceLocation("entities"), (p_258164_, p_258165_) -> {
      return SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter((p_247987_) -> {
         return p_247987_.isEnabled(p_258164_.getSource().enabledFeatures());
      }), p_258165_, EntityType::getKey, (p_212436_) -> {
         return Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(p_212436_)));
      });
   });
   private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.data.merge.failed"));
   private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType((p_139491_) -> {
      return Component.translatable("commands.data.get.invalid", p_139491_);
   });
   private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType((p_139481_) -> {
      return Component.translatable("commands.data.get.unknown", p_139481_);
   });
   private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Component.translatable("commands.data.get.multiple"));
   private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType((p_139448_) -> {
      return Component.translatable("commands.data.modify.expected_object", p_139448_);
   });
   private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType((p_264853_) -> {
      return Component.translatable("commands.data.modify.expected_value", p_264853_);
   });
   private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType((p_288740_, p_288741_) -> {
      return Component.translatable("commands.data.modify.invalid_substring", p_288740_, p_288741_);
   });
   public static final List<Function<String, EndlessDeepSpaceCommand.DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);
   public static final List<EndlessDeepSpaceCommand.DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream().map((p_139450_) -> {
      return p_139450_.apply("target");
   }).collect(ImmutableList.toImmutableList());
   public static final List<EndlessDeepSpaceCommand.DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream().map((p_139410_) -> {
      return p_139410_.apply("source");
   }).collect(ImmutableList.toImmutableList());
   private static final SimpleCommandExceptionType ERROR_NOT_MOB = new SimpleCommandExceptionType(Component.literal("Source is not a mob"));
   private static final SimpleCommandExceptionType ERROR_NO_PATH = new SimpleCommandExceptionType(Component.literal("Path not found"));
   private static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.literal("Target not reached"));
   private static final SimpleCommandExceptionType SAVE_ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.save.failed"));
   private static final DynamicCommandExceptionType ERROR_NO_UUID = new DynamicCommandExceptionType((p_137029_) -> {
      return Component.translatable("commands.endless_deep_space.chunk_loader.failed.uuid", p_137029_);
   });
   
   @SubscribeEvent
   public static void registerCommand(RegisterCommandsEvent event) {
      
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("data_plus");
      for(EndlessDeepSpaceCommand.DataProvider EndlessDeepSpaceCommand$dataprovider : TARGET_PROVIDERS) {
         literalargumentbuilder.then(EndlessDeepSpaceCommand$dataprovider.wrap(Commands.literal("merge"), (p_139471_) -> {
            return p_139471_.then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((p_142857_) -> {
               return mergeData(p_142857_.getSource(), EndlessDeepSpaceCommand$dataprovider.access(p_142857_), CompoundTagArgument.getCompoundTag(p_142857_, "nbt"));
            }));
         })).then(EndlessDeepSpaceCommand$dataprovider.wrap(Commands.literal("get"), (p_139453_) -> {
            return p_139453_.executes((p_142849_) -> {
               return getData(p_142849_.getSource(), EndlessDeepSpaceCommand$dataprovider.access(p_142849_));
            }).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((p_142841_) -> {
               return getData(p_142841_.getSource(), EndlessDeepSpaceCommand$dataprovider.access(p_142841_), NbtPathArgument.getPath(p_142841_, "path"));
            }).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes((p_142833_) -> {
               return getNumeric(p_142833_.getSource(), EndlessDeepSpaceCommand$dataprovider.access(p_142833_), NbtPathArgument.getPath(p_142833_, "path"), DoubleArgumentType.getDouble(p_142833_, "scale"));
            })));
         })).then(EndlessDeepSpaceCommand$dataprovider.wrap(Commands.literal("remove"), (p_139413_) -> {
            return p_139413_.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((p_142820_) -> {
               return removeData(p_142820_.getSource(), EndlessDeepSpaceCommand$dataprovider.access(p_142820_), NbtPathArgument.getPath(p_142820_, "path"));
            }));
         })).then(decorateModification((p_139368_, p_139369_) -> {
            p_139368_.then(Commands.literal("insert").then(Commands.argument("index", IntegerArgumentType.integer()).then(p_139369_.create((p_142859_, p_142860_, p_142861_, p_142862_) -> {
               return p_142861_.insert(IntegerArgumentType.getInteger(p_142859_, "index"), p_142860_, p_142862_);
            })))).then(Commands.literal("prepend").then(p_139369_.create((p_142851_, p_142852_, p_142853_, p_142854_) -> {
               return p_142853_.insert(0, p_142852_, p_142854_);
            }))).then(Commands.literal("append").then(p_139369_.create((p_142843_, p_142844_, p_142845_, p_142846_) -> {
               return p_142845_.insert(-1, p_142844_, p_142846_);
            }))).then(Commands.literal("set").then(p_139369_.create((p_142835_, p_142836_, p_142837_, p_142838_) -> {
               return p_142837_.set(p_142836_, Iterables.getLast(p_142838_));
            }))).then(Commands.literal("merge").then(p_139369_.create((p_142822_, p_142823_, p_142824_, p_142825_) -> {
               CompoundTag compoundtag = new CompoundTag();
               
               for(Tag tag : p_142825_) {
                  if (NbtPathArgument.NbtPath.isTooDeep(tag, 0)) {
                     throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                  }
                  
                  if (!(tag instanceof CompoundTag)) {
                     throw ERROR_EXPECTED_OBJECT.create(tag);
                  }
                  
                  CompoundTag compoundtag1 = (CompoundTag)tag;
                  compoundtag.merge(compoundtag1);
               }
               
               Collection<Tag> collection = p_142824_.getOrCreate(p_142823_, CompoundTag::new);
               int i = 0;
               
               for(Tag tag1 : collection) {
                  if (!(tag1 instanceof CompoundTag)) {
                     throw ERROR_EXPECTED_OBJECT.create(tag1);
                  }
                  
                  CompoundTag compoundtag2 = (CompoundTag)tag1;
                  CompoundTag $$12 = compoundtag2.copy();
                  compoundtag2.merge(compoundtag);
                  i += $$12.equals(compoundtag2) ? 0 : 1;
               }
               
               return i;
            })));
         }));
      }
      
      event.getDispatcher().register(Commands.literal("endless_deep_space").requires(s -> s.hasPermission(2))
            .then(Commands.literal("nuke")
                  .then(Commands.argument("Blockpos", BlockPosArgument.blockPos())
                        .then(Commands.argument("SourceEntity", EntityArgument.entity())
                              .then(Commands.argument("Radius", FloatArgumentType.floatArg(0))
                                    .then(Commands.argument("DamageReduction", FloatArgumentType.floatArg(0))
                                          .then(Commands.argument("Fire", BoolArgumentType.bool())
                                                .then(Commands.argument("MaxBlastPower", DoubleArgumentType.doubleArg(0))
                                                      .then(Commands.argument("yShorten", FloatArgumentType.floatArg(0))
                                                            .then(Commands.argument("ScreenShake", BoolArgumentType.bool())
                                                                  .then(Commands.literal("KEEP").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), EntityArgument.getEntity(p_137810_, "SourceEntity"), FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.KEEP);
                                                                  }))
                                                                  .then(Commands.literal("DESTROY").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), EntityArgument.getEntity(p_137810_, "SourceEntity"), FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.DESTROY);
                                                                  }))
                                                                  .then(Commands.literal("DESTROY_WITH_DECAY").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), EntityArgument.getEntity(p_137810_, "SourceEntity"), FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY);
                                                                  }))
                                                                  .then(Commands.literal("DESTROY_NO_LOOT").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), EntityArgument.getEntity(p_137810_, "SourceEntity"), FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.DESTROY_NO_LOOT);
                                                                  }))
                                                            )))))))
                        .then(Commands.literal("null")
                              .then(Commands.argument("Radius", FloatArgumentType.floatArg(0))
                                    .then(Commands.argument("DamageReduction", FloatArgumentType.floatArg(0))
                                          .then(Commands.argument("Fire", BoolArgumentType.bool())
                                                .then(Commands.argument("MaxBlastPower", DoubleArgumentType.doubleArg(0))
                                                      .then(Commands.argument("yShorten", FloatArgumentType.floatArg(0))
                                                            .then(Commands.argument("ScreenShake", BoolArgumentType.bool())
                                                                  .then(Commands.literal("KEEP").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), null, FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.KEEP);
                                                                  }))
                                                                  .then(Commands.literal("DESTROY").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), null, FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.DESTROY);
                                                                  }))
                                                                  .then(Commands.literal("DESTROY_WITH_DECAY").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), null, FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY);
                                                                  }))
                                                                  .then(Commands.literal("DESTROY_NO_LOOT").executes((p_137810_) -> {
                                                                     return nukeExplode(p_137810_.getSource(), BlockPosArgument.getBlockPos(p_137810_, "Blockpos"), null, FloatArgumentType.getFloat(p_137810_, "Radius"), FloatArgumentType.getFloat(p_137810_, "DamageReduction"), BoolArgumentType.getBool(p_137810_, "Fire"), DoubleArgumentType.getDouble(p_137810_, "MaxBlastPower"), FloatArgumentType.getFloat(p_137810_, "yShorten"), BoolArgumentType.getBool(p_137810_, "ScreenShake"), CustomExplosion.BlockInteraction.DESTROY_NO_LOOT);
                                                                  }))
                                                            ))))))))
            )
            
            .then(Commands.literal("attrition")
                  .then(Commands.literal("get")
                        .then(Commands.argument("target", EntityArgument.entity())
                              .then(Commands.literal("attrition")
                                    .executes((command) -> {
                                       return getAttrition(command.getSource(), EntityArgument.getEntity(command, "target"), 0);
                                    }))
                              .then(Commands.literal("attritionTick")
                                    .executes((command) -> {
                                       return getAttrition(command.getSource(), EntityArgument.getEntity(command, "target"), 1);
                                    }))
                              .then(Commands.literal("attritionMaxTick")
                                    .executes((command) -> {
                                       return getAttrition(command.getSource(), EntityArgument.getEntity(command, "target"), 2);
                                    }))
                        ))
                  .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.entities())
                              .then(Commands.literal("attrition")
                                    .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F))
                                          .executes((command) -> {
                                             return setAttrition(command.getSource(), EntityArgument.getEntities(command, "targets"), 0, FloatArgumentType.getFloat(command, "value"));
                                          })))
                              .then(Commands.literal("attritionTick")
                                    .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                          .executes((command) -> {
                                             return setAttrition(command.getSource(), EntityArgument.getEntities(command, "targets"), 1, IntegerArgumentType.getInteger(command, "value"));
                                          })))
                              .then(Commands.literal("attritionMaxTick")
                                    .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                          .executes((command) -> {
                                             return setAttrition(command.getSource(), EntityArgument.getEntities(command, "targets"), 2, IntegerArgumentType.getInteger(command, "value"));
                                          })))
                        ))
                  .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.entities())
                              .then(Commands.argument("attrition", FloatArgumentType.floatArg(0.0F))
                                    .executes((command) -> {
                                       return addAttrition(command.getSource(), EntityArgument.getEntities(command, "targets"), FloatArgumentType.getFloat(command, "attrition"), 40, true, true);
                                    })
                                    .then(Commands.argument("attritionTick", IntegerArgumentType.integer(0))
                                          .executes((command) -> {
                                             return addAttrition(command.getSource(), EntityArgument.getEntities(command, "targets"), FloatArgumentType.getFloat(command, "attrition"), IntegerArgumentType.getInteger(command, "attritionTick"), true, true);
                                          })
                                          .then(Commands.argument("addLevel", BoolArgumentType.bool())
                                                .then(Commands.argument("updateTick", BoolArgumentType.bool())
                                                      .executes((command) -> {
                                                         return addAttrition(command.getSource(), EntityArgument.getEntities(command, "targets"), FloatArgumentType.getFloat(command, "attrition"), IntegerArgumentType.getInteger(command, "attritionTick"), BoolArgumentType.getBool(command, "addLevel"), BoolArgumentType.getBool(command, "updateTick"));
                                                      })))))
                        ))
            )
            
            .then(Commands.literal("crash")
                  .executes((p_137817_) -> {
                     return crashPlayer(p_137817_.getSource(), ImmutableList.of(p_137817_.getSource().getPlayerOrException()), -1);
                  }).then(Commands.argument("players", EntityArgument.players())
                        .executes((p_137817_) -> {
                           return crashPlayer(p_137817_.getSource(), EntityArgument.getPlayers(p_137817_, "players"), -1);
                        })
                        .then(Commands.argument("exitID", IntegerArgumentType.integer()).executes((p_137810_) -> {
                           return crashPlayer(p_137810_.getSource(), EntityArgument.getPlayers(p_137810_, "players"), IntegerArgumentType.getInteger(p_137810_, "exitID"));
                        })))
                  .then(Commands.literal("server")
                        .executes((p_137817_) -> {
                           return crashServer(p_137817_.getSource(), -1);
                        })
                        .then(Commands.argument("exitID", IntegerArgumentType.integer()).executes((p_137810_) -> {
                           return crashServer(p_137810_.getSource(), IntegerArgumentType.getInteger(p_137810_, "exitID"));
                        })))
            )
            
            .then(Commands.literal("damage_more")
                  .then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes((p_288351_) -> {
                     return damage(p_288351_.getSource(), EntityArgument.getEntities(p_288351_, "targets"), FloatArgumentType.getFloat(p_288351_, "amount"), p_288351_.getSource().getLevel().damageSources().generic(), false);
                  }).then(Commands.argument("damageType", ResourceArgument.resource(event.getBuildContext(), Registries.DAMAGE_TYPE)).executes((p_270840_) -> {
                     return damage(p_270840_.getSource(), EntityArgument.getEntities(p_270840_, "targets"), FloatArgumentType.getFloat(p_270840_, "amount"), new DamageSource(ResourceArgument.getResource(p_270840_, "damageType", Registries.DAMAGE_TYPE)), false);
                  }).then(Commands.literal("at").then(Commands.argument("location", Vec3Argument.vec3()).executes((p_270444_) -> {
                     return damage(p_270444_.getSource(), EntityArgument.getEntities(p_270444_, "targets"), FloatArgumentType.getFloat(p_270444_, "amount"), new DamageSource(ResourceArgument.getResource(p_270444_, "damageType", Registries.DAMAGE_TYPE), Vec3Argument.getVec3(p_270444_, "location")), false);
                  }))).then(Commands.literal("by").then(Commands.argument("entity", EntityArgument.entity()).executes((p_270329_) -> {
                     return damage(p_270329_.getSource(), EntityArgument.getEntities(p_270329_, "targets"), FloatArgumentType.getFloat(p_270329_, "amount"), new DamageSource(ResourceArgument.getResource(p_270329_, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(p_270329_, "entity")), false);
                  }).then(Commands.literal("from").then(Commands.argument("cause", EntityArgument.entity()).executes((p_270848_) -> {
                     return damage(p_270848_.getSource(), EntityArgument.getEntities(p_270848_, "targets"), FloatArgumentType.getFloat(p_270848_, "amount"), new DamageSource(ResourceArgument.getResource(p_270848_, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(p_270848_, "entity"), EntityArgument.getEntity(p_270848_, "cause")), false);
                  }))))))))
                  .then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("actuallyHurt", BoolArgumentType.bool()).then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes((p_288351_) -> {
                     return damage(p_288351_.getSource(), EntityArgument.getEntities(p_288351_, "targets"), FloatArgumentType.getFloat(p_288351_, "amount"), p_288351_.getSource().getLevel().damageSources().generic(), BoolArgumentType.getBool(p_288351_, "actuallyHurt"));
                  }).then(Commands.argument("damageType", ResourceArgument.resource(event.getBuildContext(), Registries.DAMAGE_TYPE)).executes((p_270840_) -> {
                     return damage(p_270840_.getSource(), EntityArgument.getEntities(p_270840_, "targets"), FloatArgumentType.getFloat(p_270840_, "amount"), new DamageSource(ResourceArgument.getResource(p_270840_, "damageType", Registries.DAMAGE_TYPE)), BoolArgumentType.getBool(p_270840_, "actuallyHurt"));
                  }).then(Commands.literal("at").then(Commands.argument("location", Vec3Argument.vec3()).executes((p_270444_) -> {
                     return damage(p_270444_.getSource(), EntityArgument.getEntities(p_270444_, "targets"), FloatArgumentType.getFloat(p_270444_, "amount"), new DamageSource(ResourceArgument.getResource(p_270444_, "damageType", Registries.DAMAGE_TYPE), Vec3Argument.getVec3(p_270444_, "location")), BoolArgumentType.getBool(p_270444_, "actuallyHurt"));
                  }))).then(Commands.literal("by").then(Commands.argument("entity", EntityArgument.entity()).executes((p_270329_) -> {
                     return damage(p_270329_.getSource(), EntityArgument.getEntities(p_270329_, "targets"), FloatArgumentType.getFloat(p_270329_, "amount"), new DamageSource(ResourceArgument.getResource(p_270329_, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(p_270329_, "entity")), BoolArgumentType.getBool(p_270329_, "actuallyHurt"));
                  }).then(Commands.literal("from").then(Commands.argument("cause", EntityArgument.entity()).executes((p_270848_) -> {
                     return damage(p_270848_.getSource(), EntityArgument.getEntities(p_270848_, "targets"), FloatArgumentType.getFloat(p_270848_, "amount"), new DamageSource(ResourceArgument.getResource(p_270848_, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(p_270848_, "entity"), EntityArgument.getEntity(p_270848_, "cause")), BoolArgumentType.getBool(p_270848_, "actuallyHurt"));
                  })))))))))
                  .then(Commands.literal("playDeathAnimation").then(Commands.argument("targets", EntityArgument.entities()).executes((command) -> {
                     return playDeathAnimation(command.getSource(), EntityArgument.getEntities(command, "targets"));
                  })))
            )
            
            .then(Commands.literal("delete")
                  .executes((p_137817_) -> {
                     return delete(p_137817_.getSource(), ImmutableList.of(p_137817_.getSource().getEntityOrException()));
                  }).then(Commands.argument("targets", EntityArgument.entities()).executes((p_137810_) -> {
                     return delete(p_137810_.getSource(), EntityArgument.getEntities(p_137810_, "targets"));
                  }))
            )
            
            .then(Commands.literal("enchant_high")
                  .then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("enchantment", ResourceArgument.resource(event.getBuildContext(), Registries.ENCHANTMENT)).executes((p_137025_) -> {
                     return enchant(p_137025_.getSource(), EntityArgument.getEntities(p_137025_, "targets"), ResourceArgument.getEnchantment(p_137025_, "enchantment"), 1);
                  }).then(Commands.argument("level", IntegerArgumentType.integer(0)).executes((p_137011_) -> {
                     return enchant(p_137011_.getSource(), EntityArgument.getEntities(p_137011_, "targets"), ResourceArgument.getEnchantment(p_137011_, "enchantment"), IntegerArgumentType.getInteger(p_137011_, "level"));
                  }))))
            )
            
            .then(Commands.literal("force_tp")
                  .then(Commands.argument("entities", EntityArgument.entities())
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                              .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                    .then(Commands.argument("z", DoubleArgumentType.doubleArg()).executes((p_137810_) -> {
                                       return TPENtity(p_137810_.getSource(), EntityArgument.getEntities(p_137810_, "entities"), DoubleArgumentType.getDouble(p_137810_, "x"), DoubleArgumentType.getDouble(p_137810_, "y"), DoubleArgumentType.getDouble(p_137810_, "z"));
                                    })))))
            )
            
            .then(Commands.literal("raid")
                  .then(Commands.literal("start").then(Commands.argument("omenlvl", IntegerArgumentType.integer(0)).executes((p_180502_) -> {
                     return start(p_180502_.getSource(), IntegerArgumentType.getInteger(p_180502_, "omenlvl"));
                  }))).then(Commands.literal("stop").executes((p_180500_) -> {
                     return stop(p_180500_.getSource());
                  })).then(Commands.literal("check").executes((p_180496_) -> {
                     return check(p_180496_.getSource());
                  })).then(Commands.literal("sound").then(Commands.argument("type", ComponentArgument.textComponent()).executes((p_180492_) -> {
                     return playSound(p_180492_.getSource(), ComponentArgument.getComponent(p_180492_, "type"));
                  }))).then(Commands.literal("spawnleader").executes((p_180488_) -> {
                     return spawnLeader(p_180488_.getSource());
                  })).then(Commands.literal("setomen").then(Commands.argument("level", IntegerArgumentType.integer(0)).executes((p_180481_) -> {
                     return setBadOmenLevel(p_180481_.getSource(), IntegerArgumentType.getInteger(p_180481_, "level"));
                  }))).then(Commands.literal("glow").executes((p_180471_) -> {
                     return glow(p_180471_.getSource());
                  }))
            )
            
            .then(Commands.literal("resetchunks")
                  .executes((p_183693_) -> {
                     return resetChunks(p_183693_.getSource(), 0, true, null);
                  }).then(Commands.argument("range", IntegerArgumentType.integer(0, 5)).executes((p_183689_) -> {
                     return resetChunks(p_183689_.getSource(), IntegerArgumentType.getInteger(p_183689_, "range"), true, null);
                  }).then(Commands.argument("skipOldChunks", BoolArgumentType.bool()).executes((p_183669_) -> {
                     return resetChunks(p_183669_.getSource(), IntegerArgumentType.getInteger(p_183669_, "range"), BoolArgumentType.getBool(p_183669_, "skipOldChunks"), null);
                  })))
                  .then(Commands.argument("Blockpos", BlockPosArgument.blockPos()).then(Commands.argument("range", IntegerArgumentType.integer(0, 5)).executes((p_183689_) -> {
                           return resetChunks(p_183689_.getSource(), IntegerArgumentType.getInteger(p_183689_, "range"), true, BlockPosArgument.getBlockPos(p_183689_, "Blockpos"));
                        }).then(Commands.argument("skipOldChunks", BoolArgumentType.bool()).executes((p_183669_) -> {
                           return resetChunks(p_183669_.getSource(), IntegerArgumentType.getInteger(p_183669_, "range"), BoolArgumentType.getBool(p_183669_, "skipOldChunks"), BlockPosArgument.getBlockPos(p_183669_, "Blockpos"));
                        })))
                  )
            )
            
            .then(Commands.literal("summon_more")
                  .then(Commands.argument("entity", ResourceArgument.resource(event.getBuildContext(), Registries.ENTITY_TYPE)).suggests(ENTITIES)
                        .executes((p_248175_) -> {
                           return spawnEntity(p_248175_.getSource(), ResourceArgument.getEntityType(p_248175_, "entity"), p_248175_.getSource().getPosition(), new CompoundTag(), true);
                        })
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                              .executes((p_248173_) -> {
                                 return spawnEntity(p_248173_.getSource(), ResourceArgument.getEntityType(p_248173_, "entity"), Vec3Argument.getVec3(p_248173_, "pos"), new CompoundTag(), true);
                              })
                              .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                    .executes((p_248174_) -> {
                                       return spawnEntity(p_248174_.getSource(), ResourceArgument.getEntityType(p_248174_, "entity"), Vec3Argument.getVec3(p_248174_, "pos"), CompoundTagArgument.getCompoundTag(p_248174_, "nbt"), false);
                                    })
                              )))
                  .then(Commands.literal("endless_deep_space:coral_defenders")
                        .executes((p_248175_) -> {
                           return spawnCoralDefenders(p_248175_.getSource(), p_248175_.getSource().getPosition(), new CompoundTag(), true);
                        })
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                              .executes((p_248173_) -> {
                                 return spawnCoralDefenders(p_248173_.getSource(), Vec3Argument.getVec3(p_248173_, "pos"), new CompoundTag(), true);
                              })
                              .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                    .executes((p_248174_) -> {
                                       return spawnCoralDefenders(p_248174_.getSource(), Vec3Argument.getVec3(p_248174_, "pos"), CompoundTagArgument.getCompoundTag(p_248174_, "nbt"), false);
                                    })
                              )))
            )
            
            .then(literalargumentbuilder)
            
            .then(Commands.literal("vertigo_time")
                  .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("tick", IntegerArgumentType.integer(0))
                              .executes((command) -> {
                                 return setVertigoTime(command.getSource(), EntityArgument.getEntities(command, "targets"), IntegerArgumentType.getInteger(command, "tick"));
                              })
                        )
                        .then(Commands.literal("infinite")
                              .executes((command) -> {
                                 return setVertigoTime(command.getSource(), EntityArgument.getEntities(command, "targets"), -1);
                              })
                        )
                  )
            )
            
            .then(Commands.literal("debugpath")
                  .then(Commands.argument("target", EntityArgument.entity())
                     .then(Commands.argument("to", BlockPosArgument.blockPos()).then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.0D)).executes((p_180126_) -> {
                        return fillBlocks(p_180126_.getSource(), EntityArgument.getEntity(p_180126_, "target"), BlockPosArgument.getLoadedBlockPos(p_180126_, "to"), DoubleArgumentType.getDouble(p_180126_, "speed"));
                     })))
                  )
            )
            
            .then(Commands.literal("save-all")
                  .executes((p_138281_) -> {
                     return saveAll(p_138281_.getSource(), false);
                  }).then(Commands.literal("flush").executes((p_138274_) -> {
                     return saveAll(p_138274_.getSource(), true);
                  }))
            )
            
            .then(Commands.literal("warden_spawn_tracker")
                  .then(Commands.literal("clear").executes((p_214787_) -> {
                     return resetTracker(p_214787_.getSource(), ImmutableList.of(p_214787_.getSource().getPlayerOrException()));
                  })).then(Commands.literal("set").then(Commands.argument("warning_level", IntegerArgumentType.integer(0, 4)).executes((p_214776_) -> {
                     return setWarningLevel(p_214776_.getSource(), ImmutableList.of(p_214776_.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(p_214776_, "warning_level"));
                  })))
            )
            
            .then(Commands.literal("chunk_loader")
                  .then(Commands.literal("boss")
                        .then(Commands.literal("get")
                              .executes((command) -> {
                                 return getChunkLoader(command.getSource(), 1, null);
                              })
                              .then(Commands.argument("uuid", UuidArgument.uuid())
                                    .executes((command) -> {
                                       return getChunkLoader(command.getSource(), 1, UuidArgument.getUuid(command, "uuid"));
                                    })
                              )
                        )
                  )
                  .then(Commands.literal("guiding_stone")
                        .then(Commands.literal("get")
                              .executes((command) -> {
                                 return getChunkLoader(command.getSource(), 0, null);
                              })
                              .then(Commands.argument("uuid", UuidArgument.uuid())
                                    .executes((command) -> {
                                       return getChunkLoader(command.getSource(), 0, UuidArgument.getUuid(command, "uuid"));
                                    })
                              )
                        )
                  )
            )
            
      );
   }
   
   private static int nukeExplode(CommandSourceStack p_137814_, BlockPos blockPos, @Nullable Entity entity, float radius, float damageReduction, boolean fire, double maxBlastPower, float yShorten, boolean screenShake, CustomExplosion.BlockInteraction blockInteraction) {
      ServerLevel level = p_137814_.getLevel();
      CustomExplosion.nukeExplode(level, entity, blockPos.getX(), blockPos.getY(), blockPos.getZ(), radius, damageReduction, fire, blockInteraction, maxBlastPower, yShorten);
      if (screenShake) {
         ScreenShakeEntity.ScreenShake(level, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), radius, radius / 100.0F, 140, 40);
      }
      p_137814_.sendSuccess(() -> {
         return Component.translatable("commands.endless_deep_space.nuke.success", blockPos.toString(), entity == null ? Component.translatable("commands.endless_deep_space.nuke.success.nullentity") : entity.getDisplayName(), radius, damageReduction, fire, maxBlastPower, yShorten, screenShake, blockInteraction);
      }, true);
      return 1;
   }
   
   private static int getAttrition(CommandSourceStack p_137814_, Entity p_137815_, int type) throws CommandSyntaxException {
      if (p_137815_ instanceof LivingEntity livingEntity) {
         switch (type) {
            case 1:
               p_137814_.sendSuccess(() -> {
                  return Component.translatable("commands.endless_deep_space.attrition.get.success.attrition_tick", livingEntity.getDisplayName(), OceanDefender.getAttritionTick(livingEntity));
               }, true);
               break;
            case 2:
               p_137814_.sendSuccess(() -> {
                  return Component.translatable("commands.endless_deep_space.attrition.get.success.attrition_max_tick", livingEntity.getDisplayName(), OceanDefender.getAttritionMaxTick(livingEntity));
               }, true);
               break;
            default:
               p_137814_.sendSuccess(() -> {
                  return Component.translatable("commands.endless_deep_space.attrition.get.success.attrition", livingEntity.getDisplayName(), OceanDefender.getAttrition(livingEntity));
               }, true);
               break;
         }
      } else {
         throw ERROR_NOT_LIVING_ENTITY.create(p_137815_.getName().getString());
      }
      
      return 1;
   }
   
   private static int setAttrition(CommandSourceStack p_137814_, Collection<? extends Entity> p_137815_, int type, double value) {
      for (Entity entity : p_137815_) {
         if (entity instanceof LivingEntity livingEntity) {
            switch (type) {
               case 1:
                  OceanDefender.setAttritionTick(livingEntity, (int) value);
                  p_137814_.sendSuccess(() -> {
                     return Component.translatable("commands.endless_deep_space.attrition.set.success.attrition_tick", livingEntity.getDisplayName(), (int) value);
                  }, true);
                  break;
               case 2:
                  OceanDefender.setAttritionMaxTick(livingEntity, (int) value);
                  p_137814_.sendSuccess(() -> {
                     return Component.translatable("commands.endless_deep_space.attrition.set.success.attrition_max_tick", livingEntity.getDisplayName(), (int) value);
                  }, true);
                  break;
               default:
                  OceanDefender.setAttrition(livingEntity, (float) value);
                  p_137814_.sendSuccess(() -> {
                     return Component.translatable("commands.endless_deep_space.attrition.set.success.attrition", livingEntity.getDisplayName(), (float) value);
                  }, true);
                  break;
            }
            ModMessages.sendToAllPlayers(new UpdateAttritionS2CPacket(livingEntity, false, OceanDefender.getAttrition(livingEntity), OceanDefender.getAttritionTick(livingEntity)));
         } else {
            p_137814_.sendFailure(Component.translatable("commands.enchant.failed.entity", entity.getName().getString()));
         }
      }
      
      return p_137815_.size();
   }
   
   private static int addAttrition(CommandSourceStack p_137814_, Collection<? extends Entity> p_137815_, float attrition, int attritionTick, boolean addLevel, boolean updateTick) {
      for (Entity entity : p_137815_) {
         if (entity instanceof LivingEntity livingEntity) {
            OceanDefender.addAttrition(livingEntity, attrition, attritionTick, addLevel, updateTick);
            p_137814_.sendSuccess(() -> {
               return Component.translatable("commands.endless_deep_space.attrition.add.success.attrition", livingEntity.getDisplayName(), attrition, attritionTick, addLevel, updateTick);
            }, true);
         } else {
            p_137814_.sendFailure(Component.translatable("commands.enchant.failed.entity", entity.getName().getString()));
         }
      }
      
      return p_137815_.size();
   }
   
   private static int crashPlayer(CommandSourceStack p_137814_, Collection<? extends ServerPlayer> p_137815_, int exitID) {
      for(ServerPlayer entity : p_137815_) {
         ModMessages.sendToPlayer(new CrashGameS2CPacket(exitID), entity);
      }
      
      if (p_137815_.size() == 1) {
         p_137814_.sendSuccess(() -> {
            return Component.translatable("commands.endless_deep_space.crash.success.single", p_137815_.iterator().next().getDisplayName());
         }, true);
      } else {
         p_137814_.sendSuccess(() -> {
            return Component.translatable("commands.endless_deep_space.crash.success.multiple", p_137815_.size());
         }, true);
      }
      
      return p_137815_.size();
   }
   
   private static int crashServer(CommandSourceStack p_137814_, int exitID) {
      p_137814_.sendSuccess(() -> {
         return Component.translatable("commands.endless_deep_space.crash.success.server");
      }, true);
      
      Runtime.getRuntime().halt(exitID);
      
      return 1;
   }
   
   private static int damage(CommandSourceStack p_270409_, Collection<? extends Entity> p_270496_, float p_270836_, DamageSource p_270727_, boolean actuallyHurt) throws CommandSyntaxException {
      if (p_270496_.size() == 1) {
         Entity entity = p_270496_.iterator().next();
         boolean flag;
         if (actuallyHurt) {
            OceanDefender.actuallyHurt(entity, p_270727_, p_270836_, true);
            flag = true;
         } else {
            flag = entity.hurt(p_270727_, p_270836_);
         }
         if (flag) {
            p_270409_.sendSuccess(() -> {
               return Component.translatable("commands.damage.success", p_270836_, entity.getDisplayName());
            }, true);
         } else {
            throw ERROR_INVULNERABLE.create();
         }
      } else {
         for (Entity entity : p_270496_) {
            boolean flag;
            if (actuallyHurt) {
               OceanDefender.actuallyHurt(entity, p_270727_, p_270836_, true);
               flag = true;
            } else {
               flag = entity.hurt(p_270727_, p_270836_);
            }
            if (flag) {
               p_270409_.sendSuccess(() -> {
                  return Component.translatable("commands.damage.success", p_270836_, entity.getDisplayName());
               }, true);
            } else {
               p_270409_.sendFailure(Component.translatable("commands.damage.invulnerable"));
            }
         }
      }
      
      return p_270496_.size();
   }
   
   private static int playDeathAnimation(CommandSourceStack p_270409_, Collection<? extends Entity> p_270496_) {
      for (Entity entity : p_270496_) {
         if (entity instanceof LivingEntity livingEntity) {
            OceanDefender.setIsDying(livingEntity, true);
         } else {
            p_270409_.sendFailure(Component.translatable("commands.enchant.failed.entity", entity.getName().getString()));
         }
      }
      
      if (p_270496_.size() == 1) {
         p_270409_.sendSuccess(() -> {
            return Component.translatable("commands.kill.success.single", p_270496_.iterator().next().getDisplayName());
         }, true);
      } else {
         p_270409_.sendSuccess(() -> {
            return Component.translatable("commands.kill.success.multiple", p_270496_.size());
         }, true);
      }
      
      return p_270496_.size();
   }
   
   private static int delete(CommandSourceStack p_137814_, Collection<? extends Entity> p_137815_) {
      for(Entity entity : p_137815_) {
         if (entity instanceof ServerPlayer player) {
            player.connection.disconnect(Component.translatable("commands.endless_deep_space.delete.success.single", player.getDisplayName()));
         } else {
            ModMessages.sendToAllPlayers(new DeleteEntityS2CPacket(entity.getId()));
            entity.setRemoved(Entity.RemovalReason.DISCARDED);
            entity.canUpdate(false);
            entity.gameEvent(GameEvent.ENTITY_DIE);
            //entity.setPos(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
            entity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            entity.setLevelCallback(EntityInLevelCallback.NULL);
            entity.tickCount = Integer.MIN_VALUE;
            entity.setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
            entity.setBoundingBox(new AABB(Vec3.ZERO, Vec3.ZERO));
            ((EntityAccessor) entity).setBlockPosition(new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
            ((EntityAccessor) entity).setChunkPosition(new ChunkPos(Integer.MAX_VALUE, Integer.MIN_VALUE));
            ((EntityAccessor) entity).setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
            ((EntityAccessor) entity).setPosition(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
            ((EntityAccessor) entity).setRemovalReason(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            entity.invalidateCaps();
         }
      }
      
      if (p_137815_.size() == 1) {
         p_137814_.sendSuccess(() -> {
            return Component.translatable("commands.endless_deep_space.delete.success.single", p_137815_.iterator().next().getDisplayName());
         }, true);
      } else {
         p_137814_.sendSuccess(() -> {
            return Component.translatable("commands.endless_deep_space.delete.success.multiple", p_137815_.size());
         }, true);
      }
      
      return p_137815_.size();
   }
   
   private static int enchant(CommandSourceStack p_249815_, Collection<? extends Entity> p_248848_, Holder<Enchantment> p_251252_, int p_249941_) throws CommandSyntaxException {
      Enchantment enchantment = p_251252_.value();
      int i = 0;
      
      for(Entity entity : p_248848_) {
         if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            ItemStack itemstack = livingentity.getMainHandItem();
            if (!itemstack.isEmpty()) {
               itemstack.enchant(enchantment, p_249941_);
               ++i;
            } else if (p_248848_.size() == 1) {
               throw ERROR_NO_ITEM.create(livingentity.getName().getString());
            }
         } else if (p_248848_.size() == 1) {
            throw ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
         }
      }
      
      if (i == 0) {
         throw ERROR_NOTHING_HAPPENED.create();
      } else {
         if (p_248848_.size() == 1) {
            p_249815_.sendSuccess(() -> {
               return Component.translatable("commands.enchant.success.single", enchantment.getFullname(p_249941_), p_248848_.iterator().next().getDisplayName());
            }, true);
         } else {
            p_249815_.sendSuccess(() -> {
               return Component.translatable("commands.enchant.success.multiple", enchantment.getFullname(p_249941_), p_248848_.size());
            }, true);
         }
         
         return i;
      }
   }
   
   private static int TPENtity(CommandSourceStack p_137814_, Collection<? extends Entity> p_137815_, double x, double y, double z) {
      for(Entity entity : p_137815_) {
         entity.setPos(x, y, z);
         if (entity instanceof ServerPlayer player) {
            ModMessages.sendToPlayer(new ForceTPS2CPacket(x, y, z), player);
         }
      }
      
      if (p_137815_.size() == 1) {
         p_137814_.sendSuccess(() -> {
            return Component.translatable("commands.teleport.success.entity.single", p_137815_.iterator().next().getDisplayName(), new Vec3(x, y, z));
         }, true);
      } else {
         p_137814_.sendSuccess(() -> {
            return Component.translatable("commands.teleport.success.entity.multiple", p_137815_.size(), new Vec3(x, y, z));
         }, true);
      }
      
      return p_137815_.size();
   }
   
   private static int glow(CommandSourceStack p_180473_) throws CommandSyntaxException {
      Raid raid = getRaid(p_180473_.getPlayerOrException());
      if (raid != null) {
         for(Raider raider : raid.getAllRaiders()) {
            raider.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1000, 1));
         }
      }
      
      return 1;
   }
   
   private static int setBadOmenLevel(CommandSourceStack p_180475_, int p_180476_) throws CommandSyntaxException {
      Raid raid = getRaid(p_180475_.getPlayerOrException());
      if (raid != null) {
         int i = raid.getMaxBadOmenLevel();
         if (p_180476_ > i) {
            p_180475_.sendFailure(Component.literal("Sorry, the max bad omen level you can set is " + i));
         } else {
            int j = raid.getBadOmenLevel();
            raid.setBadOmenLevel(p_180476_);
            p_180475_.sendSuccess(() -> {
               return Component.literal("Changed village's bad omen level from " + j + " to " + p_180476_);
            }, false);
         }
      } else {
         p_180475_.sendFailure(Component.literal("No raid found here"));
      }
      
      return 1;
   }
   
   private static int spawnLeader(CommandSourceStack p_180483_) {
      p_180483_.sendSuccess(() -> {
         return Component.literal("Spawned a raid captain");
      }, false);
      Raider raider = EntityType.PILLAGER.create(p_180483_.getLevel());
      if (raider == null) {
         p_180483_.sendFailure(Component.literal("Pillager failed to spawn"));
         return 0;
      } else {
         raider.setPatrolLeader(true);
         raider.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
         raider.setPos(p_180483_.getPosition().x, p_180483_.getPosition().y, p_180483_.getPosition().z);
         raider.finalizeSpawn(p_180483_.getLevel(), p_180483_.getLevel().getCurrentDifficultyAt(BlockPos.containing(p_180483_.getPosition())), MobSpawnType.COMMAND, (SpawnGroupData)null, (CompoundTag)null);
         p_180483_.getLevel().addFreshEntityWithPassengers(raider);
         return 1;
      }
   }
   
   private static int playSound(CommandSourceStack p_180478_, @Nullable Component p_180479_) {
      if (p_180479_ != null && p_180479_.getString().equals("local")) {
         ServerLevel serverlevel = p_180478_.getLevel();
         Vec3 vec3 = p_180478_.getPosition().add(5.0D, 0.0D, 0.0D);
         serverlevel.playSeededSound((Player)null, vec3.x, vec3.y, vec3.z, SoundEvents.RAID_HORN, SoundSource.NEUTRAL, 2.0F, 1.0F, serverlevel.random.nextLong());
      }
      
      return 1;
   }
   
   private static int start(CommandSourceStack p_180485_, int p_180486_) throws CommandSyntaxException {
      ServerPlayer serverplayer = p_180485_.getPlayerOrException();
      BlockPos blockpos = serverplayer.blockPosition();
      if (serverplayer.serverLevel().isRaided(blockpos)) {
         p_180485_.sendFailure(Component.literal("Raid already started close by"));
         return -1;
      } else {
         Raids raids = serverplayer.serverLevel().getRaids();
         Raid raid = raids.createOrExtendRaid(serverplayer);
         if (raid != null) {
            raid.setBadOmenLevel(p_180486_);
            raids.setDirty();
            p_180485_.sendSuccess(() -> {
               return Component.literal("Created a raid in your local village");
            }, false);
         } else {
            p_180485_.sendFailure(Component.literal("Failed to create a raid in your local village"));
         }
         
         return 1;
      }
   }
   
   private static int stop(CommandSourceStack p_180490_) throws CommandSyntaxException {
      ServerPlayer serverplayer = p_180490_.getPlayerOrException();
      BlockPos blockpos = serverplayer.blockPosition();
      Raid raid = serverplayer.serverLevel().getRaidAt(blockpos);
      if (raid != null) {
         raid.stop();
         p_180490_.sendSuccess(() -> {
            return Component.literal("Stopped raid");
         }, false);
         return 1;
      } else {
         p_180490_.sendFailure(Component.literal("No raid here"));
         return -1;
      }
   }
   
   private static int check(CommandSourceStack p_180494_) throws CommandSyntaxException {
      Raid raid = getRaid(p_180494_.getPlayerOrException());
      if (raid != null) {
         StringBuilder stringbuilder = new StringBuilder();
         stringbuilder.append("Found a started raid! ");
         p_180494_.sendSuccess(() -> {
            return Component.literal(stringbuilder.toString());
         }, false);
         StringBuilder stringbuilder1 = new StringBuilder();
         stringbuilder1.append("Num groups spawned: ");
         stringbuilder1.append(raid.getGroupsSpawned());
         stringbuilder1.append(" Bad omen level: ");
         stringbuilder1.append(raid.getBadOmenLevel());
         stringbuilder1.append(" Num mobs: ");
         stringbuilder1.append(raid.getTotalRaidersAlive());
         stringbuilder1.append(" Raid health: ");
         stringbuilder1.append(raid.getHealthOfLivingRaiders());
         stringbuilder1.append(" / ");
         stringbuilder1.append(raid.getTotalHealth());
         p_180494_.sendSuccess(() -> {
            return Component.literal(stringbuilder1.toString());
         }, false);
         return 1;
      } else {
         p_180494_.sendFailure(Component.literal("Found no started raids"));
         return 0;
      }
   }
   
   @Nullable
   private static Raid getRaid(ServerPlayer p_180467_) {
      return p_180467_.serverLevel().getRaidAt(p_180467_.blockPosition());
   }
   
   private static int resetChunks(CommandSourceStack p_183685_, int p_183686_, boolean p_183687_, @Nullable BlockPos blockPos) {
      ServerLevel serverlevel = p_183685_.getLevel();
      ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
      serverchunkcache.chunkMap.debugReloadGenerator();
      Vec3 vec3 = p_183685_.getPosition();
      ChunkPos chunkpos = new ChunkPos(blockPos != null ? blockPos : BlockPos.containing(vec3));
      int i = chunkpos.z - p_183686_;
      int j = chunkpos.z + p_183686_;
      int k = chunkpos.x - p_183686_;
      int l = chunkpos.x + p_183686_;
      
      for(int i1 = i; i1 <= j; ++i1) {
         for(int j1 = k; j1 <= l; ++j1) {
            ChunkPos chunkpos1 = new ChunkPos(j1, i1);
            LevelChunk levelchunk = serverchunkcache.getChunk(j1, i1, false);
            if (levelchunk != null && (!p_183687_ || !levelchunk.isOldNoiseGeneration())) {
               for(BlockPos blockpos : BlockPos.betweenClosed(chunkpos1.getMinBlockX(), serverlevel.getMinBuildHeight(), chunkpos1.getMinBlockZ(), chunkpos1.getMaxBlockX(), serverlevel.getMaxBuildHeight() - 1, chunkpos1.getMaxBlockZ())) {
                  serverlevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 16);
               }
            }
         }
      }
      
      ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
      long j3 = System.currentTimeMillis();
      int k3 = (p_183686_ * 2 + 1) * (p_183686_ * 2 + 1);
      
      for(ChunkStatus chunkstatus : ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.FEATURES, ChunkStatus.INITIALIZE_LIGHT)) {
         long k1 = System.currentTimeMillis();
         CompletableFuture<Unit> completablefuture = CompletableFuture.supplyAsync(() -> {
            return Unit.INSTANCE;
         }, processormailbox::tell);
         
         for(int i2 = chunkpos.z - p_183686_; i2 <= chunkpos.z + p_183686_; ++i2) {
            for(int j2 = chunkpos.x - p_183686_; j2 <= chunkpos.x + p_183686_; ++j2) {
               ChunkPos chunkpos2 = new ChunkPos(j2, i2);
               LevelChunk levelchunk1 = serverchunkcache.getChunk(j2, i2, false);
               if (levelchunk1 != null && (!p_183687_ || !levelchunk1.isOldNoiseGeneration())) {
                  List<ChunkAccess> list = Lists.newArrayList();
                  int k2 = Math.max(1, chunkstatus.getRange());
                  
                  for(int l2 = chunkpos2.z - k2; l2 <= chunkpos2.z + k2; ++l2) {
                     for(int i3 = chunkpos2.x - k2; i3 <= chunkpos2.x + k2; ++i3) {
                        ChunkAccess chunkaccess = serverchunkcache.getChunk(i3, l2, chunkstatus.getParent(), true);
                        ChunkAccess chunkaccess1;
                        if (chunkaccess instanceof ImposterProtoChunk) {
                           chunkaccess1 = new ImposterProtoChunk(((ImposterProtoChunk)chunkaccess).getWrapped(), true);
                        } else if (chunkaccess instanceof LevelChunk) {
                           chunkaccess1 = new ImposterProtoChunk((LevelChunk)chunkaccess, true);
                        } else {
                           chunkaccess1 = chunkaccess;
                        }
                        
                        list.add(chunkaccess1);
                     }
                  }
                  
                  completablefuture = completablefuture.thenComposeAsync((p_280957_) -> {
                     return chunkstatus.generate(processormailbox::tell, serverlevel, serverchunkcache.getGenerator(), serverlevel.getStructureManager(), serverchunkcache.getLightEngine(), (p_183691_) -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                     }, list).thenApply((p_183681_) -> {
                        if (chunkstatus == ChunkStatus.NOISE) {
                           p_183681_.left().ifPresent((p_183671_) -> {
                              Heightmap.primeHeightmaps(p_183671_, ChunkStatus.POST_FEATURES);
                           });
                        }
                        
                        return Unit.INSTANCE;
                     });
                  }, processormailbox::tell);
               }
            }
         }
         
         p_183685_.getServer().managedBlock(completablefuture::isDone);
         LOGGER.debug(chunkstatus + " took " + (System.currentTimeMillis() - k1) + " ms");
      }
      
      long l3 = System.currentTimeMillis();
      
      for(int i4 = chunkpos.z - p_183686_; i4 <= chunkpos.z + p_183686_; ++i4) {
         for(int l1 = chunkpos.x - p_183686_; l1 <= chunkpos.x + p_183686_; ++l1) {
            ChunkPos chunkpos3 = new ChunkPos(l1, i4);
            LevelChunk levelchunk2 = serverchunkcache.getChunk(l1, i4, false);
            if (levelchunk2 != null && (!p_183687_ || !levelchunk2.isOldNoiseGeneration())) {
               for(BlockPos blockpos1 : BlockPos.betweenClosed(chunkpos3.getMinBlockX(), serverlevel.getMinBuildHeight(), chunkpos3.getMinBlockZ(), chunkpos3.getMaxBlockX(), serverlevel.getMaxBuildHeight() - 1, chunkpos3.getMaxBlockZ())) {
                  serverchunkcache.blockChanged(blockpos1);
               }
            }
         }
      }
      
      LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - l3) + " ms");
      long j4 = System.currentTimeMillis() - j3;
      p_183685_.sendSuccess(() -> {
         return Component.literal(String.format(Locale.ROOT, "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", k3, j4, k3, (float)j4 / (float)k3));
      }, true);
      return 1;
   }
   
   public static Entity createEntity(CommandSourceStack p_270582_, Holder.Reference<EntityType<?>> p_270277_, Vec3 p_270366_, CompoundTag p_270197_, boolean p_270947_) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(p_270366_);
      if (!Level.isInSpawnableBounds(blockpos)) {
         throw INVALID_POSITION.create();
      } else {
         CompoundTag compoundtag = p_270197_.copy();
         compoundtag.putString("id", p_270277_.key().location().toString());
         ServerLevel serverlevel = p_270582_.getLevel();
         Entity entity = EntityType.loadEntityRecursive(compoundtag, serverlevel, (p_138828_) -> {
            p_138828_.moveTo(p_270366_.x, p_270366_.y, p_270366_.z, p_138828_.getYRot(), p_138828_.getXRot());
            return p_138828_;
         });
         if (entity == null) {
            throw ERROR_FAILED.create();
         } else {
            if (p_270947_ && entity instanceof Mob) {
               ((Mob)entity).finalizeSpawn(p_270582_.getLevel(), p_270582_.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, (SpawnGroupData)null, (CompoundTag)null);
            }
            
            if (!serverlevel.tryAddFreshEntityWithPassengers(entity)) {
               throw ERROR_DUPLICATE_UUID.create();
            } else {
               return entity;
            }
         }
      }
   }
   
   private static int spawnEntity(CommandSourceStack p_249752_, Holder.Reference<EntityType<?>> p_251948_, Vec3 p_251429_, CompoundTag p_250568_, boolean p_250229_) throws CommandSyntaxException {
      Entity entity = createEntity(p_249752_, p_251948_, p_251429_, p_250568_, p_250229_);
      p_249752_.sendSuccess(() -> {
         return Component.translatable("commands.summon.success", entity.getDisplayName());
      }, true);
      return 1;
   }
   
   public static Entity createCoralDefenders(CommandSourceStack p_270582_, Vec3 p_270366_, CompoundTag p_270197_, boolean p_270947_) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(p_270366_);
      if (!Level.isInSpawnableBounds(blockpos)) {
         throw INVALID_POSITION.create();
      } else {
         CompoundTag compoundtag = p_270197_.copy();
         compoundtag.putString("id", ModEntities.OCEAN_DEFENDER.getId().toString());
         ServerLevel serverlevel = p_270582_.getLevel();
         Entity entity = EntityType.loadEntityRecursive(compoundtag, serverlevel, (p_138828_) -> {
            p_138828_.moveTo(p_270366_.x, p_270366_.y, p_270366_.z, p_138828_.getYRot(), p_138828_.getXRot());
            return p_138828_;
         });
         if (entity == null) {
            throw ERROR_FAILED.create();
         } else {
            if (p_270947_ && entity instanceof Mob) {
               ((Mob)entity).finalizeSpawn(p_270582_.getLevel(), p_270582_.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.EVENT, (SpawnGroupData)null, (CompoundTag)null);
            }
            
            if (!serverlevel.tryAddFreshEntityWithPassengers(entity)) {
               throw ERROR_DUPLICATE_UUID.create();
            } else {
               if (entity instanceof OceanDefender defender) {
                  OceanDefender.spawnCoralDefenders(defender);
               }
               return entity;
            }
         }
      }
   }
   
   private static int spawnCoralDefenders(CommandSourceStack p_249752_, Vec3 p_251429_, CompoundTag p_250568_, boolean p_250229_) throws CommandSyntaxException {
      Entity entity = createCoralDefenders(p_249752_, p_251429_, p_250568_, p_250229_);
      p_249752_.sendSuccess(() -> {
         if (entity instanceof OceanDefender defender) {
            return Component.translatable("commands.summon.success", getCoralDefendersDisplayName(defender));
         }
         return Component.translatable("commands.summon.success", entity.getDisplayName());
      }, true);
      return 1;
   }
   
   public static Component getCoralDefendersDisplayName(OceanDefender defender) {
      return PlayerTeam.formatNameForTeam(defender.getTeam(), Component.translatable("entity.endless_deep_space.ocean_defenders")).withStyle((p_185975_) -> {
         return p_185975_.withHoverEvent(createCoralDefendersHoverEvent(defender)).withInsertion(defender.getStringUUID());
      });
   }
   
   protected static HoverEvent createCoralDefendersHoverEvent(OceanDefender defender) {
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(defender.getType(), defender.getUUID(), Component.translatable("entity.endless_deep_space.ocean_defenders")));
   }
   
   private static String getAsText(Tag p_265255_) throws CommandSyntaxException {
      if (p_265255_.getType().isValue()) {
         return p_265255_.getAsString();
      } else {
         throw ERROR_EXPECTED_VALUE.create(p_265255_);
      }
   }
   
   private static List<Tag> stringifyTagList(List<Tag> p_288980_, EndlessDeepSpaceCommand.StringProcessor p_289012_) throws CommandSyntaxException {
      List<Tag> list = new ArrayList<>(p_288980_.size());
      
      for(Tag tag : p_288980_) {
         String s = getAsText(tag);
         list.add(StringTag.valueOf(p_289012_.process(s)));
      }
      
      return list;
   }
   
   private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, EndlessDeepSpaceCommand.DataManipulatorDecorator> p_139404_) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("modify");
      
      for(EndlessDeepSpaceCommand.DataProvider EndlessDeepSpaceCommand$dataprovider : TARGET_PROVIDERS) {
         EndlessDeepSpaceCommand$dataprovider.wrap(literalargumentbuilder, (p_264816_) -> {
            ArgumentBuilder<CommandSourceStack, ?> argumentbuilder = Commands.argument("targetPath", NbtPathArgument.nbtPath());
            
            for(EndlessDeepSpaceCommand.DataProvider EndlessDeepSpaceCommand$dataprovider1 : SOURCE_PROVIDERS) {
               p_139404_.accept(argumentbuilder, (p_142807_) -> {
                  return EndlessDeepSpaceCommand$dataprovider1.wrap(Commands.literal("from"), (p_142812_) -> {
                     return p_142812_.executes((p_264829_) -> {
                        return manipulateData(p_264829_, EndlessDeepSpaceCommand$dataprovider, p_142807_, getSingletonSource(p_264829_, EndlessDeepSpaceCommand$dataprovider1));
                     }).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes((p_264842_) -> {
                        return manipulateData(p_264842_, EndlessDeepSpaceCommand$dataprovider, p_142807_, resolveSourcePath(p_264842_, EndlessDeepSpaceCommand$dataprovider1));
                     }));
                  });
               });
               p_139404_.accept(argumentbuilder, (p_264836_) -> {
                  return EndlessDeepSpaceCommand$dataprovider1.wrap(Commands.literal("string"), (p_287357_) -> {
                     return p_287357_.executes((p_288732_) -> {
                        return manipulateData(p_288732_, EndlessDeepSpaceCommand$dataprovider, p_264836_, stringifyTagList(getSingletonSource(p_288732_, EndlessDeepSpaceCommand$dataprovider1), (p_264813_) -> {
                           return p_264813_;
                        }));
                     }).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes((p_288737_) -> {
                        return manipulateData(p_288737_, EndlessDeepSpaceCommand$dataprovider, p_264836_, stringifyTagList(resolveSourcePath(p_288737_, EndlessDeepSpaceCommand$dataprovider1), (p_264821_) -> {
                           return p_264821_;
                        }));
                     }).then(Commands.argument("start", IntegerArgumentType.integer()).executes((p_288753_) -> {
                        return manipulateData(p_288753_, EndlessDeepSpaceCommand$dataprovider, p_264836_, stringifyTagList(resolveSourcePath(p_288753_, EndlessDeepSpaceCommand$dataprovider1), (p_287353_) -> {
                           return substring(p_287353_, IntegerArgumentType.getInteger(p_288753_, "start"));
                        }));
                     }).then(Commands.argument("end", IntegerArgumentType.integer()).executes((p_288749_) -> {
                        return manipulateData(p_288749_, EndlessDeepSpaceCommand$dataprovider, p_264836_, stringifyTagList(resolveSourcePath(p_288749_, EndlessDeepSpaceCommand$dataprovider1), (p_287359_) -> {
                           return substring(p_287359_, IntegerArgumentType.getInteger(p_288749_, "start"), IntegerArgumentType.getInteger(p_288749_, "end"));
                        }));
                     }))));
                  });
               });
            }
            
            p_139404_.accept(argumentbuilder, (p_142799_) -> {
               return Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag()).executes((p_142803_) -> {
                  List<Tag> list = Collections.singletonList(NbtTagArgument.getNbtTag(p_142803_, "value"));
                  return manipulateData(p_142803_, EndlessDeepSpaceCommand$dataprovider, p_142799_, list);
               }));
            });
            return p_264816_.then(argumentbuilder);
         });
      }
      
      return literalargumentbuilder;
   }
   
   private static String validatedSubstring(String p_288976_, int p_288968_, int p_289018_) throws CommandSyntaxException {
      if (p_288968_ >= 0 && p_289018_ <= p_288976_.length() && p_288968_ <= p_289018_) {
         return p_288976_.substring(p_288968_, p_289018_);
      } else {
         throw ERROR_INVALID_SUBSTRING.create(p_288968_, p_289018_);
      }
   }
   
   private static String substring(String p_287625_, int p_287772_, int p_287598_) throws CommandSyntaxException {
      int i = p_287625_.length();
      int j = getOffset(p_287772_, i);
      int k = getOffset(p_287598_, i);
      return validatedSubstring(p_287625_, j, k);
   }
   
   private static String substring(String p_287744_, int p_287741_) throws CommandSyntaxException {
      int i = p_287744_.length();
      return validatedSubstring(p_287744_, getOffset(p_287741_, i), i);
   }
   
   private static int getOffset(int p_287638_, int p_287600_) {
      return p_287638_ >= 0 ? p_287638_ : p_287600_ + p_287638_;
   }
   
   private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> p_265108_, EndlessDeepSpaceCommand.DataProvider p_265370_) throws CommandSyntaxException {
      DataAccessor dataaccessor = p_265370_.access(p_265108_);
      return Collections.singletonList(dataaccessor.getData());
   }
   
   private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> p_265468_, EndlessDeepSpaceCommand.DataProvider p_265670_) throws CommandSyntaxException {
      DataAccessor dataaccessor = p_265670_.access(p_265468_);
      NbtPathArgument.NbtPath nbtpathargument$nbtpath = NbtPathArgument.getPath(p_265468_, "sourcePath");
      return nbtpathargument$nbtpath.get(dataaccessor.getData());
   }
   
   private static int manipulateData(CommandContext<CommandSourceStack> p_139376_, EndlessDeepSpaceCommand.DataProvider p_139377_, EndlessDeepSpaceCommand.DataManipulator p_139378_, List<Tag> p_139379_) throws CommandSyntaxException {
      DataAccessor dataaccessor = p_139377_.access(p_139376_);
      NbtPathArgument.NbtPath nbtpathargument$nbtpath = NbtPathArgument.getPath(p_139376_, "targetPath");
      CompoundTag compoundtag = dataaccessor.getData();
      int i = p_139378_.modify(p_139376_, compoundtag, nbtpathargument$nbtpath, p_139379_);
      if (i == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         dataaccessor.setData(compoundtag);
         p_139376_.getSource().sendSuccess(() -> {
            return dataaccessor.getModifiedSuccess();
         }, true);
         return i;
      }
   }
   
   private static int removeData(CommandSourceStack p_139386_, DataAccessor p_139387_, NbtPathArgument.NbtPath p_139388_) throws CommandSyntaxException {
      CompoundTag compoundtag = p_139387_.getData();
      int i = p_139388_.remove(compoundtag);
      if (i == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         p_139387_.setData(compoundtag);
         p_139386_.sendSuccess(() -> {
            return p_139387_.getModifiedSuccess();
         }, true);
         return i;
      }
   }
   
   private static Tag getSingleTag(NbtPathArgument.NbtPath p_139399_, DataAccessor p_139400_) throws CommandSyntaxException {
      Collection<Tag> collection = p_139399_.get(p_139400_.getData());
      Iterator<Tag> iterator = collection.iterator();
      Tag tag = iterator.next();
      if (iterator.hasNext()) {
         throw ERROR_MULTIPLE_TAGS.create();
      } else {
         return tag;
      }
   }
   
   private static int getData(CommandSourceStack p_139444_, DataAccessor p_139445_, NbtPathArgument.NbtPath p_139446_) throws CommandSyntaxException {
      Tag tag = getSingleTag(p_139446_, p_139445_);
      int i;
      if (tag instanceof NumericTag) {
         i = Mth.floor(((NumericTag)tag).getAsDouble());
      } else if (tag instanceof CollectionTag) {
         i = ((CollectionTag)tag).size();
      } else if (tag instanceof CompoundTag) {
         i = ((CompoundTag)tag).size();
      } else {
         if (!(tag instanceof StringTag)) {
            throw ERROR_GET_NON_EXISTENT.create(p_139446_.toString());
         }
         
         i = tag.getAsString().length();
      }
      
      p_139444_.sendSuccess(() -> {
         return p_139445_.getPrintSuccess(tag);
      }, false);
      return i;
   }
   
   private static int getNumeric(CommandSourceStack p_139390_, DataAccessor p_139391_, NbtPathArgument.NbtPath p_139392_, double p_139393_) throws CommandSyntaxException {
      Tag tag = getSingleTag(p_139392_, p_139391_);
      if (!(tag instanceof NumericTag)) {
         throw ERROR_GET_NOT_NUMBER.create(p_139392_.toString());
      } else {
         int i = Mth.floor(((NumericTag)tag).getAsDouble() * p_139393_);
         p_139390_.sendSuccess(() -> {
            return p_139391_.getPrintSuccess(p_139392_, p_139393_, i);
         }, false);
         return i;
      }
   }
   
   private static int getData(CommandSourceStack p_139383_, DataAccessor p_139384_) throws CommandSyntaxException {
      CompoundTag compoundtag = p_139384_.getData();
      p_139383_.sendSuccess(() -> {
         return p_139384_.getPrintSuccess(compoundtag);
      }, false);
      return 1;
   }
   
   private static int mergeData(CommandSourceStack p_139395_, DataAccessor p_139396_, CompoundTag p_139397_) throws CommandSyntaxException {
      CompoundTag compoundtag = p_139396_.getData();
      if (NbtPathArgument.NbtPath.isTooDeep(p_139397_, 0)) {
         throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
      } else {
         CompoundTag compoundtag1 = compoundtag.copy().merge(p_139397_);
         if (compoundtag.equals(compoundtag1)) {
            throw ERROR_MERGE_UNCHANGED.create();
         } else {
            p_139396_.setData(compoundtag1);
            p_139395_.sendSuccess(() -> {
               return p_139396_.getModifiedSuccess();
            }, true);
            return 1;
         }
      }
   }
   
   @FunctionalInterface
   interface DataManipulator {
      int modify(CommandContext<CommandSourceStack> p_139496_, CompoundTag p_139497_, NbtPathArgument.NbtPath p_139498_, List<Tag> p_139499_) throws CommandSyntaxException;
   }
   
   @FunctionalInterface
   interface DataManipulatorDecorator {
      ArgumentBuilder<CommandSourceStack, ?> create(EndlessDeepSpaceCommand.DataManipulator p_139501_);
   }
   
   public interface DataProvider {
      DataAccessor access(CommandContext<CommandSourceStack> p_139504_) throws CommandSyntaxException;
      
      ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> p_139502_, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> p_139503_);
   }
   
   @FunctionalInterface
   interface StringProcessor {
      String process(String p_289006_) throws CommandSyntaxException;
   }
   
   private static int setVertigoTime(CommandSourceStack p_139395_, Collection<? extends Entity> p_250411_, int tick) {
      int size = p_250411_.size();
      for (Entity entity : p_250411_) {
         if (entity instanceof LivingEntity livingEntity) {
            SwordBlockEvent.setVertigoTime(livingEntity, tick);
         } else {
            p_139395_.sendFailure(Component.translatable("commands.enchant.failed.entity", entity.getName().getString()));
            size--;
         }
      }
      
      if (size == 1) {
         p_139395_.sendSuccess(() -> {
            return Component.translatable("commands.endless_deep_space.vertigo_time.success.single", p_250411_.iterator().next().getDisplayName(), tick);
         }, true);
      } else {
         int finalSize = size;
         p_139395_.sendSuccess(() -> {
            return Component.translatable("commands.endless_deep_space.vertigo_time.success.multiple", finalSize, tick);
         }, true);
      }
      
      return size;
   }
   
   private static int fillBlocks(CommandSourceStack p_180130_, Entity entity, BlockPos p_180131_, double speed) throws CommandSyntaxException {
      if (!(entity instanceof Mob mob)) {
         throw ERROR_NOT_MOB.create();
      } else {
         PathNavigation pathnavigation = mob.getNavigation();
         Path path = pathnavigation.createPath(p_180131_, 0);
         //DebugPackets.sendPathFindingPacket(p_180130_.getLevel(), mob, path, pathnavigation.getMaxDistanceToWaypoint());
         if (path == null) {
            throw ERROR_NO_PATH.create();
         } else if (!path.canReach()) {
            throw ERROR_NOT_COMPLETE.create();
         } else {
            mob.getNavigation().moveTo(path, speed);
            p_180130_.sendSuccess(() -> {
               return Component.literal("Made path");
            }, true);
            return 1;
         }
      }
   }
   
   private static int saveAll(CommandSourceStack p_138278_, boolean p_138279_) throws CommandSyntaxException {
      p_138278_.sendSuccess(() -> {
         return Component.translatable("commands.save.saving");
      }, false);
      MinecraftServer minecraftserver = p_138278_.getServer();
      boolean flag = minecraftserver.saveEverything(true, p_138279_, true);
      if (!flag) {
         throw SAVE_ERROR_FAILED.create();
      } else {
         p_138278_.sendSuccess(() -> {
            return Component.translatable("commands.save.success");
         }, true);
         return 1;
      }
   }
   
   private static int setWarningLevel(CommandSourceStack p_214783_, Collection<? extends Player> p_214784_, int p_214785_) {
      for(Player player : p_214784_) {
         player.getWardenSpawnTracker().ifPresent((p_248188_) -> {
            p_248188_.setWarningLevel(p_214785_);
         });
      }
      
      if (p_214784_.size() == 1) {
         p_214783_.sendSuccess(() -> {
            return Component.translatable("commands.warden_spawn_tracker.set.success.single", p_214784_.iterator().next().getDisplayName());
         }, true);
      } else {
         p_214783_.sendSuccess(() -> {
            return Component.translatable("commands.warden_spawn_tracker.set.success.multiple", p_214784_.size());
         }, true);
      }
      
      return p_214784_.size();
   }
   
   private static int resetTracker(CommandSourceStack p_214780_, Collection<? extends Player> p_214781_) {
      for(Player player : p_214781_) {
         player.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
      }
      
      if (p_214781_.size() == 1) {
         p_214780_.sendSuccess(() -> {
            return Component.translatable("commands.warden_spawn_tracker.clear.success.single", p_214781_.iterator().next().getDisplayName());
         }, true);
      } else {
         p_214780_.sendSuccess(() -> {
            return Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", p_214781_.size());
         }, true);
      }
      
      return p_214781_.size();
   }
   
   private static int getChunkLoader(CommandSourceStack commandSourceStack, int loaderType, @Nullable UUID uuid) throws CommandSyntaxException {
      ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(commandSourceStack.getLevel());
      switch (loaderType) {
         case 1:
            int sizeBossPos;
            
            if (uuid != null) {
               sizeBossPos = 1;
               BlockPos blockPos = worldData.getBossPos(uuid);
               if (blockPos != null) {
                  commandSourceStack.sendSuccess(() -> {
                     return Component.translatable("commands.endless_deep_space.chunk_loader.success", getChunkLoaderOutput(blockPos, uuid, commandSourceStack.getLevel().dimension()), commandSourceStack.getLevel().dimension().location().toString());
                  }, true);
               } else {
                  throw ERROR_NO_UUID.create(uuid);
               }
            } else {
               sizeBossPos = worldData.bossPos.size();
               for (Map.Entry<UUID, BlockPos> entry : worldData.bossPos.entrySet()) {
                  commandSourceStack.sendSuccess(() -> {
                     return Component.translatable("commands.endless_deep_space.chunk_loader.success", getChunkLoaderOutput(entry, commandSourceStack.getLevel().dimension()), commandSourceStack.getLevel().dimension().location().toString());
                  }, true);
               }
            }
            
            commandSourceStack.sendSuccess(() -> {
               return Component.translatable("commands.endless_deep_space.chunk_loader.success.boss_pos", sizeBossPos, commandSourceStack.getLevel().dimension().location().toString());
            }, true);
            return sizeBossPos;
         default:
            int sizeGuidingStonePos;
            
            if (uuid != null) {
               sizeGuidingStonePos = 1;
               BlockPos blockPos = worldData.getGuidingStonePos(uuid);
               if (blockPos != null) {
                  commandSourceStack.sendSuccess(() -> {
                     return Component.translatable("commands.endless_deep_space.chunk_loader.success", getChunkLoaderOutput(blockPos, uuid, commandSourceStack.getLevel().dimension()), commandSourceStack.getLevel().dimension().location().toString());
                  }, true);
               } else {
                  throw ERROR_NO_UUID.create(uuid);
               }
            } else {
               sizeGuidingStonePos = worldData.guidingStonePos.size();
               for (Map.Entry<UUID, BlockPos> entry : worldData.guidingStonePos.entrySet()) {
                  commandSourceStack.sendSuccess(() -> {
                     return Component.translatable("commands.endless_deep_space.chunk_loader.success", getChunkLoaderOutput(entry, commandSourceStack.getLevel().dimension()), commandSourceStack.getLevel().dimension().location().toString());
                  }, true);
               }
            }
            
            commandSourceStack.sendSuccess(() -> {
               return Component.translatable("commands.endless_deep_space.chunk_loader.success.guiding_stone_pos", sizeGuidingStonePos, commandSourceStack.getLevel().dimension().location().toString());
            }, true);
            return sizeGuidingStonePos;
      }
   }
   
   public static Component getChunkLoaderOutput(Map.Entry<UUID, BlockPos> entry, ResourceKey<Level> dimension) {
      return ComponentUtils.wrapInSquareBrackets(Component.literal(String.valueOf(entry.getKey())).withStyle((p_258207_) -> {
         return p_258207_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + dimension.location() + " run tp @s " + entry.getValue().getX() + " " + entry.getValue().getY() + " " + entry.getValue().getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(Component.translatable("chat.coordinates.tooltip").getString() + " : " + entry.getValue()))).withInsertion(String.valueOf(entry.getKey()));
      }));
   }
   
   public static Component getChunkLoaderOutput(BlockPos blockPos, UUID uuid, ResourceKey<Level> dimension) {
      return ComponentUtils.wrapInSquareBrackets(Component.literal(String.valueOf(uuid)).withStyle((p_258207_) -> {
         return p_258207_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + dimension.location() + " run tp @s " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(Component.translatable("chat.coordinates.tooltip").getString() + " : " + blockPos))).withInsertion(String.valueOf(uuid));
      }));
   }
}
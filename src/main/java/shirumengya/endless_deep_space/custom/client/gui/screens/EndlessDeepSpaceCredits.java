package shirumengya.endless_deep_space.custom.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.WheelRenderer;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.client.renderer.entity.EnderLordRenderer;
import shirumengya.endless_deep_space.mixins.GuiGraphicsInvoker;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EndlessDeepSpaceCredits extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
   private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
   private static final String NAME_PREFIX = "           ";
   private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
   private static final float SPEEDUP_FACTOR = 5.0F;
   private static final float SPEEDUP_FACTOR_FAST = 15.0F;
   public final Chapters chapter;
   private final Runnable onFinished;
   private float scroll;
   private List<FormattedCharSequence> lines;
   private IntSet centeredLines;
   private int totalScrollLength;
   private boolean speedupActive;
   private final IntSet speedupModifiers = new IntOpenHashSet();
   private float scrollSpeed;
   private final float unmodifiedScrollSpeed;
   private int direction;
   private final LogoRenderer logoRenderer = new LogoRenderer(false);
   public static final WheelRenderer wheel = new WheelRenderer();
   public static boolean showProgress = false;

   public EndlessDeepSpaceCredits(Chapters chapters, float scrollSpeed, Runnable p_276294_) {
      super(GameNarrator.NO_TITLE);
      this.chapter = chapters;
      this.onFinished = p_276294_;
      this.unmodifiedScrollSpeed = scrollSpeed;

      this.direction = 1;
      this.scrollSpeed = this.unmodifiedScrollSpeed;
   }

   private float calculateScrollSpeed() {
      return this.speedupActive ? this.unmodifiedScrollSpeed * (5.0F + (float)this.speedupModifiers.size() * 15.0F) * (float)this.direction : this.unmodifiedScrollSpeed * (float)this.direction;
   }

   public void tick() {
      this.minecraft.getMusicManager().tick();
      this.minecraft.getSoundManager().tick(false);
      //GLFW.glfwSetInputMode(this.minecraft.getWindow().getWindow(), 208897, InputConstants.CURSOR_DISABLED);
      float f = (float)(this.totalScrollLength + this.height + this.height + 24);
      if (this.scroll > f) {
         this.respawn();
      }

   }

   private static void setShowProgress() {
      showProgress = !showProgress;
   }

   public boolean keyPressed(int p_169469_, int p_169470_, int p_169471_) {
      if (p_169469_ == 265) {
         this.direction = -1;
      } else if (p_169469_ != 341 && p_169469_ != 345) {
         if (p_169469_ == 32) {
            this.speedupActive = true;
         }
      } else {
         this.speedupModifiers.add(p_169469_);
      }

      if (p_169469_ == 292) {
         setShowProgress();
      }

      this.scrollSpeed = this.calculateScrollSpeed();
      return super.keyPressed(p_169469_, p_169470_, p_169471_);
   }

   public boolean keyReleased(int p_169476_, int p_169477_, int p_169478_) {
      if (p_169476_ == 265) {
         this.direction = 1;
      }

      if (p_169476_ == 32) {
         this.speedupActive = false;
      } else if (p_169476_ == 341 || p_169476_ == 345) {
         this.speedupModifiers.remove(p_169476_);
      }

      this.scrollSpeed = this.calculateScrollSpeed();
      return super.keyReleased(p_169476_, p_169477_, p_169478_);
   }

   public void onClose() {
      this.respawn();
   }

   private void respawn() {
      //GLFW.glfwSetInputMode(this.minecraft.getWindow().getWindow(), 208897, InputConstants.CURSOR_NORMAL);
      this.onFinished.run();
   }

   protected void init() {
      if (this.lines == null) {
         this.lines = Lists.newArrayList();
         this.centeredLines = new IntOpenHashSet();
         if (!this.chapter.getPoem().isEmpty()) {
            for (String poem : this.chapter.getPoem()) {
               this.wrapCreditsIO(poem, this::addPoemFile);
            }
         }

         if (!this.chapter.getCredits().isEmpty()) {
            for (String credits : this.chapter.getCredits()) {
               this.wrapCreditsIO(credits, this::addCreditsFile);
            }
         }

         if (!this.chapter.getPostcredits().isEmpty()) {
            for (String postcredits : this.chapter.getPostcredits()) {
               this.wrapCreditsIO(postcredits, this::addPoemFile);
            }
         }

         this.totalScrollLength = this.lines.size() * 12;
      }
   }

   private void wrapCreditsIO(String p_197399_, CreditsReader p_197400_) {
      try (Reader reader = this.minecraft.getResourceManager().openAsReader(new ResourceLocation(p_197399_))) {
         p_197400_.read(reader);
      } catch (Exception exception) {
         LOGGER.error("Couldn't load credits", (Throwable)exception);
      }

   }

   private void addPoemFile(Reader p_232818_) throws IOException {
      BufferedReader bufferedreader = new BufferedReader(p_232818_);
      RandomSource randomsource = RandomSource.create(8124371L);

      String s;
      while((s = bufferedreader.readLine()) != null) {
         int i;
         String s1;
         String s2;
         for(s = s.replaceAll("PLAYERNAME", this.minecraft.getUser().getName()); (i = s.indexOf(OBFUSCATE_TOKEN)) != -1; s = s1 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, randomsource.nextInt(4) + 3) + s2) {
            s1 = s.substring(0, i);
            s2 = s.substring(i + OBFUSCATE_TOKEN.length());
         }

         this.addPoemLines(s);
         this.addEmptyLine();
      }

      for(int j = 0; j < 8; ++j) {
         this.addEmptyLine();
      }

   }

   private void addCreditsFile(Reader p_232820_) {
      for(JsonElement jsonelement : GsonHelper.parseArray(p_232820_)) {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         String s = jsonobject.get("section").getAsString();
         this.addCreditsLine(SECTION_HEADING, true);
         this.addCreditsLine(Component.literal(s).withStyle(ChatFormatting.YELLOW), true);
         this.addCreditsLine(SECTION_HEADING, true);
         this.addEmptyLine();
         this.addEmptyLine();

         for(JsonElement jsonelement1 : jsonobject.getAsJsonArray("disciplines")) {
            JsonObject jsonobject1 = jsonelement1.getAsJsonObject();
            String s1 = jsonobject1.get("discipline").getAsString();
            if (StringUtils.isNotEmpty(s1)) {
               this.addCreditsLine(Component.literal(s1).withStyle(ChatFormatting.YELLOW), true);
               this.addEmptyLine();
               this.addEmptyLine();
            }

            for(JsonElement jsonelement2 : jsonobject1.getAsJsonArray("titles")) {
               JsonObject jsonobject2 = jsonelement2.getAsJsonObject();
               String s2 = jsonobject2.get("title").getAsString();
               JsonArray jsonarray = jsonobject2.getAsJsonArray("names");
               this.addCreditsLine(Component.literal(s2).withStyle(ChatFormatting.GRAY), false);

               for(JsonElement jsonelement3 : jsonarray) {
                  String s3 = jsonelement3.getAsString();
                  this.addCreditsLine(Component.literal("           ").append(s3).withStyle(ChatFormatting.WHITE), false);
               }

               this.addEmptyLine();
               this.addEmptyLine();
            }
         }
      }

   }

   private void addEmptyLine() {
      this.lines.add(FormattedCharSequence.EMPTY);
   }

   private void addPoemLines(String p_181398_) {
      this.lines.addAll(this.minecraft.font.split(Component.literal(p_181398_), 256));
   }

   private void addCreditsLine(Component p_169473_, boolean p_169474_) {
      if (p_169474_) {
         this.centeredLines.add(this.lines.size());
      }

      this.lines.add(p_169473_.getVisualOrderText());
   }

   public static void fillRenderType(GuiGraphics guiGraphics, RenderType p_331805_, int p_330261_, int p_330693_, int p_331143_, int p_331708_, int p_330497_) {
      Matrix4f matrix4f = guiGraphics.pose().last().pose();
      VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(p_331805_);
      vertexconsumer.vertex(matrix4f, (float)p_330261_, (float)p_330693_, (float)p_330497_).endVertex();
      vertexconsumer.vertex(matrix4f, (float)p_330261_, (float)p_331708_, (float)p_330497_).endVertex();
      vertexconsumer.vertex(matrix4f, (float)p_331143_, (float)p_331708_, (float)p_330497_).endVertex();
      vertexconsumer.vertex(matrix4f, (float)p_331143_, (float)p_330693_, (float)p_330497_).endVertex();
      ((GuiGraphicsInvoker)guiGraphics).invokerFlushIfUnmanaged();
   }

   private void renderBg(GuiGraphics p_282239_) {
      if (this.chapter.getRenderType() == null) {
         int i = this.width;
         float f = this.scroll * 0.5F;
         int j = 64;
         float f1 = this.scroll / this.unmodifiedScrollSpeed;
         float f2 = f1 * 0.02F;
         float f3 = (float) (this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
         float f4 = (f3 - 20.0F - f1) * 0.005F;
         if (f4 < f2) {
            f2 = f4;
         }

         if (f2 > 1.0F) {
            f2 = 1.0F;
         }

         f2 *= f2;
         f2 = f2 * 96.0F / 255.0F;
         p_282239_.setColor(f2, f2, f2, 1.0F);
         p_282239_.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, f, i, this.height, 64, 64);
         p_282239_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         fillRenderType(p_282239_, this.chapter.getRenderType(), 0, 0, this.width, this.height, 0);
      }
   }

   public void render(GuiGraphics p_281907_, int p_282364_, int p_282696_, float p_281316_) {
      this.scroll = Math.max(0.0F, this.scroll + p_281316_ * this.scrollSpeed);
      this.renderBg(p_281907_);
      int i = this.width / 2 - 128;
      int j = this.height + 50;
      float f = -this.scroll;
      p_281907_.pose().pushPose();
      p_281907_.pose().translate(0.0F, f, 0.0F);
      this.logoRenderer.renderLogo(p_281907_, this.width, 1.0F, j);
      int k = j + 100;

      for(int l = 0; l < this.lines.size(); ++l) {
         if (l == this.lines.size() - 1) {
            float f1 = (float)k + f - (float)(this.height / 2 - 6);
            if (f1 < 0.0F) {
               p_281907_.pose().translate(0.0F, -f1, 0.0F);
            }
         }

         if ((float)k + f + 12.0F + 8.0F > 0.0F && (float)k + f < (float)this.height) {
            FormattedCharSequence formattedcharsequence = this.lines.get(l);
            if (this.centeredLines.contains(l)) {
               p_281907_.drawCenteredString(this.font, formattedcharsequence, i + 128, k, 16777215);
            } else {
               p_281907_.drawString(this.font, formattedcharsequence, i, k, 16777215);
            }
         }

         k += 12;
      }

      p_281907_.pose().popPose();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
      p_281907_.blit(VIGNETTE_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      super.render(p_281907_, p_282364_, p_282696_, p_281316_);

      float endScroll = (float)(this.totalScrollLength + this.height + this.height + 24);
      if (showProgress) {
         wheel.renderWheel(p_281907_, this.width - (this.width - 12), this.height - 12, 0, 10, false, WheelRenderer.WheelLevel.SECOND, 0, 1, Color.of(62, 69, 86));
         wheel.renderWheel(p_281907_, this.width - (this.width - 12), this.height - 12, 0, 10, false, WheelRenderer.WheelLevel.SECOND, 1.0D - ((double)this.scroll / (double)endScroll), 1, Color.of(146, 146, 146));
         if (p_282364_ <= (this.width - (this.width - 12)) + 10 && p_282364_ >= (this.width - (this.width - 12)) - 10 && p_282696_ <= this.height - 12 + 10 && p_282696_ >= this.height - 12 - 10) {
            List<FormattedCharSequence> list = new ArrayList<>(List.of(Component.literal(this.scroll + "/" + endScroll).getVisualOrderText()));
            list.add(Component.literal((this.scroll / endScroll) * 100 + "%").getVisualOrderText());
            p_281907_.renderTooltip(this.minecraft.font, list, p_282364_, p_282696_);
         }
      }
   }

   public Music getBackgroundMusic() {
      return new Music(ForgeRegistries.SOUND_EVENTS.getHolder(this.chapter.getMusic()).get(), 0, 0, true);
   }

   public boolean shouldCloseOnEsc() {
      return this.chapter.shouldCloseOnEsc();
   }

   public boolean isPauseScreen() {
      return this.chapter.isPauseScreen();
   }

   public enum Chapters {
      MINECRAFT_POEM(List.of("texts/end.txt"), Arrays.asList("endless_deep_space:texts/endless_deep_space.credits.json", "texts/credits.json"), List.of("texts/postcredits.txt"), SoundEvents.MUSIC_CREDITS.get(), ModRenderType.EndPortal(EnderLordRenderer.END_SKY_LOCATION, EnderLordRenderer.END_PORTAL_LOCATION, false), true, true),
      MINECRAFT_CREDITS(Lists.newArrayList(), Arrays.asList("endless_deep_space:texts/endless_deep_space.credits.json", "texts/credits.json"), List.of("texts/postcredits.txt"), SoundEvents.MUSIC_CREDITS.get(), ModRenderType.EndGateway(EnderLordRenderer.END_SKY_LOCATION, EnderLordRenderer.END_PORTAL_LOCATION, false), true, true),
      ENDLESS_DEEP_SPACE_CREDITS(List.of("endless_deep_space:texts/endless_deep_space.end.txt"), Arrays.asList("endless_deep_space:texts/endless_deep_space.credits.json", "texts/credits.json"), List.of("texts/postcredits.txt"), SoundEvents.MUSIC_CREDITS.get(), null, true, true),
      MINECRAFT_END_POEM(List.of("texts/end.txt"), List.of("endless_deep_space:texts/endless_deep_space.credits.json"), List.of("texts/postcredits.txt"), SoundEvents.MUSIC_CREDITS.get(), ModRenderType.EndPortal(EnderLordRenderer.END_SKY_LOCATION, EnderLordRenderer.END_PORTAL_LOCATION, false), true, true);

      private final List<String> poem;
      private final List<String> credits;
      private final List<String> postcredits;
      private final SoundEvent music;
      private final boolean shouldCloseOnEsc;
      private final boolean isPauseScreen;
      @Nullable
      private final RenderType renderType;

      Chapters(List<String> poem, List<String> credits, List<String> postcredits, SoundEvent music, @Nullable RenderType renderType, boolean shouldCloseOnEsc, boolean isPauseScreen) {
         this.poem = poem;
         this.credits = credits;
         this.postcredits = postcredits;
         this.music = music;
         this.renderType = renderType;
         this.shouldCloseOnEsc = shouldCloseOnEsc;
         this.isPauseScreen = isPauseScreen;
      }

      public List<String> getPoem() {
         return this.poem;
      }

      public List<String> getCredits() {
         return this.credits;
      }

      public List<String> getPostcredits() {
         return this.postcredits;
      }

      public SoundEvent getMusic() {
         return this.music;
      }

      @Nullable
      public RenderType getRenderType() {
         return this.renderType;
      }

      public boolean shouldCloseOnEsc() {
         return this.shouldCloseOnEsc;
      }

      public boolean isPauseScreen() {
         return this.isPauseScreen;
      }
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   interface CreditsReader {
      void read(Reader p_232822_) throws IOException;
   }
}
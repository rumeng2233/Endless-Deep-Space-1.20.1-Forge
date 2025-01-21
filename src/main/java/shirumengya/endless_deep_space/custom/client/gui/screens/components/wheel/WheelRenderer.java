package shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

import javax.annotation.Nullable;
import java.util.*;

import static java.lang.Math.PI;

public class WheelRenderer {
	private final Map<WheelLevel, Wheel> wheel = new EnumMap<>(WheelLevel.class);

	public void renderWheel(GuiGraphics matrixStack, double x, double y, double z, double radius, boolean debug, WheelLevel wheelLevel, double start, double end, int red, int green, int blue) {
		this.renderWheel(matrixStack, x, y, z, radius, debug, wheelLevel, start, end, red, green, blue, 255);
	}

	public void renderWheel(GuiGraphics matrixStack, double x, double y, double z, double radius, boolean debug, WheelLevel wheelLevel, double start, double end, int red, int green, int blue, int alpha) {
		this.renderWheel(matrixStack, x, y, z, radius, debug, wheelLevel, start, end, Color.of(red, green, blue, alpha));
	}

	public void renderWheel(GuiGraphics matrixStack, double x, double y, double z, double radius, boolean debug, WheelLevel wheelLevel, double start, double end, Color color) {
		makeWheel(wheelLevel, start, end, color);
		render(matrixStack, x, y, z, radius, debug);
	}

	public void makeWheel(WheelLevel wheelLevel, double start, double end, Color color) {
		addWheel(wheelLevel, start, end, color);
	}

	@Nullable
	public Wheel getWheel(WheelLevel type) {
		return wheel.get(type);
	}

	public void addWheel(WheelLevel wheelLevel, double start, double end, Color color) {
		start = Math.max(0, start);
		end = Math.min(1, end);
		if (start >= end)
			return;
		Wheel wheel = this.wheel.get(wheelLevel);
		this.wheel.put(wheelLevel, wheel != null ? wheel.insert(new Wheel(start, end, color)) : new Wheel(start, end, color));
	}

	public void render(GuiGraphics stack, double x, double y, double z, double radius, boolean debug) {
		RenderSystem.disableDepthTest();
		if (debug) {
			float linePos = 10;
			Font font = Minecraft.getInstance().font;
			for (WheelLevel t : WheelLevel.values()) {
				Wheel wheel = getWheel(t);
				if (wheel != null) {
					linePos = stack.drawString(font, t + ":", 20, linePos, 0xFFFFFFFF, true);
					linePos = stack.drawString(font, wheel.toString(), 30, linePos, 0xFFFFFFFF, true);
				}
			}
		}

		for (WheelLevel t : WheelLevel.values()) {
			Wheel wheel = getWheel(t);
			if (wheel != null) {
				RenderSystem.setShaderTexture(0, t.texture);
				wheel.draw(stack, x, y, z, radius, debug);
			}
		}

		wheel.clear();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	public static final class Wheel {
		private double start;
		private double end;
		private final Color color;

		@Nullable
		private Wheel next;

		private Wheel(double start, double end, Color color) {
			this.start = start;
			this.end = end;
			this.color = Objects.requireNonNull(color);
		}

		public Wheel insert(Wheel wheel) {
			return insert(wheel, true);
		}

		private Wheel insert(Wheel wheel, boolean overwrite) {
			if (wheel.start <= this.start) {
				if (wheel.end >= this.end) {
					return this.next != null ? wheel.insert(this.next, false) : wheel;
				} else {
					this.start = wheel.end;
					wheel.next = this;
					return wheel;
				}
			} else {
				if(overwrite && wheel.start < this.end) {
					this.end = wheel.start;
				}
				next = next != null ? next.insert(wheel, overwrite) : wheel;
				return this;
			}
		}

		private static final double[] renderPoints = {0, 1/8.0, 3/8.0, 5/8.0, 7/8.0, 1};

		public void draw(GuiGraphics stack, double x, double y, double z, double radius, boolean debug) {
			List<Vec2> debugVertices = debug ? new ArrayList<>() : null;
			drawInternal(x, y, z, radius, debugVertices);

			if(debugVertices!=null) {
				stack.pose().pushPose();
				stack.pose().translate(x, y, z);
				Font font = Minecraft.getInstance().font;
				for(Vec2 vec : debugVertices) {
					String s = vec.x+" "+vec.y;
					stack.drawString(font, s,
							vec.x>0 ? vec.x*(float)radius+2 : vec.x*(float)radius-2-font.width(s),
							vec.y>0 ? vec.y*(float)-radius-2-font.lineHeight : vec.y*(float)-radius+2,
							0xFF00FF00,
							true);
				}
				stack.pose().popPose();
			}
		}

		private void drawInternal(double x, double y, double z, double radius, @Nullable List<Vec2> debugVertices) {
			RenderSystem.setShaderColor(color.red, color.green, color.blue, color.alpha);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder b = tesselator.getBuilder();
			b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
			b.vertex(x, y, z).uv(0.5f, 0.5f).endVertex();
			for(int i = 0;i < renderPoints.length-1;i++){
				double currentStart = renderPoints[i];
				if (currentStart >= end) {
					break;
				}
				double currentEnd = renderPoints[i+1];
				if (currentEnd <= start) {
					continue;
				}

				if(currentStart <= start) {
					vert(b, x, y, z, start, radius, debugVertices);
				}
				if(currentEnd >= end) {
					break;
				}
				vert(b, x, y, z, currentEnd, radius, debugVertices);
			}
			vert(b, x, y, z, end, radius, debugVertices);
			tesselator.end();
			if(next != null) {
				next.drawInternal(x, y, z, radius, debugVertices);
			}
		}

		private void vert(BufferBuilder b, double x, double y, double z, double point, double radius, @Nullable List<Vec2> debugVertices) {
			double vx, vy;
			if(point == 0 || point == 1) {
				vx = 0;
				vy = 1;
			} else if(point == 1 / 8.0) {
				vx = -1;
				vy = 1;
			} else if(point == 3 / 8.0) {
				vx = -1;
				vy = -1;
			} else if(point == 5 / 8.0) {
				vx = 1;
				vy = -1;
			} else if(point == 7 / 8.0) {
				vx = 1;
				vy = 1;
			} else if(point < 1 / 8.0 || point > 7 / 8.0) {
				vx = -Math.tan(point*(2*PI));
				vy = 1;
			} else if(point < 3 / 8.0) {
				vx = -1;
				vy = 1 / Math.tan(point * (2 * PI));
			} else if(point < 5 / 8.0) {
				vx = Math.tan(point * (2 * PI));
				vy = -1;
			} else {
				vx = 1;
				vy = -1 / Math.tan(point * (2 * PI));
			}
			b.vertex(x + vx * radius, y + vy * -radius, z).uv((float)(vx / 2 + 0.5), (float)(vy / 2 + 0.5)).endVertex();
			if (debugVertices != null) {
				debugVertices.add(new Vec2((float)vx, (float)vy));
			}
		}

		@Override
		public String toString() {
			return next != null ?
					String.format("[%f ~ %f](#%s) -> \n%s", start, end, color, next) :
					String.format("[%f ~ %f](#%s)", start, end, color);
		}
	}

	public enum WheelLevel {
		FIRST(new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/wheel/first.png")),
		SECOND(new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/wheel/second.png")),
		THIRD(new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/wheel/third.png")),
		FULL(new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/wheel/full.png"));

		public final ResourceLocation texture;

		WheelLevel(ResourceLocation texture) {
			this.texture = Objects.requireNonNull(texture);
		}

		private int start() {
			return 1000 * ordinal();
		}
		private int end() {
			return 1000 * (1 + ordinal());
		}

		public double getProportion(int value) {
			int start = start();
			if (start >= value) {
				return 0;
			}
			int end = end();
			if (end <= value) {
				return 1;
			}
			return (double)(value - start) / (end - start);
		}
	}
}

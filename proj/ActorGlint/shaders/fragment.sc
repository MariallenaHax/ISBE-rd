$input v_color0, v_fog, v_light, v_texcoord0, v_layerUv

#include <bgfx_shader.sh>
#include <utils/ActorUtil.h>
#include <utils/FogUtil.h>
#include <utils/GlintUtil.h>

uniform vec4 ColorBased;
uniform vec4 ChangeColor;
uniform vec4 UseAlphaRewrite;
uniform vec4 TintedAlphaTestEnabled;
uniform vec4 MatColor;
uniform vec4 OverlayColor;
uniform vec4 TileLightColor;
uniform vec4 MultiplicativeTintColor;
uniform vec4 FogColor;
uniform vec4 FogControl;
uniform vec4 ActorFPEpsilon;
uniform vec4 LightDiffuseColorAndIlluminance;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];
uniform vec4 UVScale;
uniform vec4 GlintColor;

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_MatTexture1);
vec3 film(vec3 x){
	 float a = 3.15;
	 float b = 0.01;
	 float c = 2.43;
	 float d = 0.59;
	 float e = 0.14;
	return clamp((x*(a*x+b))/(x*(c*x+d)+e),0.06,1.0);
}
void main() {
#if DEPTH_ONLY_PASS
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
#elif DEPTH_ONLY_OPAQUE_PASS
    gl_FragColor = vec4(applyFog(vec3(1.0, 1.0, 1.0), v_fog.rgb, v_fog.a), 1.0);
    return;
#else

    vec4 albedo = getActorAlbedoNoColorChange(v_texcoord0, s_MatTexture, s_MatTexture1, MatColor);

#if ALPHA_TEST_PASS
    float alpha = mix(albedo.a, (albedo.a * OverlayColor.a), TintedAlphaTestEnabled.x);
    if(shouldDiscard(albedo.rgb, alpha, ActorFPEpsilon.x)) {
        discard;
    }
#endif // ALPHA_TEST

#if CHANGE_COLOR__MULTI
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
#elif CHANGE_COLOR__ON
    albedo = applyColorChange(albedo, ChangeColor, albedo.a);
    albedo.a *= ChangeColor.a;
#endif // CHANGE_COLOR_MULTI

#if ALPHA_TEST_PASS
    albedo.a = max(UseAlphaRewrite.r, albedo.a);
#endif

    albedo = applyActorDiffuse(albedo, v_color0.rgb, v_light, ColorBased.x, OverlayColor);
    albedo = applyGlint(albedo, v_layerUv, s_MatTexture1, GlintColor, TileLightColor);

#if TRANSPARENT_PASS
    albedo = applyHudOpacity(albedo, HudOpacity.x);
#endif

    albedo.rgb = applyFog(albedo.rgb, v_fog.rgb, v_fog.a);
    albedo.rgb = film(albedo.rgb);
    gl_FragColor = albedo;
#endif // DEPTH_ONLY
}
$input v_color0, v_fog, v_light, v_texcoord0, v_worldPos, v_clipPosition

uniform vec4 UseAlphaRewrite;

#include <bgfx_shader.sh>

#include <utils/ActorUtil.h>
#include <utils/FogUtil.h>

uniform vec4 ChangeColor;
uniform vec4 OverlayColor;
uniform vec4 ActorFPEpsilon;
uniform vec4 ColorBased;
uniform vec4 DitherParams2[3];
uniform vec4 DitherParams;
uniform vec4 DitheringEnabledToggle;
uniform vec4 HudOpacity;
uniform vec4 MatColor;
uniform vec4 TintedAlphaTestEnabled;
uniform vec4 MultiplicativeTintColor;

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
#ifdef DEPTH_ONLY_PASS
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
#else
#ifdef DEPTH_ONLY_OPAQUE_PASS
    gl_FragColor = vec4(applyFog(vec3(1.0, 1.0, 1.0), v_fog.rgb, v_fog.a), 1.0);
    return;
#else

    vec4 albedo = getActorAlbedoNoColorChange(v_texcoord0, s_MatTexture, s_MatTexture1, MatColor);
#if ALPHA_TEST_PASS
    float alpha = lerp(albedo.a, (albedo.a * OverlayColor.a), TintedAlphaTestEnabled.x);
    bool dither;
    if (DitheringEnabledToggle.x != 0.0)
    {
        vec2 _1306 = floor(((((v_clipPosition.xyz / vec3(v_clipPosition.w,v_clipPosition.w,v_clipPosition.w)).xy * 0.5) + vec2(0.5,0.5)) * DitherParams.xy) / vec2(DitherParams2[0].z,DitherParams2[0].z)) * DitherParams2[0].z;
        vec2 _1308 = floor(_1306 * 0.25);
        vec2 _1309 = floor(_1306 * 0.5);
        vec2 _1310 = floor(_1306);
        dither = smoothstep(DitherParams2[0].x, DitherParams2[0].y, dot(-normalize(u_view[2].xyz), v_worldPos - (vec4(0.0, 0.0, 0.0, 1.0) * u_invView[0]).xyz)) <= (((((((fract((_1308.x * 0.5) + ((_1308.y * _1308.y) * 0.75)) * 0.25) + fract((_1309.x * 0.5) + ((_1309.y * _1309.y) * 0.75))) * 0.25) + fract((_1310.x * 0.5) + ((_1310.y * _1310.y) * 0.75))) * 64.0) + 0.5) * 0.015625);
    }
    else
    {
        dither = false;
    }
    if(dither || shouldDiscard(albedo.rgb, alpha, ActorFPEpsilon.x)) {
        discard;
    }
#endif // ALPHA_TEST

#ifdef CHANGE_COLOR__MULTI
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
#else
#ifdef CHANGE_COLOR__ON
    albedo = applyColorChange(albedo, ChangeColor, albedo.a);
    albedo.a *= ChangeColor.a;
#endif
#endif

#ifdef ALPHA_TEST_PASS
    albedo.a = max(UseAlphaRewrite.r, albedo.a);
#endif
    albedo = applyActorDiffuse(albedo, v_color0.rgb, v_light, ColorBased.x, OverlayColor);

#ifdef TRANSPARENT_PASS
    albedo = applyHudOpacity(albedo, HudOpacity.x);
#endif
    albedo.rgb = applyFog(albedo.rgb, v_fog.rgb, v_fog.a);
    albedo.rgb = film(albedo.rgb);
    gl_FragColor = albedo;
#endif
#endif
}

$input v_texcoord0, v_color0, v_light, v_fog

#include <bgfx_shader.sh>
#include <utils/DynamicUtil.h>
#include <utils/FogUtil.h>

uniform vec4 ChangeColor;
uniform vec4 OverlayColor;
uniform vec4 ColorBased;
uniform vec4 MultiplicativeTintColor;
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
#else
    vec4 albedo;
    albedo.rgb = mix(vec3(1.0, 1.0, 1.0), v_color0.rgb, ColorBased.x);
    albedo.a = 1.0;

#if MULTI_COLOR_TINT__ON
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
#else
    albedo = applyColorChange(albedo, ChangeColor, v_color0.a);
#endif

    albedo = applyOverlayColor(albedo, OverlayColor);
    albedo = applyLighting(albedo, v_light);

#if ALPHA_TEST_PASS
    if (albedo.a < 0.5) {
        discard;
    }
#endif

    albedo.rgb = applyFog(albedo.rgb, v_fog.rgb, v_fog.a);
    albedo.rgb = film(albedo.rgb);
    gl_FragColor = albedo;
#endif // DEPTH_ONLY
}

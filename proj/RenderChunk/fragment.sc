$input v_texcoord0, v_color0, v_fog, v_lightmapUV

#include <bgfx_shader.sh>

#ifndef DEPTH_ONLY_OPAQUE
  SAMPLER2D_AUTOREG(s_LightMapTexture);
  SAMPLER2D_AUTOREG(s_MatTexture);

  #if defined(SEASONS) && (defined(ALPHA_TEST) || defined(OPAQUE))
    SAMPLER2D_AUTOREG(s_SeasonsTexture);
  #endif
#endif
uniform vec4 FogAndDistanceControl;
uniform vec4 FogColor;
uniform vec4 ViewPositionAndTime;


vec3 Film(vec3 x)
{
	 float a = 3.15;
	 float b = 0.02;
	 float c = 2.43;
	 float d = 0.59;
	 float e = 0.14;
	return clamp((x*(a*x+b))/(x*(c*x+d)+e),0.05,1.0);
}


void main() {
  highp float TIME = ViewPositionAndTime.w;
  #ifndef DEPTH_ONLY_OPAQUE
    vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);

    #ifdef ALPHA_TEST
      if (diffuse.a < 0.5) {
        discard;
      }
    #endif

    #if defined(SEASONS) && (defined(ALPHA_TEST) || defined(OPAQUE))
      diffuse.rgb *= mix(vec3_splat(1.0), 2.0 * texture2D(s_SeasonsTexture, v_color0.xy).rgb, v_color0.y);
      diffuse.rgb *= v_color0.aaa;
    #else
      diffuse *= v_color0;
    #endif
    diffuse.rgb *= texture2D(s_LightMapTexture, v_lightmapUV).xyz;

float rain = 1.0 - pow(FogAndDistanceControl.y,11.0);

diffuse.rgb *= mix(vec3(1.0,1.0,1.0),vec3(0.66,0.66,0.66),rain);

diffuse.rgb = Film(diffuse.rgb);

float shadow = mix(0.55,1.0,smoothstep(0.855,0.875,v_lightmapUV.y));
diffuse.rgb *= mix(shadow,1.0,v_lightmapUV.x);

vec3 colorA = vec3(0.8,0.45,0.0);
vec3 colorB = vec3(0.9,0.40,0.0);
highp float ti = abs(sin(TIME));
vec3 light = mix(colorA,colorB,ti);
diffuse.rgb += light *max(v_lightmapUV.x-0.45,0.0)*(1.0-diffuse.rgb);

    diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);

    gl_FragColor = diffuse;
  #else
    gl_FragColor = vec4_splat(0.0);
  #endif
}


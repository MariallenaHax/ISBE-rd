$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
    $input i_data1, i_data2, i_data3
#endif
$output v_color0, v_fog, v_texcoord0, v_lightmapUV

#include <bgfx_shader.sh>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;
highp float hash11(highp float p)
{
	p = fract(p * .1031);
	p *= p + 33.33;
	p *= p + p;
	return fract(p);
}
highp float rand(highp vec3 p){
	highp float x = (p.x+p.y+p.z)/3.0+ViewPositionAndTime.w;
	return mix(hash11(floor(x)),hash11(ceil(x)),smoothstep(0.0,1.0,fract(x)))*2.0;
}
void main() {
    mat4 model;
#ifdef INSTANCING
    model[0] = vec4(i_data1.x, i_data2.x, i_data3.x, 0.0);
    model[1] = vec4(i_data1.y, i_data2.y, i_data3.y, 0.0);
    model[2] = vec4(i_data1.z, i_data2.z, i_data3.z, 0.0);
    model[3] = vec4(i_data1.w, i_data2.w, i_data3.w, 1.0);
#else
    model = u_model[0];
#endif

    vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;
    vec4 color;
#ifdef RENDER_AS_BILLBOARDS
    worldPos += vec3(0.5, 0.5, 0.5);
    vec3 viewDir = normalize(worldPos - ViewPositionAndTime.xyz);
    vec3 boardPlane = normalize(mul(vec3(0.0,1.0,0.0),viewDir));
    worldPos = (worldPos -
        ((((viewDir.yzx * boardPlane.zxy) - (viewDir.zxy * boardPlane.yzx)) *
        (a_color0.z - 0.5)) +
        (boardPlane * (a_color0.x - 0.5))));
    color = vec4(1.0, 1.0, 1.0, 1.0);
#else
    color = a_color0;
#endif

    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
    vec4 fogColor;
    fogColor.rgb = FogColor.rgb;
    fogColor.a = clamp(((((camDis / FogAndDistanceControl.z) + RenderChunkFogAlpha.x) -
        FogAndDistanceControl.x) / (FogAndDistanceControl.y - FogAndDistanceControl.x)), 0.0, 1.0);

#ifdef TRANSPARENT
    if(a_color0.a < 0.95) {
        color.a = mix(a_color0.a, 1.0, clamp((camDis / FogAndDistanceControl.w), 0.0, 1.0));
    };
#endif
    v_texcoord0 = a_texcoord0;
    v_lightmapUV = a_texcoord1;
    v_color0 = color;
    v_fog = fogColor;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
    vec3 l = fract(a_position.xyz*.0625)*16.;
#ifdef TRANSPARENT
    if(a_color0.g != a_color0.b) {
		gl_Position.y += sin(ViewPositionAndTime.w*3.0 + l.x + l.y + l.z)*0.03;
    }
#endif
    #ifdef ALPHA_TEST
	if(a_color0.g != a_color0.b)
    {
		gl_Position.x += sin(ViewPositionAndTime.w*3.0 + 2.0*l.x + 2.0*l.z + l.y)*rand(l)*0.02;
    }
    #endif
}
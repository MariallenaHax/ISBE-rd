$input v_color0, v_texcoord0, v_worldPos, v_prevWorldPos
#include <bgfx_shader.sh>
#include <utils/snoise.h>

uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;
uniform vec4 SkyColor;
uniform vec4 FogAndDistanceControl;

highp float fBM(const int octaves, const float lowerBound, const float upperBound, highp vec2 st) {
	highp float TIME = ViewPositionAndTime.w;
	highp float value = 0.0;
	highp float amplitude = 0.5;
	for (int i = 0; i < octaves; i++) {
		value += amplitude * (snoise(st) * 0.5 + 0.5);
		if (value >= upperBound) {break;}
		else if (value + amplitude <= lowerBound) {break;}
		st        *= 2.0;
		st.x      -=TIME/256.0*float(i+1);
		amplitude *= 0.5;
	}
	return smoothstep(lowerBound, upperBound, value);
}
void main()
{
	vec3 CC_DC = vec3(1.3,1.3,1.1);
	vec3 CC_NC = vec3(0.62,0.62,0.62);
	highp float TIME = ViewPositionAndTime.w;
	vec4 n_color = v_color0;
float weather = smoothstep(.8,1.,FogAndDistanceControl.y);
n_color = mix(mix(n_color,FogColor,.33),FogColor,smoothstep(.0,1.,FogColor.r));

	float day = smoothstep(.15,.25,FogColor.g);
	vec3 cc = mix(CC_NC,CC_DC,day);
	float lb = mix(.0,.55,weather);
	float cm = -fBM(10,lb,1.2,v_prevWorldPos.xz*4.5 -TIME*.005);
	n_color.rgb = mix(n_color.rgb, cc, cm);
gl_FragColor = mix(n_color, FogColor, FogColor.r);
}

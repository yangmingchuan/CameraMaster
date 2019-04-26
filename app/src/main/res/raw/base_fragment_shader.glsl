#extension GL_OES_EGL_image_external : require
uniform samplerExternalOES uTextureSampler;
precision mediump float;
uniform int vColorType;
varying vec2 vTextureCoord;
uniform int vArraysSize;
uniform vec3 vChangeColor;
uniform vec3 vChangeColorB;
uniform vec3 vChangeColorC;

float debugFloatA;
float debugFloatB;

void main()
{
    vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
    gl_FragColor = vec4(vCameraColor.r, vCameraColor.g, vCameraColor.b, 1.0);
    for(int i = 0;i<vArraysSize;++i){
        debugFloatA =  vCameraColor.r * 255.0 - 1.0 ;
        debugFloatB = vCameraColor.r * 255.0 + 1.0 ;
        if( debugFloatA <= vChangeColor[i] ){
            if(  vChangeColor[i] <= debugFloatB ){
                gl_FragColor = vec4(1.0-vCameraColor.r,  1.0-vCameraColor.g,1.0- vCameraColor.b, 1.0);
            }
        }
    }
}
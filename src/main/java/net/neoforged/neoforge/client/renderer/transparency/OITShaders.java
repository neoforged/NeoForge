package net.neoforged.neoforge.client.renderer.transparency;

public final class OITShaders
{

    private OITShaders()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ForgeTransparencyShaders. This is a utility class");
    }

    public static final String WBOIT_FRAGMENT_SHADER_VERSION_HEADER = """
            #version 150 core
            #extension GL_ARB_explicit_attrib_location : require
            """;

    public static final String WBOIT_FRAGMENT_SHADER_PREFIX = """
            layout (location = 0) out vec4 accum;
            layout (location = 1) out float reveal;
            layout (location = 2) out float bucket;
            """;

    public static final String WBOIT_FRAGMENT_SHADER_SUFFIX = """
            float weight(vec4 color, float z) {
                float alphaFactor = max(pow(10, -2), 3000 * pow((1-z), 3));
                return color.a * alphaFactor;
            }
            
            void main() {
                mainShader();
            
                if (!oitEnabled) {
                    accum = %OUT_COLOR%;
                    return;
                }
            
                vec4 color = %OUT_COLOR%;
            
                ivec2 coords = ivec2(gl_FragCoord.xy);
                float distance = texelFetch(oitDepthTexture, coords, 0).r;
            
                float weight = weight(color, gl_FragCoord.z);
            
                accum = vec4(color.rgb * weight, color.a);
                reveal = color.a * weight;
            }
            """;

    public static final String MBOIT_FRAGMENT_SHADER_VERSION_HEADER = """
            #version 150 core
            #extension GL_ARB_explicit_attrib_location : require
            """;

    public static final String MBOIT_FRAGMENT_SHADER_PREFIX = """
            layout (location = 1) out vec4 firstMoments;
            layout (location = 2) out float zerothMoment;
            """;


    public static final String MBOIT_FRAGMENT_SHADER_SUFFIX = """
            void main() {
                mainShader();
            
                if (!oitEnabled) {
                    firstMoments = %OUT_COLOR%;
                    return;
                }
            
                vec4 color = %OUT_COLOR%;
            
                ivec2 coords = ivec2(gl_FragCoord.xy);
                float distance = texelFetch(oitDepthTexture, coords, 0).r;
            
                float weight = weight(color, gl_FragCoord.z);
            
                accum = vec4(color.rgb * weight, color.a);
                reveal = color.a * weight;
            }
            """;
}

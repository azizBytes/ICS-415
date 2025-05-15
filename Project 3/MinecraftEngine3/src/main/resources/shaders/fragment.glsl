#version 330 core

in vec2 TexCoord;
in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

uniform sampler2D texture1;
uniform vec3 lightDir;
uniform vec3 cameraPos;

void main() {
    // Normalize vectors
    vec3 norm = normalize(Normal);
    vec3 lightDirection = normalize(-lightDir);

    // Ambient
    float ambientStrength = 0.2;
    vec3 ambient = ambientStrength * vec3(1.0);

    // Diffuse
    float diff = max(dot(norm, lightDirection), 0.0);
    vec3 diffuse = diff * vec3(1.0);

    vec3 lighting = (ambient + diffuse);
    vec4 texColor = texture(texture1, TexCoord);

    FragColor = vec4(texColor.rgb * lighting, texColor.a);
}

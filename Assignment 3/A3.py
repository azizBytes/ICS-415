import numpy as np
from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass
class Light:
    type: str  # Type of light: "ambient", "point", or "directional"
    intensity: float  # Intensity of the light
    position: np.ndarray = None  # Position of the light (for point lights)
    direction: np.ndarray = None  # Direction of the light (for directional lights)


@dataclass
class Sphere:
    center: np.ndarray  # Center of the sphere
    radius: float  # Radius of the sphere
    color: np.ndarray  # Color of the sphere (RGB)
    specular: int  # Specular exponent (controls shininess)
    reflective: float = 0.0  # Reflectivity (0 = no reflection, 1 = perfect mirror)


class Scene:
    def __init__(self):
        # Define the spheres in the scene
        self.spheres = [
            Sphere(
                center=np.array([0, -1, 3]),
                radius=1,
                color=np.array([255, 0, 0]),  # Red sphere
                specular=500,  # Shiny
                reflective=0.2  # A bit reflective
            ),
            Sphere(
                center=np.array([2, 0, 4]),
                radius=1,
                color=np.array([0, 0, 255]),  # Blue sphere
                specular=500,  # Shiny
                reflective=0.3  # A bit more reflective
            ),
            Sphere(
                center=np.array([-2, 0, 4]),
                radius=1,
                color=np.array([0, 255, 0]),  # Green sphere
                specular=10,  # Somewhat shiny
                reflective=0.4  # Even more reflective
            ),
            Sphere(
                center=np.array([0, -5001, 0]),
                radius=5000,
                color=np.array([255, 255, 0]),  # Yellow ground
                specular=1000,  # Very shiny
                reflective=0.5  # Half reflective
            )
        ]

        # Define the lights in the scene
        self.lights = [
            Light(type="ambient", intensity=0.2),  # Ambient light
            Light(
                type="point",
                intensity=0.6,  # Point light
                position=np.array([2, 1, 0])
            ),
            Light(
                type="directional",
                intensity=0.2,  # Directional light
                direction=np.array([1, 4, 4])
            )
        ]


def normalize(vector: np.ndarray) -> np.ndarray:
    """Normalize a vector to have a length of 1."""
    return vector / np.linalg.norm(vector)


def intersect_ray_sphere(O: np.ndarray, D: np.ndarray, sphere: Sphere) -> Tuple[float, float]:
    """Calculate the intersection points of a ray with a sphere."""
    C = sphere.center
    r = sphere.radius

    oc = O - C
    a = np.dot(D, D)
    b = 2 * np.dot(oc, D)
    c = np.dot(oc, oc) - r * r

    discriminant = b * b - 4 * a * c

    if discriminant < 0:
        return np.inf, np.inf  # No intersection

    t1 = (-b + np.sqrt(discriminant)) / (2 * a)
    t2 = (-b - np.sqrt(discriminant)) / (2 * a)
    return t1, t2


def compute_lighting(P: np.ndarray, N: np.ndarray, V: np.ndarray, s: int, scene: Scene) -> float:
    """Compute the lighting at a point on a sphere."""
    i = 0.0
    for light in scene.lights:
        if light.type == "ambient":
            i += light.intensity  # Add ambient light
        else:
            if light.type == "point":
                L = light.position - P  # Vector to point light
            else:
                L = light.direction  # Directional light direction

            # Diffuse reflection (Lambertian reflectance)
            n_dot_l = np.dot(N, L)
            if n_dot_l > 0:
                i += light.intensity * n_dot_l / (np.linalg.norm(N) * np.linalg.norm(L))

            # Specular reflection (Phong model)
            if s != -1:
                R = 2 * N * np.dot(N, L) - L  # Reflected light direction
                r_dot_v = np.dot(R, V)
                if r_dot_v > 0:
                    i += light.intensity * pow(r_dot_v / (np.linalg.norm(R) * np.linalg.norm(V)), s)

    return i


def reflect_ray(R: np.ndarray, N: np.ndarray) -> np.ndarray:
    """Compute the reflection of a ray."""
    return 2 * N * np.dot(N, R) - R


def closest_intersection(O: np.ndarray, D: np.ndarray, t_min: float, t_max: float, scene: Scene) -> Tuple[
    Optional[Sphere], float]:
    """Find the closest intersection of a ray with the spheres in the scene."""
    closest_t = np.inf
    closest_sphere = None

    for sphere in scene.spheres:
        t1, t2 = intersect_ray_sphere(O, D, sphere)
        if t_min <= t1 <= t_max and t1 < closest_t:
            closest_t = t1
            closest_sphere = sphere
        if t_min <= t2 <= t_max and t2 < closest_t:
            closest_t = t2
            closest_sphere = sphere

    return closest_sphere, closest_t


def trace_ray(O: np.ndarray, D: np.ndarray, t_min: float, t_max: float, scene: Scene, recursion_depth: int = 3) -> np.ndarray:
    """Trace a ray and compute the color of the pixel it hits."""
    closest_sphere, closest_t = closest_intersection(O, D, t_min, t_max, scene)

    if closest_sphere is None:
        return np.array([0, 0, 0])  # Black background

    # Compute intersection point
    P = O + closest_t * D

    # Compute sphere normal at intersection
    N = normalize(P - closest_sphere.center)

    # Compute local color
    local_color = closest_sphere.color * compute_lighting(P, N, -D, closest_sphere.specular, scene)

    # If we hit the recursion limit or the object is not reflective, return the local color
    r = closest_sphere.reflective
    if recursion_depth <= 0 or r <= 0:
        return local_color

    # Compute the reflected color
    R = reflect_ray(-D, N)
    reflected_color = trace_ray(P, R, 0.001, np.inf, scene, recursion_depth - 1)

    # Blend local color and reflected color based on reflectivity
    return local_color * (1 - r) + reflected_color * r


def render(width: int, height: int, scene: Scene) -> np.ndarray:
    """Render the scene into an image."""
    aspect_ratio = width / height
    viewport_width = 1.0
    viewport_height = 1.0 / aspect_ratio

    image = np.zeros((height, width, 3))
    O = np.array([0, 0, 0])  # Camera at origin

    for i in range(height):
        for j in range(width):
            # Compute ray direction for each pixel
            x = (j + 0.5) / width * viewport_width - 0.5
            y = -(i + 0.5) / height * viewport_height + 0.5
            D = normalize(np.array([x, y, 1]))

            # Trace the ray and compute the pixel color
            color = trace_ray(O, D, 1, np.inf, scene)
            image[i, j] = np.clip(color, 0, 255)

    return image.astype(np.uint8)


# Main rendering code
if __name__ == "__main__":
    import matplotlib.pyplot as plt

    scene = Scene()
    width, height = 400, 400
    image = render(width, height, scene)

    plt.imshow(image)
    plt.axis('off')
    plt.show()
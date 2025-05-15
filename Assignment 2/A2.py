import numpy as np
from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass
class Light:
    type: str
    intensity: float
    position: np.ndarray = None
    direction: np.ndarray = None


@dataclass
class Sphere:
    center: np.ndarray
    radius: float
    color: np.ndarray
    specular: int


class Scene:
    def __init__(self):
        self.spheres = [
            Sphere(
                center=np.array([0, -1, 3]),
                radius=1,
                color=np.array([255, 0, 0]),  # Red
                specular=500
            ),
            Sphere(
                center=np.array([2, 0, 4]),
                radius=1,
                color=np.array([0, 0, 255]),  # Blue
                specular=500
            ),
            Sphere(
                center=np.array([-2, 0, 4]),
                radius=1,
                color=np.array([0, 255, 0]),  # Green
                specular=10
            ),
            Sphere(
                center=np.array([0, -5001, 0]),
                radius=5000,
                color=np.array([255, 255, 0]),  # Yellow
                specular=1000
            )
        ]

        self.lights = [
            Light(type="ambient", intensity=0.2),
            Light(
                type="point",
                intensity=0.6,
                position=np.array([2, 1, 0])
            ),
            Light(
                type="directional",
                intensity=0.2,
                direction=np.array([1, 4, 4])
            )
        ]


def normalize(vector: np.ndarray) -> np.ndarray:
    return vector / np.linalg.norm(vector)


def intersect_ray_sphere(O: np.ndarray, D: np.ndarray, sphere: Sphere) -> Tuple[float, float]:
    C = sphere.center
    r = sphere.radius

    oc = O - C
    a = np.dot(D, D)
    b = 2 * np.dot(oc, D)
    c = np.dot(oc, oc) - r * r

    discriminant = b * b - 4 * a * c

    if discriminant < 0:
        return np.inf, np.inf

    t1 = (-b + np.sqrt(discriminant)) / (2 * a)
    t2 = (-b - np.sqrt(discriminant)) / (2 * a)
    return t1, t2


def compute_lighting(P: np.ndarray, N: np.ndarray, V: np.ndarray, s: int, scene: Scene) -> float:
    i = 0.0
    for light in scene.lights:
        if light.type == "ambient":
            i += light.intensity
        else:
            if light.type == "point":
                L = light.position - P
            else:
                L = light.direction

            # Diffuse
            n_dot_l = np.dot(N, L)
            if n_dot_l > 0:
                i += light.intensity * n_dot_l / (np.linalg.norm(N) * np.linalg.norm(L))

            # Specular
            if s != -1:
                R = 2 * N * np.dot(N, L) - L
                r_dot_v = np.dot(R, V)
                if r_dot_v > 0:
                    i += light.intensity * pow(r_dot_v / (np.linalg.norm(R) * np.linalg.norm(V)), s)

    return i


def closest_intersection(O: np.ndarray, D: np.ndarray, t_min: float, t_max: float, scene: Scene) -> Tuple[
    Optional[Sphere], float]:
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


def trace_ray(O: np.ndarray, D: np.ndarray, t_min: float, t_max: float, scene: Scene) -> np.ndarray:
    closest_sphere, closest_t = closest_intersection(O, D, t_min, t_max, scene)

    if closest_sphere is None:
        return np.array([255, 255, 255])  # White background

    # Compute intersection point
    P = O + closest_t * D

    # Compute sphere normal at intersection
    N = normalize(P - closest_sphere.center)

    # Compute lighting
    intensity = compute_lighting(P, N, -D, closest_sphere.specular, scene)

    return closest_sphere.color * intensity


def render(width: int, height: int, scene: Scene) -> np.ndarray:
    aspect_ratio = width / height
    viewport_width = 1.0
    viewport_height = 1.0 / aspect_ratio

    image = np.zeros((height, width, 3))
    O = np.array([0, 0, 0])  # Camera at origin

    for i in range(height):
        for j in range(width):
            x = (j + 0.5) / width * viewport_width - 0.5
            y = -(i + 0.5) / height * viewport_height + 0.5
            D = normalize(np.array([x, y, 1]))

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
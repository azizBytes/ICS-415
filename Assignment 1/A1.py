import math
from dataclasses import dataclass
import numpy as np
from PIL import Image


@dataclass
class Sphere:
    center: tuple
    radius: float
    color: tuple


@dataclass
class Scene:
    spheres: list


def subtract_vectors(v1, v2):
    return tuple(a - b for a, b in zip(v1, v2))


def dot_product(v1, v2):
    return sum(a * b for a, b in zip(v1, v2))


def canvas_to_viewport(x, y, Vw, Vh, Cw, Ch, d):
    return (x * Vw / Cw, y * Vh / Ch, d)


def intersect_ray_sphere(O, D, sphere):
    r = sphere.radius
    CO = subtract_vectors(O, sphere.center)

    a = dot_product(D, D)
    b = 2 * dot_product(CO, D)
    c = dot_product(CO, CO) - r * r

    discriminant = b * b - 4 * a * c

    if discriminant < 0:
        return float('inf'), float('inf')

    t1 = (-b + math.sqrt(discriminant)) / (2 * a)
    t2 = (-b - math.sqrt(discriminant)) / (2 * a)

    return t1, t2


def trace_ray(O, D, t_min, t_max, scene):
    closest_t = float('inf')
    closest_sphere = None

    for sphere in scene.spheres:
        t1, t2 = intersect_ray_sphere(O, D, sphere)

        if t_min <= t1 <= t_max and t1 < closest_t:
            closest_t = t1
            closest_sphere = sphere

        if t_min <= t2 <= t_max and t2 < closest_t:
            closest_t = t2
            closest_sphere = sphere

    if closest_sphere is None:
        return BACKGROUND_COLOR

    return closest_sphere.color


def render(scene, Cw, Ch, Vw, Vh, d):
    # Initialize canvas as a numpy array
    canvas = np.zeros((Ch, Cw, 3), dtype=np.uint8)
    O = (0, 0, 0)  # Camera origin

    for x in range(-Cw // 2, Cw // 2):
        for y in range(-Ch // 2, Ch // 2):
            D = canvas_to_viewport(x, y, Vw, Vh, Cw, Ch, d)
            color = trace_ray(O, D, 1, float('inf'), scene)

            # Convert canvas coordinates to image coordinates
            canvas_x = x + Cw // 2
            canvas_y = y + Ch // 2
            canvas[canvas_y, canvas_x] = color

    return canvas


# Constants
Cw, Ch = 400, 400  # Canvas width and height
Vw, Vh = 1, 1  # Viewport dimensions
d = 1  # Projection plane distance
BACKGROUND_COLOR = (255, 255, 255)  # White

# Scene setup
scene = Scene([
    Sphere(center=(0, 1, 3), radius=1, color=(255, 0, 0)),  # Red sphere
    Sphere(center=(2, 0, 4), radius=1, color=(0, 0, 255)),  # Blue sphere
    Sphere(center=(-2, 0, 4), radius=1, color=(0, 255, 0))  # Green sphere
])

# Main execution
if __name__ == "__main__":
    canvas = render(scene, Cw, Ch, Vw, Vh, d)

    # Convert numpy array to PIL Image and save
    image = Image.fromarray(canvas)
    image.save("raytraced_spheres.png")
    print("Image saved as 'raytraced_spheres.png'")
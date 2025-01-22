# Ray Tracing Renderer with Lighting

This project is an enhanced ray tracing algorithm that renders 3D scenes consisting of spheres with lighting effects. The rendered output includes ambient, point, and directional lighting, providing a realistic depiction of the scene. The final image is displayed using Matplotlib.

## Features
- **Ray-Sphere Intersection**: Determines where rays intersect spheres in the scene.
- **Lighting Models**:
  - **Ambient Lighting**: Uniform light across the scene.
  - **Point Lighting**: Light emanates from a specific point.
  - **Directional Lighting**: Light from a specific direction.
- **Specular Highlights**: Adds shiny reflections to surfaces based on the material's specular property.
- **Customizable Scene**: Define spheres, lights, and their properties.
- **Image Rendering**: Displays the rendered scene using Matplotlib.

## How It Works
1. **Scene Definition**:
   - A scene is made up of spheres and light sources.
   - Each sphere has a center, radius, color, and specular property.
   - Lights can be ambient, point, or directional.

2. **Ray Tracing**:
   - For each pixel, a ray is cast into the scene.
   - The algorithm computes intersections with spheres and determines the closest object intersected by the ray.
   - Lighting calculations consider ambient, diffuse, and specular components.

3. **Rendering**:
   - The resulting image is stored as a NumPy array and displayed using Matplotlib.

## Code Structure
### Classes
- **Sphere**:
  - Represents a sphere with attributes:
    - `center`: 3D coordinates of the sphere's center.
    - `radius`: Radius of the sphere.
    - `color`: RGB color of the sphere.
    - `specular`: Specular intensity for shiny surfaces.

- **Light**:
  - Represents a light source with attributes:
    - `type`: Light type (`ambient`, `point`, or `directional`).
    - `intensity`: Intensity of the light.
    - `position`: Position of the light (for point lights).
    - `direction`: Direction of the light (for directional lights).

- **Scene**:
  - Holds a list of spheres and lights.

### Key Functions
- **Ray-Sphere Intersection**:
  - `intersect_ray_sphere`: Computes where a ray intersects a sphere.

- **Lighting**:
  - `compute_lighting`: Calculates lighting at a point based on the light sources.

- **Ray Tracing**:
  - `trace_ray`: Determines the color of a pixel by tracing a ray through the scene.

- **Rendering**:
  - `render`: Renders the scene to a 2D image array.

## Usage
1. Install dependencies:
   ```bash
   pip install numpy matplotlib
   ```

2. Run the script:
   ```bash
   python ray_tracing_lighting.py
   ```

3. The rendered image will be displayed in a Matplotlib window.

## Scene Customization
You can modify the scene by editing the `Scene` class. Example:
```python
self.spheres = [
    Sphere(center=np.array([0, -1, 3]), radius=1, color=np.array([255, 0, 0]), specular=500),
    Sphere(center=np.array([2, 0, 4]), radius=1, color=np.array([0, 0, 255]), specular=500),
    Sphere(center=np.array([-2, 0, 4]), radius=1, color=np.array([0, 255, 0]), specular=10)
]

self.lights = [
    Light(type="ambient", intensity=0.2),
    Light(type="point", intensity=0.6, position=np.array([2, 1, 0])),
    Light(type="directional", intensity=0.2, direction=np.array([1, 4, 4]))
]
```
- **Spheres**:
  - `center`: Position in 3D space.
  - `radius`: Size of the sphere.
  - `color`: RGB color tuple.
  - `specular`: Specular intensity for highlights.

- **Lights**:
  - `type`: Ambient, point, or directional.
  - `intensity`: Brightness of the light.
  - `position`: Coordinates of point lights.
  - `direction`: Vector for directional lights.

## Output Example
The rendered output will display multiple colored spheres illuminated by ambient, point, and directional lights. The scene includes specular highlights for added realism.

## Dependencies
- Python 3.x
- `numpy` for numerical computations
- `matplotlib` for image display

## License
This project is open-source and available under the MIT License.

## Acknowledgments
This implementation is inspired by foundational concepts in computer graphics and ray tracing techniques.


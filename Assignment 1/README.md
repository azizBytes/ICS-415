# Ray Tracing Renderer

This Assignment implements a basic ray tracing algorithm to render 3D scenes consisting of spheres onto a 2D image. The final output is a PNG file showcasing the spheres with accurate color and shading.

## Features
- **Ray-Sphere Intersection**: Calculates intersections between rays and spheres to determine visibility.
- **Scene Rendering**: Renders spheres in a 3D scene onto a 2D canvas.
- **Customizable Scene**: Easily modify spheres, their positions, radii, and colors.
- **Viewport and Canvas Transformation**: Maps 2D canvas coordinates to 3D viewport coordinates for ray tracing.
- **Output as Image**: Saves the rendered scene as a PNG image using PIL.

## How It Works
1. **Scene Setup**:
   - The scene is represented as a collection of spheres, each defined by its center, radius, and color.

2. **Ray Tracing**:
   - For each pixel on the canvas, a ray is cast into the 3D scene.
   - The algorithm calculates intersections between the ray and all spheres in the scene.
   - If a ray hits a sphere, the sphere's color is assigned to the corresponding pixel.
   - If no intersection is found, the pixel is assigned the background color.

3. **Image Rendering**:
   - The rendered image is saved as `raytraced_spheres.png` in the project directory.

## Code Structure
- **Sphere Class**:
  Represents a sphere in the scene with attributes for center, radius, and color.

- **Scene Class**:
  Holds a collection of spheres to be rendered.

- **Mathematical Helpers**:
  - `subtract_vectors`: Computes the difference between two vectors.
  - `dot_product`: Calculates the dot product of two vectors.

- **Key Functions**:
  - `canvas_to_viewport`: Converts 2D canvas coordinates to 3D viewport coordinates.
  - `intersect_ray_sphere`: Finds the intersection points of a ray and a sphere.
  - `trace_ray`: Determines the color of a pixel by tracing a ray through the scene.
  - `render`: Renders the entire scene by iterating over all canvas pixels.

## Usage
1. Install the required dependencies:
   ```bash
   pip install numpy pillow
   ```

2. Run the script:
   ```bash
   python ray_tracing.py
   ```

3. The rendered image will be saved as `raytraced_spheres.png` in the current directory.

## Scene Customization
You can modify the scene by editing the `scene` variable in the script. Example:
```python
scene = Scene([
    Sphere(center=(0, 1, 3), radius=1, color=(255, 0, 0)),  # Red sphere
    Sphere(center=(2, 0, 4), radius=1, color=(0, 0, 255)),  # Blue sphere
    Sphere(center=(-2, 0, 4), radius=1, color=(0, 255, 0))  # Green sphere
])
```
- **Center**: Position of the sphere in 3D space (x, y, z).
- **Radius**: Radius of the sphere.
- **Color**: RGB color tuple of the sphere.

## Output Example
The output is a 2D image showing red, blue, and green spheres on a white background. The spheres are positioned in a 3D scene and rendered using perspective projection.

## Dependencies
- Python 3.x
- `numpy` for numerical calculations
- `Pillow` for image handling

## License
This Code is open-source and available under the MIT License.

## Acknowledgments
Special thanks to computer graphics pioneers for foundational ray tracing techniques!

![image](https://github.com/user-attachments/assets/81ad38c9-e5d6-4245-a0fa-98ced3ece7b6)





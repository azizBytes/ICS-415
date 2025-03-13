import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

// Main class: Entry point
public class Project_RayTracing {
    public static void main(String[] args) throws IOException {
        // Image settings
        final int imageWidth = 600;
        final int imageHeight = 400;
        final int samplesPerPixel = 100;
        final int maxDepth = 50;

        // Create a random scene of spheres
        HitList world = randomScene();

        // Camera setup
        Vec3 lookFrom = new Vec3(13, 2, 3);
        Vec3 lookAt   = new Vec3(0, 0, 0);
        Vec3 vup      = new Vec3(0, 1, 0);
        double fovDeg = 20.0;
        double aspectRatio = (double) imageWidth / imageHeight;

        // Depth of field parameters
        double Aperture = 0.0;
        double FocusDist = 10.0;


        Camera cam = new Camera(lookFrom, lookAt, vup, fovDeg, aspectRatio, Aperture, FocusDist);

        // Prepare a BufferedImage to store the result
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        // Rendering (Nested loops over all pixels)
        Random rng = new Random();
        for (int j = 0; j < imageHeight; j++) {
            int row = imageHeight - 1 - j; // flip vertically
            System.out.printf("Scanlines remaining: %d\n", imageHeight - 1 - j);
            for (int i = 0; i < imageWidth; i++) {
                Vec3 pixelColor = new Vec3(0, 0, 0);

                // Multisample for anti-aliasing
                for (int s = 0; s < samplesPerPixel; s++) {
                    double u = (i + rng.nextDouble()) / (imageWidth - 1);
                    double v = (j + rng.nextDouble()) / (imageHeight - 1);
                    Ray r = cam.getRay(u, v, rng);
                    pixelColor = pixelColor.add(rayColor(r, world, maxDepth, rng));
                }
                // Average color and gamma-correct
                pixelColor = pixelColor.scale(1.0 / samplesPerPixel);
                pixelColor = new Vec3(
                        Math.sqrt(pixelColor.x), // gamma 2.0
                        Math.sqrt(pixelColor.y),
                        Math.sqrt(pixelColor.z)
                );

                // Convert to [0..255] and store
                int ir = (int) (255.999 * clamp(pixelColor.x, 0.0, 1.0));
                int ig = (int) (255.999 * clamp(pixelColor.y, 0.0, 1.0));
                int ib = (int) (255.999 * clamp(pixelColor.z, 0.0, 1.0));
                int rgb = (ir << 16) | (ig << 8) | (ib);
                image.setRGB(i, row, rgb);
            }
        }

        // Write out to disk
        ImageIO.write(image, "png", new File("output.png"));
        System.out.println("Done! Saved to output.png");
    }

    // Generate the random scene from the snippet logic
    private static HitList randomScene() {
        HitList world = new HitList();
        Random rng = new Random();

        // Ground sphere
        Material groundMat = new Lambertian(new Vec3(0.5, 0.5, 0.5));
        world.add(new Sphere(new Vec3(0, -1000, 0), 1000, groundMat));

        // Many random small spheres
        for (int a = -11; a < 11; a++) {
            for (int b = -11; b < 11; b++) {
                double chooseMat = rng.nextDouble();
                Vec3 center = new Vec3(a + 0.9*rng.nextDouble(), 0.2, b + 0.9*rng.nextDouble());
                // Avoid placing too close to big spheres below
                if ((center.subtract(new Vec3(4, 0.2, 0))).length() > 0.9) {
                    Material sphereMat;
                    if (chooseMat < 0.8) {
                        // diffuse
                        Vec3 albedo = Vec3.random(rng).mul(Vec3.random(rng));
                        sphereMat = new Lambertian(albedo);
                        world.add(new Sphere(center, 0.2, sphereMat));
                    } else if (chooseMat < 0.95) {
                        // metal
                        Vec3 albedo = Vec3.random(rng, 0.5, 1.0);
                        double fuzz = rng.nextDouble() * 0.5;
                        sphereMat = new Metal(albedo, fuzz);
                        world.add(new Sphere(center, 0.2, sphereMat));
                    } else {
                        // glass
                        sphereMat = new Dielectric(1.5);
                        world.add(new Sphere(center, 0.2, sphereMat));
                    }
                }
            }
        }

        // Three larger spheres
        world.add(new Sphere(new Vec3(0, 1, 0),   1.0, new Dielectric(1.5)));
        world.add(new Sphere(new Vec3(-4, 1, 0),  1.0, new Lambertian(new Vec3(0.4, 0.2, 0.1))));
        world.add(new Sphere(new Vec3(4, 1, 0),   1.0, new Metal(new Vec3(0.7, 0.6, 0.5), 0.0)));

        return world;
    }

    // Return background color or scattered ray color
    private static Vec3 rayColor(Ray r, Hittable world, int depth, Random rng) {
        // If we've exceeded the ray bounce limit, no more light is gathered.
        if (depth <= 0) {
            return new Vec3(0,0,0);
        }

        HitRecord rec = new HitRecord();
        if (world.hit(r, 0.001, Double.POSITIVE_INFINITY, rec)) {
            Ray scattered = new Ray();
            Vec3 attenuation = new Vec3();
            if (rec.mat.scatter(r, rec, attenuation, scattered, rng)) {
                return attenuation.mul(rayColor(scattered, world, depth-1, rng));
            }
            return new Vec3(0, 0, 0);
        }

        // Background: a simple gradient sky
        Vec3 unitDir = r.direction.normalize();
        double t = 0.5*(unitDir.y + 1.0);
        return new Vec3(1.0, 1.0, 1.0).scale(1.0 - t).add(new Vec3(0.5, 0.7, 1.0).scale(t));
    }

    // Utility to clamp color channels
    private static double clamp(double x, double min, double max) {
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }
}

// ----------------------------------------------------------------------
// Basic vector class for 3D geometry & color representation
// ----------------------------------------------------------------------
class Vec3 {
    public double x, y, z;

    // Three-argument constructor
    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Optional no-arg constructor if your code needs it
    public Vec3() {
        this(0,0,0);
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(x + v.x, y + v.y, z + v.z);
    }
    public Vec3 subtract(Vec3 v) {
        return new Vec3(x - v.x, y - v.y, z - v.z);
    }
    public Vec3 mul(Vec3 v) {
        return new Vec3(x * v.x, y * v.y, z * v.z);
    }
    public Vec3 scale(double t) {
        return new Vec3(x * t, y * t, z * t);
    }
    public double dot(Vec3 v) {
        return x*v.x + y*v.y + z*v.z;
    }
    public Vec3 cross(Vec3 v) {
        return new Vec3(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }
    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }
    public Vec3 normalize() {
        double len = length();
        return new Vec3(x/len, y/len, z/len);
    }



    public static Vec3 random(Random rng) {
        return new Vec3(rng.nextDouble(), rng.nextDouble(), rng.nextDouble());
    }

    public static Vec3 random(Random rng, double min, double max) {
        return new Vec3(
                min + (max-min)*rng.nextDouble(),
                min + (max-min)*rng.nextDouble(),
                min + (max-min)*rng.nextDouble()
        );
    }
}

// ----------------------------------------------------------------------
// Ray class: R(t) = origin + t*direction
// ----------------------------------------------------------------------
class Ray {
    public Vec3 origin;
    public Vec3 direction;

    public Ray() {
        origin = new Vec3(0,0,0);
        direction = new Vec3(0,0,0);
    }

    public Ray(Vec3 origin, Vec3 direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vec3 at(double t) {
        return origin.add(direction.scale(t));
    }
}

// ----------------------------------------------------------------------
// Record of a hit event: point, normal, t, material
// ----------------------------------------------------------------------
class HitRecord {
    public Vec3 p;
    public Vec3 normal;
    public Material mat;
    public double t;
    public boolean frontFace;

    public void setFaceNormal(Ray r, Vec3 outwardNormal) {
        frontFace = r.direction.dot(outwardNormal) < 0;
        normal = frontFace ? outwardNormal : outwardNormal.scale(-1);
    }
}

// ----------------------------------------------------------------------
// Hittable interface + List that holds multiple Hittable objects
// ----------------------------------------------------------------------
interface Hittable {
    boolean hit(Ray r, double tMin, double tMax, HitRecord rec);
}

class HitList implements Hittable {
    private final java.util.List<Hittable> objects = new java.util.ArrayList<>();

    public void add(Hittable obj) {
        objects.add(obj);
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        HitRecord tempRec = new HitRecord();
        boolean hitAnything = false;
        double closestSoFar = tMax;

        for (Hittable obj : objects) {
            if (obj.hit(r, tMin, closestSoFar, tempRec)) {
                hitAnything = true;
                closestSoFar = tempRec.t;
                rec.p = tempRec.p;
                rec.normal = tempRec.normal;
                rec.mat = tempRec.mat;
                rec.t = tempRec.t;
                rec.frontFace = tempRec.frontFace;
            }
        }

        return hitAnything;
    }
}

// ----------------------------------------------------------------------
// Sphere: Hittable geometry
// ----------------------------------------------------------------------
class Sphere implements Hittable {
    public Vec3 center;
    public double radius;
    public Material mat;

    public Sphere(Vec3 center, double radius, Material m) {
        this.center = center;
        this.radius = radius;
        this.mat = m;
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        Vec3 oc = r.origin.subtract(center);
        double a = r.direction.dot(r.direction);
        double halfB = oc.dot(r.direction);
        double c = oc.dot(oc) - radius*radius;
        double discriminant = halfB*halfB - a*c;
        if (discriminant < 0) return false;
        double sqrtd = Math.sqrt(discriminant);

        double root = (-halfB - sqrtd) / a;
        if (root < tMin || root > tMax) {
            root = (-halfB + sqrtd) / a;
            if (root < tMin || root > tMax) {
                return false;
            }
        }

        rec.t = root;
        rec.p = r.at(rec.t);
        Vec3 outwardNormal = rec.p.subtract(center).scale(1.0 / radius);
        rec.setFaceNormal(r, outwardNormal);
        rec.mat = mat;
        return true;
    }
}

// ----------------------------------------------------------------------
// Material interface + Lambertian, Metal, Dielectric
// ----------------------------------------------------------------------
abstract class Material {
    // Scatter the ray and produce attenuation color & a scattered ray if any
    public abstract boolean scatter(Ray rIn, HitRecord rec, Vec3 attenuation, Ray scattered, Random rng);
}

class Lambertian extends Material {
    public Vec3 albedo;

    public Lambertian(Vec3 a) {
        albedo = a;
    }

    @Override
    public boolean scatter(Ray rIn, HitRecord rec, Vec3 attenuation, Ray scattered, Random rng) {
        Vec3 scatterDir = rec.normal.add(randomUnitVector(rng));
        if (nearZero(scatterDir)) {
            scatterDir = rec.normal;
        }
        scattered.origin = rec.p;
        scattered.direction = scatterDir;
        attenuation.x = albedo.x;
        attenuation.y = albedo.y;
        attenuation.z = albedo.z;
        return true;
    }

    private Vec3 randomUnitVector(Random rng) {
        double a = 2.0 * Math.PI * rng.nextDouble();
        double z = -1 + 2 * rng.nextDouble();
        double r = Math.sqrt(1 - z*z);
        return new Vec3(r*Math.cos(a), r*Math.sin(a), z);
    }

    private boolean nearZero(Vec3 v) {
        final double s = 1e-8;
        return (Math.abs(v.x) < s) && (Math.abs(v.y) < s) && (Math.abs(v.z) < s);
    }
}

class Metal extends Material {
    public Vec3 albedo;
    public double fuzz;

    public Metal(Vec3 a, double f) {
        albedo = a;
        fuzz = (f < 1) ? f : 1;
    }

    @Override
    public boolean scatter(Ray rIn, HitRecord rec, Vec3 attenuation, Ray scattered, Random rng) {
        Vec3 reflected = reflect(rIn.direction.normalize(), rec.normal);
        scattered.origin = rec.p;
        scattered.direction = reflected.add(randomInUnitSphere(rng).scale(fuzz));
        attenuation.x = albedo.x;
        attenuation.y = albedo.y;
        attenuation.z = albedo.z;
        return scattered.direction.dot(rec.normal) > 0;
    }

    private Vec3 reflect(Vec3 v, Vec3 n) {
        return v.subtract(n.scale(2 * v.dot(n)));
    }

    private Vec3 randomInUnitSphere(Random rng) {
        while (true) {
            Vec3 p = Vec3.random(rng, -1, 1);
            if (p.dot(p) < 1) return p;
        }
    }
}

class Dielectric extends Material {
    public double ir; // Index of refraction

    public Dielectric(double indexOfRefraction) {
        ir = indexOfRefraction;
    }

    @Override
    public boolean scatter(Ray rIn, HitRecord rec, Vec3 attenuation, Ray scattered, Random rng) {
        attenuation.x = 1.0;
        attenuation.y = 1.0;
        attenuation.z = 1.0;
        double refractionRatio = rec.frontFace ? (1.0 / ir) : ir;

        Vec3 unitDir = rIn.direction.normalize();
        double cosTheta = Math.min(unitDir.scale(-1).dot(rec.normal), 1.0);
        double sinTheta = Math.sqrt(1.0 - cosTheta*cosTheta);

        boolean cannotRefract = refractionRatio * sinTheta > 1.0;
        Vec3 direction;
        if (cannotRefract || reflectance(cosTheta, refractionRatio) > rng.nextDouble()) {
            direction = reflect(unitDir, rec.normal);
        } else {
            direction = refract(unitDir, rec.normal, refractionRatio);
        }

        scattered.origin = rec.p;
        scattered.direction = direction;
        return true;
    }

    private Vec3 reflect(Vec3 v, Vec3 n) {
        return v.subtract(n.scale(2 * v.dot(n)));
    }

    private Vec3 refract(Vec3 uv, Vec3 n, double etaiOverEtat) {
        double cosTheta = Math.min(uv.scale(-1).dot(n), 1.0);
        Vec3 rOutPerp = uv.add(n.scale(cosTheta)).scale(etaiOverEtat);
        Vec3 rOutParallel = n.scale(-Math.sqrt(Math.abs(1.0 - rOutPerp.dot(rOutPerp))));
        return rOutPerp.add(rOutParallel);
    }

    private double reflectance(double cosine, double refIdx) {
        // Use Schlick's approximation for reflectance.
        double r0 = (1 - refIdx) / (1 + refIdx);
        r0 = r0*r0;
        return r0 + (1 - r0)*Math.pow((1 - cosine), 5);
    }
}

// ----------------------------------------------------------------------
// Simple thin-lens Camera
// ----------------------------------------------------------------------
class Camera {
    private Vec3 origin;
    private Vec3 lowerLeftCorner;
    private Vec3 horizontal;
    private Vec3 vertical;
    private Vec3 u, v, w;
    private double lensRadius;
    private final Random rng = new Random();

    public Camera(Vec3 lookFrom, Vec3 lookAt, Vec3 vup,
                  double vfov, // vertical field-of-view in degrees
                  double aspectRatio,
                  double aperture,
                  double focusDist) {

        double theta = Math.toRadians(vfov);
        double h = Math.tan(theta / 2);
        double viewportHeight = 2.0 * h;
        double viewportWidth  = aspectRatio * viewportHeight;

        w = (lookFrom.subtract(lookAt)).normalize();
        u = (vup.cross(w)).normalize();
        v = w.cross(u);

        origin = lookFrom;
        horizontal = u.scale(focusDist * viewportWidth);
        vertical   = v.scale(focusDist * viewportHeight);
        lowerLeftCorner = origin.subtract(horizontal.scale(0.5))
                .subtract(vertical.scale(0.5))
                .subtract(w.scale(focusDist));

        lensRadius = aperture / 2;
    }

    public Ray getRay(double s, double t, Random rng) {
        Vec3 rd = randomInUnitDisk(rng).scale(lensRadius);
        Vec3 offset = u.scale(rd.x).add(v.scale(rd.y));

        Vec3 dir = lowerLeftCorner
                .add(horizontal.scale(s))
                .add(vertical.scale(t))
                .subtract(origin)
                .subtract(offset);
        return new Ray(origin.add(offset), dir);
    }

    private Vec3 randomInUnitDisk(Random rng) {
        while (true) {
            double x = 2.0*rng.nextDouble() - 1.0;
            double y = 2.0*rng.nextDouble() - 1.0;
            if (x*x + y*y < 1.0) {
                return new Vec3(x, y, 0);
            }
        }
    }
}
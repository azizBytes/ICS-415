import pygame
import sys
import random

pygame.init()
screen = pygame.display.set_mode((1300, 750))
pygame.display.set_caption("Bezier & Spline Editor")
clock = pygame.time.Clock()
font = pygame.font.SysFont("Segoe UI", 16)
large_font = pygame.font.SysFont("Segoe UI", 22, bold=True)

curves = [[]]
curve_colors = [(random.randint(50, 200), random.randint(50, 200), random.randint(50, 200))]
line_colors = [(random.randint(200, 255), random.randint(200, 255), random.randint(200, 255))]
control_line_colors = [(random.randint(100, 150), random.randint(100, 150), random.randint(100, 150))]

dragging_point = None
active_curve = 0
show_spline = False
grid_enabled = True
curve_rects = []

# Color palette
color_palette = [(255, 0, 0), (0, 255, 0), (0, 100, 255), (255, 200, 0), (255, 0, 255), (0, 255, 255)]


def draw_grid(spacing=25):
    for x in range(0, 1100, spacing):
        pygame.draw.line(screen, (220, 220, 220), (x, 40), (x, screen.get_height()))
    for y in range(40, screen.get_height(), spacing):
        pygame.draw.line(screen, (220, 220, 220), (0, y), (1100, y))

def de_casteljau(points, t):
    while len(points) > 1:
        points = [((1 - t) * p0[0] + t * p1[0], (1 - t) * p0[1] + t * p1[1])
                  for p0, p1 in zip(points[:-1], points[1:])]
    return points[0]

def catmull_rom(p0, p1, p2, p3, t):
    t2 = t * t
    t3 = t2 * t
    x = 0.5 * ((2 * p1[0]) + (-p0[0] + p2[0]) * t +
               (2*p0[0] - 5*p1[0] + 4*p2[0] - p3[0]) * t2 +
               (-p0[0] + 3*p1[0] - 3*p2[0] + p3[0]) * t3)
    y = 0.5 * ((2 * p1[1]) + (-p0[1] + p2[1]) * t +
               (2*p0[1] - 5*p1[1] + 4*p2[1] - p3[1]) * t2 +
               (-p0[1] + 3*p1[1] - 3*p2[1] + p3[1]) * t3)
    return x, y

def draw_bezier(points, color, resolution=100):
    if len(points) < 2:
        return
    prev = de_casteljau(points[:], 0)
    for i in range(1, resolution + 1):
        t = i / resolution
        current = de_casteljau(points[:], t)
        pygame.draw.line(screen, color, prev, current, 3)
        prev = current

def draw_spline(points, color, resolution=20):
    if len(points) < 4:
        return
    for i in range(1, len(points) - 2):
        prev = catmull_rom(points[i - 1], points[i], points[i + 1], points[i + 2], 0)
        for j in range(1, resolution + 1):
            t = j / resolution
            current = catmull_rom(points[i - 1], points[i], points[i + 1], points[i + 2], t)
            pygame.draw.line(screen, color, prev, current, 3)
            prev = current

def draw_labels(points):
    for i, pt in enumerate(points):
        label = font.render(f"P{i}", True, (0, 0, 0))
        screen.blit(label, (pt[0] + 10, pt[1] + 10))

def draw_ui():
    pygame.draw.rect(screen, (255, 255, 255), (0, 0, 1300, 40))
    title = large_font.render(" Bezier & Spline Editor", True, (0, 0, 80))
    screen.blit(title, (20, 8))
    mode = "Spline" if show_spline else "Bezier"
    mode_label = font.render(f"Mode: {mode}  |  Active Curve: {active_curve + 1}/{len(curves)}", True, (50, 50, 50))
    screen.blit(mode_label, (350, 12))
    help_label = font.render("[Click] Add/Move | [Right Click] Delete | [C] Clear | [S] Save | [G] Grid | [N] New | [Tab] Switch", True, (100, 100, 100))
    screen.blit(help_label, (750, 12))

def draw_sidebar(mouse_pos):
    global curve_rects
    curve_rects = []
    pygame.draw.rect(screen, (240, 240, 255), (1100, 40, 200, 710))
    label = large_font.render("Curve List", True, (0, 0, 80))
    screen.blit(label, (1120, 50))
    for i, color in enumerate(curve_colors):
        y = 90 + i * 40
        rect = pygame.Rect(1110, y, 170, 30)
        pygame.draw.rect(screen, (255, 255, 255), rect)
        pygame.draw.rect(screen, color, (1115, y + 5, 20, 20))
        pygame.draw.rect(screen, line_colors[i], (1140, y + 5, 20, 20))
        tag = "(Active)" if i == active_curve else ""
        text = font.render(f"Curve {i+1} {tag}", True, (0, 0, 0))
        screen.blit(text, (1170, y + 7))
        curve_rects.append((rect, i))

    # Color palette
    screen.blit(large_font.render("Pick Line Color:", True, (0, 0, 80)), (1120, 650))
    for idx, color in enumerate(color_palette):
        rect = pygame.Rect(1115 + (idx % 3) * 60, 690 + (idx // 3) * 30, 50, 20)
        pygame.draw.rect(screen, color, rect)
        pygame.draw.rect(screen, (0, 0, 0), rect, 1)
        if rect.collidepoint(mouse_pos) and pygame.mouse.get_pressed()[0]:
            line_colors[active_curve] = color


def save_image():
    pygame.image.save(screen, "bezier_editor_export.png")
    print("Image saved as bezier_editor_export.png")

def get_point_under_mouse(points, pos, radius=10):
    for i, pt in enumerate(points):
        if (pt[0] - pos[0]) ** 2 + (pt[1] - pos[1]) ** 2 <= radius ** 2:
            return i
    return None

running = True
while running:
    screen.fill((255, 255, 255))
    mouse_pos = pygame.mouse.get_pos()

    if grid_enabled:
        draw_grid()

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        elif event.type == pygame.MOUSEBUTTONDOWN:
            if mouse_pos[0] < 1100:
                if event.button == 1:
                    idx = get_point_under_mouse(curves[active_curve], mouse_pos)
                    if idx is not None:
                        dragging_point = idx
                    else:
                        curves[active_curve].append(mouse_pos)
                elif event.button == 3:
                    idx = get_point_under_mouse(curves[active_curve], mouse_pos)
                    if idx is not None:
                        del curves[active_curve][idx]
        elif event.type == pygame.MOUSEBUTTONUP:
            if event.button == 1:
                dragging_point = None
        elif event.type == pygame.MOUSEMOTION:
            if dragging_point is not None:
                curves[active_curve][dragging_point] = mouse_pos
        elif event.type == pygame.KEYDOWN:
            if event.key == pygame.K_c:
                curves[active_curve].clear()
            elif event.key == pygame.K_s:
                save_image()
            elif event.key == pygame.K_SPACE:
                show_spline = not show_spline
            elif event.key == pygame.K_g:
                grid_enabled = not grid_enabled
            elif event.key == pygame.K_n:
                curves.append([])
                curve_colors.append((random.randint(50, 200), random.randint(50, 200), random.randint(50, 200)))
                line_colors.append((random.randint(200, 255), random.randint(200, 255), random.randint(200, 255)))
                control_line_colors.append((random.randint(100, 150), random.randint(100, 150), random.randint(100, 150)))
                active_curve = len(curves) - 1
            elif event.key == pygame.K_TAB:
                active_curve = (active_curve + 1) % len(curves)

    if pygame.mouse.get_pressed()[0]:
        for rect, i in curve_rects:
            if rect.collidepoint(mouse_pos):
                active_curve = i

    for i, pts in enumerate(curves):
        curve_color = curve_colors[i]
        line_color = line_colors[i]
        control_color = control_line_colors[i]

        if len(pts) >= 2:
            pygame.draw.lines(screen, control_color, False, pts, 1)

        for pt in pts:
            pygame.draw.circle(screen, curve_color, pt, 7)

        draw_labels(pts)

        if show_spline:
            draw_spline(pts, line_color)
        else:
            for j in range(0, len(pts) - 3, 3):
                draw_bezier(pts[j:j+4], line_color)

    draw_ui()
    draw_sidebar(mouse_pos)
    pygame.display.flip()
    clock.tick(60)

pygame.quit()
sys.exit()

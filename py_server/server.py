from flask import Flask, request, jsonify
import random

app = Flask(__name__)

MAP_WIDTH = 28
MAP_HEIGHT = 16
SAFE_DISTANCE = 2
CELL_SIZE = 50
DIRECTIONS = ["U", "D", "L", "R", "N"]

def manhattan(p1, p2):
    return abs(p1["x"] - p2["x"]) + abs(p1["y"] - p2["y"])

def in_bounds(x, y):
    return 0 <= x < MAP_WIDTH and 0 <= y < MAP_HEIGHT

def bomb_will_hit(pos, bomb):
    bx, by = bomb["position"]["x"], bomb["position"]["y"]
    r = bomb.get("range", 1)
    return (pos["x"] == bx and abs(pos["y"] - by) <= r) or \
           (pos["y"] == by and abs(pos["x"] - bx) <= r)

def get_safe_directions(my_pos, bombs):
    unsafe = set()
    for bomb in bombs:
        if manhattan(my_pos, bomb["position"]) <= bomb.get("range", 1) + SAFE_DISTANCE:
            bx, by = bomb["position"]["x"], bomb["position"]["y"]
            if bx == my_pos["x"]:
                unsafe.add("U" if by > my_pos["y"] else "D")
            elif by == my_pos["y"]:
                unsafe.add("R" if bx > my_pos["x"] else "L")
    return [d for d in DIRECTIONS if d not in unsafe]

def is_safe_here(my_pos, bombs):
    for bomb in bombs:
        if bomb_will_hit(my_pos, bomb):
            return False
    return True

def greedy_move(my_pos, game_map):
    best_dir = "N"
    best_score = -999
    directions = {"U": (0, 1), "D": (0, -1), "L": (-1, 0), "R": (1, 0)}
    for d, (dx, dy) in directions.items():
        nx, ny = my_pos["x"] + dx, my_pos["y"] + dy
        if not in_bounds(nx, ny):
            continue
        cell = game_map[ny][nx]
        terrain = cell.get("terrain", "P")
        owner = cell.get("ownership", "N")
        if terrain in ("I", "N"):
            score = -10
        elif owner == "N":
            score = 5
        elif owner == "B":
            score = 3
        else:
            score = 0
        if score > best_score:
            best_score = score
            best_dir = d
    return best_dir

def should_place_bomb(my_pos, data):
    enemies = data.get("other_players", [])
    for e in enemies:
        if manhattan(my_pos, e["position"]) <= 3:
            return random.random() < 0.3
    return random.random() < 0.1


@app.route("/api/v1/command", methods=["POST"])
def command():
    try:
        data = request.get_json(force=True)
        print(data)
        my = data.get("my_player", {})
        px = my.get("position", {}).get("x", 0)
        py = my.get("position", {}).get("y", 0)
        my_pos = {"x": int(px // CELL_SIZE), "y": int(py // CELL_SIZE)}
        my_pos["x"] = max(0, min(my_pos["x"], MAP_WIDTH - 1))
        my_pos["y"] = max(0, min(my_pos["y"], MAP_HEIGHT - 1))
        game_map = data.get("map", [])
        bombs = data.get("bombs", [])
        safe_dirs = get_safe_directions(my_pos, bombs)
        move_dir = "N"
        place_bomb = False
        stride = 0
        if not is_safe_here(my_pos, bombs):
            move_dir = safe_dirs[0] if safe_dirs else "N"
        else:
            move_dir = greedy_move(my_pos, game_map)
            if move_dir not in safe_dirs:
                move_dir = safe_dirs[0] if safe_dirs else "N"
            place_bomb = should_place_bomb(my_pos, data)

        # 修复：使用计算出的 place_bomb 而不是硬编码的 False
        result = {"direction": move_dir, "is_place_bomb": place_bomb}

    except Exception as e:
        result = {"direction": "N", "is_place_bomb": False}
    print("Response:", result)
    return jsonify(result)

@app.route("/api/v1/ping", methods=["HEAD"])
def ping():
    return "", 200

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=4999)

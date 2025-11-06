import java.util.*;

class Game {
    private static final int FIELD_SIZE = 5;
    private char[][] field = new char[FIELD_SIZE][FIELD_SIZE];
    private Random random = new Random();
    private Player player;
    private Castle castle;
    private List<Monster> monsters = new ArrayList<>();
    private int lives = 3;
    private int level = 1;

    // Список загадок и ответов
    private List<String> riddles = Arrays.asList(
            "Что растёт вниз головой?",
            "Что можно поймать, но нельзя удержать?",
            "Что всегда перед тобой, но ты не можешь его поймать?",
            "Что идёт вверх и вниз, но не двигается?",
            "Что можно увидеть один раз в минуте, дважды в моменте, но ни разу в тысяче лет?"
    );

    private List<String> answers = Arrays.asList(
            "сосна",
            "дыхание",
            "будущее",
            "лестница",
            "м"
    );

    public static void main(String[] args) {
        new Game().start();
    }

    private void start() {
        initField();
        selectDifficulty();
        generateCastle();
        generatePlayer();
        generateMonsters();
        gameLoop();
    }

    private void initField() {
        for (int y = 0; y < FIELD_SIZE; y++) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                field[x][y] = '.';
            }
        }
    }

    private void selectDifficulty() {
        // Use nextLine to avoid newline issues after reading input
        Scanner sc = new Scanner(System.in);
        System.out.println("Выберите уровень сложности (1-легкий, 2-средний):");
        while (true) {
            String line = sc.nextLine().trim();
            if (line.equals("1") || line.equals("2")) {
                level = Integer.parseInt(line);
                break;
            } else {
                System.out.println("Некорректный ввод. Попробуйте снова:");
            }
        }
    }

    private void generateCastle() {
        int x = random.nextInt(FIELD_SIZE);
        castle = new Castle(x, 0);
        field[x][0] = 'C';
    }

    private void generatePlayer() {
        int x = random.nextInt(FIELD_SIZE);
        int y = FIELD_SIZE - 1;
        player = new Player(x, y);
        field[x][y] = 'P';
    }

    private void generateMonsters() {
        // Распределение монстров по карте
        int maxMonsters = level == 1 ? 3 : 5;
        for (int i = 0; i < maxMonsters; i++) {
            int x, y;
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while ((x == player.x && y == player.y)
                    || (x == castle.x && y == castle.y)
                    || isMonsterAt(x, y));

            Monster m = new Monster(x, y, level);
            monsters.add(m);
            field[x][y] = 'M';
        }

        // Назначаем загадки/ответы монстрам
        Collections.shuffle(riddles);
        Collections.shuffle(answers);
        for (int i = 0; i < monsters.size(); i++) {
            Monster mm = monsters.get(i);
            mm.setRiddle(riddles.get(i % riddles.size()));
            mm.setAnswer(answers.get(i % answers.size()));
        }
    }

    private boolean isMonsterAt(int x, int y) {
        for (Monster m : monsters) {
            if (m.getX() == x && m.getY() == y)
                return true;
        }
        return false;
    }

    private void gameLoop() {
        Scanner sc = new Scanner(System.in);
        boolean gameOver = false;
        while (!gameOver && lives > 0) {
            printField();
            System.out.println("Жизни: " + lives);
            System.out.print("Введите команду для перемещения (w/a/s/d): ");
            String command = sc.nextLine().trim().toLowerCase();
            int nx = player.x, ny = player.y;
            switch (command) {
                case "w": ny -= 1; break;
                case "s": ny += 1; break;
                case "a": nx -= 1; break;
                case "d": nx += 1; break;
                default:
                    System.out.println("Некорректная команда");
                    continue;
            }
            if (!isValidMove(nx, ny))
                continue;
            movePlayer(nx, ny);
            Monster monster = checkMonsterCollision();
            if (monster != null) {
                System.out.println("Встреча с монстром! Решите загадку:");
                System.out.println(monster.getRiddle());
                String answer = sc.nextLine().trim().toLowerCase();
                if (answer.equals(monster.getAnswer().toLowerCase())) {
                    System.out.println("Правильно! Монстр побеждён");
                    // Удаляем монстра и оставляем клетку за игроком как 'P'
                    monsters.remove(monster);
                    field[monster.getX()][monster.getY()] = 'P';
                } else {
                    lives--;
                    System.out.println("Неправильно! Жизни: " + lives);
                    if (lives == 0) {
                        System.out.println("Игра окончена, проигрыш");
                        break;
                    }
                    // Игрок остаётся на своей клетке
                }
            }

            if (player.x == castle.x && player.y == castle.y) {
                System.out.println("Поздравляем! Вы достигли замка");
                gameOver = true;
            }
        }
        printField();
        System.out.println("Конец игры");
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < FIELD_SIZE && y >= 0 && y < FIELD_SIZE;
    }

    private void movePlayer(int x, int y) {
        field[player.x][player.y] = '.';
        player.x = x;
        player.y = y;
        field[x][y] = 'P';
    }

    private Monster checkMonsterCollision() {
        for (Monster m : monsters) {
            if (m.getX() == player.x && m.getY() == player.y)
                return m;
        }
        return null;
    }

    private void printField() {
        System.out.println("+---+---+---+---+---+");
        for (int y = 0; y < FIELD_SIZE; y++) {
            System.out.print("|");
            for (int x = 0; x < FIELD_SIZE; x++) {
                System.out.print(" " + field[x][y] + " |");
            }
            System.out.println();
            System.out.println("+---+---+---+---+---+");
        }
    }

    class Player {
        int x, y;
        Player(int x, int y) { this.x = x; this.y = y; }
    }

    class Castle {
        int x, y;
        Castle(int x, int y) { this.x = x; this.y = y; }
    }

    class Monster {
        // Строки для представления размеров
        static final String SIZE_SMALL = "SMALL";
        static final String SIZE_MEDIUM = "MEDIUM";
        static final String SIZE_BIG = "BIG";

        int x, y;
        String riddle;
        String answer;
        String size; // Тип изменился на String

        Monster(int x, int y, int level) {
            this.x = x;
            this.y = y;
            setSize(level);
        }

        void setRiddle(String riddle) {
            this.riddle = riddle;
        }

        void setAnswer(String answer) {
            this.answer = answer;
        }

        void setSize(int level) {
            if (level == 2) {
                this.size = SIZE_BIG;
            } else {
                double randSize = random.nextDouble();
                if (randSize < 0.33) {
                    this.size = SIZE_SMALL;
                } else if (randSize < 0.66) {
                    this.size = SIZE_MEDIUM;
                } else {
                    this.size = SIZE_BIG;
                }
            }
        }

        int getX() { return x; }
        int getY() { return y; }

        String getRiddle() { return riddle; }

        String getAnswer() { return answer; }

        String getSize() { return size; }
    }
}
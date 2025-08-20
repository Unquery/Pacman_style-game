import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class StartWindow extends JFrame {

    private Thread timeThread;
    private Thread moveThread;
    private Thread exitThread;

    private volatile int lives = 3;
    private final ArrayList<Ghost> ghosts = new ArrayList<>();

    private volatile boolean right = false;
    private volatile boolean left  = false;
    private volatile boolean up    = false;
    private volatile boolean down  = false;

    private final Window parent;

    private int pacmanIndexX = 1;
    private int pacmanIndexY = 1;

    private int pacmanStartPosX;
    private int pacmanStartPosY;

    private volatile int pacmanSpeed = 600; // ms per step

    private int sizeX = -1;
    private int sizeY = -1;
    private int[][] tableArr;

    private BuffImageIconRenderer renderer;
    private JTable table;
    private JLabel livesL;
    private JLabel pointsL;
    private int points = 0;

    private static final int WALL   = 0;
    private static final int STAR   = 1;
    private static final int PACMAN = 2;
    private static final int GHOST  = 3;
    private static final int EMPTY  = -1;

    private final Random rng = new Random();

    public StartWindow(Window window) {
        this.parent = window;
        buildTopBar();
        selectBoardSize();
        initBoard();
        buildTable();
        setupKeyListeners();
        startPacmanThread();
        startGhosts();
        startTimerThread();
        startExitWatcherThread();
        layoutUI();
    }


    private void buildTopBar() {
        livesL = new JLabel();
        livesL.setOpaque(true);
        livesL.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 30));
        livesL.setText("     Lives:" + lives);
        livesL.setForeground(Color.WHITE);
        livesL.setBackground(Color.BLACK);

        pointsL = new JLabel();
        pointsL.setOpaque(true);
        pointsL.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 30));
        pointsL.setText("Points:" + points + "     ");
        pointsL.setForeground(Color.WHITE);
        pointsL.setBackground(Color.BLACK);
    }

    private void selectBoardSize() {
        do {
            if (sizeY != -1 && sizeX != -1) {
                JOptionPane.showMessageDialog(
                        null,
                        "This size is not available yet.\nPlease choose between 10 and 100 (inclusive).",
                        "Wrong coordinates",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            String sizeXstring = JOptionPane.showInputDialog(
                    null,
                    "Enter the X coordinate of a Table (10..100)",
                    "Construction...",
                    JOptionPane.QUESTION_MESSAGE
            );
            String sizeYstring = JOptionPane.showInputDialog(
                    null,
                    "Enter the Y coordinate of a Table (10..100)",
                    "Construction...",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (sizeYstring != null && sizeXstring != null) {
                try {
                    sizeX = Integer.parseInt(sizeXstring.trim());
                    sizeY = Integer.parseInt(sizeYstring.trim());
                } catch (NumberFormatException nfe) {
                    sizeX = -1;
                    sizeY = -1;
                }
            } else {
                sizeX = -1;
                sizeY = -1;
            }
        } while (sizeX < 10 || sizeX > 100 || sizeY < 10 || sizeY > 100);
    }

    private void initBoard() {
        tableArr = new int[sizeY][sizeX];
        makeMaze(tableArr);

        int[] pos = findRandomCellWithValue(STAR);
        if (pos == null) {
            pos = findRandomCellWithValue(EMPTY);
            if (pos == null) pos = new int[]{1,1};
        }
        pacmanIndexY = pacmanStartPosY = pos[0];
        pacmanIndexX = pacmanStartPosX = pos[1];
        tableArr[pacmanIndexY][pacmanIndexX] = PACMAN;
    }

    private void buildTable() {
        PacmanTable model = new PacmanTable(sizeY, sizeX);
        for (int i = 0; i < sizeY; i++) {
            for (int j = 0; j < sizeX; j++) {
                model.setValueAt(tableArr[i][j], i, j);
            }
        }
        table = new JTable(model);

        renderer = new BuffImageIconRenderer(tableArr);
        table.setDefaultRenderer(Object.class, renderer);

        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setSize(700, 700);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i)
                    .setMinWidth((int) (getWidth() / (table.getColumnCount() + 0.5)));
            table.setRowHeight(getHeight() / (table.getRowCount() + 3));
        }
    }

    private void layoutUI() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.BLACK);

        JLabel timeL = new JLabel();
        timeL.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 30));
        timeL.setBackground(Color.BLACK);
        timeL.setForeground(Color.WHITE);
        timeL.setOpaque(true);

        panel.add(pointsL);
        panel.add(timeL);
        panel.add(livesL);

        getContentPane().setBackground(Color.BLACK);
        getContentPane().add(panel, BorderLayout.SOUTH);
        getContentPane().add(table, BorderLayout.NORTH);

        this.timeLabel = timeL;

        addKeyListener(new ExitKeyListener(this));
        setFocusable(true);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) { /* no-op */ }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!isLeft() && !isRight() && !isUp() && !isDown()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_A -> set_Left(true);
                        case KeyEvent.VK_W -> set_Up(true);
                        case KeyEvent.VK_D -> set_Right(true);
                        case KeyEvent.VK_S -> set_Down(true);
                    }
                }
            }

            @Override public void keyReleased(KeyEvent e) { /* no-op */ }
        });
    }

    private void startPacmanThread() {
        moveThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                boolean moved = false;
                try {
                    if (isDown())  moved = attemptPacmanStep(+1, 0, 2, 1,  0);
                    if (isUp())    moved = attemptPacmanStep(-1, 0, 1, -1, 0) || moved;
                    if (isLeft())  moved = attemptPacmanStep(0, -1, 3, 0,  -1) || moved;
                    if (isRight()) moved = attemptPacmanStep(0, +1, 4, 0,  +1) || moved;

                    if (!moved) {
                        Thread.sleep(10);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Pacman-Move");
        moveThread.start();
    }

    /**
     * Tries to move Pac-Man by (dy, dx). Returns true if moved (and sleeps pacmanSpeed).
     */
    private boolean attemptPacmanStep(int dy, int dx, int rendererDirCode, int eventDY, int eventDX) throws InterruptedException {
        final int newY = pacmanIndexY + dy;
        final int newX = pacmanIndexX + dx;

        if (dy == 1 && dx == 0) set_Down(false);
        if (dy == -1 && dx == 0) set_Up(false);
        if (dy == 0 && dx == -1) set_Left(false);
        if (dy == 0 && dx == 1) set_Right(false);

        if (isAWall(newY, newX)) return false;

        if (isGhost(newY, newX)) {
            pacmanDeath();
            return true;
        }

        if (isAStar(newY, newX)) {
            points += 10;
            updatePointsLabel();
        }
        if (isAFruit(newY, newX)) {
            points += 10;
            updatePointsLabel();
            makeARandomEvent(eventDY, eventDX);
        }

        final int oldY = pacmanIndexY;
        final int oldX = pacmanIndexX;

        SwingUtilities.invokeLater(() -> {
            renderer.changeDirection(rendererDirCode);
            renderer.setVAt(oldY, oldX, EMPTY);
            tableArr[oldY][oldX] = EMPTY;

            renderer.setVAt(newY, newX, PACMAN);
            tableArr[newY][newX] = PACMAN;

            table.repaint();
        });

        pacmanIndexY = newY;
        pacmanIndexX = newX;

        Thread.sleep(pacmanSpeed);
        return true;
    }

    private JLabel timeLabel;

    private void startTimerThread() {
        timeThread = new Thread(() -> {
            MyTimer mt = new MyTimer();
            SwingUtilities.invokeLater(() -> timeLabel.setText("00:00:00"));
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    String t = mt.addSecond();
                    SwingUtilities.invokeLater(() -> timeLabel.setText(t));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Game-Timer");
        timeThread.start();
    }

    private void startExitWatcherThread() {
        exitThread = new Thread(() -> {
            boolean allEaten = false;
            boolean lose = false;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (lives <= 0) {
                        lose = true;
                        break;
                    }

                    allEaten = areAllStarsEaten();

                    if (allEaten) break;

                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            interruptAll();
            if (lose) {
                disposeLose();
            } else if (allEaten) {
                dispose();
            }
        }, "Exit-Watcher");
        exitThread.start();
    }

    private boolean areAllStarsEaten() {
        for (int i = 0; i < tableArr.length; i++) {
            for (int j = 0; j < tableArr[i].length; j++) {
                if (tableArr[i][j] == STAR) return false;
            }
        }
        return true;
    }

    public void startGhosts() {
        int numberOfGhosts = (sizeX + sizeY) / 8;
        for (int i = 0; i < numberOfGhosts; i++) {
            ghosts.add(new Ghost(renderer, tableArr, table, this));
        }
    }

    @Override
    public void dispose() {
        String userName = JOptionPane.showInputDialog(
                null,
                "Enter your username",
                "Saving the results...",
                JOptionPane.QUESTION_MESSAGE
        );
        try {
            Score.openWriter();
            Score.write(new Score(userName, points));
        } catch (Exception e) {
            return;
        }
        super.dispose();
    }

    public void disposeLose() {
        JOptionPane.showMessageDialog(
                null,
                "Sorry, you lost",
                "Thank you for playing",
                JOptionPane.PLAIN_MESSAGE
        );
        super.dispose();
    }

    public void disposeExit() {

        interruptAll();
        if (exitThread != null) exitThread.interrupt();
        JOptionPane.showMessageDialog(
                null,
                "You exited the game, you will receive 0 points",
                "Info",
                JOptionPane.WARNING_MESSAGE
        );
        super.dispose();
    }

    public void interruptAll() {
        if (timeThread != null) timeThread.interrupt();
        if (moveThread != null) moveThread.interrupt();
        if (exitThread != null) exitThread.interrupt();
        for (Ghost g : ghosts) {
            g.interruptAll();
        }
        parent.setVisible(true);
    }

    public static void makeMaze(int[][] maze) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (j % 2 == 0 || i % 2 == 0 || j == maze[i].length - 1 || i == maze.length - 1) {
                    maze[i][j] = WALL;
                } else {
                    maze[i][j] = STAR;
                }
            }
        }
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == STAR && j != 0 && i != 0 && j != maze[i].length - 1 && i != maze.length - 1) {
                    deleteWalls(maze, i, j);
                }
            }
        }
        generateBorders(maze);
        makePath(maze);
        deleteFourCorners(maze);
    }

    public static void deleteWalls(int[][] maze, int indexY, int indexX) {
        if (Math.random() < 0.2) maze[indexY + 1][indexX] = STAR;
        if (Math.random() < 0.2) maze[indexY - 1][indexX] = STAR;
        if (Math.random() < 0.2) maze[indexY][indexX + 1] = STAR;
        if (Math.random() < 0.2) maze[indexY][indexX - 1] = STAR;
    }

    public static void generateBorders(int[][] maze) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (j == 0 || i == 0 || j == maze[i].length - 1 || i == maze.length - 1) {
                    maze[i][j] = WALL;
                }
            }
        }
    }

    public static void makePath(int[][] maze) {
        int freeCells;
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == STAR && j != 0 && i != 0 && j != maze[i].length - 1 && i != maze.length - 1) {
                    freeCells = getFreeCells(maze, i, j);
                    if (freeCells < 2) {
                        if (j < maze[i].length / 2) {
                            if (i < maze.length / 2) {
                                maze[i][j + 1] = STAR;
                                maze[i + 1][j] = STAR;
                            } else {
                                maze[i][j + 1] = STAR;
                                maze[i - 1][j] = STAR;
                            }
                        }
                        if (j >= maze[i].length / 2) {
                            if (i < maze.length / 2) {
                                maze[i][j - 1] = STAR;
                                maze[i + 1][j] = STAR;
                            } else {
                                maze[i][j - 1] = STAR;
                                maze[i - 1][j] = STAR;
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getFreeCells(int[][] maze, int indexY, int indexX) {
        return maze[indexY - 1][indexX]
                + maze[indexY + 1][indexX]
                + maze[indexY][indexX - 1]
                + maze[indexY][indexX + 1];
    }

    public static void deleteFourCorners(int[][] maze) {
        if (maze.length >= 8 && maze[0].length >= 10) {
            maze[3][4] = STAR;
            maze[maze.length - 4][4] = STAR;
            maze[3][maze[0].length - 5] = STAR;
            maze[maze.length - 4][maze[0].length - 5] = STAR;
        }
    }


    private int[] findRandomCellWithValue(int value) {
        int count = 0;
        int selY = -1, selX = -1;
        for (int y = 0; y < tableArr.length; y++) {
            for (int x = 0; x < tableArr[y].length; x++) {
                if (tableArr[y][x] == value) {
                    count++;
                    if (rng.nextInt(count) == 0) {
                        selY = y;
                        selX = x;
                    }
                }
            }
        }
        return (selY >= 0) ? new int[]{selY, selX} : null;
    }

    private void updatePointsLabel() {
        SwingUtilities.invokeLater(() -> pointsL.setText("Points:" + points + "     "));
    }

    public boolean isAWall(int y, int x) {
        return y < 0 || x < 0 || y >= tableArr.length || x >= tableArr[0].length || tableArr[y][x] == WALL;
    }

    public boolean isGhost(int y, int x) {
        return !(y < 0 || x < 0 || y >= tableArr.length || x >= tableArr[0].length) && tableArr[y][x] == GHOST;
    }

    public boolean isAStar(int y, int x) {
        return !(y < 0 || x < 0 || y >= tableArr.length || x >= tableArr[0].length) && tableArr[y][x] == STAR;
    }

    public boolean isAFruit(int y, int x) {
        return !(y < 0 || x < 0 || y >= tableArr.length || x >= tableArr[0].length)
                && tableArr[y][x] > 3 && tableArr[y][x] < 9;
    }

    public boolean isRight() { return right; }
    public boolean isLeft()  { return left; }
    public boolean isUp()    { return up; }
    public boolean isDown()  { return down; }
    public void set_Right(boolean right) { this.right = right; }
    public void set_Left(boolean left)   { this.left = left; }
    public void set_Up(boolean up)       { this.up = up; }
    public void set_Down(boolean down)   { this.down = down; }

    public void pacmanDeath() {
        points -= 20;
        lives--;
        updatePointsLabel();
        SwingUtilities.invokeLater(() -> {
            livesL.setText("     Lives:" + lives);
            renderer.setVAt(pacmanIndexY, pacmanIndexX, EMPTY);
            tableArr[pacmanIndexY][pacmanIndexX] = EMPTY;
            pacmanIndexX = pacmanStartPosX;
            pacmanIndexY = pacmanStartPosY;
            renderer.setVAt(pacmanIndexY, pacmanIndexX, PACMAN);
            tableArr[pacmanIndexY][pacmanIndexX] = PACMAN;
            table.repaint();
        });
    }

    public void makeARandomEvent(int y, int x) {
        double decision = Math.random();
        if (decision < 0.2) {
            new Thread(() -> {
                try {
                    pacmanSpeed = 300;
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                } finally {
                    pacmanSpeed = 600;
                }
            }, "Event-SpeedUp").start();
        } else if (decision < 0.4) {
            new Thread(() -> {
                try {
                    pacmanSpeed = 900;
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                } finally {
                    pacmanSpeed = 600;
                }
            }, "Event-SlowDown").start();
        } else if (decision < 0.6) {
            SwingUtilities.invokeLater(() -> {
                renderer.setVAt(pacmanIndexY, pacmanIndexX, EMPTY);
                tableArr[pacmanIndexY][pacmanIndexX] = EMPTY;
                int ny = clamp(pacmanIndexY + y, 0, tableArr.length - 1);
                int nx = clamp(pacmanIndexX + x, 0, tableArr[0].length - 1);
                renderer.setVAt(ny, nx, EMPTY);
                tableArr[ny][nx] = EMPTY;

                int[] pos = findRandomCellWithValue(STAR);
                if (pos == null) pos = new int[]{pacmanStartPosY, pacmanStartPosX};
                pacmanIndexY = pos[0];
                pacmanIndexX = pos[1];
                renderer.setVAt(pacmanIndexY, pacmanIndexX, PACMAN);
                tableArr[pacmanIndexY][pacmanIndexX] = PACMAN;
                table.repaint();
            });
        } else if (decision < 0.8) {
            ghosts.add(new Ghost(renderer, tableArr, table, this));
        } else {
            new Thread(() -> {
                try {
                    for (Ghost g : ghosts) {
                        g.freeze();
                    }
                } catch (InterruptedException ignored) {
                }
            }, "Event-FreezeGhosts").start();
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

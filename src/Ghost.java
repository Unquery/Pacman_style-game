import javax.swing.*;

public class Ghost {
    private int posX=1;
    private int posY=1;

    private boolean up = false;
    private boolean down = false;

    private boolean left = false;
    private boolean right = false;

    private int loop;
    private boolean freeze = false;

    private final BuffImageIconRenderer renderer;

    private final int[][]tableArr;

    private JTable table;

    private int prevVal = 1;

    private Thread moveThread;
    private final StartWindow parent;

    private Thread freezeThread = new Thread();
    private Thread upgradesThread;
    private final String monitor = " ";

    public Ghost(BuffImageIconRenderer renderer, int[][] tableArr, JTable table, StartWindow parent) {
        this.renderer = renderer;
        this.tableArr = tableArr;
        this.table = table;
        this.parent = parent;
        setPosition();
        startMoving();
        startMakingUpgrades();
    }

    public boolean isAWallOrGhost(int y, int x){
        return tableArr[y][x] == 0 || tableArr[y][x] == 3;
    }

    public void setPosition(){
        boolean ghostInTable = false;
        for(int i = 0; i < tableArr.length && !ghostInTable; i++){
            for(int j = 0; j < tableArr[i].length && !ghostInTable;j++){
                if(tableArr[i][j] == 1){
                    if(Math.random() < 0.0005){
                        placeGhost(j, i);
                        ghostInTable = true;
                    }
                }
            }
            if(i == tableArr.length-1){
                i = 0;
            }
        }
    }

    private void placeGhost(int x, int y){
        tableArr[y][x] = 3;
        posX = x;
        posY = y;
    }

    public void startMoving(){
        moveThread = new Thread(()->{
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if(freeze){
                        Thread.sleep(50);
                        continue;
                    }

                    loop = 0;

                    Direction dir = pickDirection();
                    setDirectionFlags(dir);

                    int[] d = delta(dir);
                    moveGhost(d[0], d[1]);

                    Thread.sleep(600);
                }catch (Exception e){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        moveThread.start();
    }

    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private Direction pickDirection() {
        double d = java.util.concurrent.ThreadLocalRandom.current().nextDouble();
        if (d < 0.20) return Direction.UP;
        if (d < 0.40) return Direction.DOWN;
        if (d < 0.70) return Direction.LEFT;
        return Direction.RIGHT;
    }

    private void setDirectionFlags(Direction dir) {
        set_Up(dir == Direction.UP);
        set_Down(dir == Direction.DOWN);
        set_Left(dir == Direction.LEFT);
        set_Right(dir == Direction.RIGHT);
    }

    private int[] delta(Direction dir) {
        return switch (dir) {
            case UP    -> new int[]{0, -1};
            case DOWN  -> new int[]{0,  1};
            case LEFT  -> new int[]{-1, 0};
            case RIGHT -> new int[]{1,  0};
        };
    }

    private void moveGhost(int x, int y){
        if (!isAWallOrGhost(posY + y, posX + x)) {
            if (isAPacman(posY + y, posX + x)) {
                parent.pacmanDeath();
            }
            synchronized (monitor) {
                if (prevVal != 3 && prevVal != 2) {
                    renderer.setVAt(posY, posX, prevVal);
                    tableArr[posY][posX] = prevVal;
                }
            }
            prevVal = tableArr[posY + y][posX + x];
            renderer.setVAt(posY + y, posX + x, 3);
            posY+=y;
            posX+=x;
            tableArr[posY][posX] = 3;
            table.repaint();
        }
    }

    public void set_Up(boolean up) {
        if(!isAWallOrGhost(posY-1, posX)) {
            this.up = up;
        }else if(up){
            set_Down(true);
        }else{
            this.up = false;
        }
    }

    public void set_Down(boolean down) {
        loop++;
        if(loop == 2){
            this.down = false;
        }else {
            if (!isAWallOrGhost(posY + 1, posX)) {
                this.down = down;
            } else if (down) {
                set_Left(true);
            } else {
                this.down = false;
            }
        }
    }

    public void set_Left(boolean left) {
        if(!isAWallOrGhost(posY, posX-1)) {
            this.left = left;
        }else if(left){
            set_Right(true);
        }else{
            this.left = false;
        }
    }

    public void set_Right(boolean right) {
        if(!isAWallOrGhost(posY, posX+1)) {
            this.right = right;
        }else if(right){
            set_Up(true);
        }else{
            this.right = false;
        }
    }

    public boolean isAPacman(int y, int x){
        return tableArr[y][x] == 2;
    }

    public void startMakingUpgrades(){
        upgradesThread = new Thread(()->{
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(5000);
                    if(Math.random() < 0.25) {
                        synchronized (monitor) {
                            prevVal = 4+(int)(Math.random()*5);;
                        }
                    }
                }catch (InterruptedException e){return;}
            }
        });
        upgradesThread.start();
    }

    public void freeze() throws InterruptedException{
        freezeThread.interrupt();
        freezeThread = new Thread(()->{
            freeze = true;
            try {
                Thread.sleep(4000);
                freeze = false;
            }catch (InterruptedException e){return;}
        });
        freezeThread.start();
    }

    public void interruptAll(){
        if(freezeThread != null)
            freezeThread.interrupt();
        upgradesThread.interrupt();
        moveThread.interrupt();
    }
}

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class BuffImageIconRenderer implements TableCellRenderer {

    int [][]tableArr;

    BufferedImage pacman;
    BufferedImage pacmanLeft;
    BufferedImage pacmanRight;
    BufferedImage pacmanUp;
    BufferedImage pacmanDown;
    BufferedImage pacmanClosed;
    BufferedImage smallStar;
    BufferedImage ghost;

    boolean isClosed;

    ArrayList<BufferedImage> fruits = new ArrayList<>();

    BuffImageIconRenderer(int[][] arr){
        this.tableArr = arr;
        try {
            pacmanLeft = ImageIO.read(new File("src\\Images\\pacmanLeft.png"));
            pacmanRight = ImageIO.read(new File("src\\Images\\pacmanRight.png"));
            pacmanUp = ImageIO.read(new File("src\\Images\\pacmanUp.png"));
            pacmanDown = ImageIO.read(new File("src\\Images\\pacmanDown.png"));
            pacmanClosed = ImageIO.read(new File("src\\Images\\pacmanClosed.png"));
            smallStar = ImageIO.read(new File("src\\Images\\smallstar.png"));
            ghost = ImageIO.read(new File("src\\Images\\ghost.png"));

            fruits.add(ImageIO.read(new File("src\\Images\\strawberry.png")));
            fruits.add(ImageIO.read(new File("src\\Images\\watermelon.png")));
            fruits.add(ImageIO.read(new File("src\\Images\\cherry.png")));
            fruits.add(ImageIO.read(new File("src\\Images\\apple.png")));
            fruits.add(ImageIO.read(new File("src\\Images\\orange.png")));

            pacman = pacmanRight;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setVAt(int i, int j, int val){
        tableArr[i][j] = val;
    }

    static class ImagePanel extends JPanel {
        private final Image img;

        ImagePanel(Image img) {
            this.img = img;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        JPanel cell = new JPanel(new BorderLayout());
        int v = tableArr[row][column];

        switch (v) {
            case 1 -> cell.add(new ImagePanel(smallStar));
            case 0 -> cell.setBackground(Color.BLACK);
            case 2 -> cell.add(new ImagePanel(pacman));
            case -1 -> cell.setBackground(Color.WHITE);
            case 3 -> cell.add(new ImagePanel(ghost));
            case 4, 5, 6, 7, 8 -> {
                int idx = v - 4;
                if (idx < fruits.size()) {
                    cell.add(new ImagePanel(fruits.get(idx)));
                }
            }
            default -> cell.setBackground(Color.GRAY);
        }

        return cell;
    }

    public void changeDirection(int direction){
        if (isClosed) {
            pacman = pacmanClosed;
            isClosed = false;
            return;
        }

        pacman = switch (direction) {
            case 1 -> pacmanUp;
            case 2 -> pacmanDown;
            case 3 -> pacmanLeft;
            default -> pacmanRight;
        };
        isClosed = true;
    }
}

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Window extends JFrame {

    Window(){
        Window window = this;

        JPanel buttons = new JPanel();

        JButton exit = new JButton();
        JButton start = new JButton();
        JButton highScores = new JButton();
        exit.setText("EXIT");
        start.setText("START");
        highScores.setText("HIGH SCORES");

        exit.addActionListener((e)->{Score.closeWriter();dispose();});

        start.addActionListener((e)->{
            setVisible(false);
            SwingUtilities.invokeLater(()->new StartWindow(window));
        });


        highScores.addActionListener((e)->{
            setVisible(false);
            SwingUtilities.invokeLater(()->new HighScoresList(this));
        });

        exit.setPreferredSize(new Dimension(90,60));
        start.setPreferredSize(new Dimension(90,60));
        highScores.setPreferredSize(new Dimension(150,60));

        exit.setForeground(Color.WHITE);
        start.setForeground(Color.white);
        highScores.setForeground(Color.white);

        exit.setBackground(Color.black);
        start.setBackground(Color.black);
        highScores.setBackground(Color.black);

        exit.setBorderPainted(false);
        start.setBorderPainted(false);
        highScores.setBorderPainted(false);

        buttons.setLayout(new FlowLayout());
        buttons.add(start);
        buttons.add(highScores);
        buttons.add(exit);
        buttons.setBackground(Color.BLACK);
        try {
            BufferedImage menu = ImageIO.read(new File("src\\images\\menu.png"));
            JPanel image = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(menu, 0, 0, getWidth(), getHeight(), null);
                }
            };
            setContentPane(image);
        }catch (IOException e){
            e.printStackTrace();
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttons, BorderLayout.SOUTH);

        addKeyListener(new ExitKeyListener(this));

        setFocusable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500,300);
        setLocationRelativeTo(null);
        setVisible(true);

    }
}

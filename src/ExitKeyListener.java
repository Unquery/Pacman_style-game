import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ExitKeyListener implements KeyListener {
    StartWindow frame;
    Window frameMain;
    HighScoresList scores;
    ExitKeyListener(StartWindow startWindow){
        frame = startWindow;
    }
    ExitKeyListener(Window window){frameMain = window;}

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_Q && e.isControlDown()){
            if(frame != null) {
                frame.disposeExit();
            }
            if(frameMain != null){
                frameMain.dispose();
            }
            if(scores != null){
                scores.dispose();
            }
            try{
                Score.closeWriter();
            }catch (Exception ex){
                return;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

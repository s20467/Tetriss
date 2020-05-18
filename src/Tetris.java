import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

import static java.lang.Thread.sleep;

public class Tetris extends JFrame{
    int screenHeight;
    int screenWidth;
    int highScore;
    int score;
    BoardPanel boardPanel;
    Board board;
    JButton rozpocznij;
    JLabel up;
    JLabel down;
    JLabel left;
    JLabel right;
    JLabel highestScore;
    Box menuBox;
    JButton boardMenuButton;
    GridBagConstraints GBC;

    void menu(){
        setTitle("TETRIS");
        setSize(300, 270);
        setLocation((screenWidth-this.getWidth())/2, (screenHeight-this.getHeight())/2);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        menuInit();
        setVisible(true);
    }

    void menuInit(){
        rozpocznij = new JButton("Start");
        up = new JLabel("Strzalka w gore, aby obrocic blok");
        down = new JLabel("Strzalka w dol, aby obnizyc blok");
        right = new JLabel("Strzalka w prawo, aby przesunac blok w prawo");
        left = new JLabel("Strzalka w lewo, aby przesunac blok w lewo");
        highestScore = new JLabel("najwyzszy wynik: "+ highScore);
        menuBox = Box.createVerticalBox();
        menuBox.add(Box.createVerticalStrut(15));
        menuBox.add(rozpocznij);
        rozpocznij.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBox.add(Box.createVerticalStrut(15));
        menuBox.add(highestScore);
        highestScore.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBox.add(Box.createVerticalStrut(15));
        menuBox.add(up);
        up.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBox.add(Box.createVerticalStrut(15));
        menuBox.add(down);
        down.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBox.add(Box.createVerticalStrut(15));
        menuBox.add(right);
        right.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBox.add(Box.createVerticalStrut(15));
        menuBox.add(left);
        left.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(menuBox);

        rozpocznij.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                game();
            }
        });

    }

    void game(){
        remove(menuBox);
        setSize(417, 840);
        setLocation((screenWidth-this.getWidth())/2, (screenHeight-this.getHeight())/2);
        board = new Board();
        boardPanel = new BoardPanel();
        boardMenuButton = new JButton("Wroc");
        boardMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                score = board.getPoints();
                saveScore();
                remove(boardPanel);
                menu();
            }
        });
        boardPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        boardPanel.add(boardMenuButton);




        boardPanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_UP){
                    board.rotate();
                }
                else if(e.getKeyCode() == KeyEvent.VK_LEFT){
                    board.moveLeft();
                }
                else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    board.moveRight();
                }
                else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                    board.moveDown();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        add(boardPanel);
        boardPanel.setFocusable(true);
        boardPanel.requestFocus();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!(board.isLost())) {
                    boardPanel.repaint();
                    try {
                        sleep(100);
                    } catch (InterruptedException exc) {
                        System.out.println(exc.getMessage());
                    }
                }
                score = board.getPoints();
                saveScore();
                remove(boardPanel);
                menu();
            }
        });
        thread.start();

    }

    void saveScore(){
        if(score>highScore){
            highScore=score;
            try(FileOutputStream Fout = new FileOutputStream(new File("highestScore.txt"))){
                Fout.write((byte)score);
            }
            catch(IOException exc){
                System.out.println(exc.getMessage());
            }
        }

    }

    void loadScore(){
        try(FileInputStream Fin = new FileInputStream(new File("highestScore.txt"))) {
            highScore = (int)(Fin.read());
        }
        catch(IOException exc){
            System.out.println(exc.getMessage());
        }
    }

    public static void START(){
        Tetris frame = new Tetris();
        frame.screenHeight=Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.screenWidth=Toolkit.getDefaultToolkit().getScreenSize().width;
        frame.loadScore();
        frame.menu();
    }



    class BoardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(new Color(60, 60, 60));
            g.setColor(new Color(255, 0, 0));
            g.drawLine(0, 100, 400,100);
            g.setColor(new Color(0,255,0));
            g.drawString("Wynik: "+ board.getPoints(), 340, 45);
            boolean[][] currBoard = board.getBoard();
            for(int i=0; i<16; i++) {
                for (int j = 0; j < 8; j++) {
                    if(currBoard[i][j]) {
                        g.setColor(new Color(170, 170, 0));
                        g.fillRect(50*j, 50*i, 50, 50);
                        g.setColor(new Color(255, 255, 255));
                        g.drawRect(50*j, 50*i, 50, 50);
                    }
                }
            }
        }
    }

}

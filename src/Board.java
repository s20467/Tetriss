import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.lang.Thread.sleep;

public class Board {
    boolean lost;
    int points = 0;
    boolean[][] arr;
    Block block;

    class Position {
        int x;
        int y;
        public void copy(Position toCpy) {x = toCpy.getX(); y = toCpy.getY();}
        public Position(int x, int y) { this.x=x; this.y=y; }
        public void setY(int y) { this.y = y; }
        public void setX(int x) { this.x = x; }
        public int getX() { return x; }
        public int getY() { return y; }
    }

    public Board(){
        arr = new boolean[16][8];
        lost=false;
        block = new Block();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!lost){
                    try {
                        sleep(1000);
                        moveDown();
                    }
                    catch(InterruptedException exc){
                        System.out.println(exc.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    public boolean isLost() {
        return lost;
    }

    public int getPoints() {
        return points;
    }

    public void playSound(String path){
        File file = new File(path);
        try{
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(file));
            clip.start();
        }
        catch(Exception exc){
            System.out.println(exc.getMessage());
        }
    }

    public void moveRight(){
        boolean[][] blockArr = block.getFullArray();
        for(int i=0; i<16; i++){
            if(blockArr[i][7])
                return;
        }
        for(int i=0; i<16; i++){
            for(int j=6; j>=0; j--){
                if(blockArr[i][j])
                    blockArr[i][j + 1] = true;
            }
        }
        if(ifCollision(blockArr))
            return;
        block.moveRight();
    }

    public void moveLeft(){
        boolean[][] blockArr = block.getFullArray();
        for(int i=0; i<16; i++){
            if(blockArr[i][0])
                return;
        }
        boolean[][] tmp = new boolean[16][8];

        for(int i=0; i<16; i++){
            for(int j=1; j<8; j++){
                if(blockArr[i][j])
                    tmp[i][j - 1] = true;
            }
        }
        if(ifCollision(tmp))
            return;
        block.moveLeft();
    }

    public void moveDown(){
        boolean[][] blockArr = block.getFullArray();

        for(int i=0; i<8; i++){
            if(blockArr[15][i]) {
                for(int k=0; k<16; k++){
                    for(int l=0; l<8; l++){
                        arr[k][l] = arr[k][l] || blockArr[k][l];
                    }
                }
                checkIfPoints();
                generateBlock();
                return;
            }
        }

        boolean[][] tmp = new boolean[16][8];

        for(int i=14; i>0; i--){
            for(int j=0; j<8; j++){
                if(blockArr[i][j])
                    tmp[i + 1][j] = true;
            }
        }
        if(ifCollision(tmp)) {
            for (int k = 0; k < 16; k++) {
                for (int l = 0; l < 8; l++) {
                    arr[k][l] = arr[k][l] || blockArr[k][l];
                    }
                }
            checkIfPoints();
            generateBlock();
            return;
        }

        block.moveDown();

        for(int i=0; i<8; i++){
            if(blockArr[15][i]) {
                for(int k=0; k<16; k++){
                    for(int l=0; l<8; l++){
                        arr[k][l] = arr[k][l] || blockArr[k][l];
                    }
                }
                checkIfPoints();
                generateBlock();
                return;
            }
        }
    }

    public void rotate(){
        playSound("rotate.wav");
        if(block.blockPosition.getX()<0 || block.blockPosition.getX()>5)
            return;
        Block tmp = new Block();
        tmp.copy(block);
        tmp.rotate();
        if(!(ifCollision(tmp.getFullArray())))
            block.rotate();
    }

    public boolean ifCollision(boolean[][] tmp) {
        for(int i=0; i<16; i++) {
            for (int j = 0; j < 8; j++) {
                if(tmp[i][j] && arr[i][j])
                    return true;
            }
        }
        return false;
    }

    public void generateBlock(){
        playSound("placed.wav");
        block = new Block();
        if(ifCollision(block.getFullArray()))
            lost=true;
        for(int i=0; i<2; i++){
            for(int j=0; j<8; j++){
                if(arr[i][j]) {
                    lost = true;
                    playSound("lost.wav");
                }
            }
        }
    }

    public void checkIfPoints(){
        int counter=0;
        for(int i=0; i<16; i++){
            counter=0;
            for(int j=0; j<8; j++){
                if(arr[i][j])
                    counter++;
            }
            if(counter==8) {
                deleteRow(i);
                points++;
                playSound("score.wav");
            }
        }
    }

    public void deleteRow(int nr){
        for(int i=nr; i>0; i--){
            for(int j=0; j<8; j++){
                arr[i][j] = arr[i-1][j];
            }
        }
        for(int i=0; i<8; i++)
            arr[0][i]=false;
    }

    public boolean[][] getBoard(){
        boolean[][] blockArr = block.getFullArray();
        for(int i=0; i<16; i++){
            for(int j=0; j<8; j++) {
                blockArr[i][j]=(arr[i][j] || blockArr[i][j]);
            }
        }
        return blockArr;
    }


    class Block{
        Position blockPosition;    //left-upper corner of bounding square 3x3
        String blockShape;
        boolean[][] block;
        int actualRotation;
        boolean[][][] LBlocksRotations;
        boolean[][][] IBlocksRotations;
        boolean[][][] TBlocksRotations;
        boolean[][][] SquareBlocksRotations;
        public Block(){
            initBlocksRotations();
            block = new boolean[3][3];
            int rndm = (int)(Math.random()*4);
            switch(rndm){
                case 0:
                    blockPosition = new Position(0, 0);
                    actualRotation = 0;
                    blockShape = "L";
                    for(int i=0; i<3; i++){
                        for(int j=0; j<3; j++){
                            block[i][j] = LBlocksRotations[0][i][j];
                        }
                    }
                    break;
                case 1:
                    blockPosition = new Position(0, 0);
                    actualRotation = 0;
                    blockShape = "I";
                    for(int i=0; i<3; i++){
                        for(int j=0; j<3; j++){
                            block[i][j] = IBlocksRotations[0][i][j];
                        }
                    }
                    break;
                case 2:
                    blockPosition = new Position(0, 0);
                    actualRotation = 0;
                    blockShape = "T";
                    for(int i=0; i<3; i++){
                        for(int j=0; j<3; j++){
                            block[i][j] = TBlocksRotations[0][i][j];
                        }
                    }
                    break;
                case 3:
                    blockPosition = new Position(0, 0);
                    actualRotation = 0;
                    blockShape = "Square";
                    for(int i=0; i<3; i++){
                        for(int j=0; j<3; j++){
                            block[i][j] = SquareBlocksRotations[0][i][j];
                        }
                    }
                    break;
            }
        }

        public void rotate(){
            switch(blockShape){
                case "L":
                    block = LBlocksRotations[(actualRotation+1)%4];
                    actualRotation=(actualRotation+1)%4;
                    break;
                case "I":
                    block = IBlocksRotations[(actualRotation+1)%4];
                    actualRotation=(actualRotation+1)%4;
                    break;
                case "T":
                    block = TBlocksRotations[(actualRotation+1)%4];
                    actualRotation=(actualRotation+1)%4;
                    break;
                case "Square":
                    break;
            }
        }

        public void copy(Block toCpy){
            blockPosition.copy(toCpy.getBlockPosition());
            blockShape = toCpy.blockShape;
            for(int i=0; i<3; i++){
                for(int j=0; j<3; j++){
                    block[i][j]=toCpy.block[i][j];
                }
            }
            actualRotation = toCpy.actualRotation;
        }

        public void moveRight(){
            blockPosition.setX(blockPosition.getX()+1);
        }
        public void moveLeft(){
            blockPosition.setX(blockPosition.getX()-1);
        }
        public void moveDown(){
            blockPosition.setY(blockPosition.getY()+1);
        }
        public boolean[][] getFullArray(){
            boolean[][] fullArray = new boolean[16][8];
            for(int i=0; i<3; i++){
                for(int j=0; j<3; j++){
                    if(block[i][j])
                        fullArray[i + blockPosition.getY()][j + blockPosition.getX()] = true;
                }
            }
            return fullArray;
        }

        public void initBlocksRotations(){
            LBlocksRotations = new boolean[4][3][3];
            //rotation 0
                LBlocksRotations[0][0][1]=true;
                LBlocksRotations[0][1][1]=true;
                LBlocksRotations[0][2][1]=true;
                LBlocksRotations[0][2][2]=true;
            //rotation 1
                LBlocksRotations[1][1][0]=true;
                LBlocksRotations[1][1][1]=true;
                LBlocksRotations[1][1][2]=true;
                LBlocksRotations[1][2][0]=true;
            //rotation 2
                LBlocksRotations[2][0][1]=true;
                LBlocksRotations[2][1][1]=true;
                LBlocksRotations[2][2][1]=true;
                LBlocksRotations[2][0][0]=true;
            //rotation 3
                LBlocksRotations[3][1][0]=true;
                LBlocksRotations[3][1][1]=true;
                LBlocksRotations[3][1][2]=true;
                LBlocksRotations[3][0][2]=true;

            TBlocksRotations = new boolean[4][3][3];
            //rotation 0
                TBlocksRotations[0][1][0]=true;
                TBlocksRotations[0][1][1]=true;
                TBlocksRotations[0][1][2]=true;
                TBlocksRotations[0][2][1]=true;
            //rotation 1
                TBlocksRotations[1][0][1]=true;
                TBlocksRotations[1][1][1]=true;
                TBlocksRotations[1][2][1]=true;
                TBlocksRotations[1][1][0]=true;
            //rotation 2
                TBlocksRotations[2][1][0]=true;
                TBlocksRotations[2][1][1]=true;
                TBlocksRotations[2][1][2]=true;
                TBlocksRotations[2][0][1]=true;
            //rotation 3
                TBlocksRotations[3][0][1]=true;
                TBlocksRotations[3][1][1]=true;
                TBlocksRotations[3][2][1]=true;
                TBlocksRotations[3][1][2]=true;

            IBlocksRotations = new boolean[4][3][3];
            //rotation 0
                IBlocksRotations[0][0][1]=true;
                IBlocksRotations[0][1][1]=true;
                IBlocksRotations[0][2][1]=true;
            //rotation 1
                IBlocksRotations[1][1][0]=true;
                IBlocksRotations[1][1][1]=true;
                IBlocksRotations[1][1][2]=true;
            //rotation 2
                IBlocksRotations[2][0][1]=true;
                IBlocksRotations[2][1][1]=true;
                IBlocksRotations[2][2][1]=true;
            //rotation 3
                IBlocksRotations[3][1][0]=true;
                IBlocksRotations[3][1][1]=true;
                IBlocksRotations[3][1][2]=true;

            SquareBlocksRotations = new boolean[4][3][3];
            //rotation 0
                SquareBlocksRotations[0][0][0]=true;
                SquareBlocksRotations[0][1][0]=true;
                SquareBlocksRotations[0][0][1]=true;
                SquareBlocksRotations[0][1][1]=true;
            //rotation 1,2,3
                SquareBlocksRotations[3]=SquareBlocksRotations[2]=SquareBlocksRotations[1]=SquareBlocksRotations[0];


        }

        public Position getBlockPosition() {
            return blockPosition;
        }

    }

}

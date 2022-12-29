package com.sonicgdx.sonicswirl;

public class Tile {

    boolean isFlipped, empty;
    byte[] height,width;
    byte solidity;
    // 0 = solid from top, 1 = solid from bottom, 2 = solid from left, 3 = solid from right, 4 = solid from all sides
    float angle;
    Tile(byte[] heightArray, byte[] widthArray, float angle, byte solid, boolean flipped){
        this.empty = false;
        this.height = heightArray;
        this.width = widthArray;
        this.angle = angle;
        this.isFlipped = flipped;
        this.solidity = solid;

    }

    Tile()
    {
        this.empty = true;
        this.height = null;
        this.width = null;
        this.angle = 0;
    }

    public byte getHeight(int block)
    {
        if (empty) return 0;
        try{
            return height[block];
        }
        catch(Exception e){
            //Gdx.app.error("getHeight() error", String.valueOf(e));
            return 0;
        }

    }

    public byte getWidth(int block)
    {
        if (empty) return 0;
        else return width[block];
    }


}

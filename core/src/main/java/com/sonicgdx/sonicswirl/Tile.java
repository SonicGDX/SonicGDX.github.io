/*
 * Copyright 2023 SonicGDX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sonicgdx.sonicswirl;

public class Tile {

    boolean horizontalFlip, verticalFlip, empty;
    int[] heightArray, widthArray;
    int solidity;
    // 0 = solid from top, 1 = solid from bottom, 2 = solid from left, 3 = solid from right, 4 = solid from all sides
    float angle;
    Tile(int[] heightArray, int[] widthArray, float angle, int solidity, boolean flippedHorizontally, boolean flippedVertically) {
        this.empty = false;

        if (heightArray.length == 16) this.heightArray = heightArray;
        else throw new RuntimeException("heightArray Length = " + heightArray.length);
        if (widthArray.length == 16) this.widthArray = widthArray;
        else throw new RuntimeException("widthArray Length = " + widthArray.length);

        this.angle = angle;
        this.horizontalFlip = flippedHorizontally;
        this.verticalFlip = flippedVertically;
        this.solidity = solidity;
    }
    Tile()
    {
        this.empty = true;
        this.heightArray = null;
        this.widthArray = null;
        this.angle = 0;
    }

    public int getHeight(int block)
    {
        if (empty || block < 0 || block > 15) return 0;
        else {
            return heightArray[block];
        }

    }
    public int getWidth(int block)
    {
        if (empty || block < 0 || block > 15) return 0;
        else {
            return widthArray[block];
        }
    }

    public boolean isEmpty() {
        return empty;
    }

}

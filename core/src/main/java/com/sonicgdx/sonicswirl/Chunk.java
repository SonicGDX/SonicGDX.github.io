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

import com.badlogic.gdx.graphics.Texture;

import java.util.Collections;

public class Chunk {
    private Texture texture;
    private Tile[][] tileArray;
    private boolean empty;

    public Chunk(Texture texture, Tile[][] tileArray) {
        this.texture = texture;
        this.tileArray = tileArray;
        this.empty = false;
    }

    public Chunk(Tile[][] tileArray) {
        this.empty = true;
        this.tileArray = tileArray;
    }

    public Texture getTexture() {
        return texture;
    }

    public Tile[][] getTileArray() {
        return tileArray;
    }

    public boolean isEmpty() {
        return empty;
    }
}
package org.loader.music.pojo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/22 0022.
 */

public class MusicList  implements Serializable {
 private ArrayList<Music> results  = new ArrayList<Music>();

    public ArrayList<Music> getResults() {
        return results;
    }

    public void setResults(ArrayList<Music> results) {
        this.results = results;
    }
}

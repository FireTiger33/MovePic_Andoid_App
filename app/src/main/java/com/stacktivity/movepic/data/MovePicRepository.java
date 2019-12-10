package com.stacktivity.movepic.data;

import com.stacktivity.movepic.movepic.MovePicContract;

import java.util.ArrayList;
import java.util.List;

public class MovePicRepository implements MovePicContract.Repository {

    private ArrayList<String> paths;

    public MovePicRepository(List<String> paths) {
        this.paths = new ArrayList<>(paths);
    }

    public MovePicRepository() {
        this.paths = new ArrayList<>();
    }

    @Override
    public ArrayList<String> getAllPaths() {
        return paths;
    }

    @Override
    public String getBindPath(int pos) {
        return paths.get(pos);
    }

    @Override
    public int getBindButtonsCount() {
        return paths.size();
    }

    @Override
    public void addNewPath(String path) {
        paths.add(path);
    }
}

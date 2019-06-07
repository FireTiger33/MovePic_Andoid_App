package com.stacktivity.movepic.data;

import java.util.List;

public class BindPaths {
    private String name;
    private List<String> paths;

    public BindPaths(String name, List<String> paths) {
        this.name = name;
        this.paths = paths;
    }

    public List<String> getPaths() {
        return paths;
    }
}

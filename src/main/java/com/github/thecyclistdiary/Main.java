package com.github.thecyclistdiary;


import files.DefaultGpxRunner;
import files.MarkdownRunner;
import folder.GpxToMapWalker;
import map.gpx.DefaultGpxMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Program args should contain only the folder name");
        }
        String executionFolder = args[0];
        DefaultGpxMapper defaultGpxMapper = new DefaultGpxMapper.builder().build();
        var gpxToMapWalker = new GpxToMapWalker.builder<>()
                .setGpxFileRunner(new DefaultGpxRunner(defaultGpxMapper))
                .setPostVisitRunner(new MarkdownRunner())
                .build();

        Files.walkFileTree(Paths.get(executionFolder), gpxToMapWalker);
    }
}

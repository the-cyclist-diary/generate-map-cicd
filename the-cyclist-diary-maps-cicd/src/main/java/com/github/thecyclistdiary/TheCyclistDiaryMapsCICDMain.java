package com.github.thecyclistdiary;


public class TheCyclistDiaryMapsCICDMain {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Program args should contain only the folder name");
        }
        String executionFolder = args[0];
    }
}

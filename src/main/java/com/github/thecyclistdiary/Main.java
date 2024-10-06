package com.github.thecyclistdiary;


import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            throw new IllegalArgumentException("""
                    Program args should be exactly the following :
                    - content folder (where your gow files are located
                    - git directory
                    - your GitHub username
                    - your GitHub authentication token
                    """);
        }
        String executionFolder = args[0];
        String gitPath = args[1];
        String userName = args[2];
        String userToken = args[3];
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Path basePath = Paths.get(executionFolder);
        Path gitRepoPath = Path.of(gitPath);
        Set<String> modifiedGpxFiles = GitHelper.getModifiedGpxList(repositoryBuilder, gitRepoPath);
        var gpxToMapWalker = new GitAwareGpxToMapWalker(modifiedGpxFiles);
        LOGGER.info("Starting analysis of content folder {}", executionFolder);
        Files.walkFileTree(basePath, gpxToMapWalker);
        LOGGER.info("Done analysis of content folder");
        if (gpxToMapWalker.getExtractedResults().findAny().isPresent()){
            LOGGER.info("Commiting to git repository...");
            GitHelper.commitChanges(gitRepoPath, userName, userToken);
        }
        LOGGER.info("Closing program");
    }

}

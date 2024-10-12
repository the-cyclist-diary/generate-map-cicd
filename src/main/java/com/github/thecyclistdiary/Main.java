package com.github.thecyclistdiary;


import map.gpx.DefaultGpxMapper;
import map.gpx.GpxStyler;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, GitAPIException {
        if (args.length != 4) {
            throw new IllegalArgumentException(String.format("""
                    Program args should be exactly the following :
                    - content folder (where your gow files are located
                    - git directory
                    - your GitHub username
                    - your GitHub authentication token
                    
                    Found : %s
                    """, List.of(args)));
        }
        String executionFolder = args[0];
        String repositoryUrl = args[1];
        String userName = args[2];
        String userToken = args[3];
        Path repoDirectory = Files.createTempDirectory("git");

        CloneCommand cloneCommand = Git.cloneRepository()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, userToken))
                .setDirectory(repoDirectory.toFile())
                .setURI(repositoryUrl);
        try (Git git = cloneCommand.call()) {
            Repository repository = git.getRepository();
            Set<String> modifiedGpxFiles = GitHelper.getModifiedGpxList(git, repository);
            GpxStyler gpxStyler = new GpxStyler.builder()
                    .withGraphChartPadding(10)
                    .build();
            DefaultGpxMapper gpxMapper = new DefaultGpxMapper.builder()
                    .withHeight(800)
                    .withWidth(1200)
                    .withChartHeight(100)
                    .withGpxStyler(gpxStyler)
                    .build();
            var gpxToMapWalker = new GitAwareGpxToMapWalker(modifiedGpxFiles, gpxMapper);
            Path completeExecutionFolder = repoDirectory.resolve(executionFolder);
            LOGGER.info("Starting analysis of content folder {}", completeExecutionFolder);
            Files.walkFileTree(completeExecutionFolder, gpxToMapWalker);
            LOGGER.info("Done analysis of content folder");
            if (gpxToMapWalker.getExtractedResults().findAny().isPresent()) {
                LOGGER.info("Commiting to git repository...");
                GitHelper.commitChanges(git, userName, userToken);
            }
            LOGGER.info("Closing program");
        }
    }

}

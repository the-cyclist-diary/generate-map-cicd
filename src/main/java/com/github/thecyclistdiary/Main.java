package com.github.thecyclistdiary;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Program args should contain only the GPX folder name followed by the git repo path");
        }
        String executionFolder = args[0];
        String gitPath = args[1];
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Path basePath = Paths.get(executionFolder);
        Path gitRepo = Paths.get(gitPath);
        Set<String> modifiedGpxFiles = getModifiedGpxList(repositoryBuilder, gitRepo);
        var gpxToMapWalker = new GitAwareGpxToMapWalker(modifiedGpxFiles);
        Files.walkFileTree(basePath, Collections.singleton(FileVisitOption.FOLLOW_LINKS), 2, gpxToMapWalker );
    }

    private static Set<String> getModifiedGpxList(FileRepositoryBuilder repositoryBuilder, Path gitRepo) {
        Set<String> modifiedGpxFiles = new HashSet<>();
        try (Repository repository = repositoryBuilder.setGitDir(gitRepo.toFile()).build()) {
            // Récupère le dernier commit
            Git git = new Git(repository);
            RevWalk revWalk = new RevWalk(repository);
            RevCommit latestCommit = revWalk.parseCommit(repository.resolve("HEAD"));
            RevCommit parentCommit = latestCommit.getParent(0);

            // Prépare les deux arbres pour la comparaison
            AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, parentCommit.getId().getName());
            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, latestCommit.getId().getName());

            // Compare les commits
            List<DiffEntry> diffs = git.diff()
                    .setOldTree(oldTreeParser)
                    .setNewTree(newTreeParser)
                    .call();

            // Filtre les fichiers .gpx
            for (DiffEntry diff : diffs) {
                String fileName = Path.of(diff.getNewPath()).getFileName().toString();
                if (fileName.endsWith(".gpx")) {
                    modifiedGpxFiles.add(fileName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return modifiedGpxFiles;
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws Exception {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (org.eclipse.jgit.lib.ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, commit.getTree().getId());
            }
            walk.dispose();
            return treeParser;

        }
    }
}

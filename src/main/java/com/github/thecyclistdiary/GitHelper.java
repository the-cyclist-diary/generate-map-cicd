package com.github.thecyclistdiary;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GitHelper {
    public static final Logger LOGGER = LoggerFactory.getLogger(GitHelper.class);

    public static Set<String> getModifiedGpxList(FileRepositoryBuilder repositoryBuilder, Path gitRepo) {
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

    public static void commitChanges(Path gitRepo, String username, String githubToken) {
        try (Git git = Git.open(gitRepo.toFile())) {
            git.add().addFilepattern(".").call();
            LOGGER.info("Modifications indexed");
            String commitMessage = String.format("deploy: auto-generated map images - %s", LocalDateTime.now());
            git.commit().setMessage(commitMessage).call();
            LOGGER.info("Modifications committed");
            String remote = String.format("https://%s@github.com/%s/urban-happiness.git", githubToken, username);
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new URIish(remote))
                    .call();
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, githubToken))
                    .call();
            LOGGER.info("Modifications pushed - Commit message : {}", commitMessage);
        } catch (GitAPIException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
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

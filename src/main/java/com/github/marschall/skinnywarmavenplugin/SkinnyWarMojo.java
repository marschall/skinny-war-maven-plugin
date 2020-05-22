package com.github.marschall.skinnywarmavenplugin;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Repackages the WAR files in an EAR file into
 * <a href="https://maven.apache.org/plugins/maven-ear-plugin/examples/skinny-wars.html">skinny WARs</a>.
 */
@Mojo(
    name = "repackage",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresProject = true,
    threadSafe = true)
public class SkinnyWarMojo extends AbstractMojo {

  /**
   * The Maven project.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  /**
   * Directory containing the generated archive.
   */
  @Parameter(defaultValue = "${project.build.directory}", required = true)
  private File outputDirectory;

  /**
   * Name of the generated archive.
   */
  @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
  private String finalName;

  /**
   * Skip the execution.
   */
  @Parameter(property = "skinny-war.repackage.skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (this.skip) {
      this.getLog().info("skipping plugin execution");
      return;
    }

    Artifact sourceArtifact = this.getSourceArtifact();

    File repackaged;
    try {
      repackaged = this.repackage(sourceArtifact.getFile());
    } catch (IOException e) {
      throw new MojoExecutionException("could not repackage file: " + sourceArtifact.getFile(), e);
    }
    sourceArtifact.setFile(repackaged);
  }

  private Artifact getSourceArtifact() {
    return this.project.getArtifact();
  }

  private File repackage(File file) throws IOException {
    Path tempDirectory = Files.createTempDirectory("skinny-war-maven-plugin");
    try {
      unzip(file.toPath(), tempDirectory);
      processWarFiles(tempDirectory);

      File repackaged = new File(this.outputDirectory, this.finalName + "-skinny.ear");
      zip(tempDirectory, repackaged.toPath());
      return repackaged;
    } finally {
      deleteRecursively(tempDirectory);
    }
  }

  /**
   * Takes all .war files in the root of an .ear file and copies all the .jar files
   * in their WEB-INF/lib folders to lib/ folder in the .ear file.
   *
   * @param earFolder the folder of the extracted .ear
   * @throws IOException if an IO exception happens
   */
  private static void processWarFiles(Path earFolder) throws IOException {
    Path lib = earFolder.resolve("lib");
    if (!Files.exists(lib)) {
      Files.createDirectory(lib);
    }
    try (DirectoryStream<Path> wars = Files.newDirectoryStream(earFolder, "*.war")) {
      for (Path war : wars) {
        if (Files.isRegularFile(war)) {
          processWarFile(war, lib);
        }
      }
    }
  }

  /**
   * Takes all the .jar files in WEB-INF/lib of a .war and moves the to the lib/ folder
   * in an .ear or deletes them it they are already present in the lib/ folder.
   *
   * @param war the .war file
   * @param lib the lib/ folder in the EAR
   * @throws IOException if an IO exception happens
   */
  private static void processWarFile(Path war, Path lib) throws IOException {
    try (FileSystem zipFileSystem = newZipFileSystem(war, false)) {
      Path webInfLib = zipFileSystem.getPath("WEB-INF", "lib");
      try (DirectoryStream<Path> jars = Files.newDirectoryStream(webInfLib , "*.jar")) {
        for (Path jar : jars) {
          if (Files.isRegularFile(jar)) {
            Path targetFile = lib.resolve(jar.getFileName().toString());
            if (Files.exists(targetFile)) {
              Files.delete(jar);
            } else {
              Files.move(jar, targetFile, COPY_ATTRIBUTES);
            }
          }
        }
      }
    }
  }

  static void zip(Path directory, Path targetZipFile) throws IOException {
    try (FileSystem zipFileSystem = newZipFileSystem(targetZipFile, true)) {

      Path zipRoot = zipFileSystem.getPath("/");
      Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          Path relative = directory.relativize(dir);
          Path targetDir = zipRoot.resolve(relative.toString());
          if (!Files.exists(targetDir)) {
            Files.createDirectory(targetDir);
          }

          return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Path relative = directory.relativize(file);
          Path targetFile = zipRoot.resolve(relative.toString());
          Files.copy(file, targetFile, REPLACE_EXISTING, COPY_ATTRIBUTES);

          return CONTINUE;
        }

      });
    }
  }

  static void unzip(Path zipFile, Path targetDirectory) throws IOException {
    try (FileSystem zipFileSystem = newZipFileSystem(zipFile, false)) {

      Path zipRoot = zipFileSystem.getPath("/");
      Files.walkFileTree(zipRoot, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          Path relative = zipRoot.relativize(dir);
          Path targetPath = targetDirectory.resolve(relative.toString());
          if (!Files.exists(targetPath)) {
            Files.createDirectory(targetPath);
          }

          return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Path relative = zipRoot.relativize(file);
          Path targetPath = targetDirectory.resolve(relative.toString());
          Files.copy(file, targetPath, REPLACE_EXISTING, COPY_ATTRIBUTES);

          return CONTINUE;
        }

      });
    }
  }

  private static void deleteRecursively(Path directory) throws IOException {
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return CONTINUE;
      }

    });
  }

  static FileSystem newZipFileSystem(Path path, boolean create) throws IOException {
    Map<String, String> env = Collections.singletonMap("create", Boolean.toString(create));
    URI fileUri = path.toUri();
    URI zipUri;
    try {
      zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);
    } catch (URISyntaxException e) {
      throw new IOException("invalid uri syntax:" + fileUri, e);
    }
    return FileSystems.newFileSystem(zipUri, env);
  }

}

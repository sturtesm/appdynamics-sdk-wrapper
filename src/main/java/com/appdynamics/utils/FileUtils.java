package com.appdynamics.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;


public class FileUtils {

	private Logger logger = Logger.getLogger(FileUtils.class);

	private void copyFromJar(String source, final Path target) throws URISyntaxException, IOException {
		URL urlResource = getClass().getResource("/boostrap-admin-template");
		URI resource = urlResource.toURI();

		FileSystem ext2fs = FileSystems.newFileSystem(resource, null);

		final Path jarPath = ext2fs.getPath(source);

		Files.walkFileTree(jarPath, new SimpleFileVisitor<Path>() {

			private Path currentTarget;

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				currentTarget = target.resolve(jarPath.relativize(dir).toString());
				Files.createDirectories(currentTarget);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}

		});
	}

	/** 
	 * Will look in the local file system and embedded jar and try to copy the source folder to the target.
	 * If the target is null, then it creates a temp folder. 
	 *
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public void recursivelyCopy(String source, Path destination) throws Exception {
		Path sourcePath = null;

		if (destination == null) {
			destination = Files.createTempDirectory("bootstrap-admin-template");
		}

		try {
			sourcePath = getFilesystemPath(source);
		}
		catch (Exception e) {
			e.printStackTrace();

			logger.error("Error getting path to " + source + " from file system, will look in JAR as well.");
		}

		if (sourcePath == null) {
			throw new Exception ("JAR recursive copy not yet supported");
		}

		final Path finalTarget = destination;
		final Path finalSource = sourcePath;

		Files.walkFileTree(finalSource, new SimpleFileVisitor<Path>() {

			private Path currentTarget;

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				currentTarget = finalTarget.resolve(finalSource.relativize(dir).toString());
				Files.createDirectories(currentTarget);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, finalTarget.resolve(finalSource.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});

		logger.info("Recursively copied " + finalSource + " to " + finalTarget);
	}


	/**
	 * resolve a path from the file system
	 * 
	 * @param path
	 * @throws Exception
	 */
	private Path getFilesystemPath(String path) throws Exception
	{
		File file = new File(path);

		URI rootURI = new URI("file:///");
		Path rootPath = Paths.get(rootURI);

		Path targetPath = rootPath.resolve(file.getAbsolutePath());

		return targetPath;

		//System.out.println(resource);
		//FileStore dirFileStore = Files.getFileStore(dirPath);
		//printFileStore(dirFileStore, path);
	}

	private void printFileStore(FileStore filestore, String path)
	{
		try
		{
			System.out.println("Name: " + filestore.name());
			System.out.println("\tPath: " + path);
			System.out.println("\tSize: " + filestore.getTotalSpace());
			System.out.println("\tUnallocated: " + filestore.getUnallocatedSpace());
			System.out.println("\tUsable: " + filestore.getUsableSpace());
			System.out.println("\tType: " + filestore.type());
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}


	/**
	 * Export a resource embedded into a Jar file to the local file path.
	 *
	 * @param resourceName ie.: "/SmartLibrary.dll"
	 * @return The path to the exported resource
	 * @throws Exception
	 */
	public String exportResource(String resourceName) throws Exception {
		InputStream stream = null;
		OutputStream resStreamOut = null;
		String jarFolder;

		try {
			//note that each / is a directory down in the "jar tree" been the jar the root of the tree
			stream = FileUtils.class.getResourceAsStream(resourceName);
			if(stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];
			jarFolder = new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
			resStreamOut = new FileOutputStream(jarFolder + resourceName);
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			stream.close();
			resStreamOut.close();
		}

		return jarFolder + resourceName;
	}
}


/*
 * This file is part of Bakura, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016, Jamie Mansfield <https://www.jamierocks.uk/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package uk.jamierocks.mana.bakura.dependency;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * The manager for dependencies.
 *
 * @author Jamie Mansfield
 * @since 1.0.0
 */
public final class DependencyManager {

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private final List<Dependency> dependencies = new ArrayList<>();

    public DependencyManager() {
    }

    /**
     * Add a dependency to the manager.
     *
     * @param dependency The dependency
     * @since 1.0.0
     */
    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    /**
     * Checks all the dependencies for the specified path.
     *
     * @param librariesPath The libraries path
     * @throws IOException In error
     * @throws NoSuchAlgorithmException In error
     * @since 1.0.0
     */
    public void checkDependencies(Path librariesPath) throws IOException, NoSuchAlgorithmException {
        for (Dependency dependency : this.dependencies) {
            this.checkDependency(librariesPath, dependency);
        }
    }

    private boolean checkDependency(Path librariesPath, Dependency dependency) throws IOException, NoSuchAlgorithmException {
        final Path dependencyPath = librariesPath.resolve(dependency.getJarPath());
        final String remoteUrl = dependency.getRepoUrl() + dependency.getJarPath();

        if (Files.notExists(dependencyPath) && !verifyDownload(remoteUrl, dependencyPath)) {
            return false;
        }

        return Files.exists(dependencyPath) || verifyDownload(remoteUrl, dependencyPath);
    }

    private boolean verifyDownload(String remoteUrl, Path localPath) throws IOException, NoSuchAlgorithmException {
        Files.createDirectories(localPath.getParent());

        final String name = localPath.getFileName().toString();
        final URL remote = new URL(remoteUrl);

        System.out.println("Downloading " + name + "... This can take a while.");
        System.out.println(remote);

        URLConnection con = remote.openConnection();
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        try (ReadableByteChannel source = Channels.newChannel(new DigestInputStream(con.getInputStream(), md5));
                FileChannel out = FileChannel.open(localPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            out.transferFrom(source, 0, Long.MAX_VALUE);
        }

        String expected = getETag(con);
        if (!expected.isEmpty() && !expected.startsWith("{SHA1{")) {
            String hash = toHexString(md5.digest());
            if (hash.equals(expected)) {
                System.out.println("Successfully downloaded " + name + " and verified checksum!");
            } else {
                Files.delete(localPath);
                throw new IOException("Checksum verification failed: Expected " + expected + ", got " + hash);
            }
        }

        return true;
    }

    private static String getETag(URLConnection con) {
        String hash = con.getHeaderField("ETag");
        if (hash == null || hash.isEmpty()) {
            return "";
        }

        if (hash.startsWith("\"") && hash.endsWith("\"")) {
            hash = hash.substring(1, hash.length() - 1);
        }

        return hash;
    }

    private static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}

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

import uk.jamierocks.mana.bakura.util.BakuraConstants;

/**
 * Represents a Maven dependency.
 *
 * @author Jamie Mansfield
 * @since 1.0.0
 */
public class Dependency {

    private final String repoUrl;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String basePath;

    public Dependency(String dependency) {
        this(dependency.split(":"));
    }

    public Dependency(String repoUrl, String dependency) {
        this(repoUrl, dependency.split(":"));
    }

    private Dependency(String[] dependencySplit) {
        this(dependencySplit[0], dependencySplit[1], dependencySplit[2]);
    }

    private Dependency(String repoUrl, String[] dependencySplit) {
        this(repoUrl, dependencySplit[0], dependencySplit[1], dependencySplit[2]);
    }

    public Dependency(String groupId, String artifactId, String version) {
        this(BakuraConstants.MAVEN_CENTRAL, groupId, artifactId, version);
    }

    public Dependency(String repoUrl, String groupId, String artifactId, String version) {
        this.repoUrl = repoUrl;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        final StringBuilder builder = new StringBuilder();
        builder.append(this.groupId.replace(".", "/")).append("/");
        builder.append(this.artifactId).append("/");
        builder.append(this.version).append("/");
        builder.append(this.artifactId).append("-").append(this.version);
        this.basePath = builder.toString();
    }

    /**
     * Gets the url of the Maven repository the artifact is stored.
     *
     * @return The repository url
     * @since 1.0.0
     */
    public String getRepoUrl() {
        return this.repoUrl;
    }

    /**
     * Gets the group id of the artifact.
     *
     * @return The group id
     * @since 1.0.0
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Gets the artifact id of the dependency.
     *
     * @return The artifact id.
     * @since 1.0.0
     */
    public String getArtifactId() {
        return this.artifactId;
    }

    /**
     * Gets the version of the dependency.
     *
     * @return The version
     * @since 1.0.0
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the base path for this dependency.
     *
     * @return The base path
     * @since 1.0.0
     */
    public String getBasePath() {
        return this.basePath;
    }

    /**
     * Gets the path of the jar file for this dependency.
     *
     * @return The jar path
     * @since 1.0.0
     */
    public String getJarPath() {
        return this.basePath + ".jar";
    }
}

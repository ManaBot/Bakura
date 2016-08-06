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

package uk.jamierocks.mana.bakura;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import uk.jamierocks.mana.bakura.dependency.Dependency;
import uk.jamierocks.mana.bakura.dependency.DependencyManager;
import uk.jamierocks.mana.bakura.util.BakuraConstants;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * The core of Bakura.
 *
 * @author Jamie Mansfield
 * @since 1.0.0
 */
public final class Bakura {

    public static final BakuraClassLoader classLoader;
    public static final DependencyManager dependencies;
    public static BakuraConfiguration configuration;

    static {
        classLoader = new BakuraClassLoader(((URLClassLoader) Bakura.class.getClassLoader()).getURLs());
        dependencies = new DependencyManager();
        try {
            final ConfigurationNode node = HoconConfigurationLoader.builder().setURL(Bakura.class.getResource("/bakura.conf")).build().load();
            configuration = new BakuraConfiguration(node);
        } catch (IOException e) {
            System.out.println("Failed to load Bakura configuration!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void launch(String target, String[] args) {
        launch(BakuraConstants.DEFAULT_PROGRAM_PATH, target, args);
    }

    public static void launch(Path programPath, String target, String[] args) {
        for (BakuraConfiguration.Dependency dependency : configuration.getDependencies()) {
            dependencies.addDependency(new Dependency(dependency.getRepo(), dependency.getName()));
        }

        try {
            dependencies.checkDependencies(programPath);
        } catch (Exception e) {
            System.out.println("Failed to get dependencies!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            final Class targetClass = Class.forName(target, false, classLoader);
            final Method mainMethod = targetClass.getMethod("main", new Class[]{String.class});

            System.out.println("Launching " + target + "...");
            mainMethod.invoke(null, args);
        } catch (Exception e) {
            System.out.println("Failed to launch target class: " + target);
            e.printStackTrace();
            System.exit(1);
        }
    }

}

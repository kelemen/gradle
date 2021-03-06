/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.changedetection.state;

import org.gradle.api.internal.cache.StringInterner;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.hash.FileHasher;
import org.gradle.internal.nativeintegration.filesystem.FileSystem;
import org.gradle.internal.nativeintegration.filesystem.FileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DefaultCompileClasspathSnapshotter extends AbstractFileCollectionSnapshotter implements CompileClasspathSnapshotter {
    private static final Comparator<FileDetails> FILE_DETAILS_COMPARATOR = new Comparator<FileDetails>() {
        @Override
        public int compare(FileDetails o1, FileDetails o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    };
    private final JvmClassHasher jvmClassHasher;

    public DefaultCompileClasspathSnapshotter(FileHasher hasher, StringInterner stringInterner, FileSystem fileSystem, DirectoryFileTreeFactory directoryFileTreeFactory, FileSystemMirror fileSystemMirror, JvmClassHasher jvmClassHasher) {
        super(hasher, stringInterner, fileSystem, directoryFileTreeFactory, fileSystemMirror);
        this.jvmClassHasher = jvmClassHasher;
    }

    @Override
    public Class<? extends FileCollectionSnapshotter> getRegisteredType() {
        return CompileClasspathSnapshotter.class;
    }

    @Override
    protected List<FileDetails> normaliseTreeElements(List<FileDetails> nonRootElements) {
        // Collect the signatures of each class file
        List<FileDetails> sorted = new ArrayList<FileDetails>(nonRootElements.size());
        for (FileDetails details : nonRootElements) {
            if (details.getType() == FileType.RegularFile && details.getName().endsWith(".class")) {
                sorted.add(details.withContent(jvmClassHasher.hashClassFile(details)));
            }
        }

        // Sort classes as their order is not important
        Collections.sort(sorted, FILE_DETAILS_COMPARATOR);
        return sorted;
    }

    @Override
    protected FileDetails normaliseFileElement(FileDetails details) {
        return details.withContent(jvmClassHasher.hashJarFile(details));
    }
}

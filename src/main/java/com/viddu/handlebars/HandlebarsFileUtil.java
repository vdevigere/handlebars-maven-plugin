/*******************************************************************************
 * Copyright 2012 Viddu Devigere
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.viddu.handlebars;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class HandlebarsFileUtil {
    // The supported extensions for input files.
    private static String[] supportedExt = {
            "handlebars", "hbr"
    };

    /**
     * Utility method to get contents of the File.
     * 
     * @param inputFile
     * @return
     * @throws IOException
     */
    public static String getFileContents(File inputFile) throws IOException {
        FileInputStream stream = new FileInputStream(inputFile);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }

    }

    /**
     * Utility method to load Files in a given directory.
     * 
     * @param outDir
     * @return
     */
    public static List<File> loadHandlebarTemplates(File outDir) {
        List<File> fileList = new ArrayList<File>();
        if (outDir.isDirectory()) {
            File[] filesAndDirs = outDir.listFiles();
            for (File file : filesAndDirs) {
                if (file.isDirectory()) {
                    fileList.addAll(loadHandlebarTemplates(file));
                } else {
                    if (FilenameUtils.isExtension(file.getName(), supportedExt)) {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }
}

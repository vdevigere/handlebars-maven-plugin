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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class HandlebarsMojoTest extends TestCase {

    protected Scriptable loadHandlebarsLibrary() throws FileNotFoundException {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        Context handlebarContext = Context.enter();
        URL handleBarsUrl = ccl.getResource("handlebars-1.0.rc.1.js");
        Scriptable globalScope = handlebarContext.initStandardObjects();
        Reader handlebarsReader = new FileReader(new
                File(handleBarsUrl.getPath()));
        try {
            handlebarContext.evaluateReader(globalScope, handlebarsReader,
                    "Handlebars 1_0", 0,
                    null);
        } catch (IOException e) {
            throw new RuntimeException("Exception loading handlebars Library");
        } finally {
            Context.exit();
        }
        return globalScope;
    }

    protected void render(File compiledTemplate, Object model) throws
            IOException {
        Scriptable globalScope = loadHandlebarsLibrary();

        for (File file : HandlebarsFileUtil.loadHandlebarTemplates(compiledTemplate)) {
            String sInputFile = HandlebarsFileUtil.getFileContents(file);
            String fileNameWithoutExt =
                    FilenameUtils.removeExtension(file.getName());
            Context renderContext = Context.enter();
            Scriptable renderScope = renderContext.newObject(globalScope);
            renderScope.setParentScope(globalScope);
            renderContext.evaluateString(renderScope, sInputFile, "Loading template",
                    0, null);
            StringBuilder strb = new
                    StringBuilder("var template = Handlebars.templates['").append(
                            fileNameWithoutExt).append("']; template(context);");
            renderScope.put("context", renderScope, model);
            Object result = renderContext.evaluateString(renderScope,
                    strb.toString(), "Render", 0,
                    null);
            System.out.println(result);
        }
    }

    public void testExecute() throws MojoExecutionException {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        URL handleBarsUrl = ccl.getResource("handlebars-1.0.rc.1.js");
        URL templatesUrl = ccl.getResource("templates");
        String outputDir = "target/compiledTemplates";

        HandlebarsMojo hbMojo = new HandlebarsMojo();
        hbMojo.setHandlebarsLibrary(new File(handleBarsUrl.getPath()));
        hbMojo.setInputDirectory(new File(templatesUrl.getPath()));
        hbMojo.setOutputDirectory(new File(outputDir));
        hbMojo.execute();
    }

    public void testRender() throws IOException {
        MessageInstance messageInstance = new MessageInstance("Hello World");
        render(new File("target/compiledTemplates"),
                messageInstance);
    }

}

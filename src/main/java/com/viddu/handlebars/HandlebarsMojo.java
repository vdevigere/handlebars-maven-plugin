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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Goal which compiles Handlebar templates.
 * 
 * @goal compile
 * @phase compile
 */
public class HandlebarsMojo extends AbstractMojo {

    /**
     * Location of the file.
     * 
     * @parameter expression="${compile.output}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of Source Directory containing the uncompiled handlebar
     * templates
     * 
     * @parameter expression="${compile.input}"
     * @required
     */
    private File inputDirectory;

    /**
     * The .js handlebars library to use.
     * 
     * @parameter expression="${compile.library}"
     * @required
     */
    private File handlebarsLibrary;

    /**
     * Compiles Handlebar template file.
     * 
     * @param inputFile : The uncompiled Handlebars template file
     * @return A compiled Handlebar template file
     * @throws IOException
     */
    protected String compile(Scriptable globalScope, File inputFile) throws IOException {
        getLog().info("Compiling file:" + inputFile.toString());
        String sInputFile = HandlebarsFileUtil.getFileContents(inputFile);
        String fileNameWithoutExt = FilenameUtils.removeExtension(inputFile.getName());
        Context compileContext = Context.enter();
        Scriptable compileScope = compileContext.newObject(globalScope);
        compileScope.setParentScope(globalScope);
        compileScope.put("template", compileScope, sInputFile);
        Object result = compileContext.evaluateString(compileScope,
                "Handlebars.precompile(template);", "PreCompile", 0, null);
        String compiledTemplate = Context.toString(result);
        StringBuilder strb = new StringBuilder(
                "(function(){"
                        + "\n  var template = Handlebars.template, templates = Handlebars.templates = Handlebars.templates || {};\n"
                        +
                        "templates[\'").append(fileNameWithoutExt).append("\'] = template(")
                .append(compiledTemplate).append(");\n}());");
        return strb.toString();
    }

    /**
     * Load the handlebars library into scope.
     * 
     * @param hbrLib
     * @return
     */
    protected Scriptable loadHandlebarsLibrary(File hbrLib) {
        Context handlebarContext = Context.enter();
        Scriptable globalScope = handlebarContext.initStandardObjects();
        try {
            Reader handlebarsReader = new FileReader(hbrLib);
            handlebarContext.evaluateReader(globalScope, handlebarsReader, "Handlebars Library", 0,
                    null);
        } catch (IOException e) {
            throw new RuntimeException("Exception loading handlebars Library");
        } finally {
            Context.exit();
        }
        return globalScope;
    }

    public void execute() throws MojoExecutionException {
        File outDir = outputDirectory;
        File inDir = inputDirectory;
        File hbrLib = handlebarsLibrary;

        // Load the handlebars library into the scope
        Scriptable globalScope = loadHandlebarsLibrary(hbrLib);

        // Check if output path is a directory and create if it does not exist.
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        if (outDir.exists() && !outDir.isDirectory()) {
            throw new MojoExecutionException("OutputDir:" + outDir.toString()
                    + "is not Directory");
        }

        // Iterate over files in the input Directory
        if (inDir.isDirectory()) {
            for (File file : HandlebarsFileUtil.loadHandlebarTemplates(inDir)) {
                try {
                    String output = compile(globalScope, file);
                    Writer fwriter = new BufferedWriter(new FileWriter(
                            new File(outDir, file.getName())));
                    fwriter.write(output);
                    fwriter.close();
                } catch (IOException e) {
                    throw new MojoExecutionException(
                            "Exception creating compiled File", e);
                }
            }
        } else {
            throw new MojoExecutionException("Input Directory:" + inDir.toString()
                    + "is not Directory");

        }
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public void setHandlebarsLibrary(File handlebarsLibrary) {
        this.handlebarsLibrary = handlebarsLibrary;
    }
}

/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.codegeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;


public class JavaCode {
    private final File srcDir;
    private final List<File> files = new ArrayList<>();
    private final List<String> mainClasses = new ArrayList<>();

    public JavaCode() throws IOException {
        srcDir = Files.createTempDirectory("absjavabackend").toFile();
    }

    public JavaCode(File srcDir) {
        this.srcDir = srcDir;
    }

    public void addFile(File file) {
        files.add(file);
    }

    public String[] getFileNames() {
        String[] res = new String[files.size()];
        int i = 0;
        for (File f : files) {
            res[i++] = f.getAbsolutePath();
        }
        return res;
    }

    public Package createPackage(String packageName) throws IOException {
        return new Package(packageName);
    }

    public class Package {
        public final String packageName;
        public final File packageDir;
        private String firstPackagePart;

        public Package(String packageName) throws IOException {
            this.packageName = packageName;
            this.packageDir = new File(srcDir, packageName.replace('.', File.separatorChar));
            this.firstPackagePart = packageName.split("\\.")[0];
            if (!packageDir.mkdirs() && !packageDir.isDirectory()) {
                throw new IOException("Could not create directory " + packageDir.toString());
            }
        }

        public File createJavaFile(String name) throws IOException, JavaCodeGenerationException {
            if (name.equals(firstPackagePart)) {
                if (name.equals("Main")) {
                    throw new JavaCodeGenerationException("The Java backend does not support main blocks in " +
                    		"modules with name 'Main'. Please try to use a different name.");
                }
                throw new JavaCodeGenerationException("The Java backend does not support using the name " +
                      name + " as module name, because it collides with a generated classname. " +
                      		"Please try to use a different name.");
            }
            File file = new File(packageDir, name + ".java");
            addFile(file);
            if (!file.createNewFile()) {
                throw new IOException("File already exists: " + file.toString());
            }
            return file;
        }

        public void addMainClass(String s) {
            mainClasses.add(packageName + "." + s);
        }
    }

    public File getSrcDir() {
        return srcDir;
    }

    public void deleteCode() throws IOException {
        FileUtils.deleteDirectory(srcDir);
    }

    public void compile() throws JavaCodeGenerationException {
        compile(srcDir);
    }

    public void compile(File destDir) throws JavaCodeGenerationException {
        compile("-classpath", System.getProperty("java.class.path"), "-d", destDir.getAbsolutePath());
    }

    public void compile(String... args) throws JavaCodeGenerationException {
        ArrayList<String> args2 = new ArrayList<>();
        args2.addAll(Arrays.asList(args));
        args2.addAll(Arrays.asList(getFileNames()));
        JavaCompiler.compile(args2.toArray(new String[0]));
    }

    public String getFirstMainClass() {
        if (mainClasses.isEmpty())
            throw new IllegalStateException("There is no main class");

        return mainClasses.get(0);
    }

    public String toString() {
        StringBuilder res = new StringBuilder();

        for (File f : files) {
            append(res, f);
        }

        return res.toString();
    }

    private void append(StringBuilder res, File f) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            try {
                while (reader.ready()) {
                    res.append(reader.readLine() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                reader.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

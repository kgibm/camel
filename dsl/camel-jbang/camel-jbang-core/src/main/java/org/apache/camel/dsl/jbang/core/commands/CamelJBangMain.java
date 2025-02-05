/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dsl.jbang.core.commands;

import java.util.concurrent.Callable;

import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "camel", description = "Apache Camel CLI", mixinStandardHelpOptions = true)
public class CamelJBangMain implements Callable<Integer> {
    private static CommandLine commandLine;

    public static void run(String... args) {
        commandLine = new CommandLine(new CamelJBangMain())
                .addSubcommand("run", new CommandLine(new Run()))
                .addSubcommand("init", new CommandLine(new Init()))
                .addSubcommand("bind", new CommandLine(new Bind()))
                .addSubcommand("package", new CommandLine(new Package())
                        .addSubcommand("uber-jar", new UberJar()))
                .addSubcommand("generate", new CommandLine(new CodeGenerator())
                        .addSubcommand("rest", new CodeRestGenerator()))
                .addSubcommand("build", new CommandLine(new Build())
                        .addSubcommand("manifests", new Manifest())
                        .addSubcommand("image", new Image()))
                .addSubcommand("deploy", new CommandLine(new Deploy()))
                .addSubcommand("undeploy", new CommandLine(new Undeploy()))
                .addSubcommand("search", new CommandLine(new Search())
                        .addSubcommand("kamelets", new SearchKamelets())
                        .addSubcommand("components", new SearchComponents())
                        .addSubcommand("languages", new SearchLanguages())
                        .addSubcommand("others", new SearchOthers()))
                .addSubcommand("create", new CommandLine(new Create())
                        .addSubcommand("project", new Project()));

        commandLine.getCommandSpec().versionProvider(() -> {
            CamelCatalog catalog = new DefaultCamelCatalog();
            String v = catalog.getCatalogVersion();
            return new String[] { v };
        });

        PropertiesHelper.augmentWithProperties(commandLine);
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        commandLine.execute("--help");
        return 0;
    }
}

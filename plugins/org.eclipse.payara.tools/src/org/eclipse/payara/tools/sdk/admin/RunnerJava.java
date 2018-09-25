/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.data.GlassFishConfig;
import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.sdk.server.config.ConfigBuilderProvider;
import org.eclipse.payara.tools.sdk.server.config.GlassFishConfigManager;
import org.eclipse.payara.tools.sdk.server.config.JavaSEPlatform;
import org.eclipse.payara.tools.sdk.server.config.JavaSESet;
import org.eclipse.payara.tools.sdk.utils.JavaUtils;
import org.eclipse.payara.tools.sdk.utils.ServerUtils;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * GlassFish server administration command execution using local Java VM.
 * <p/>
 *
 * @author Tomas Kraus
 */
abstract class RunnerJava extends Runner {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerJava.class);

    /** Specifies program encapsulated in a JAR file to execute. */
    static final String JAR_PARAM = "-jar";

    /** Character used to separate query string from list of parameters. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final char QUERY_SEPARATOR = ' ';

    /** Character used to separate individual parameters. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final char PARAM_SEPARATOR = ' ';

    /** Character used to assign value to parameter. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final char PARAM_ASSIGN_VALUE = ' ';

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get {@link GlassFishConfig} instance for provided GlassFish server which shall not be
     * <code>null</code>.
     * <p/>
     * {@link CommandException} is thrown when configuration object value is <code>null</code>.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @return GlassFish server features configuration which is not <code>null</code>.
     */
    static GlassFishConfig getServerConfig(final PayaraServer server) {
        final String METHOD = "getServerConfig";
        GlassFishConfig config = GlassFishConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(
                        server.getVersion()));
        if (config == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "noConfig"),
                    server.getVersion());
        }
        return config;
    }

    /**
     * Get {@link JavaSESet} instance for provided GlassFish server features configuration.
     * <p/>
     *
     * @param config GlassFish server features configuration.
     * @return GlassFish JavaSE configuration which is not <code>null</code>.
     */
    static JavaSESet getJavaSEConfig(final GlassFishConfig config) {
        final String METHOD = "getJavaSEConfig";
        JavaSESet javaSEConfig = config.getJavaSE();
        if (javaSEConfig == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "noJavaSEConfig"));
        }
        return javaSEConfig;
    }

    /**
     * Constructs path to Java VM executable and verifies if it exists.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command lassFish server administration command with local Java VM.
     * @return Path to Java VM executable
     */
    private static String getJavaVM(final PayaraServer server,
            final CommandJava command) {
        final String METHOD = "getJavaVM";
        String javaVmExe = JavaUtils.javaVmExecutableFullPath(command.javaHome);
        File javaVmFile = new File(javaVmExe);
        // Java VM executable should exist and should be executable.
        if (!javaVmFile.canExecute()) {
            LOGGER.log(Level.INFO, METHOD, "noJavaVMExe", javaVmExe);
            return null;
        }
        return javaVmExe;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandJava command;

    /** Java VM executable. */
    final String javaVMExe;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using command line asadmin interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     * @param query Query string for this command.
     */
    public RunnerJava(final PayaraServer server, final Command command,
            final String query) {
        super(server, command, null, query);
        final String METHOD = "init";
        if (command instanceof CommandJava) {
            this.command = (CommandJava) command;
        } else {
            throw new CommandException(LOGGER.excMsg(METHOD, "noCommandJava"));
        }
        javaVMExe = getJavaVM(server, this.command);
        if (javaVMExe == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "noJavaVMExe"),
                    new Object[] { this.command.javaHome, server.getName() });
        }
    }

    /**
     * Constructs an instance of administration command executor using command line asadmin interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerJava(final PayaraServer server, final Command command) {
        this(server, command, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Do not send information to the server via HTTP POST by default.
     * <p/>
     *
     * @return <code>true</code> if using HTTP POST to send to server or <code>false</code> otherwise
     */
    /**
     * Do not send information to the server via HTTP POST by default.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     *
     * @return Always returns <code>false</code>.
     */
    @Override
    public boolean getDoOutput() {
        return false;
    }

    /**
     * Inform whether this runner implementation accepts gzip format.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     *
     * @return Always returns <code>false</code>.
     */
    @Override
    public boolean acceptsGzip() {
        return false;
    }

    /**
     * Build GlassFish administration interface command URL.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     *
     * @return Always returns <code>null</code>.
     * @throws <code>CommandException</code> if there is a problem with building command URL.
     */
    @Override
    protected String constructCommandUrl() throws CommandException {
        return null;
    }

    /**
     * The type of HTTP method used to access administration interface command.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     *
     * @return Always returns <code>null</code>.
     */
    @Override
    protected String getRequestMethod() {
        return null;
    }

    /**
     * Handle sending data to server using HTTP administration command interface.
     * <p/>
     * Does nothing. This method makes no sense for this runner.
     */
    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method helpers //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Verifies if Java VM version is supported by provided GlassFish server.
     * <p/>
     *
     * @return Value of <code>true</code> when Java VM executable version is known and supported by
     * provided GlassFish server or <code>false</code> otherwise.
     */
    boolean verifyJavaVM() {
        final String METHOD = "verifyJavaVM";
        // Java VM executable version must be known.
        JavaUtils.JavaVersion javaVersion = JavaUtils.javaVmVersion(new File(javaVMExe));
        if (javaVersion == null) {
            LOGGER.log(Level.INFO, METHOD, "unknown", javaVMExe);
            return false;
        } else {
            LOGGER.log(Level.FINEST, METHOD, "info",
                    new Object[] { javaVMExe, javaVersion.toString() });
        }
        // Java VM executable version must be supported by provided server.
        Set<JavaSEPlatform> platforms = getJavaSEConfig(getServerConfig(server)).platforms();
        if (!platforms.contains(javaVersion.toPlatform())) {
            LOGGER.log(Level.INFO, METHOD, "unsupported",
                    new Object[] { javaVMExe, server.getName() });
            return false;
        }
        return true;
    }

    /**
     * Prepare Java VM environment for Glassfish server execution.
     * <p/>
     *
     * @param env Process builder environment <code>Map</code>.
     * @param command GlassFish Server Administration Command Entity.
     */
    static void setJavaEnvironment(Map<String, String> env,
            CommandJava command) {
        
        env.clear();
        copyIfPresent("TEMP", env);
        copyIfPresent("TMP", env);
        env.putAll(command.environmentVars);
        
        // Java VM home stored in AS environment variables JAVA_HOME and AS_JAVA
        env.put(JavaUtils.JAVA_HOME_ENV, command.javaHome);
        env.put(ServerUtils.AS_JAVA_ENV, command.javaHome);
        
        
    }
    
    private static void copyIfPresent(String varName, Map<String, String> env) {
        final String varValue = System.getenv(varName);
        if(varValue!=null) {
            env.put(varName, varValue);
        }
    }

    /**
     * Set server process current directory to domain directory if exists.
     * <p/>
     * No current directory will be set when domain directory does not exist.
     * <p/>
     *
     * @param pb Process builder object where to set current directory.
     */
    void setProcessCurrentDir(ProcessBuilder pb) {
        final String METHOD = "setProcessCurrentDir";
        String domainsFolder = server.getDomainsFolder();
        if (domainsFolder != null && domainsFolder.length() > 0) {
            File currentDir = new File(
                    ServerUtils.getDomainConfigPath(domainsFolder));
            if (currentDir.exists()) {
                LOGGER.log(Level.FINEST, METHOD, "dir",
                        new Object[] { server.getName(), currentDir });
                pb.directory(currentDir);
            }
        }
    }

}

/**
 * Copyright (c) 2020 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.payara.tools.micro;

public interface MicroConstants {

    String PLUGIN_ID = "org.eclipse.payara.tools";

    String DEFAULT_HOST = "localhost";
    int DEFAULT_DEBUG_PORT = 9889;

    String ATTR_HOST_NAME = "hostname";
    String ATTR_PORT = "port";

    String JAVA_HOME_ENV_VAR = "JAVA_HOME";

    String ATTR_CONTEXT_PATH = "contextPath";
    String ATTR_MICRO_VERSION = "microVersion";
    String ATTR_BUILD_ARTIFACT = "buildArtifact";
    String ATTR_DEBUG_PORT = "debugPort";

    String WAR_BUILD_ARTIFACT = "War";
    String EXPLODED_WAR_BUILD_ARTIFACT = "Exploded War";
    String UBER_JAR_BUILD_ARTIFACT = "Uber Jar";

}

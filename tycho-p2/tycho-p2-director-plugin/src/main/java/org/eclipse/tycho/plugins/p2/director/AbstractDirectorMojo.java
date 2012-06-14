/*******************************************************************************
 * Copyright (c) 2012 Compuware Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Compuware Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.plugins.p2.director;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.eclipse.tycho.core.TargetEnvironment;
import org.eclipse.tycho.core.TargetPlatformConfiguration;
import org.eclipse.tycho.core.TychoConstants;
import org.eclipse.tycho.core.utils.TychoProjectUtils;
import org.eclipse.tycho.p2.resolver.TargetDefinitionFile;
import org.eclipse.tycho.p2.target.facade.TargetDefinition.InstallableUnitLocation;
import org.eclipse.tycho.p2.target.facade.TargetDefinition.Repository;

public abstract class AbstractDirectorMojo extends AbstractProductMojo {
    protected String toCommaSeparatedList(List<URI> repositories) {
        if (repositories.size() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (URI uri : repositories) {
            result.append(uri.toString());
            result.append(',');
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }

    protected String toCommaSeparatedString(List<String> list) {
        if (list.size() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (String entry : list) {
            result.append(entry.toString());
            result.append(',');
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }

    protected String extractTargetPlatformRepos() {
        TargetPlatformConfiguration configuration = TychoProjectUtils.getTargetPlatformConfiguration(getProject());
        String commaSeparatedP2ReposURIs = "";
        TargetDefinitionFile file;
        List<URI> p2ReposURIs = new ArrayList<URI>();

        try {
            file = TargetDefinitionFile.read(configuration.getTarget());
            @SuppressWarnings("unchecked")
            List<InstallableUnitLocation> locations = (List<InstallableUnitLocation>) file.getLocations();
            for (InstallableUnitLocation location : locations) {
                List<? extends Repository> repositories = location.getRepositories();
                for (Repository repository : repositories) {
                    p2ReposURIs.add(repository.getLocation());
                }
            }
            commaSeparatedP2ReposURIs = "," + toCommaSeparatedList(p2ReposURIs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commaSeparatedP2ReposURIs;
    }

    protected List<String> getExtraReqs() {
        List<String> extraReqs = new ArrayList<String>();

        TargetPlatformConfiguration tpc = (TargetPlatformConfiguration) getProject().getContextValue(
                TychoConstants.CTX_TARGET_PLATFORM_CONFIGURATION);

        List<Dependency> reqs = tpc.getExtraRequirements();
        for (Dependency d : reqs) {
            if (d.getType().equalsIgnoreCase("p2-installable-unit")) {
                extraReqs.add(d.getArtifactId());
            }
        }

        return extraReqs;
    }

    protected String[] getArgsForDirectorCall(Product product, String extraIUs, TargetEnvironment env,
            File destination, String metadataRepositoryURLs, String artifactRepositoryURLs, String nameForEnvironment,
            boolean installFeatures) {
        String[] args = new String[] { "-metadatarepository", metadataRepositoryURLs, //
                "-artifactrepository", artifactRepositoryURLs, //
                "-installIU", product.getId() + extraIUs, //
                "-destination", destination.getAbsolutePath(), //
                "-profile", nameForEnvironment, //
                "-profileProperties", "org.eclipse.update.install.features=" + String.valueOf(installFeatures), //
                "-roaming", //
                "-p2.os", env.getOs(), "-p2.ws", env.getWs(), "-p2.arch", env.getArch() };
        return args;
    }

}

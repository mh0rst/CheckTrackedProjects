/*
 * CheckTrackedProjects - An Android tracked AOSP project checker.  
 * Copyright (C) 2017 Moritz Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mh0rst.android.checktrackedprojects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckTrackedProjects {

    private static final String BASE_URL_PLATFORM = "https://android.googlesource.com/platform/";

    /**
     * I know, reading HTML with regexp is bad practice, but I can spare a
     * dependency here
     */
    private static final Pattern COMMIT = Pattern.compile("<li (.+?)</li>", Pattern.DOTALL);

    /** Same as above, I don't want to use JAXB */
    private static final Pattern PROJECT = Pattern.compile("<project path=\"(.+?)\" name=\"(.+?)\"(.*?remote=\"(.*?)\"|).*?>");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Must give latest tag and tag to compare as option");
        }
        String latest = args[0];
        String toCompare = args[1];
        Manifest los = args.length > 2 ? Manifest.valueOf(args[2]) : Manifest.LOS14_1;
        List<Project> cmProjects = parseManifest(getManifest(los));
        List<Project> aospProjects = parseManifest(getAOSPManifest(latest));
        List<Project> trackedProjects = findTrackedProjects(los, cmProjects, aospProjects);
        System.out.println("Found " + trackedProjects.size() + " tracked projects");
        int updateCounter = checkTrackedProjects(latest, toCompare, trackedProjects);
        System.out.println("Found " + updateCounter + " updated projects in AOSP");

    }

    private static int checkTrackedProjects(String latest, String toCompare, List<Project> trackedProjects)
        throws IOException {
        int updateCounter = 0;
        for (Project project : trackedProjects) {
            String log = getLogForTag(project.name, latest);
            Matcher matcher = COMMIT.matcher(log);
            while (matcher.find()) {
                String match = matcher.group(1);
                if (!match.contains(latest)) {
                    continue;
                }
                project.latestFound = true;
                if (!match.contains(toCompare)) {
                    System.out.println("Tracked project " + project.name + " has been updated");
                    updateCounter++;
                    break;
                }
                project.latestIsEqual = true;
            }
        }
        return updateCounter;
    }

    private static List<Project> findTrackedProjects(Manifest los, List<Project> cmProjects,
        List<Project> aospProjects) {
        List<Project> trackedProjects = new ArrayList<>();
        for (Project cmProject : cmProjects) {
            if (!"aosp".equals(cmProject.remote)) {
                aospProjects.stream()
                            .filter((Predicate<Project>) p -> p.path.equals(cmProject.path) ||
                                                              p.path.equals(los.entries.map.get(cmProject.path)))
                            .forEach(trackedProjects::add);
            }
        }
        return trackedProjects;
    }

    private static List<Project> parseManifest(String manifest) {
        Matcher matcher = PROJECT.matcher(manifest);
        List<Project> projects = new ArrayList<>();
        while (matcher.find()) {
            Project project = new Project(matcher.group(1), matcher.group(2));
            if (matcher.groupCount() > 3) {
                project.remote = matcher.group(4);
            }
            projects.add(project);
        }
        return projects;
    }

    private static String getManifest(Manifest linOS) throws IOException {
        StringBuilder manifest = new StringBuilder();
        for (String source : linOS.sources.sources) {
            manifest.append(Fetcher.fetchFromURL(source));
        }
        return manifest.toString();
    }

    private static String getAOSPManifest(String tagName) throws IOException {
        return new String(Base64.getDecoder().decode(Fetcher.fetchFromURL(BASE_URL_PLATFORM + "manifest/+/" + tagName +
                                                                          "/default.xml?format=TEXT")),
            StandardCharsets.UTF_8);
    }

    private static String getLogForTag(String name, String tag) throws IOException {
        String tagPage = Fetcher.fetchFromURL("/" + name + "/+/" + tag);
        String link = Fetcher.findLink(tagPage, "+log");
        if (link == null) {
            throw new IllegalStateException("Could not find log link for tag" + tag + " in project " + name);
        }
        return Fetcher.fetchFromURL(link);
    }
}
